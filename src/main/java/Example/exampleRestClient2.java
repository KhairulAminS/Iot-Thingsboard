package Example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONObject;
import org.springframework.core.io.ClassPathResource;
import org.thingsboard.rest.client.RestClient;
import org.thingsboard.server.common.data.Customer;
import org.thingsboard.server.common.data.Dashboard;
import org.thingsboard.server.common.data.Device;
import org.thingsboard.server.common.data.id.DashboardId;
import org.thingsboard.server.common.data.id.DeviceId;
import org.thingsboard.server.common.data.security.DeviceCredentials;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;
import java.util.UUID;

public class exampleRestClient2 {
    static String url = "http://localhost:8080";
    static RestClient client = new RestClient(url);
    static Customer newCustomer = new Customer();
    static Device newDevice = new Device();
    static Dashboard dashboard = new Dashboard();
    static ArrayList<String> split = new ArrayList<>();
    static ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) throws IOException, InterruptedException {

        login();
        readTextFile();
        makeCustomer();
        makeDevice();
        createDashboard();
        assignment();
        LaptopMonitor.connectTODashboard();
    }

    public static void login() {

        // Default Tenant Administrator credentials
        String username = "tenant@thingsboard.org";
        String password = "tenant";

        // Creating new rest client and auth with credentials
        client.login(username, password);
    }

    public static void readTextFile() throws IOException {
        String path = "Thingsboard Assignment.txt";
        File file = new ClassPathResource(path).getFile();

        Scanner sc = new Scanner(file);

        String line = null;

        int i = 0;
        int numberOfLine = 2;
        while(i<numberOfLine) {
            line = sc.nextLine();
            split.add(line.split(": ")[1]);
            i++;
        }
    }

    public static void makeCustomer(){
        newCustomer.setTitle(split.get(0));
        newCustomer=client.saveCustomer(newCustomer);
    }

    public static void makeDevice(){
        newDevice.setName(split.get(1));
        newDevice.setCustomerId(newCustomer.getId());
        newDevice = client.saveDevice(newDevice);

        DeviceCredentials deviceCredentials = client.getDeviceCredentialsByDeviceId(newDevice.getId()).get();
        deviceCredentials.setCredentialsId("myPC_token");
        client.saveDeviceCredentials(deviceCredentials);
    }

    public static void assignment(){

        //Assign device to customer
        client.assignDeviceToCustomer(newCustomer.getId(), newDevice.getId());

        client.assignDashboardToCustomer(newCustomer.getId(), dashboard.getId());
    }

    public static void createDashboard() throws IOException {

        JsonNode dashboardJson = mapper.readTree(exampleRestClient.class.getClassLoader().getResourceAsStream("laptop_readings.json"));
        JsonNode entityAlias = dashboardJson.get("configuration").get("entityAliases").get("37e1e0c0-8061-ad2b-591a-84ec169129f6")
                .get("filter").get("singleEntity");
        ((ObjectNode) entityAlias).put("id", newDevice.getId().toString());
        dashboard.setTitle(dashboardJson.get("title").asText());
        dashboard.setConfiguration(dashboardJson.get("configuration"));
        dashboard = client.saveDashboard(dashboard);

    }
}
