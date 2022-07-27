package software.amazon.route53resolver.outpostresolver.util;

import com.amazonaws.services.route53resolver.model.TagResourceRequest;
import com.amazonaws.services.route53resolver.model.UntagResourceRequest;
import org.mockito.ArgumentMatcher;

public class UnTagResourceMatcher implements ArgumentMatcher<UntagResourceRequest> {

    private UntagResourceRequest value1;

    public UnTagResourceMatcher(UntagResourceRequest req){
        this.value1 = req;
    }

    @Override
    public boolean matches(UntagResourceRequest value2) {
        return value1.getResourceArn().equals(value2.getResourceArn()) && value1.getTagKeys().equals(value2.getTagKeys());
    }
}