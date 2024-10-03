package com.app.mahilakosh;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import org.json.JSONException;

public class BeneficiaryLinkActivity extends AppCompatActivity {

    private TextView beneficiaryNameTextView;
    private Button loanInfoButton;
    private Button economicActivityButton;
    private Button incomeSavingsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beneficiary_link);

        beneficiaryNameTextView = findViewById(R.id.beneficiaryNameTextView);
        loanInfoButton = findViewById(R.id.loanInfoButton);
        economicActivityButton = findViewById(R.id.economicActivityButton);
        incomeSavingsButton = findViewById(R.id.incomeSavingsButton);

        String beneficiaryEmail = getIntent().getStringExtra("beneficiary_email");

        if (beneficiaryEmail != null) {
            fetchBeneficiaryName(beneficiaryEmail);

            loanInfoButton.setOnClickListener(v -> {
                fetchBeneficiaryData(beneficiaryEmail, "loan_information", LoanInformationActivity.class);
            });

            economicActivityButton.setOnClickListener(v -> {
                fetchBeneficiaryData(beneficiaryEmail, "economic_activity", EconomicActivitiesActivity.class);
            });

            incomeSavingsButton.setOnClickListener(v -> {
                fetchBeneficiaryData(beneficiaryEmail, "income_savings", IncomeSavingsActivity.class);
            });
        } else {
            Toast.makeText(this, "Beneficiary email is missing", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchBeneficiaryName(String email) {
        String apiUrl = ApiUtils.getCurrentUrl() + ApiUtils.API_PATH + "profileinfo?email=" + email + "&client=android";

        RequestQueue queue = MyApplication.getRequestQueue();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, apiUrl, null,
                response -> {
                    try {
                        String beneficiaryName = response.getString("name");
                        beneficiaryNameTextView.setText(beneficiaryName);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(BeneficiaryLinkActivity.this, "Error fetching beneficiary name", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("BeneficiaryLinkActivity", "VolleyError: " + error.getMessage());
                    Toast.makeText(BeneficiaryLinkActivity.this, "Error fetching beneficiary name", Toast.LENGTH_SHORT).show();
                });
        queue.add(request);
    }

    private void fetchBeneficiaryData(String beneficiaryEmail, String apiEndpoint, Class<?> activityClass) {
        // Construct the API URL with the beneficiary's email as a query parameter
        String apiUrl = ApiUtils.getCurrentUrl() + ApiUtils.API_PATH + apiEndpoint + "?client=android&email=" + beneficiaryEmail;

        // Create a new RequestQueue
        RequestQueue queue = MyApplication.getRequestQueue();

        // Create a new JsonObjectRequest
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, apiUrl, null,
                response -> {
                    // Data fetched successfully, start the corresponding activity
                    Intent intent = new Intent(BeneficiaryLinkActivity.this, activityClass);
                    intent.putExtra("response_data", response.toString());
                    startActivity(intent);
                },
                error -> {
                    // Handle Volley error
                    Log.e("BeneficiaryLink", "Error fetching data: " + error.toString());
                    Toast.makeText(BeneficiaryLinkActivity.this, "Error fetching data", Toast.LENGTH_SHORT).show();
                });

        // Add the request to the RequestQueue
        queue.add(request);
    }
}