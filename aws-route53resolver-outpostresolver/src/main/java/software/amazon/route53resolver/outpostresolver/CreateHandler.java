package software.amazon.route53resolver.outpostresolver;

import com.amazonaws.services.route53resolver.AmazonRoute53Resolver;
import com.amazonaws.services.route53resolver.model.*;
import software.amazon.cloudformation.exceptions.*;
import software.amazon.cloudformation.exceptions.ResourceNotFoundException;
import software.amazon.cloudformation.proxy.*;
import software.amazon.route53resolver.outpostresolver.util.OutpostResolverUtil;

public class CreateHandler extends BaseHandler<CallbackContext> {

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
        final CallbackContext context = callbackContext == null ? CallbackContext.builder().build() : callbackContext;

        if (!context.isMutationStarted()) {
            final CreateOutpostResolverRequest createRequest = new CreateOutpostResolverRequest()
                    .withCreatorRequestId(request.getClientRequestToken())
                    .withName(model.getName())
                    .withInstanceCount(model.getInstanceCount())
                    .withOutpostArn(model.getOutpostArn())
                    .withPreferredInstanceType(model.getPreferredInstanceType())
                    .withTags(TagHelper.translateTags(TagHelper.getAllResourceTags(model, request.getDesiredResourceTags())));
            context.setMutationStarted(true);
            context.setMutationStabilized(false);
            try {
                final CreateOutpostResolverResult createResult =
                        proxy.injectCredentialsAndInvoke(createRequest, client::createOutpostResolver);
                if (createResult != null) {
                    context.setOutpostResolverId(createResult.getOutpostResolver().getId());
                    OutpostResolverUtil.outpostResolverToModelMapper(model, createResult.getOutpostResolver(), proxy, client);
                }

                logger.log("...........................................");
                logger.log("the OutpostResolver resource is being created...");
                logger.log("the OutpostResolver Name is " + model.getName());
                logger.log("the OutpostResolver Id is " + model.getId());
                logger.log("current status is " + model.getStatus());
                logger.log("pending stabilization...");
                logger.log("...........................................");

            } catch (InvalidRequestException | InvalidParameterException | ValidationException e) {
                throw new CfnInvalidRequestException(createRequest.toString(), e);
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
            final GetOutpostResolverRequest getRequest = new GetOutpostResolverRequest()
                    .withId(context.getOutpostResolverId());

            final GetOutpostResolverResult getResult =
                    proxy.injectCredentialsAndInvoke(getRequest, client::getOutpostResolver);

            if (getResult != null && getResult.getOutpostResolver() != null) {

                OutpostResolverUtil.outpostResolverToModelMapper(model, getResult.getOutpostResolver(), proxy, client);

                //Possible end state of OutpostResolver include OPERATIONAL, ACTION_NEEDED, and FAILED_CREATION
                context.setMutationStabilized(model.getStatus().equals(OutpostResolverStatus.OPERATIONAL.toString())
                        || model.getStatus().equals(OutpostResolverStatus.ACTION_NEEDED.toString())
                        || model.getStatus().equals(OutpostResolverStatus.FAILED_CREATION.toString()));
                context.setOutpostResolverId(model.getId());

            }

            if(context.isMutationStabilized()) {
                logger.log(String.format("%s [%s] is stablized with status %s",
                        ResourceModel.TYPE_NAME, model.getId(), model.getStatus()));

                return ProgressEvent.<ResourceModel, CallbackContext>builder()
                        .callbackContext(context)
                        .message(String.format("OutpostResolver creation stabilized with the status of: %s", model.getStatus()))
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
}
