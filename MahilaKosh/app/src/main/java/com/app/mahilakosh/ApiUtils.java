package com.app.mahilakosh;

import android.util.Log;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class ApiUtils {

    private static final String NGROK_URL = "remarkably-settled-tapir.ngrok-free.app"; // Replace with your ngrok URL
    private static final String LOCALHOST_URL = "10.0.2.2:5000"; // Use 10.0.2.2 for the emulator
    public static final String API_PATH = "/api/";
    public static final String HTTP = "http://";
    public static final String HTTPS = "https://";
    public static Object user = null;
    public static String currentRole = "";


    public static String getCurrentUrl() {
        try {
            InetAddress address = InetAddress.getByName(LOCALHOST_URL.split(":")[0]); // Get host only
            if(address.isReachable(1000))
            {
                return HTTP + LOCALHOST_URL;
            }
            else
            {
                return HTTPS + NGROK_URL;
            }
        } catch (UnknownHostException e) {
//            Log.e("ApiUtils", "Localhost not found: " + e.getMessage());
            return HTTP + LOCALHOST_URL;
        } catch (Exception e) {
//            Log.e("ApiUtils", "Error checking localhost reachability: " + e.getMessage());
            return HTTP + LOCALHOST_URL;
        }
    }
}