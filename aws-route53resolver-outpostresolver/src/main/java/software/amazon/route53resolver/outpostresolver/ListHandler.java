package software.amazon.route53resolver.outpostresolver;

import com.amazonaws.services.route53resolver.AmazonRoute53Resolver;
import com.amazonaws.services.route53resolver.model.*;
import software.amazon.cloudformation.exceptions.*;
import software.amazon.cloudformation.exceptions.ResourceNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.ArrayList;
import java.util.List;

public class ListHandler extends BaseHandler<CallbackContext> {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final Logger logger) {

        ClientBuilder builder = new ClientBuilder();
        // This Lambda will continually be re-invoked with the current state of the stack, finally succeeding when state stabilizes.
        return doHandleRequest(proxy, request, callbackContext, logger, builder.getClient());
    }

    public ProgressEvent<ResourceModel, CallbackContext> doHandleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final Logger logger,
            final AmazonRoute53Resolver client) {

        final List<ResourceModel> responseModels = new ArrayList<>();
        final ResourceModel requestModel = request.getDesiredResourceState();
        logger.log("ListHandler request: " + request);

        final ListOutpostResolversRequest listRequest = new ListOutpostResolversRequest()
                .withOutpostArn(requestModel.getOutpostArn());

        try {
            final ListOutpostResolversResult listResult =
                    proxy.injectCredentialsAndInvoke(listRequest, client::listOutpostResolvers);
            if (listResult != null) {
                if (listResult.getNextToken()!=null){
                    callbackContext.setListOperationNextToken(listResult.getNextToken());
                }
                logger.log("listResult.getOutpostResolvers().size()="+listResult.getOutpostResolvers().size());

                for (OutpostResolver resolver : listResult.getOutpostResolvers()) {
                    ResourceModel resolverModel = ResourceModel.builder()
                            .id(resolver.getId())
                            .arn(resolver.getArn())
                            .outpostArn(resolver.getOutpostArn())
                            .instanceCount(resolver.getInstanceCount())
                            .preferredInstanceType(resolver.getPreferredInstanceType())
                            .name(resolver.getName())
                            .status(resolver.getStatus())
                            .statusMessage(resolver.getStatusMessage())
                            .creationTime(resolver.getCreationTime())
                            .creatorRequestId(resolver.getCreatorRequestId())
                            .build();
                    resolverModel.setTags(TagHelper.listAndTranslateTags(client, proxy, resolverModel));
                    responseModels.add(resolverModel);
                    logger.log(String.format("%s [%s] read successfully",
                            ResourceModel.TYPE_NAME, resolverModel.getPrimaryIdentifier().toString()));
                }
            }
        } catch (InvalidRequestException | InvalidParameterException | ValidationException e) {
            throw new CfnInvalidRequestException(listRequest.toString(), e);
        } catch (ResourceNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, e.toString());
        } catch (LimitExceededException e) {
            throw new CfnServiceLimitExceededException(ResourceModel.TYPE_NAME, e.toString());
        } catch (InternalServiceErrorException e) {
            throw new CfnServiceInternalErrorException(ResourceModel.TYPE_NAME, e);
        } catch (ThrottlingException e) {
            throw new  CfnThrottlingException(ResourceModel.TYPE_NAME, e);
        }
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .callbackContext(callbackContext)
                .resourceModels(responseModels)
                .status(OperationStatus.SUCCESS)
                .build();
    }
}
