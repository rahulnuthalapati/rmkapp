package com.app.mahilakosh;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.squareup.picasso.Picasso;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class LoanDetailsActivity extends AppCompatActivity {

    private TextView loanIdTextView, emailTextView, loanAmountTextView, loanStatusTextView;
    private ImageView aadhaarImageView, panImageView, passbookImageView, passportImageView;
    private Button verifyButton, approveButton, rejectButton, disburseButton;
    private Button recordPaymentButton, viewPaymentHistoryButton, completeLoanButton;

    private String beneficiaryEmail;
    private int loanId;
    private boolean isOrganizer; // Flag to check if the user is an organizer
    private boolean isManager; // Flag to check if the user is a manager

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loan_details);

        // Initialize UI elements
        loanIdTextView = findViewById(R.id.loanIdTextView);
        emailTextView = findViewById(R.id.emailTextView);
        loanAmountTextView = findViewById(R.id.loanAmountTextView);
        loanStatusTextView = findViewById(R.id.loanStatusTextView);

        aadhaarImageView = findViewById(R.id.aadhaarImageView);
        panImageView = findViewById(R.id.panImageView);
        passbookImageView = findViewById(R.id.passbookImageView);
        passportImageView = findViewById(R.id.passportImageView);

        verifyButton = findViewById(R.id.verifyButton);
        approveButton = findViewById(R.id.approveButton);
        rejectButton = findViewById(R.id.rejectButton);
        disburseButton = findViewById(R.id.disburseButton);
        recordPaymentButton = findViewById(R.id.recordPaymentButton);
        viewPaymentHistoryButton = findViewById(R.id.viewPaymentHistoryButton);
        completeLoanButton = findViewById(R.id.completeLoanButton);

        // Get loan details from intent extras
        Intent intent = getIntent();
        beneficiaryEmail = intent.getStringExtra("email");
        loanId = intent.getIntExtra("loanId", -1);

        // Get user role from SharedPreferences
