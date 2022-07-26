package software.amazon.route53resolver.outpostresolver.util;

import com.amazonaws.services.route53resolver.AmazonRoute53Resolver;
import com.amazonaws.services.route53resolver.model.OutpostResolver;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.route53resolver.outpostresolver.ResourceModel;
import software.amazon.route53resolver.outpostresolver.TagHelper;

public class OutpostResolverUtil {



    public static void outpostResolverToModelMapper(ResourceModel model, OutpostResolver or, AmazonWebServicesClientProxy proxy,
                                                    AmazonRoute53Resolver client) {
        model.setId(or.getId());
        model.setArn(or.getArn());
        model.setOutpostArn(or.getOutpostArn());
        model.setInstanceCount(or.getInstanceCount());
        model.setPreferredInstanceType(or.getPreferredInstanceType());
        model.setName(or.getName());
        model.setStatus(or.getStatus());
        model.setStatusMessage(or.getStatusMessage());
        model.setCreatorRequestId(or.getCreatorRequestId());
        model.setCreationTime(or.getCreationTime());
        model.setModificationTime(or.getModificationTime());
        //we pass in null when we do not want to translate tags
        //for example when deleting its possible the Outpost Resolver is deleted and we are no longer allowed to call listTagsForResource
        //and for updates we do not want to update the desiredState
        if (proxy !=null && client != null) {
            model.setTags(TagHelper.listAndTranslateTags(client, proxy, model));
        }
    }
}
