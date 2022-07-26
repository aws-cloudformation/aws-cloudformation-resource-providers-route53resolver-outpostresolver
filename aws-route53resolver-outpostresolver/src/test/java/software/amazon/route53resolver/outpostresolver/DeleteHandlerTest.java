package software.amazon.route53resolver.outpostresolver;

import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.services.route53resolver.model.*;
import com.amazonaws.services.route53resolver.model.Tag;
import software.amazon.cloudformation.proxy.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DeleteHandlerTest {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    private DeleteHandler handler;
    private ResourceModel model;
    private ResourceHandlerRequest<ResourceModel> request;
    private CallbackContext context;
    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
        handler = new DeleteHandler();
        model = ResourceModel.builder().id("rslvr-op-123").build();
        request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();
        context =  CallbackContext.builder().build();
    }

    @Test
    public void testDeleteSuccess() {
        DeleteOutpostResolverResult deleteResponse = new DeleteOutpostResolverResult().withOutpostResolver(
                new OutpostResolver().withId("rslvr-op-123").withStatus(OutpostResolverStatus.DELETING.toString()));

        GetOutpostResolverResult deletinglResponse = new GetOutpostResolverResult().withOutpostResolver(
                new OutpostResolver().withId("rslvr-op-123").withStatus(OutpostResolverStatus.DELETING.toString()));

        GetOutpostResolverResult deletedResponse = new GetOutpostResolverResult();

        when(proxy.injectCredentialsAndInvoke(any(AmazonWebServiceRequest.class), any(Function.class)))
                .thenReturn(deleteResponse)
                .thenReturn(deletinglResponse)
                .thenReturn(deletedResponse);

        final ProgressEvent<ResourceModel, CallbackContext> call1Response
                = handler.handleRequest(proxy, request, context, logger);

        assertThat(call1Response).isNotNull();
        assertThat(call1Response.getStatus()).isEqualTo(OperationStatus.IN_PROGRESS);
        assertThat(call1Response.getCallbackContext()).isNotNull();
        assertThat(call1Response.getResourceModel().getStatus()).isEqualTo(OutpostResolverStatus.DELETING.toString());
        assertThat(call1Response.getCallbackContext().getOutpostResolverId()).isEqualTo("rslvr-op-123");
        assertThat(call1Response.getCallbackContext().isMutationStarted()).isTrue();
        assertThat(call1Response.getCallbackContext().isMutationStabilized()).isFalse();

        final ProgressEvent<ResourceModel, CallbackContext> call2Response
                = handler.handleRequest(proxy, request, context, logger);
        assertThat(call2Response).isNotNull();
        assertThat(call2Response.getStatus()).isEqualTo(OperationStatus.IN_PROGRESS);
        assertThat(call2Response.getCallbackContext()).isNotNull();
        assertThat(call1Response.getResourceModel().getStatus()).isEqualTo(OutpostResolverStatus.DELETING.toString());
        assertThat(call2Response.getCallbackContext().getOutpostResolverId()).isEqualTo("rslvr-op-123");
        assertThat(call2Response.getCallbackContext().isMutationStarted()).isTrue();
        assertThat(call2Response.getCallbackContext().isMutationStabilized()).isFalse();

        final ProgressEvent<ResourceModel, CallbackContext> call3Response
                = handler.handleRequest(proxy, request, context, logger);
        assertThat(call3Response).isNotNull();
        assertThat(call3Response.getStatus()).isEqualTo(OperationStatus.SUCCESS);

    }

    @Test
    public void testDeleteNonExistentOutpostResolver() {
        model = ResourceModel.builder().build();
        request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> call1Response
                = handler.handleRequest(proxy, request, context, logger);
        assertThat(call1Response).isNotNull();
        assertThat(call1Response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(call1Response.getErrorCode()).isEqualTo(HandlerErrorCode.NotFound);
    }

    @Test
    public void testDeleteFailed() {
        DeleteOutpostResolverResult deleteResponse = new DeleteOutpostResolverResult().withOutpostResolver(
                new OutpostResolver().withId("rslvr-op-123").withStatus(OutpostResolverStatus.DELETING.toString()));

        GetOutpostResolverResult failedlResponse = new GetOutpostResolverResult().withOutpostResolver(
                new OutpostResolver().withId("rslvr-op-123").withStatus(OutpostResolverStatus.FAILED_DELETION.toString()));

        when(proxy.injectCredentialsAndInvoke(any(AmazonWebServiceRequest.class), any(Function.class)))
                .thenReturn(deleteResponse)
                .thenReturn(failedlResponse);

        final ProgressEvent<ResourceModel, CallbackContext> call1Response
                = handler.handleRequest(proxy, request, context, logger);

        assertThat(call1Response).isNotNull();
        assertThat(call1Response.getStatus()).isEqualTo(OperationStatus.IN_PROGRESS);
        assertThat(call1Response.getCallbackContext()).isNotNull();
        assertThat(call1Response.getCallbackContext().getOutpostResolverId()).isEqualTo("rslvr-op-123");
        assertThat(call1Response.getResourceModel().getStatus()).isEqualTo(OutpostResolverStatus.DELETING.toString());
        assertThat(call1Response.getCallbackContext().isMutationStarted()).isTrue();
        assertThat(call1Response.getCallbackContext().isMutationStabilized()).isFalse();

        final ProgressEvent<ResourceModel, CallbackContext> call2Response
                = handler.handleRequest(proxy, request, context, logger);

        assertThat(call2Response).isNotNull();
        assertThat(call2Response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(call2Response.getCallbackContext().getOutpostResolverId()).isEqualTo("rslvr-op-123");
        assertThat(call2Response.getResourceModel().getStatus()).isEqualTo(OutpostResolverStatus.FAILED_DELETION.toString());
        assertThat(call2Response.getCallbackContext().isMutationStarted()).isTrue();
        assertThat(call2Response.getCallbackContext().isMutationStabilized()).isTrue();

    }

}
