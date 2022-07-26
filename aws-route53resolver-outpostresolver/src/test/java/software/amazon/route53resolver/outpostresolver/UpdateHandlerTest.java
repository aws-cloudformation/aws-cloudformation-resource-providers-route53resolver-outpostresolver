package software.amazon.route53resolver.outpostresolver;

import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.services.route53resolver.AbstractAmazonRoute53Resolver;
import com.amazonaws.services.route53resolver.AmazonRoute53Resolver;
import com.amazonaws.services.route53resolver.model.*;
import com.amazonaws.services.route53resolver.model.Tag;
import org.junit.jupiter.api.Tags;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.proxy.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.route53resolver.outpostresolver.util.TagResourceMatcher;
import software.amazon.route53resolver.outpostresolver.util.UnTagResourceMatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UpdateHandlerTest {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    private AmazonRoute53Resolver client;
    private UpdateHandler handler;
    private ResourceModel model;
    private ResourceHandlerRequest<ResourceModel> request;
    private CallbackContext context;
    private ListTagsForResourceResult listTagResponse;

    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        client = new ClientBuilder().getClient();
        logger = mock(Logger.class);
        handler = new UpdateHandler();
        model = ResourceModel.builder().id("rslvr-op-123").outpostArn("op-111").build();
        request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .previousResourceState(model)
                .build();
        context =  CallbackContext.builder().build();
        com.amazonaws.services.route53resolver.model.Tag tag = new Tag().withKey("key1").withValue("value1");
        this.listTagResponse = new ListTagsForResourceResult().withTags(tag);
    }

    @Test
    public void updateToOperational() {
        UpdateOutpostResolverResult updateResponse = new UpdateOutpostResolverResult().withOutpostResolver(
                new OutpostResolver().withId("rslvr-op-123").withOutpostArn("op-111").withStatus(OutpostResolverStatus.UPDATING.toString()));

        GetOutpostResolverResult updatingResponse = new GetOutpostResolverResult().withOutpostResolver(
                new OutpostResolver().withId("rslvr-op-1").withOutpostArn("op-111").withArn("arn-123").withStatus(OutpostResolverStatus.UPDATING.toString()));

        GetOutpostResolverResult operationalResponse = new GetOutpostResolverResult().withOutpostResolver(
                new OutpostResolver().withId("rslvr-op-123").withOutpostArn("op-111").withArn("arn-123").withStatus(OutpostResolverStatus.OPERATIONAL.toString()));

        //first call will mock update response
        lenient().when(proxy.injectCredentialsAndInvoke(any(UpdateOutpostResolverRequest.class), any(Function.class)))
                .thenReturn(updateResponse);

        //second call will mock updating response, third call will mock operational response
        lenient().when(proxy.injectCredentialsAndInvoke(any(GetOutpostResolverRequest.class), any(Function.class)))
                .thenReturn(updatingResponse)
                .thenReturn(operationalResponse);

        lenient().when(proxy.injectCredentialsAndInvoke(any(ListTagsForResourceRequest.class), any(Function.class)))
                .thenReturn(listTagResponse);
        lenient().when(proxy.injectCredentialsAndInvoke(any(TagResourceRequest.class), any(Function.class)))
                .thenReturn(null);
        lenient().when(proxy.injectCredentialsAndInvoke(any(UntagResourceRequest.class), any(Function.class)))
                .thenReturn(null);


        //first call, update response
        final ProgressEvent<ResourceModel, CallbackContext> call1Response
                = handler.handleRequest(proxy, request, context, logger);

        assertThat(call1Response).isNotNull();
        assertThat(call1Response.getStatus()).isEqualTo(OperationStatus.IN_PROGRESS);
        assertThat(call1Response.getCallbackContext()).isNotNull();
        assertThat(call1Response.getResourceModel().getId()).isEqualTo("rslvr-op-123");
        assertThat(call1Response.getResourceModel().getStatus()).isEqualTo(OutpostResolverStatus.UPDATING.toString());
        assertThat(call1Response.getCallbackContext().getOutpostResolverId()).isEqualTo("rslvr-op-123");
        assertThat(call1Response.getCallbackContext().isMutationStarted()).isTrue();
        assertThat(call1Response.getCallbackContext().isMutationStabilized()).isFalse();

        //second call, should be in updating
        final ProgressEvent<ResourceModel, CallbackContext> call2Response
                = handler.handleRequest(proxy, request, context, logger);

        assertThat(call2Response).isNotNull();
        assertThat(call2Response.getStatus()).isEqualTo(OperationStatus.IN_PROGRESS);
        assertThat(call2Response.getCallbackContext()).isNotNull();
        assertThat(call2Response.getResourceModel().getId()).isEqualTo("rslvr-op-123");
        assertThat(call1Response.getResourceModel().getStatus()).isEqualTo(OutpostResolverStatus.UPDATING.toString());
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
        assertThat(call1Response.getResourceModel().getStatus()).isEqualTo(OutpostResolverStatus.OPERATIONAL.toString());
        assertThat(call3Response.getCallbackContext().getOutpostResolverId()).isEqualTo("rslvr-op-123");
        assertThat(call3Response.getCallbackContext().isMutationStarted()).isTrue();
        assertThat(call3Response.getCallbackContext().isMutationStabilized()).isTrue();
    }


    @Test
    public void updateOutpostArn() {
        ResourceModel desiredModel = ResourceModel.builder().id("rslvr-op-123").outpostArn("op-222").build();
        request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(desiredModel)
                .previousResourceState(model)
                .build();

        try {
            final ProgressEvent<ResourceModel, CallbackContext> call1Response
                    = handler.handleRequest(proxy, request, context, logger);
            fail("Should throw exception");
        } catch (CfnInvalidRequestException e) {
            assertThat(e.getMessage()).isEqualTo("Invalid request provided: op-222, OutpostArn is not updatable");
        }
    }

    @Test
    public void testUpdateNonExistentOutpostResolver() {
        model = ResourceModel.builder().build();
        request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .previousResourceState(model)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> call1Response
                = handler.handleRequest(proxy, request, context, logger);
        assertThat(call1Response).isNotNull();
        assertThat(call1Response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(call1Response.getErrorCode()).isEqualTo(HandlerErrorCode.NotFound);
    }

    @Test
    public void testTagResurce() {
        GetOutpostResolverResult actionNeededResponse = new GetOutpostResolverResult().withOutpostResolver(
                new OutpostResolver().withId("rslvr-op-123").withOutpostArn("op-111").withArn("arn-123").withStatus(OutpostResolverStatus.OPERATIONAL.toString()));
        lenient().when(proxy.injectCredentialsAndInvoke(any(GetOutpostResolverRequest.class), any(Function.class)))
                .thenReturn(actionNeededResponse);

        lenient().when(proxy.injectCredentialsAndInvoke(any(TagResourceRequest.class), any(Function.class)))
                .thenReturn(null);
        lenient().when(proxy.injectCredentialsAndInvoke(any(UntagResourceRequest.class), any(Function.class)))
                .thenReturn(null);

        ResourceHandlerRequest<ResourceModel> request = new ResourceHandlerRequest<ResourceModel>();
        request.setPreviousResourceTags(new HashMap<String, String>() {{
            put("previousKey1", "value1");
            put("desiredKey1", "value2");
        }});
        request.setDesiredResourceTags(new HashMap<String, String>() {{
            put("desiredKey1", "value1");
        }});
        request.setDesiredResourceState(ResourceModel.builder().build());
        request.setPreviousResourceState(ResourceModel.builder().build());

        handler.tagResource(request, proxy, client, request.getDesiredResourceState(), request.getPreviousResourceState());

        Tag reqTag = new Tag().withKey("desiredKey1").withValue("value1");
        TagResourceRequest tagRequest = new TagResourceRequest().withResourceArn("arn-123").withTags(reqTag);
        UntagResourceRequest untagRequest = new UntagResourceRequest().withResourceArn("arn-123").withTagKeys(Arrays.asList("previousKey1"));

        verify(proxy, times(1)).injectCredentialsAndInvoke(argThat(new TagResourceMatcher(tagRequest)), any(Function.class));
        verify(proxy, times(1)).injectCredentialsAndInvoke(argThat(new UnTagResourceMatcher(untagRequest)), any(Function.class));
    }

    @Test
    public void testUpdateToFail() {
        UpdateOutpostResolverResult updateResponse = new UpdateOutpostResolverResult().withOutpostResolver(
                new OutpostResolver().withId("rslvr-op-123").withOutpostArn("op-111").withStatus("UPDATING"));

        GetOutpostResolverResult actionNeededResponse = new GetOutpostResolverResult().withOutpostResolver(
                new OutpostResolver().withId("rslvr-op-123").withOutpostArn("op-111").withArn("arn-123").withStatus(OutpostResolverStatus.ACTION_NEEDED.toString()));


        //first call will mock update response
        lenient().when(proxy.injectCredentialsAndInvoke(any(UpdateOutpostResolverRequest.class), any(Function.class)))
                .thenReturn(updateResponse);

        //second call will mock action needed response
        lenient().when(proxy.injectCredentialsAndInvoke(any(GetOutpostResolverRequest.class), any(Function.class)))
                .thenReturn(actionNeededResponse);

        lenient().when(proxy.injectCredentialsAndInvoke(any(ListTagsForResourceRequest.class), any(Function.class)))
                .thenReturn(listTagResponse);
        lenient().when(proxy.injectCredentialsAndInvoke(any(TagResourceRequest.class), any(Function.class)))
                .thenReturn(null);
        lenient().when(proxy.injectCredentialsAndInvoke(any(UntagResourceRequest.class), any(Function.class)))
                .thenReturn(null);

        //first call, start update
        final ProgressEvent<ResourceModel, CallbackContext> call1Response
                = handler.handleRequest(proxy, request, context, logger);

        assertThat(call1Response).isNotNull();
        assertThat(call1Response.getStatus()).isEqualTo(OperationStatus.IN_PROGRESS);
        assertThat(call1Response.getCallbackContext()).isNotNull();
        assertThat(call1Response.getResourceModel().getId()).isEqualTo("rslvr-op-123");
        assertThat(call1Response.getResourceModel().getStatus()).isEqualTo(OutpostResolverStatus.UPDATING.toString());
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
        assertThat(call1Response.getResourceModel().getStatus()).isEqualTo(OutpostResolverStatus.ACTION_NEEDED.toString());
        assertThat(call2Response.getCallbackContext().getOutpostResolverId()).isEqualTo("rslvr-op-123");
        assertThat(call2Response.getCallbackContext().isMutationStarted()).isTrue();
        assertThat(call2Response.getCallbackContext().isMutationStabilized()).isTrue();

    }
}
