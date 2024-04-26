import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.net.util.SubnetUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

public class ServiceTag {
    
    public static void main(String[] args) {
        try {
            // TODO: update URL to the latest version
            ServiceTagModel data = loadData("https://download.microsoft.com/download/7/1/D/71D86715-5596-4529-9B13-DA13A5DE5B63/ServiceTags_Public_20240422.json");
            Scanner scanner = new Scanner(System.in);
            String option = "";
            while (!option.equals("0")) {
                System.out.println("\r\n*****************************");
                System.out.println("* Azure Service Tags Finder *");
                System.out.println("*****************************");
                System.out.println("1. Enter IP address to find service tags");
                System.out.println("2. Enter service tag name to get its list of address prefixes (Note: can be a very long list)");
                System.out.println("3. Enter system service name to find service tags");
                System.out.println("0. Exit");
                option = scanner.nextLine();

                if (option.equals("1")) {
                    System.out.println("Enter IP address: ");
                    String ip = scanner.nextLine();
                    List<String> serviceTags = getServiceTagForIP(data, ip);
                    if (serviceTags.isEmpty()) {
                        System.out.println("No service tags found for IP address " + ip);
                    } else {
                        System.out.println("\r\nService tags found for IP address " + ip + ":");
                        for (String tag : serviceTags) {
                            System.out.println(tag);
                        }
                    }                    
                } else if (option.equals("2")) {
                    System.out.println("Enter service tag name: ");
                    String tagName = scanner.nextLine();
                    for (ServiceTagValue tag : data.getValues()) {
                        if (tag.getName().equalsIgnoreCase(tagName)) {
                            System.out.println("\r\nAddress prefixes for service tag " + tagName + ":");
                            for (String prefix : tag.getProperties().getAddressPrefixes()) {
                                System.out.println(prefix);
                            }
                        }
                    }
                } else if (option.equals("3")) {
                    System.out.println("Enter system service name: ");
                    String systemService = scanner.nextLine();
                    System.out.println("\r\nService tag for system service " + systemService + ":");
                    for (ServiceTagValue tag : data.getValues()) {
                        if (tag.getProperties().getSystemService().equalsIgnoreCase(systemService)) {
                            System.out.println(tag.getName() + " - " + tag.getProperties().toString());
                        }
                    }
                }
            }

            scanner.close();
        } catch (IOException e) {
            System.out.println("Error loading data: " + e.getMessage());
        }
    }

    private static class ServiceTagModel {
        private int changeNumber;
        private String cloud;
        private List<ServiceTagValue> values;

        public int getChangeNumber() {
            return changeNumber;
        }
        public void setChangeNumber(int changeNumber) {
            this.changeNumber = changeNumber;
        }
        public String getCloud() {
            return cloud;
        }
        public void setCloud(String cloud) {
            this.cloud = cloud;
        }
        public List<ServiceTagValue> getValues() {
            return values;
        }
        public void setValues(List<ServiceTagValue> values) {
            this.values = values;
        }
      
    }
    
    private static class ServiceTagValue {
        private String name;
        private String id;
        private ServiceTagProperties properties;

        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public String getId() {
            return id;
        }
        public void setId(String id) {
            this.id = id;
        }
        public ServiceTagProperties getProperties() {
            return properties;
        }
        public void setProperties(ServiceTagProperties properties) {
            this.properties = properties;
        }
    
    }
    
    private static class ServiceTagProperties {
        private int changeNumber;
        private String region;
        private int regionId;
        private String platform;
        private String systemService;
        private List<String> addressPrefixes;
        private List<String> networkFeatures;

        public int getChangeNumber() {
            return changeNumber;
        }
        public void setChangeNumber(int changeNumber) {
            this.changeNumber = changeNumber;
        }
        public String getRegion() {
            return region;
        }
        public void setRegion(String region) {
            this.region = region;
        }
        public int getRegionId() {
            return regionId;
        }
        public void setRegionId(int regionId) {
            this.regionId = regionId;
        }
        public String getPlatform() {
            return platform;
        }
        public void setPlatform(String platform) {
            this.platform = platform;
        }
        public String getSystemService() {
            return systemService;
        }
        public void setSystemService(String systemService) {
            this.systemService = systemService;
        }
        public List<String> getAddressPrefixes() {
            return addressPrefixes;
        }
        public void setAddressPrefixes(List<String> addressPrefixes) {
            this.addressPrefixes = addressPrefixes;
        }
        public List<String> getNetworkFeatures() {
            return networkFeatures;
        }
        public void setNetworkFeatures(List<String> networkFeatures) {
            this.networkFeatures = networkFeatures;
        }
        public String toString() {
            return "ServiceTagProperties [changeNumber=" + changeNumber + ", region=" + region + ", regionId=" + regionId
                    + ", platform=" + platform + ", systemService=" + systemService + ", addressPrefixes=" + addressPrefixes.size()
                    + ", networkFeatures=" + networkFeatures.size() + "]";        
        }   
    }
    private static ServiceTagModel loadData(String filename) throws IOException {
        URI uri;
        StringBuilder json = new StringBuilder();
        try {
            uri = new URI(filename);
            InputStream input = uri.toURL().openStream();
            InputStreamReader isr = new InputStreamReader(input);
            BufferedReader reader = new BufferedReader(isr);
            int c;
            while ((c = reader.read()) != -1) {
                json.append((char) c);
            }
        } catch (URISyntaxException e) {
            System.out.println("Error loading data: " + e.getMessage());
        }
        System.out.println("Azure json downloaded: " + json.length() + " bytes");
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json.toString(), ServiceTagModel.class);
    }

    private static List<String> getServiceTagForIP(ServiceTagModel data, String ip) {
        List<String> serviceTagFound = new ArrayList<String>();
        IPAddressChecker checker = new IPAddressChecker();
        for (ServiceTagValue tag : data.getValues()) {
            for (String prefix : tag.getProperties().getAddressPrefixes()) {
                //if(isValidAddressPrefix(prefix)) {
                    try {
                        if (checker.isIPAddressInSubnet(prefix, ip)) {
                            serviceTagFound.add(tag.getName() + " (" + prefix + ") - " + tag.properties.toString());
                        }
                    } catch (Exception e) {
                        // ignore
                    }
                //}
            }
        }
        return serviceTagFound;
    }

    
    private static class IPAddressChecker {
        public boolean isIPAddressInSubnet(String subnet, String ipAddress) {
            SubnetUtils utils = new SubnetUtils(subnet);
            return utils.getInfo().isInRange(ipAddress);
        }
    }

    private static boolean isValidAddressPrefix(String prefix) {
        try {
            // Split the string into address and CIDR parts
            String[] parts = prefix.split("/");
            if (parts.length > 2) {
                return false; // Invalid format
            }
    
            // Check if the address part is a valid IPv4 address
            InetAddress address = InetAddress.getByName(parts[0]);
            if (!(address instanceof Inet4Address)) {
                return false; // Not an IPv4 address
            }
    
            // If there's a CIDR part, check if it's a valid integer between 0 and 32
            if (parts.length == 2) {
                int cidr = Integer.parseInt(parts[1]);
                if (cidr < 0 || cidr > 32) {
                    return false; // Invalid CIDR
                }
            }
    
            return true; // The prefix is valid
        } catch (UnknownHostException | NumberFormatException e) {
            return false; // The prefix is invalid
        }
    }
}
