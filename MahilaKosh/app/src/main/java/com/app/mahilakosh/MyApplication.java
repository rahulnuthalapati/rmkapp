package com.app.mahilakosh;

import android.app.Application;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.Volley;
import java.net.CookieHandler;

public class MyApplication extends Application {

    private static RequestQueue requestQueue;

    @Override
    public void onCreate() {
        super.onCreate();

        // Create and set the custom cookie manager
        PersistentCookieManager cookieManager = new PersistentCookieManager(this);
        CookieHandler.setDefault(cookieManager);

        // Initialize Volley RequestQueue
        requestQueue = Volley.newRequestQueue(getApplicationContext(), new HurlStack());
    }

    public static RequestQueue getRequestQueue() {
        return requestQueue;
    }
}