package software.amazon.route53resolver.outpostresolver;

import com.amazonaws.services.route53resolver.AmazonRoute53Resolver;
import com.amazonaws.services.route53resolver.model.*;
import software.amazon.cloudformation.exceptions.*;
import software.amazon.cloudformation.exceptions.ResourceNotFoundException;
import software.amazon.cloudformation.proxy.*;
import software.amazon.route53resolver.outpostresolver.util.OutpostResolverUtil;

import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;

public class UpdateHandler extends BaseHandler<CallbackContext> {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final Logger logger) {

        ClientBuilder builder = new ClientBuilder();
        // This Lambda will continually be re-invoked with the current state of the stack, finally finishes when state stabilizes.
        return doHandleRequest(proxy, request, callbackContext, logger, builder.getClient());
    }

    public ProgressEvent<ResourceModel, CallbackContext> doHandleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final Logger logger,
            final AmazonRoute53Resolver client){

        final ResourceModel model = request.getDesiredResourceState();
        final ResourceModel prevModel = request.getPreviousResourceState();

        final CallbackContext context = callbackContext == null ? CallbackContext.builder().build() : callbackContext;
        logger.log("UpdateHandler request: " + request);

        if (!context.isMutationStarted()) {
            context.setOutpostResolverId(prevModel.getId());
            if (context.getOutpostResolverId() == null) {
                return ProgressEvent.<ResourceModel, CallbackContext>builder()
                        .resourceModel(model)
                        .status(OperationStatus.FAILED)
                        .message("OutpostResolver not found")
                        .errorCode(HandlerErrorCode.NotFound)
                        .build();
            }

            //FrontierAPI does not support updating of OutpostArn, so we throw CfnInvalidRequestException if the request has a different OutpostArn
            if (!request.getDesiredResourceState().getOutpostArn().equals(request.getPreviousResourceState().getOutpostArn())){
                logger.log(request.getDesiredResourceState().getOutpostArn() + ", OutpostArn is not updatable");
                throw new CfnInvalidRequestException(request.getDesiredResourceState().getOutpostArn() + ", OutpostArn is not updatable");
            }

            final UpdateOutpostResolverRequest updateRequest = new UpdateOutpostResolverRequest()
                    .withId(model.getId())
                    .withName(model.getName())
                    .withInstanceCount(model.getInstanceCount())
                    .withPreferredInstanceType(model.getPreferredInstanceType());
            context.setMutationStarted(true);
            context.setMutationStabilized(false);
            try {
                final UpdateOutpostResolverResult updateResult =
                        proxy.injectCredentialsAndInvoke(updateRequest, client::updateOutpostResolver);
                if(updateResult == null || updateResult.getOutpostResolver() == null) {
                    context.setMutationStabilized(true);
                    return ProgressEvent.<ResourceModel, CallbackContext>builder()
                            .status(OperationStatus.FAILED)
                            .errorCode(HandlerErrorCode.NotFound)
                            .build();
                } else {
                    context.setOutpostResolverId(updateResult.getOutpostResolver().getId());
                    OutpostResolverUtil.outpostResolverToModelMapper(model, updateResult.getOutpostResolver(), null, null);
                }

                logger.log("...........................................");
                logger.log("the OutpostResolver resource is being updated...");
                logger.log("the OutpostResolver Name is " + model.getName());
                logger.log("the OutpostResolver Id is " + model.getId());
                logger.log("current status is " + model.getStatus());
                logger.log("pending stabilization...");
                logger.log("...........................................");

            } catch (InvalidRequestException | InvalidParameterException | ValidationException e) {
                throw new CfnInvalidRequestException(updateRequest.toString(), e);
            } catch (ResourceNotFoundException e) {
                throw new CfnNotFoundException(ResourceModel.TYPE_NAME, e.toString());
            } catch (ConflictException e) {
                throw new CfnResourceConflictException(ResourceModel.TYPE_NAME, context.getOutpostResolverId(), e.toString());
            } catch (LimitExceededException e) {
                throw new CfnServiceLimitExceededException(ResourceModel.TYPE_NAME, e.toString());
            } catch (InternalServiceErrorException e) {
                throw new CfnServiceInternalErrorException(ResourceModel.TYPE_NAME, e);
            } catch (ThrottlingException e) {
                throw new  CfnThrottlingException(ResourceModel.TYPE_NAME, e);
            }

            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .callbackContext(context)
                    .resourceModel(model)
                    .status(OperationStatus.IN_PROGRESS)
                    .build();
        }

        if (!context.isMutationStabilized()) {
            final GetOutpostResolverRequest readRequest = new GetOutpostResolverRequest()
                    .withId(context.getOutpostResolverId());

            final GetOutpostResolverResult getResult =
                    proxy.injectCredentialsAndInvoke(readRequest, client::getOutpostResolver);

            if (getResult != null && getResult.getOutpostResolver()!=null) {


                //Possible end state of OutpostResolver include OPERATIONAL and ACTION_NEEDED,
                context.setMutationStabilized(getResult.getOutpostResolver().getStatus().equals(OutpostResolverStatus.OPERATIONAL.toString())
                        || getResult.getOutpostResolver().getStatus().equals(OutpostResolverStatus.ACTION_NEEDED.toString()));
                context.setOutpostResolverId(model.getId());
            }
            if(context.isMutationStabilized()) {
                logger.log(String.format("%s [%s] is stabilized with status %s",
                        ResourceModel.TYPE_NAME, model.getId(), model.getStatus()));
                if (getResult.getOutpostResolver().getStatus().equals(OutpostResolverStatus.OPERATIONAL.toString())){
                    //we update tags after OPERATIONAL to prevent having to roll back as the CFN rollback does not roll back Tagris and it can cause mismatch of CFN template tags with actual Tagris tags
                    tagResource(request,proxy,client, request.getDesiredResourceState(), request.getPreviousResourceState());
                }
                OutpostResolverUtil.outpostResolverToModelMapper(model, getResult.getOutpostResolver(), proxy, client);
                return ProgressEvent.<ResourceModel, CallbackContext>builder()
                        .callbackContext(context)
                        .message(String.format("OutpostResolver update stabilized with the status of: %s", model.getStatus()))
                        .status(model.getStatus().equals(OutpostResolverStatus.OPERATIONAL.toString()) ? OperationStatus.SUCCESS : OperationStatus.FAILED)
                        .resourceModel(model)
                        .build();
            }
        }
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .callbackContext(context)
                .status(OperationStatus.IN_PROGRESS)
                .resourceModel(model)
                .build();
    }

    public void tagResource(final ResourceHandlerRequest<ResourceModel> request, final AmazonWebServicesClientProxy proxy, final AmazonRoute53Resolver client, ResourceModel model, ResourceModel prevModel) {
        Set<Tag> tagsToRemove = TagHelper.getAllResourceTags(prevModel, request.getPreviousResourceTags());
        Set<Tag> tagsToAdd = TagHelper.getAllResourceTags(model, request.getDesiredResourceTags());
        for (Tag oldTag : new ArrayList<>(tagsToRemove)) {
            for (Tag newTag : new ArrayList<>(tagsToAdd)) {
                if (oldTag.getKey().equals(newTag.getKey())) {
                    if (oldTag.getValue().equals(newTag.getValue())) {
                        tagsToAdd.remove(newTag);
                    }
                    tagsToRemove.remove(oldTag);
                    break;
                }
            }
        }
        final GetOutpostResolverRequest readRequest = new GetOutpostResolverRequest()
                .withId(model.getId());
        String arn = proxy.injectCredentialsAndInvoke(readRequest, client::getOutpostResolver)
                .getOutpostResolver().getArn();
        TagResourceRequest tagRequest = null;
        UntagResourceRequest untagRequest = null;
        if (tagsToRemove.size() > 0) {
            untagRequest = new UntagResourceRequest()
                    .withResourceArn(arn)
                    .withTagKeys(tagsToRemove.stream().map(Tag::getKey).collect(Collectors.toList()));
        }
        if (tagsToAdd.size() > 0) {
            tagRequest = new TagResourceRequest()
                    .withResourceArn(arn)
                    .withTags(TagHelper.translateTags(tagsToAdd));
        }
        if (untagRequest != null) {
            proxy.injectCredentialsAndInvoke(untagRequest, client::untagResource);
        }
        if (tagRequest != null) {
            proxy.injectCredentialsAndInvoke(tagRequest, client::tagResource);
        }

    }
}
