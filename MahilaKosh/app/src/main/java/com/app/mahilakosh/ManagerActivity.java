package com.app.mahilakosh;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;

public class ManagerActivity extends AppCompatActivity {

    private TextView greetingTextView;
    private Button beneficiaryProfilesButton;
    private Button loanManagementButton;
    private Button systemSettingsButton;
    private Button feedbackButton;
    private ImageButton notificationButton;
    private Button logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager); // Make sure this matches your layout file name

        greetingTextView = findViewById(R.id.greeting);
        beneficiaryProfilesButton = findViewById(R.id.beneficiaryProfilesButton);
        loanManagementButton = findViewById(R.id.loanManagementButton);
        feedbackButton = findViewById(R.id.feedbackButton);
        systemSettingsButton = findViewById(R.id.systemSettingsButton);
//        notificationButton = findViewById(R.id.notificationButton);
        logoutButton = findViewById(R.id.logoutButton);

        // Fetch and display the user's name
        fetchUserName();

        // Button click listeners
        beneficiaryProfilesButton.setOnClickListener(v -> {
            // Start the BeneficiaryProfilesActivity
            startActivity(new Intent(ManagerActivity.this, BeneficiaryProfilesActivity.class));
        });

        loanManagementButton.setOnClickListener(v -> {
            // Start the LoanManagementActivityb
            startActivity(new Intent(ManagerActivity.this, LoanManagementActivity.class));
        });

        feedbackButton.setOnClickListener(v -> {
            Intent intent = new Intent(ManagerActivity.this, FeedbackActivity.class);
            startActivity(intent);
        });

        systemSettingsButton.setOnClickListener(v -> {
            // Start the SystemSettingsActivity
            startActivity(new Intent(ManagerActivity.this, SystemSettingsActivity.class));
        });

//        notificationButton.setOnClickListener(v -> {
//            // Handle notification button click
//            Toast.makeText(ManagerActivity.this, "Notification button clicked", Toast.LENGTH_SHORT).show();
//            // Add your notification logic here
//        });

        logoutButton.setOnClickListener(v -> {
            // Handle logout
            Toast.makeText(ManagerActivity.this, "Logging out...", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ManagerActivity.this, MainActivity.class);
            startActivity(intent);
        });

    }

    // Fetch and display the user's name
    private void fetchUserName() {
        // Replace with your actual API call to get user data
        String apiUrl = ApiUtils.getCurrentUrl() + ApiUtils.API_PATH + "profileinfo" + "?client=android";

        RequestQueue queue = MyApplication.getRequestQueue();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, apiUrl, null,
                response -> {
                    try {
                        String userName = response.getString("name");
                        greetingTextView.setText("Hello, " + userName + "!");
                    } catch (JSONException e) {
                        // Handle the JSON parsing error
                        e.printStackTrace();
                        Toast.makeText(ManagerActivity.this, "Error fetching user name", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    // Handle Volley error
                    Log.e("ManagerActivity", "Error fetching user name: " + error.getMessage());
                    Toast.makeText(ManagerActivity.this, "Error fetching user name", Toast.LENGTH_SHORT).show();
                });

        queue.add(request);
    }
}