package software.amazon.route53resolver.outpostresolver;

import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.services.route53resolver.model.GetOutpostResolverResult;
import com.amazonaws.services.route53resolver.model.ListTagsForResourceResult;
import com.amazonaws.services.route53resolver.model.OutpostResolver;
import com.amazonaws.services.route53resolver.model.OutpostResolverStatus;
import com.amazonaws.services.route53resolver.model.Tag;
import software.amazon.cloudformation.proxy.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReadHandlerTest {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    private ReadHandler handler;
    private ResourceModel model;
    private ResourceHandlerRequest<ResourceModel> request;
    private CallbackContext context;

    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
        handler = new ReadHandler();

        context =  CallbackContext.builder().build();
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        model = ResourceModel.builder().id("rslvr-op-123").build();
        request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        com.amazonaws.services.route53resolver.model.Tag tag = new Tag().withKey("key1").withValue("value1");

        ListTagsForResourceResult listTagResult = new ListTagsForResourceResult().withTags(tag);

        GetOutpostResolverResult deletinglResponse = new GetOutpostResolverResult().withOutpostResolver(
                new OutpostResolver().withId("rslvr-op-123").withStatus(OutpostResolverStatus.DELETING.toString()));

        when(proxy.injectCredentialsAndInvoke(any(AmazonWebServiceRequest.class), any(Function.class)))
                .thenReturn(deletinglResponse)
                .thenReturn(listTagResult);

        final ProgressEvent<ResourceModel, CallbackContext> call1Response
                = handler.handleRequest(proxy, request, context, logger);

        assertThat(call1Response).isNotNull();
        assertThat(call1Response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(call1Response.getResourceModel().getId()).isEqualTo("rslvr-op-123");
        assertThat(call1Response.getResourceModel().getStatus()).isEqualTo("DELETING");
    }

    @Test
    public void handleRequest_NotFound() {
        model = ResourceModel.builder().id("rslvr-op-123").build();
        request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        GetOutpostResolverResult emptylResponse = new GetOutpostResolverResult();

        when(proxy.injectCredentialsAndInvoke(any(AmazonWebServiceRequest.class), any(Function.class)))
                .thenReturn(emptylResponse);

        final ProgressEvent<ResourceModel, CallbackContext> call1Response
                = handler.handleRequest(proxy, request, context, logger);

        assertThat(call1Response).isNotNull();
        assertThat(call1Response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(call1Response.getErrorCode()).isEqualTo(HandlerErrorCode.NotFound);
    }
}
