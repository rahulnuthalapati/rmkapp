package com.app.mahilakosh;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.URI;
import java.util.List;
import java.util.Map;

public class PersistentCookieManager extends CookieManager {

    private static final String TAG = PersistentCookieManager.class.getSimpleName();
    private static final String PREF_NAME = "CookiePrefsFile";
    private static final String COOKIE_PREF_KEY = "cookies";

    private SharedPreferences sharedPreferences;

    public PersistentCookieManager(Context context) {
        super();
        setCookiePolicy(CookiePolicy.ACCEPT_ALL);

        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        loadCookiesFromPreferences();
    }

    @Override
    public void put(URI uri, Map<String, List<String>> responseHeaders) throws IOException {
        super.put(uri, responseHeaders);
        saveCookiesToPreferences();
    }

    private void saveCookiesToPreferences() {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // The cookie store is not directly accessible, so we get all cookies
        // and store them as a concatenated string in SharedPreferences.
        String cookieString = "";
        for (HttpCookie cookie : getCookieStore().getCookies()) {
            cookieString += cookie.toString() + ";"; // Semicolon separates cookies
        }
        editor.putString(COOKIE_PREF_KEY, cookieString);
        editor.apply();

        Log.d(TAG, "Cookies saved to SharedPreferences: " + cookieString);
    }

    private void loadCookiesFromPreferences() {
        String cookiesString = sharedPreferences.getString(COOKIE_PREF_KEY, "");

        if (!cookiesString.isEmpty()) {
            String[] cookiesArray = cookiesString.split(";");

            for (String cookieStr : cookiesArray) {
                try {
                    // Create HttpCookie objects from the stored string
                    HttpCookie cookie = HttpCookie.parse(cookieStr).get(0);
                    URI cookieUri = new URI(cookie.getDomain()); // Example: using the cookie domain
                    getCookieStore().add(cookieUri, cookie); // Add cookies back to the CookieStore
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing cookie: " + e.getMessage());
                }
            }

            Log.d(TAG, "Cookies loaded from SharedPreferences: " + cookiesString);
        }
    }
}