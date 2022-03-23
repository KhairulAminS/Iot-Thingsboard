package Example;
import org.json.JSONObject;
import org.springframework.core.io.ClassPathResource;
import org.thingsboard.rest.client.RestClient;
import org.thingsboard.server.common.data.Customer;
import org.thingsboard.server.common.data.Dashboard;
import org.thingsboard.server.common.data.DashboardInfo;
import org.thingsboard.server.common.data.Device;
import org.thingsboard.server.common.data.id.CustomerId;
import org.thingsboard.server.common.data.id.DashboardId;
import org.thingsboard.server.common.data.id.DeviceId;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.page.PageLink;
import org.thingsboard.server.common.data.security.DeviceCredentials;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.UUID;


public class exampleRestClient {

    static String url = "http://localhost:8080";
    static RestClient client = new RestClient(url);
    static Customer newCustomer = new Customer();
    static Device newDevice = new Device();
    static ArrayList<String> split = new ArrayList<>();

    public static void main(String[] args) throws IOException, InterruptedException {

        login();
        readTextFile();
        makeCustomer();
        makeDevice();
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

        UUID dashboardUUID = UUID.fromString("8f5cc4c0-9f7e-11ec-b14a-ed2dbe18640a");
        DashboardId dashID = new DashboardId(dashboardUUID);

        client.assignDashboardToCustomer(newCustomer.getId(), dashID);
    }

}
