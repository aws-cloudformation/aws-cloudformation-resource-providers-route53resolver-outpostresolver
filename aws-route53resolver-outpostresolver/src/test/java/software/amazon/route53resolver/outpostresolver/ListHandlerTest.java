package software.amazon.route53resolver.outpostresolver;

import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.services.route53resolver.model.*;
import com.amazonaws.services.route53resolver.model.Tag;
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

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ListHandlerTest {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    private ListHandler handler;
    private ResourceModel model;
    private ResourceHandlerRequest<ResourceModel> request;
    private CallbackContext context;
    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
        handler = new ListHandler();
        model = ResourceModel.builder().build();
        request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();
        context =  CallbackContext.builder().build();
    }

    @Test
    public void handleRequest_SimpleSuccess() {

        model = ResourceModel.builder().outpostArn("outpost-123").build();
        request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        OutpostResolver op1 = new OutpostResolver().withArn("outpost-123").withId("rslvr-op-1").withStatus(OutpostResolverStatus.DELETING.toString());
        OutpostResolver op2 = new OutpostResolver().withArn("outpost-123").withId("rslvr-op-2").withStatus(OutpostResolverStatus.OPERATIONAL.toString());

        ListOutpostResolversResult listResponse = new ListOutpostResolversResult()
                .withOutpostResolvers(Arrays.asList(op1, op2))
                .withNextToken("token-1");
        com.amazonaws.services.route53resolver.model.Tag tag = new Tag().withKey("key1").withValue("value1");

        ListTagsForResourceResult listTagResult = new ListTagsForResourceResult().withTags(tag);

        when(proxy.injectCredentialsAndInvoke(any(AmazonWebServiceRequest.class), any(Function.class)))
                .thenReturn(listResponse)
                .thenReturn(listTagResult);

        final ProgressEvent<ResourceModel, CallbackContext> response =
            handler.handleRequest(proxy, request, context, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNotNull();
        assertThat(response.getResourceModels().size()).isEqualTo(2);
        assertThat(response.getResourceModels().get(0).getStatus()).isEqualTo(OutpostResolverStatus.DELETING.toString());
        assertThat(response.getResourceModels().get(1).getStatus()).isEqualTo(OutpostResolverStatus.OPERATIONAL.toString());
        assertThat(response.getCallbackContext().getListOperationNextToken()).isEqualTo("token-1");
    }
}
