package com.app.mahilakosh;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

public class BeneficiaryActivity extends AppCompatActivity {

    private ImageView profileImageView;
    private TextView userNameTextView;
    private ImageButton notificationButton;
    private Button logoutButton;
    private Button applyForLoanButton;
    private Button loanInfoButton;
    private Button economicActivityButton;
    private Button incomeSavingsButton;
    private Button feedbackButton;
    private Button chatButton;
    private Button manageProfileButton;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beneficiary);

        profileImageView = findViewById(R.id.profileImageView);
        userNameTextView = findViewById(R.id.userNameTextView);
//        notificationButton = findViewById(R.id.notificationButton);
        logoutButton = findViewById(R.id.logoutButton);
        applyForLoanButton = findViewById(R.id.applyForLoanButton);
        loanInfoButton = findViewById(R.id.loanInfoButton);
        economicActivityButton = findViewById(R.id.economicActivityButton);
        incomeSavingsButton = findViewById(R.id.incomeSavingsButton);
        feedbackButton = findViewById(R.id.feedbackButton);
        chatButton = findViewById(R.id.chatButton);
        manageProfileButton = findViewById(R.id.manageProfileButton);

        String userName = null;
        String profileImageUrl;
        try {
            userName = (String) ((JSONObject) ApiUtils.user).get("name");
            profileImageUrl = (String) ((JSONObject) ApiUtils.user).get("profilePic");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        userNameTextView.setText(userName);
        Picasso.get().load(profileImageUrl)
                .placeholder(R.drawable.default_profile)
                .error(R.drawable.default_profile)
                .into(profileImageView);

        logoutButton.setOnClickListener(v -> {
            Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(BeneficiaryActivity.this, MainActivity.class);
            startActivity(intent);
        });

//        notificationButton.setOnClickListener(v -> {
//            // Handle notification button click (start NotificationsActivity)
//            Toast.makeText(this, "Notification button clicked", Toast.LENGTH_SHORT).show();
//            Intent intent = new Intent(BeneficiaryActivity.this, NotificationsActivity.class); // Replace with Notifications Activity
//            startActivity(intent);
//        });

        applyForLoanButton.setOnClickListener(v -> {
            Intent intent = new Intent(BeneficiaryActivity.this, LoanApplicationActivity.class);
            startActivity(intent);
        });

        loanInfoButton.setOnClickListener(v -> {
            Intent intent = new Intent(BeneficiaryActivity.this, LoanInformationActivity.class);
            startActivity(intent);
        });

        economicActivityButton.setOnClickListener(v -> {
            Intent intent = new Intent(BeneficiaryActivity.this, EconomicActivitiesActivity.class);
            startActivity(intent);
        });

        incomeSavingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(BeneficiaryActivity.this, IncomeSavingsActivity.class);
            startActivity(intent);
        });

        feedbackButton.setOnClickListener(v -> {
            Intent intent = new Intent(BeneficiaryActivity.this, FeedbackActivity.class);
            startActivity(intent);
        });

        chatButton.setOnClickListener(v -> {
            Intent intent = new Intent(BeneficiaryActivity.this, ChatActivity.class);
            startActivity(intent);
        });

        manageProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(BeneficiaryActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
    }
}