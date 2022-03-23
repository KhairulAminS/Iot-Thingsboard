package Example;

import com.sun.management.OperatingSystemMXBean;
import org.json.JSONObject;
import oshi.SystemInfo;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.net.HttpURLConnection;
import java.net.URL;

public class LaptopMonitor {

    static double load;
    static double memory;
    static double temp;

    public static void connectTODashboard() throws IOException, InterruptedException {
        String url = "http://localhost:8080/api/v1/myPC_token/telemetry";
        URL obj = new URL(url);
        JSONObject object = new JSONObject();

        System.out.println("Stream Start...");

        while(true){

            getCPUTemp();
            getCPUUsage();
            getMemoryUsed();
            makeJson(object);
            postHTTP(obj, object);
            long millis = System.currentTimeMillis();
            Thread.sleep(1000 - millis % 1000);
        }
    }

    public static void getCPUTemp(){
        SystemInfo systemInfo = new SystemInfo();
        temp = systemInfo.getHardware().getSensors().getCpuTemperature();
    }

    public static void getCPUUsage(){
        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(
                OperatingSystemMXBean.class);
        load = osBean.getSystemCpuLoad();
        //            System.out.println("CPU Load : " + String.format("%.2f", load* 100) + " %");
    }

    public static void getMemoryUsed(){
        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(
                OperatingSystemMXBean.class);
        long totalPhysicalMemorySize = osBean.getTotalPhysicalMemorySize() - osBean.getFreePhysicalMemorySize();
        memory = (double) totalPhysicalMemorySize/osBean.getTotalPhysicalMemorySize() *100;
    }

    public static void postHTTP(URL url,JSONObject object) throws IOException {
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type","application/json");
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(object.toString());
        wr.flush();
        wr.close();

//        int responseCode = con.getResponseCode();
//        System.out.println("Response Code : " + responseCode);
//
//        printing result from response
//        System.out.println(response.toString());

        BufferedReader iny = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String output;
        StringBuffer response = new StringBuffer();

        while ((output = iny.readLine()) != null) {
            response.append(output);
        }
        iny.close();
    }

    public static void makeJson(JSONObject object){
        object.put("CPU Usage",String.format("%.2f", load* 100));
        object.put("CPU Temp",String.format("%.2f", temp));
        object.put("Memory Usage",String.format("%.2f", memory));

        StringWriter out = new StringWriter();
        object.write(out);

        System.out.println(object);
    }
}
