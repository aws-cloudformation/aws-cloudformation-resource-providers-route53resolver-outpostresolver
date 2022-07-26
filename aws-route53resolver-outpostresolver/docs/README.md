# AWS::Route53Resolver::OutpostResolver

Resource schema for AWS::Route53Resolver::OutpostResolver.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::Route53Resolver::OutpostResolver",
    "Properties" : {
        "<a href="#name" title="Name">Name</a>" : <i>String</i>,
        "<a href="#maxresults" title="MaxResults">MaxResults</a>" : <i>Integer</i>,
        "<a href="#nexttoken" title="NextToken">NextToken</a>" : <i>String</i>,
        "<a href="#outpostarn" title="OutpostArn">OutpostArn</a>" : <i>String</i>,
        "<a href="#preferredinstancetype" title="PreferredInstanceType">PreferredInstanceType</a>" : <i>String</i>,
        "<a href="#instancecount" title="InstanceCount">InstanceCount</a>" : <i>Integer</i>,
        "<a href="#tags" title="Tags">Tags</a>" : <i>[ <a href="tag.md">Tag</a>, ... ]</i>
    }
}
</pre>

### YAML

<pre>
Type: AWS::Route53Resolver::OutpostResolver
Properties:
    <a href="#name" title="Name">Name</a>: <i>String</i>
    <a href="#maxresults" title="MaxResults">MaxResults</a>: <i>Integer</i>
    <a href="#nexttoken" title="NextToken">NextToken</a>: <i>String</i>
    <a href="#outpostarn" title="OutpostArn">OutpostArn</a>: <i>String</i>
    <a href="#preferredinstancetype" title="PreferredInstanceType">PreferredInstanceType</a>: <i>String</i>
    <a href="#instancecount" title="InstanceCount">InstanceCount</a>: <i>Integer</i>
    <a href="#tags" title="Tags">Tags</a>: <i>
      - <a href="tag.md">Tag</a></i>
</pre>

## Properties

#### Name

The OutpostResolver name.

_Required_: Yes

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>255</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### MaxResults

The OutpostResolver MaxResults from List handler.

_Required_: No

_Type_: Integer

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### NextToken

The OutpostResolver NextToken from List handler.

_Required_: No

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>1024</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### OutpostArn

The Outpost ARN.

_Required_: Yes

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>1024</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### PreferredInstanceType

The OutpostResolver instance type.

_Required_: Yes

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>255</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### InstanceCount

The number of OutpostResolvers.

_Required_: No

_Type_: Integer

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Tags

An array of key-value pairs to apply to this resource.

_Required_: No

_Type_: List of <a href="tag.md">Tag</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the Id.

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### Id

Id

#### Arn

The OutpostResolver ARN.

#### Status

The OutpostResolver status, possible values are CREATING, OPERATIONAL, UPDATING, DELETING, ACTION_NEEDED, FAILED_CREATION and FAILED_DELETION.

#### StatusMessage

The OutpostResolver status message.

#### CreatorRequestId

The id of the creator request.

#### CreationTime

The OutpostResolver creation time

#### ModificationTime

The OutpostResolver last modified time

