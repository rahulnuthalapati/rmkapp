package com.app.mahilakosh;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.textfield.TextInputEditText;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class RecordPaymentActivity extends AppCompatActivity {

    private TextInputEditText loanIdEditText, emailEditText, amountEditText, paymentDateEditText;
    private Button recordPaymentButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_payment);

        loanIdEditText = findViewById(R.id.loanIdEditText);
        emailEditText = findViewById(R.id.emailEditText);
        amountEditText = findViewById(R.id.amountEditText);
        paymentDateEditText = findViewById(R.id.paymentDateEditText);
        recordPaymentButton = findViewById(R.id.recordPaymentButton);

        // Get loan ID and email from Intent
        int loanId = getIntent().getIntExtra("loanId", -1);
        String email = getIntent().getStringExtra("email");

        // Populate the read-only fields
        if (loanId != -1 && email != null) {
            loanIdEditText.setText(String.valueOf(loanId));
            emailEditText.setText(email);
        } else {
            Toast.makeText(this, "Loan ID or email is missing", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity if required data is missing
        }

        // Set the current date in the Payment Date field
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1; // Month is 0-indexed
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        String currentDate = String.format("%04d-%02d-%02d", year, month, day);
        paymentDateEditText.setText(currentDate);

        recordPaymentButton.setOnClickListener(v -> recordPayment(loanId, email));
    }

    private void recordPayment(int loanId, String email) {
        String apiUrl = ApiUtils.getCurrentUrl() + ApiUtils.API_PATH + "record_payment?client=android";

        String amount = amountEditText.getText().toString();
        String paymentDate = paymentDateEditText.getText().toString();

        // Basic input validation
        if (amount.isEmpty()) {
            Toast.makeText(this, "Please enter the amount paid", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestQueue queue = MyApplication.getRequestQueue();

        StringRequest request = new StringRequest(Request.Method.POST, apiUrl,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.has("message") && jsonResponse.getString("message").equals("Payment recorded successfully")) {
                            Toast.makeText(RecordPaymentActivity.this, "Payment recorded successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(RecordPaymentActivity.this, "Error recording payment", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("RecordPayment", "JSONException: " + e.getMessage());
                        Toast.makeText(RecordPaymentActivity.this, "Error processing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("RecordPayment", "VolleyError: " + error.toString());
                    Toast.makeText(RecordPaymentActivity.this, "Error recording payment", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("loan_id", String.valueOf(loanId));
                params.put("email", email);
                params.put("amount", amount);
                params.put("payment_date", paymentDate);
                return params;
            }
        };
        queue.add(request);
    }
}