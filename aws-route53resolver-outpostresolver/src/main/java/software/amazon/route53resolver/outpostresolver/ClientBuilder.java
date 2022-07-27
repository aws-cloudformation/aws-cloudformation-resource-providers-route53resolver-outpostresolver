package software.amazon.route53resolver.outpostresolver;

import com.amazonaws.services.route53resolver.AmazonRoute53ResolverClientBuilder;
import com.amazonaws.services.route53resolver.AmazonRoute53Resolver;
import com.amazonaws.util.StringUtils;


public class ClientBuilder {

    private static final String AWS_REGION = "AWS_REGION";
    private static final String DEFAULT_AWS_REGION = "us-west-2";

    public AmazonRoute53Resolver getClient() {
        final AmazonRoute53ResolverClientBuilder awsRoute53ResolverBuilder = AmazonRoute53ResolverClientBuilder.standard();
        awsRoute53ResolverBuilder.withRegion(getEnvironmentValue(AWS_REGION, DEFAULT_AWS_REGION));
        return awsRoute53ResolverBuilder.build();
    }

    //Lambda will have an environment variable for AWS_REGION
    //https://docs.aws.amazon.com/lambda/latest/dg/configuration-envvars.html
    private static String getEnvironmentValue(final String environmentVariable, final String defaultValue) {
        final String value = System.getenv(environmentVariable);
        return StringUtils.isNullOrEmpty(value) ? defaultValue : value;
    }
}
