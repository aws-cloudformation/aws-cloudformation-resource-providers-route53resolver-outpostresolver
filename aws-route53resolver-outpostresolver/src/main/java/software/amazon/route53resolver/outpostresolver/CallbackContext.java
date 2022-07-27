package software.amazon.route53resolver.outpostresolver;

import lombok.Builder;
import software.amazon.cloudformation.proxy.StdCallbackContext;

@lombok.Getter
@lombok.Setter
@lombok.ToString
@lombok.Builder
@lombok.EqualsAndHashCode(callSuper = true)
@lombok.AllArgsConstructor
@lombok.NoArgsConstructor

public class CallbackContext extends StdCallbackContext {
    @Builder.Default
    private boolean mutationStarted = false;
    @Builder.Default
    private boolean mutationStabilized = false;

    private String listOperationNextToken;

    private String outpostResolverId;
}
