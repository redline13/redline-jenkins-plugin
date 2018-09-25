package com.redline.jenkins;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.ListBoxModel;
import org.kohsuke.stapler.DataBoundConstructor;

public class Servers extends AbstractDescribableImpl<Servers> {

    public String location = null;
    public String size  = null;
    public Integer numberServers = null;
    public Boolean useSpot = false;
    public Double maxPrice = null;
    public Integer volumeSize  = null;
    public String subnetId = null;
    public Boolean associatePublicIpAddress = true;
    public String securityGroupIds;
    public Integer usersPerServer;

    /**
     * Gather all the inputs required to define cloud settings data to launch
     * tests.
     *
     * @param location AWS EC2 Location (Default us-east-1)
     * @param size AWS EC2 Size (Default m3.medium)
     * @param numberServers Number of servers to launch, default 1
     * @param usersPerServer Users per server for custom tests
     * @param useSpot On Demand or Spot Pricing Default F
     * @param maxPrice For Spot pricing
     * @param volumeSize Disk Size, default 8GB
     * @param subnetId Subnet to launch instances on
     * @param associatePublicIpAddress If SubNet should we give it public IP
     * @param securityGroupIds Security Groups
     */
    @DataBoundConstructor
    public Servers(
            String location,
            String size,
            Integer numberServers,
            Integer usersPerServer,
            Boolean useSpot,
            Double maxPrice,
            Integer volumeSize,
            String subnetId,
            Boolean associatePublicIpAddress,
            String securityGroupIds) {
        this.location = location;
        this.size = size;
        this.numberServers = numberServers;
        this.usersPerServer = usersPerServer;
        this.useSpot = useSpot;
        this.maxPrice = maxPrice;
        this.volumeSize = volumeSize;
        this.subnetId = subnetId;
        this.associatePublicIpAddress = associatePublicIpAddress;
        this.securityGroupIds = securityGroupIds;
    }

    public String getLocation() {
        return location;
    }

    public String getSize() {
        return size;
    }

    public Integer getNumberServers() {
        if ( numberServers == null ){
            return 1;
        }
        return numberServers;
    }

    public Integer getUsersPerServer(){
        if ( usersPerServer == null ){
            return 1;
        }
        return usersPerServer;
    }

    public Boolean getUseSpot() {
        return useSpot;
    }

    public Double getMaxPrice() {
        return maxPrice;
    }

    public Integer getVolumeSize() {
        return volumeSize;
    }

    public String getSubnetId() {
        return subnetId;
    }

    public Boolean getAssociatePublicIpAddress() {
        return associatePublicIpAddress;
    }

    public String getSecurityGroupIds() {
        return securityGroupIds;
    }


    @Extension
    public static class DescriptorImpl extends Descriptor<Servers> {

        @Override
        public String getDisplayName() {
            return "";
        }

        public ListBoxModel doFillLocationItems() {
            ListBoxModel items = new ListBoxModel();
            items.add("Virginia (us-east-1)","us-east-1");
            items.add("US East (Ohio) (us-east-2)","us-east-2");
            items.add("California (us-west-1)","us-west-1");
            items.add("Oregon (us-west-2)","us-west-2");
            items.add("Ireland (eu-west-1)","eu-west-1");
            items.add("Frankfurt (eu-central-1)","eu-central-1");
            items.add("South America (Sao Paulo) (sa-east-1)","sa-east-1");
            items.add("Asia Pacific Southeast (Singapore) (ap-southeast-1)","ap-southeast-1");
            items.add("Asia Pacific Southeast (Sydney) (ap-southeast-2)","ap-southeast-2");
            items.add("Asia Pacific Northeast (Tokyo) (ap-northeast-1)","ap-northeast-1");
            items.add("Asia Pacific Northeast (Seoul) (ap-northeast-2)","ap-northeast-2");
            items.add("Asia Pacific South (Mumbai) (ap-south-1)","ap-south-1");
            return items;
        }

