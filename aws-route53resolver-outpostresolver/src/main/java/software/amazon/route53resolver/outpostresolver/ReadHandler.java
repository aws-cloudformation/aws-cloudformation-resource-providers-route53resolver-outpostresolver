package software.amazon.route53resolver.outpostresolver;

import com.amazonaws.services.route53resolver.AmazonRoute53Resolver;
import com.amazonaws.services.route53resolver.model.*;
import software.amazon.cloudformation.exceptions.*;
import software.amazon.cloudformation.exceptions.ResourceNotFoundException;
import software.amazon.cloudformation.proxy.*;

public class ReadHandler extends BaseHandler<CallbackContext> {

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
        logger.log("ReadHandler request: " + request);
        final GetOutpostResolverRequest readRequest = new GetOutpostResolverRequest()
                .withId(model.getId());
        try {
            final GetOutpostResolverResult getResult =
                    proxy.injectCredentialsAndInvoke(readRequest, client::getOutpostResolver);
            if (getResult != null && getResult.getOutpostResolver()!=null) {
                model.setId(getResult.getOutpostResolver().getId());
                model.setArn(getResult.getOutpostResolver().getArn());
                model.setOutpostArn(getResult.getOutpostResolver().getOutpostArn());
                model.setInstanceCount(getResult.getOutpostResolver().getInstanceCount());
                model.setPreferredInstanceType(getResult.getOutpostResolver().getPreferredInstanceType());
                model.setName(getResult.getOutpostResolver().getName());
                model.setStatus(getResult.getOutpostResolver().getStatus());
                model.setStatusMessage(getResult.getOutpostResolver().getStatusMessage());
                model.setCreatorRequestId(getResult.getOutpostResolver().getCreatorRequestId());
                model.setCreationTime(getResult.getOutpostResolver().getCreationTime());
                model.setModificationTime(getResult.getOutpostResolver().getModificationTime());
                model.setTags(TagHelper.listAndTranslateTags(client, proxy, model));
            } else {
                return ProgressEvent.<ResourceModel, CallbackContext>builder()
                        .resourceModel(model)
                        .status(OperationStatus.FAILED)
                        .errorCode(HandlerErrorCode.NotFound)
                        .build();
            }
            logger.log(String.format("%s [%s] read successfully",
                    ResourceModel.TYPE_NAME, model.getPrimaryIdentifier().toString()));
        } catch (InvalidRequestException | InvalidParameterException | ValidationException e) {
            throw new CfnInvalidRequestException(readRequest.toString(), e);
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
                .resourceModel(model)
                .status(OperationStatus.SUCCESS)
                .build();
    }

}
