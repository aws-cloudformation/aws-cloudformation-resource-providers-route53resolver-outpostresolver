package software.amazon.route53resolver.outpostresolver;

import com.amazonaws.services.route53resolver.AmazonRoute53Resolver;
import com.amazonaws.services.route53resolver.model.*;
import software.amazon.route53resolver.outpostresolver.Tag;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class TagHelper {
    public static Set<com.amazonaws.services.route53resolver.model.Tag> translateTags(Set<Tag> tags) {
        return tags == null ? null : tags.stream()
                .map(tag -> new com.amazonaws.services.route53resolver.model.Tag()
                        .withKey(tag.getKey())
                        .withValue(tag.getValue()))
                .collect(Collectors.toSet());
    }

    public static Set<Tag> tagsFromRequest(Map<String, String> requestTags) {
        return requestTags.entrySet().stream()
                .map(tag -> Tag.builder()
                        .key(tag.getKey())
                        .value(tag.getValue())
                        .build())
                .collect(Collectors.toSet());
    }

    public static Set<Tag> getAllResourceTags(ResourceModel model, Map<String, String> requestTags) {
        Set<Tag> tags = new HashSet<>();
        if (model.getTags() != null) {
            tags.addAll(model.getTags());
        }
        if (requestTags != null) {
            tags.addAll(tagsFromRequest(requestTags));
        }
        return tags;
    }

    public static Set<Tag> listAndTranslateTags(
            final AmazonRoute53Resolver client,
            final AmazonWebServicesClientProxy proxy,
            final ResourceModel model) {
        HashSet<com.amazonaws.services.route53resolver.model.Tag> tags = new HashSet<>();
        String nextToken = null;
        do {
            final ListTagsForResourceRequest listTagsRequest = new ListTagsForResourceRequest()
                    .withResourceArn(model.getArn())
                    .withNextToken(nextToken);
            try {
                final ListTagsForResourceResult listTagsResult =
                        proxy.injectCredentialsAndInvoke(listTagsRequest, client::listTagsForResource);


                nextToken = null;
                if (listTagsResult != null) {
                    tags.addAll(listTagsResult.getTags());
                    nextToken = listTagsResult.getNextToken();
                }
            } catch (AccessDeniedException e) {
                throw new CfnAccessDeniedException("ListTagsForResource for OutpostResolver", e);
            } catch (ResourceNotFoundException e) {
                throw new CfnNotFoundException(listTagsRequest.toString(), model.getId());
            } catch (final InternalServiceErrorException e) {
                throw new CfnServiceInternalErrorException(ResourceModel.TYPE_NAME, e);
            } catch (InvalidRequestException | InvalidParameterException | InvalidNextTokenException | ValidationException e) {
                throw new CfnInvalidRequestException(listTagsRequest.toString(), e);
            } catch (ThrottlingException e) {
                throw new CfnThrottlingException(ResourceModel.TYPE_NAME, e);
            }

        } while (nextToken != null);

        return tags.stream()
                .map(tag -> Tag.builder()
                        .key(tag.getKey())
                        .value(tag.getValue())
                        .build())
                .collect(Collectors.toSet());
    }
}