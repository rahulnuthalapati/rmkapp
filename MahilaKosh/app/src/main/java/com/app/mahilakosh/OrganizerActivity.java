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
import org.json.JSONObject;

public class OrganizerActivity extends AppCompatActivity {

    private Button loanRequestsButton;
    private Button beneficiaryLoanManagementButton;
    private Button chatButton;
    private Button systemSettingsButton;
    private ImageButton notificationButton;
    private Button feedbackButton;
    private Button logoutButton;
    private TextView greetingTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer);

        loanRequestsButton = findViewById(R.id.loanRequestsButton);
        beneficiaryLoanManagementButton = findViewById(R.id.beneficiaryManagementButton);
        chatButton = findViewById(R.id.chatButton);
        systemSettingsButton = findViewById(R.id.systemSettingsButton);
//        notificationButton = findViewById(R.id.notificationButton);
        feedbackButton = findViewById(R.id.feedbackButton);

        logoutButton = findViewById(R.id.logoutButton);
        greetingTextView = findViewById(R.id.greeting);

        fetchUserName();

        loanRequestsButton.setOnClickListener(view -> {
            Intent intent = new Intent(OrganizerActivity.this, LoanManagementActivity.class);
            startActivity(intent);
        });

        beneficiaryLoanManagementButton.setOnClickListener(view -> {
            Intent intent = new Intent(OrganizerActivity.this, BeneficiaryProfilesActivity.class);
            startActivity(intent);
        });

        chatButton.setOnClickListener(view -> {
            Intent intent = new Intent(OrganizerActivity.this, ChatActivity.class);
            startActivity(intent);
        });

        systemSettingsButton.setOnClickListener(view -> {
            Intent intent = new Intent(OrganizerActivity.this, SystemSettingsActivity.class);
            startActivity(intent);
        });

        feedbackButton.setOnClickListener(v -> {
            Intent intent = new Intent(OrganizerActivity.this, FeedbackActivity.class);
            startActivity(intent);
        });

//        notificationButton.setOnClickListener(v -> {
//            Toast.makeText(OrganizerActivity.this, "Notification button clicked", Toast.LENGTH_SHORT).show();
//            // Add your notification logic here
//        });

        logoutButton.setOnClickListener(v -> {
            Toast.makeText(OrganizerActivity.this, "Logging out...", Toast.LENGTH_SHORT).show();
            // Add your logout logic here
            Intent intent = new Intent(OrganizerActivity.this, MainActivity.class);
            startActivity(intent);
        });
    }

    private void fetchUserName() {
        String apiUrl = ApiUtils.getCurrentUrl() + ApiUtils.API_PATH + "profileinfo" + "?client=android";

        RequestQueue queue = MyApplication.getRequestQueue();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, apiUrl, null,
                response -> {
                    try {
                        String userName = response.getString("name");
                        greetingTextView.setText("Hello " + userName + "!");
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(OrganizerActivity.this, "Error fetching user name", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("OrganizerActivity", "Error fetching user name: " + error.getMessage());
                    Toast.makeText(OrganizerActivity.this, "Error fetching user name", Toast.LENGTH_SHORT).show();
                });
        queue.add(request);
    }
}