        public ListBoxModel doFillSizeItems(){
            ListBoxModel items = new ListBoxModel();
            items.add("t2.nano","t2.nano");
            items.add("t2.micro","t2.micro");
            items.add("t2.small","t2.small");
            items.add("t2.medium","t2.medium");
            items.add("t2.large","t2.large");
            items.add("t2.xlarge","t2.xlarge");
            items.add("t2.2xlarge","t2.2xlarge");
            items.add("t3.nano","t3.nano");
            items.add("t3.micro","t3.micro");
            items.add("t3.small","t3.small");
            items.add("t3.medium","t3.medium");
            items.add("t3.large","t3.large");
            items.add("t3.xlarge","t3.xlarge");
            items.add("t3.2xlarge","t3.2xlarge");
            items.add("m3.medium","m3.medium");
            items.add("m3.large","m3.large");
            items.add("m3.xlarge","m3.xlarge");
            items.add("m3.2xlarge","m3.2xlarge");
            items.add("m4.large","m4.large");
            items.add("m4.xlarge","m4.xlarge");
            items.add("m4.2xlarge","m4.2xlarge");
            items.add("m4.4xlarge","m4.4xlarge");
            items.add("m4.10xlarge","m4.10xlarge");
            items.add("m4.16xlarge","m4.16xlarge");
            items.add("m5.large","m5.large");
            items.add("m5.xlarge","m5.xlarge");
            items.add("m5.2xlarge","m5.2xlarge");
            items.add("m5.4xlarge","m5.4xlarge");
            items.add("m5.12xlarge","m5.12xlarge");
            items.add("m5.24xlarge","m5.24xlarge");
            items.add("c3.large","c3.large");
            items.add("c3.xlarge","c3.xlarge");
            items.add("c3.2xlarge","c3.2xlarge");
            items.add("c3.4xlarge","c3.4xlarge");
            items.add("c3.8xlarge","c3.8xlarge");
            items.add("c4.large","c4.large");
            items.add("c4.xlarge","c4.xlarge");
            items.add("c4.2xlarge","c4.2xlarge");
            items.add("c4.4xlarge","c4.4xlarge");
            items.add("c4.8xlarge","c4.8xlarge");
            items.add("c5.large","c5.large");
            items.add("c5.xlarge","c5.xlarge");
            items.add("c5.2xlarge","c5.2xlarge");
            items.add("c5.4xlarge","c5.4xlarge");
            items.add("c5.9xlarge","c5.9xlarge");
            items.add("c5.18xlarge","c5.18xlarge");
            items.add("r3.large","r3.large");
            items.add("r3.xlarge","r3.xlarge");
            items.add("r3.2xlarge","r3.2xlarge");
            items.add("r3.4xlarge","r3.4xlarge");
            items.add("r3.8xlarge","r3.8xlarge");
            items.add("r4.large","r4.large");
            items.add("r4.xlarge","r4.xlarge");
            items.add("r4.2xlarge","r4.2xlarge");
            items.add("r4.4xlarge","r4.4xlarge");
            items.add("r4.8xlarge","r4.8xlarge");
            items.add("r4.16xlarge","r4.16xlarge");
            items.add("r5.large","r5.large");
            items.add("r5.xlarge","r5.xlarge");
            items.add("r5.2xlarge","r5.2xlarge");
            items.add("r5.4xlarge","r5.4xlarge");
            items.add("r5.12xlarge","r5.12xlarge");
            items.add("r5.24xlarge","r5.24xlarge");
            items.add("i2.xlarge","i2.xlarge");
            items.add("i2.2xlarge","i2.2xlarge");
            items.add("i2.4xlarge","i2.4xlarge");
            items.add("i2.8xlarge","i2.8xlarge");
            items.add("x1.16xlarge","x1.16xlarge");
            items.add("x1.32xlarge","x1.32xlarge");
            return items;
        }

    }
}
