package com.app.mahilakosh;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class IncomeSavingsActivity extends AppCompatActivity {

    private TextInputEditText monthlyIncomeEditText, annualIncomeEditText, sourcesIncomeEditText;
    private TextInputEditText savingsAccNoEditText, currentSavingsEditText, monthlySavingsEditText;
    private Button editButton, saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_income_savings);

        monthlyIncomeEditText = findViewById(R.id.monthlyIncomeEditText);
        annualIncomeEditText = findViewById(R.id.annualIncomeEditText);
        sourcesIncomeEditText = findViewById(R.id.sourcesIncomeEditText);
        savingsAccNoEditText = findViewById(R.id.savingsAccNoEditText);
        currentSavingsEditText = findViewById(R.id.currentSavingsEditText);
        monthlySavingsEditText = findViewById(R.id.monthlySavingsEditText);
        editButton = findViewById(R.id.editButton);
        saveButton = findViewById(R.id.saveButton);

        if(ApiUtils.currentRole.equals("beneficiary")){
            editButton.setOnClickListener(v -> {
                enableEditFields(true);

                // Show the Save button, hide the Edit button
                editButton.setVisibility(View.GONE);
                saveButton.setVisibility(View.VISIBLE);
            });

            saveButton.setOnClickListener(v -> saveIncomeSavingsData());
        }
        else
        {
            editButton.setVisibility(View.GONE);
            saveButton.setVisibility(View.GONE);
        }

        String responseData = getIntent().getStringExtra("response_data");

        if (responseData != null) {
            fetchIncomeSavingsData(responseData);
        } else {
            fetchIncomeSavingsData("");
        }
    }

    private void fetchIncomeSavingsData(String responseData) {
        if ( !responseData.isEmpty()) {
            try {
                processFetchIncomeSavingsData(new JSONObject(responseData));
            }
            catch (JSONException e) {
                Log.e("IncomeSavingsActivity", "Error parsing JSON: " + e.getMessage());
                Toast.makeText(IncomeSavingsActivity.this, "Error fetching income and savings data.", Toast.LENGTH_SHORT).show();
            }
        } else {
            String apiUrl = ApiUtils.getCurrentUrl() + ApiUtils.API_PATH + "income_savings" + "?client=android";

            RequestQueue queue = MyApplication.getRequestQueue();
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, apiUrl, null,
                    response -> {
                        try {
                            processFetchIncomeSavingsData(response);
                        } catch (JSONException e) {
                            Log.e("IncomeSavingsActivity", "Error parsing JSON: " + e.getMessage());
                            Toast.makeText(IncomeSavingsActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        Log.e("IncomeSavingsActivity", "VolleyError: " + error.getMessage());
                        Toast.makeText(IncomeSavingsActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
                    });

            queue.add(request);
        }
    }

    private void processFetchIncomeSavingsData(JSONObject response) throws JSONException {
        if (response.getBoolean("success")) { // Check for success flag
            // Populate EditTexts with data
            monthlyIncomeEditText.setText(String.valueOf(response.optInt("monthly_income", 0)));
            annualIncomeEditText.setText(String.valueOf(response.optInt("annual_income", 0)));
            sourcesIncomeEditText.setText(response.optString("sources", "N/A"));
            savingsAccNoEditText.setText(response.optString("savings_acc_details", "N/A"));
            currentSavingsEditText.setText(String.valueOf(response.optInt("savings_balance", 0)));
            monthlySavingsEditText.setText(String.valueOf(response.optInt("monthly_savings_contributions", 0)));
        } else {
            // Handle error, e.g., display error message
            Toast.makeText(IncomeSavingsActivity.this, response.optString("error", "Failed to fetch data."), Toast.LENGTH_SHORT).show();
        }
    }


    private void saveIncomeSavingsData() {
        String apiUrl = ApiUtils.getCurrentUrl() + ApiUtils.API_PATH + "income_savings" + "?client=android";

        Map<String, Object> data = new HashMap<>();
        data.put("monthly_income", monthlyIncomeEditText.getText().toString());
        data.put("annual_income", annualIncomeEditText.getText().toString());
        data.put("sources", sourcesIncomeEditText.getText().toString());
        data.put("savings_acc_details", savingsAccNoEditText.getText().toString());
        data.put("savings_balance", currentSavingsEditText.getText().toString());
        data.put("monthly_savings_contributions", monthlySavingsEditText.getText().toString());

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, apiUrl, new JSONObject(data),
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            // Data saved successfully
                            Toast.makeText(IncomeSavingsActivity.this, "Data saved successfully.", Toast.LENGTH_SHORT).show();

                            // Disable editing for all input fields
                            enableEditFields(false);

                            // Hide the Save button, show the Edit button
                            editButton.setVisibility(View.VISIBLE);
                            saveButton.setVisibility(View.GONE);
                        } else {
                            // Handle error from the server
                            String errorMessage = response.optString("error", "Failed to save data.");
                            Toast.makeText(IncomeSavingsActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(IncomeSavingsActivity.this, "Error processing response.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("IncomeSavingsActivity", "VolleyError: " + error.getMessage());
                    Toast.makeText(IncomeSavingsActivity.this, "Failed to save data.", Toast.LENGTH_SHORT).show();
                });

        RequestQueue queue = MyApplication.getRequestQueue();
        queue.add(request);
    }

    private void enableEditFields(boolean enabled) {
        monthlyIncomeEditText.setEnabled(enabled);
        annualIncomeEditText.setEnabled(enabled);
        sourcesIncomeEditText.setEnabled(enabled);
        savingsAccNoEditText.setEnabled(enabled);
        currentSavingsEditText.setEnabled(enabled);
        monthlySavingsEditText.setEnabled(enabled);

    }

}