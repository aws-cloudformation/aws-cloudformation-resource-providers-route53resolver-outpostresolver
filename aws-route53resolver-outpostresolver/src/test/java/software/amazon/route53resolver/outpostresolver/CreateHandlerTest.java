package software.amazon.route53resolver.outpostresolver;

import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.services.route53resolver.model.*;
import com.amazonaws.services.route53resolver.model.Tag;
import org.mockito.Mockito;
import org.mockito.ArgumentMatchers;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CreateHandlerTest {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    private CreateHandler handler;
    private ResourceModel model;
    private ResourceHandlerRequest<ResourceModel> request;
    private CallbackContext context;
    private ListTagsForResourceResult listTagResponse;

    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
        handler = new CreateHandler();
        model = ResourceModel.builder().build();
        request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();
        context =  CallbackContext.builder().build();
        com.amazonaws.services.route53resolver.model.Tag tag = new Tag().withKey("key1").withValue("value1");
        this.listTagResponse = new ListTagsForResourceResult().withTags(tag);
    }

    @Test
    public void testCreateToOperational() {


        CreateOutpostResolverResult createResponse = new CreateOutpostResolverResult().withOutpostResolver(
                new OutpostResolver().withId("rslvr-op-123").withStatus(OutpostResolverStatus.CREATING.toString()));

        GetOutpostResolverResult creatingResponse = new GetOutpostResolverResult().withOutpostResolver(
                new OutpostResolver().withId("rslvr-op-123").withStatus(OutpostResolverStatus.CREATING.toString()));

        GetOutpostResolverResult operationalResponse = new GetOutpostResolverResult().withOutpostResolver(
                new OutpostResolver().withId("rslvr-op-123").withStatus(OutpostResolverStatus.OPERATIONAL.toString()));

        lenient().when(proxy.injectCredentialsAndInvoke(any(ListTagsForResourceRequest.class), any(Function.class)))
                .thenReturn(listTagResponse);

        //first call will mock create response
        lenient().when(proxy.injectCredentialsAndInvoke(any(CreateOutpostResolverRequest.class), any(Function.class)))
                .thenReturn(createResponse);
        //second call will mock creating response, third call will mock operational response
        lenient().when(proxy.injectCredentialsAndInvoke(any(GetOutpostResolverRequest.class), any(Function.class)))
                .thenReturn(creatingResponse)
                .thenReturn(operationalResponse);


        //first call, start create
        final ProgressEvent<ResourceModel, CallbackContext> call1Response
                = handler.handleRequest(proxy, request, context, logger);

        assertThat(call1Response).isNotNull();
        assertThat(call1Response.getStatus()).isEqualTo(OperationStatus.IN_PROGRESS);
        assertThat(call1Response.getCallbackContext()).isNotNull();
        assertThat(call1Response.getResourceModel().getId()).isEqualTo("rslvr-op-123");
        assertThat(call1Response.getResourceModel().getStatus()).isEqualTo(OutpostResolverStatus.CREATING.toString());
        assertThat(call1Response.getCallbackContext().getOutpostResolverId()).isEqualTo("rslvr-op-123");
        assertThat(call1Response.getCallbackContext().isMutationStarted()).isTrue();
        assertThat(call1Response.getCallbackContext().isMutationStabilized()).isFalse();

        //second call, should be in creating
        final ProgressEvent<ResourceModel, CallbackContext> call2Response
                = handler.handleRequest(proxy, request, context, logger);

        assertThat(call2Response).isNotNull();
        assertThat(call2Response.getStatus()).isEqualTo(OperationStatus.IN_PROGRESS);
        assertThat(call2Response.getCallbackContext()).isNotNull();
        assertThat(call2Response.getResourceModel().getId()).isEqualTo("rslvr-op-123");
        assertThat(call2Response.getResourceModel().getStatus()).isEqualTo(OutpostResolverStatus.CREATING.toString());
        assertThat(call2Response.getCallbackContext().getOutpostResolverId()).isEqualTo("rslvr-op-123");
        assertThat(call2Response.getCallbackContext().isMutationStarted()).isTrue();
        assertThat(call2Response.getCallbackContext().isMutationStabilized()).isFalse();

        //third call, should be in operational
        final ProgressEvent<ResourceModel, CallbackContext> call3Response
                = handler.handleRequest(proxy, request, context, logger);

        assertThat(call3Response).isNotNull();
        assertThat(call3Response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(call3Response.getCallbackContext()).isNotNull();
        assertThat(call3Response.getResourceModel().getId()).isEqualTo("rslvr-op-123");
        assertThat(call3Response.getResourceModel().getStatus()).isEqualTo(OutpostResolverStatus.OPERATIONAL.toString());
        assertThat(call3Response.getCallbackContext().getOutpostResolverId()).isEqualTo("rslvr-op-123");
        assertThat(call3Response.getCallbackContext().isMutationStarted()).isTrue();
        assertThat(call3Response.getCallbackContext().isMutationStabilized()).isTrue();

    }

    @Test
    public void testCreateToFail() {
        CreateOutpostResolverResult creatingResponse = new CreateOutpostResolverResult().withOutpostResolver(
                new OutpostResolver().withId("rslvr-op-123").withStatus(OutpostResolverStatus.CREATING.toString()));

        GetOutpostResolverResult failedResponse = new GetOutpostResolverResult().withOutpostResolver(
                new OutpostResolver().withId("rslvr-op-123").withStatus(OutpostResolverStatus.FAILED_CREATION.toString()));


        lenient().when(proxy.injectCredentialsAndInvoke(any(ListTagsForResourceRequest.class), any(Function.class)))
                .thenReturn(listTagResponse);


        //first call will mock create response
        lenient().when(proxy.injectCredentialsAndInvoke(any(CreateOutpostResolverRequest.class), any(Function.class)))
                .thenReturn(creatingResponse);
        //second call will mock failed response
        lenient().when(proxy.injectCredentialsAndInvoke(any(GetOutpostResolverRequest.class), any(Function.class)))
                .thenReturn(failedResponse);


        //first call, start create
        final ProgressEvent<ResourceModel, CallbackContext> call1Response
                = handler.handleRequest(proxy, request, context, logger);

        assertThat(call1Response).isNotNull();
        assertThat(call1Response.getStatus()).isEqualTo(OperationStatus.IN_PROGRESS);
        assertThat(call1Response.getCallbackContext()).isNotNull();
        assertThat(call1Response.getResourceModel().getId()).isEqualTo("rslvr-op-123");
        assertThat(call1Response.getResourceModel().getStatus()).isEqualTo(OutpostResolverStatus.CREATING.toString());
        assertThat(call1Response.getCallbackContext().getOutpostResolverId()).isEqualTo("rslvr-op-123");
        assertThat(call1Response.getCallbackContext().isMutationStarted()).isTrue();
        assertThat(call1Response.getCallbackContext().isMutationStabilized()).isFalse();

        //second call, should be in failed
        final ProgressEvent<ResourceModel, CallbackContext> call2Response
                = handler.handleRequest(proxy, request, context, logger);

        assertThat(call2Response).isNotNull();
        assertThat(call2Response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(call2Response.getCallbackContext()).isNotNull();
        assertThat(call2Response.getResourceModel().getId()).isEqualTo("rslvr-op-123");
        assertThat(call1Response.getResourceModel().getStatus()).isEqualTo(OutpostResolverStatus.FAILED_CREATION.toString());
        assertThat(call2Response.getCallbackContext().getOutpostResolverId()).isEqualTo("rslvr-op-123");
        assertThat(call2Response.getCallbackContext().isMutationStarted()).isTrue();
        assertThat(call2Response.getCallbackContext().isMutationStabilized()).isTrue();

    }
}
