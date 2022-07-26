package software.amazon.route53resolver.outpostresolver.util;

import com.amazonaws.services.route53resolver.model.TagResourceRequest;
import org.mockito.ArgumentMatcher;

public class TagResourceMatcher implements ArgumentMatcher<TagResourceRequest> {

    private TagResourceRequest value1;

    public TagResourceMatcher(TagResourceRequest req){
        this.value1 = req;
    }

    @Override
    public boolean matches(TagResourceRequest value2) {
        return value1.getResourceArn().equals(value2.getResourceArn()) && value1.getTags().equals(value2.getTags());
    }
}