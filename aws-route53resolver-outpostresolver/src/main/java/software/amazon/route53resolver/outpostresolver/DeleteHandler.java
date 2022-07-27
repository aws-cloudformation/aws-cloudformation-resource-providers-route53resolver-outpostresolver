package software.amazon.route53resolver.outpostresolver;

import com.amazonaws.services.route53resolver.AmazonRoute53Resolver;
import com.amazonaws.services.route53resolver.model.*;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;
import software.amazon.cloudformation.proxy.*;
import software.amazon.route53resolver.outpostresolver.util.OutpostResolverUtil;


public class DeleteHandler extends BaseHandler<CallbackContext> {

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

        final ResourceModel model = request.getDesiredResourceState();
        logger.log("DeleteHandler request: " + request);
        final CallbackContext context = callbackContext == null ? CallbackContext.builder().build() : callbackContext;

        if (!context.isMutationStarted()) {
            final DeleteOutpostResolverRequest deleteRequest = new DeleteOutpostResolverRequest()
                    .withId(model.getId());
            context.setMutationStarted(true);
            context.setMutationStabilized(false);
            try {
                final DeleteOutpostResolverResult deleteResult =
                        proxy.injectCredentialsAndInvoke(deleteRequest, client::deleteOutpostResolver);
                if(deleteResult == null || deleteResult.getOutpostResolver() == null) {
                    context.setMutationStabilized(true);
                    return ProgressEvent.<ResourceModel, CallbackContext>builder()
                            .resourceModel(model)
                            .status(OperationStatus.FAILED)
                            .errorCode(HandlerErrorCode.NotFound)
                            .build();
                } else {
                    context.setOutpostResolverId(deleteResult.getOutpostResolver().getId());
                    OutpostResolverUtil.outpostResolverToModelMapper(model, deleteResult.getOutpostResolver(), null, null);
                }

                logger.log("...........................................");
                logger.log("the OutpostResolver is being deleted...");
                logger.log("the OutpostResolver name is " + model.getName());
                logger.log("the OutpostResolver Id is " + model.getId());
                logger.log("current status is " + model.getStatus());
                logger.log("pending stabilization...");
                logger.log("...........................................");

            } catch (final ResourceNotFoundException e) {
                throw new CfnNotFoundException(ResourceModel.TYPE_NAME, model.getPrimaryIdentifier().toString());
            } catch (InvalidRequestException | InvalidParameterException | ValidationException e) {
                throw new CfnInvalidRequestException(deleteRequest.toString(), e);
            } catch (InternalServiceErrorException e) {
                throw new CfnServiceInternalErrorException(ResourceModel.TYPE_NAME, e);
            } catch (ThrottlingException e) {
                throw new CfnThrottlingException(ResourceModel.TYPE_NAME, e);
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

            GetOutpostResolverResult getResult =
                    proxy.injectCredentialsAndInvoke(getRequest, client::getOutpostResolver);

            //Outpost resolver deleted will not return anything
            if (getResult == null || getResult.getOutpostResolver() == null) {
                logger.log(String.format("%s [%s] deleted successfully",
                        ResourceModel.TYPE_NAME, model.getId()));
                context.setMutationStabilized(true);
                return ProgressEvent.<ResourceModel, CallbackContext>builder()
                        .status(OperationStatus.SUCCESS)
                        .build();
            }

            context.setOutpostResolverId(getResult.getOutpostResolver().getId());
            OutpostResolverUtil.outpostResolverToModelMapper(model, getResult.getOutpostResolver(), null, null);
            if (getResult.getOutpostResolver().getStatus().equals(OutpostResolverStatus.FAILED_DELETION.toString())) {
                context.setMutationStabilized(true);
                return ProgressEvent.<ResourceModel, CallbackContext>builder()
                        .callbackContext(context)
                        .status(OperationStatus.FAILED)
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