//        SharedPreferences sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
//        String userRole = sharedPreferences.getString("role", "");
        isOrganizer = ApiUtils.currentRole.equals("organizer");
        isManager = ApiUtils.currentRole.equals("manager");

        if (beneficiaryEmail != null && loanId != -1) {
            fetchLoanDetails();
        } else {
            Toast.makeText(this, "Loan details missing", Toast.LENGTH_SHORT).show();
            finish();
        }

        verifyButton.setOnClickListener(v -> modifyLoanStatus("verified"));
        approveButton.setOnClickListener(v -> modifyLoanStatus("approved"));
        rejectButton.setOnClickListener(v -> modifyLoanStatus("rejected"));
        disburseButton.setOnClickListener(v -> modifyLoanStatus("disbursed"));
        recordPaymentButton.setOnClickListener(v -> openRecordPaymentActivity());
        viewPaymentHistoryButton.setOnClickListener(v -> openLoanPaymentActivity());
        completeLoanButton.setOnClickListener(v -> modifyLoanStatus("completed"));
    }

    private void fetchLoanDetails() {
        String apiUrl = ApiUtils.getCurrentUrl() + ApiUtils.API_PATH + "user_loan?client=android&email=" + beneficiaryEmail + "&loanId=" + loanId;

        RequestQueue queue = MyApplication.getRequestQueue();
        StringRequest request = new StringRequest(Request.Method.GET, apiUrl,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getBoolean("loan_requests_exists")) {
                            JSONArray loansArray = jsonObject.getJSONArray("loans");
                            if (loansArray.length() > 0) {
                                JSONObject loanDetails = loansArray.getJSONObject(0);
                                loanIdTextView.setText("Loan ID: " + loanDetails.getInt("loan_id"));
                                emailTextView.setText("Email: " + loanDetails.getString("email"));
                                loanAmountTextView.setText("Loan Amount: ₹" + loanDetails.getDouble("loan_amount"));
                                loanStatusTextView.setText("Loan Status: " + loanDetails.getString("loan_status"));

                                loadImages(loanDetails);
                                updateButtonVisibility(loanDetails.getString("loan_status"));
                            } else {
                                Toast.makeText(this, "No loan details found", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "No loan details found", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("LoanDetailsActivity", "Error parsing JSON: " + e.getMessage());
                        Toast.makeText(LoanDetailsActivity.this, "Error fetching loan details", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("LoanDetailsActivity", "VolleyError: " + error.getMessage());
                    Toast.makeText(LoanDetailsActivity.this, "Error fetching loan details", Toast.LENGTH_SHORT).show();
                });

        queue.add(request);
    }

    private void loadImages(JSONObject loanDetails) throws JSONException {
        String baseUrl = (ApiUtils.getCurrentUrl() + ApiUtils.API_PATH).replace("api/", "");

        Picasso.get().load(baseUrl + loanDetails.getString("aadhar_path")).placeholder(R.drawable.placeholder_image).into(aadhaarImageView);
        Picasso.get().load(baseUrl + loanDetails.getString("pan_path")).placeholder(R.drawable.placeholder_image).into(panImageView);
        Picasso.get().load(baseUrl + loanDetails.getString("passbook_path")).placeholder(R.drawable.placeholder_image).into(passbookImageView);
        Picasso.get().load(baseUrl + loanDetails.getString("passport_path")).placeholder(R.drawable.placeholder_image).into(passportImageView);
    }

    private void updateButtonVisibility(String status) {
        // Initially hide all buttons
        verifyButton.setVisibility(View.GONE);
        approveButton.setVisibility(View.GONE);
        rejectButton.setVisibility(View.GONE);
        disburseButton.setVisibility(View.GONE);
        recordPaymentButton.setVisibility(View.GONE);
        viewPaymentHistoryButton.setVisibility(View.GONE);
        completeLoanButton.setVisibility(View.GONE);


        if (isOrganizer) { // Organizer logic
            if (status.equals("pending") || status.equals("verified")) {
                approveButton.setVisibility(View.VISIBLE);
                rejectButton.setVisibility(View.VISIBLE);
            } else if (status.equals("approved")) {
                disburseButton.setEnabled(true);
                disburseButton.setVisibility(View.VISIBLE);
            } else if (status.equals("disbursed")) {
                recordPaymentButton.setVisibility(View.VISIBLE);
                viewPaymentHistoryButton.setVisibility(View.VISIBLE);
            }
        } else { // Manager (and other roles) logic
            if (status.equals("pending")) {
                verifyButton.setVisibility(View.VISIBLE);
                rejectButton.setVisibility(View.VISIBLE);
            } else if (status.equals("verified")) {
                disburseButton.setVisibility(View.VISIBLE);
                disburseButton.setBackgroundColor(getResources().getColor(R.color.light_gray));
                disburseButton.setEnabled(false);
                disburseButton.setText("Disburse (Pending Approval)");
            } else if (status.equals("rejected")) {
                disburseButton.setEnabled(false);
            } else if (status.equals("approved")) {
                disburseButton.setVisibility(View.VISIBLE);
            } else if (status.equals("disbursed")) {
                recordPaymentButton.setVisibility(View.VISIBLE);
                viewPaymentHistoryButton.setVisibility(View.VISIBLE);
            }
        }


        if (isManager && status.equals("disbursed")) {
            completeLoanButton.setVisibility(View.VISIBLE);
        }
    }

    // Method to modify loan status
    private void modifyLoanStatus(String newStatus) {
        String apiUrl = ApiUtils.getCurrentUrl() + ApiUtils.API_PATH + "modify_loan_status?client=android";

        RequestQueue queue = MyApplication.getRequestQueue();

        StringRequest request = new StringRequest(Request.Method.POST, apiUrl,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getBoolean("success")) {
                            Toast.makeText(LoanDetailsActivity.this, "Loan status updated successfully", Toast.LENGTH_SHORT).show();
                            // Refresh loan details
                            fetchLoanDetails();
                        } else {
                            String errorMessage = jsonResponse.getString("error");
                            Toast.makeText(LoanDetailsActivity.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(LoanDetailsActivity.this, "Error processing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("LoanDetailsActivity", "Error updating loan status: " + error.toString());
                    Toast.makeText(LoanDetailsActivity.this, "Failed to update loan status", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json; charset=UTF-8");
                return params;
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                JSONObject jsonBody = new JSONObject();
                try {
                    jsonBody.put("loan_id", loanId);
                    jsonBody.put("loan_status", newStatus);
                } catch (JSONException e) {
                    e.printStackTrace();
                    // Handle JSON exception
                    return null;
                }
                return jsonBody.toString().getBytes();
            }
        };

        queue.add(request);
    }

    // Method to open RecordPaymentActivity
    private void openRecordPaymentActivity() {
        Intent intent = new Intent(LoanDetailsActivity.this, RecordPaymentActivity.class);
        intent.putExtra("loanId", loanId);
        intent.putExtra("email", beneficiaryEmail);
        startActivity(intent);
    }

    // Method to open LoanPaymentActivity
    private void openLoanPaymentActivity() {
        Intent intent = new Intent(LoanDetailsActivity.this, LoanPaymentActivity.class);
        intent.putExtra("loanId", loanId);
        startActivity(intent);
    }

}