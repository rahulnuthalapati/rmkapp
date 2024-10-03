package com.app.mahilakosh;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class EconomicActivitiesActivity extends AppCompatActivity {

    private TextInputEditText businessNameEditText, businessAddressEditText, businessTypeEditText;
    private TextInputEditText monthlyRevenueEditText, annualRevenueEditText, monthlyExpensesEditText, annualExpensesEditText, profitMarginEditText;
    private Button editButton, saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_economic_activities);

        // Initialize views
        businessNameEditText = findViewById(R.id.businessNameEditText);
        businessAddressEditText = findViewById(R.id.businessAddressEditText);
        businessTypeEditText = findViewById(R.id.businessTypeEditText);
        monthlyRevenueEditText = findViewById(R.id.monthlyRevenueEditText);
        annualRevenueEditText = findViewById(R.id.annualRevenueEditText);
        monthlyExpensesEditText = findViewById(R.id.monthlyExpensesEditText);
        annualExpensesEditText = findViewById(R.id.annualExpensesEditText);
        profitMarginEditText = findViewById(R.id.profitMarginEditText);

        editButton = findViewById(R.id.editButton);
        saveButton = findViewById(R.id.saveButton);

        if(ApiUtils.currentRole.equals("beneficiary")){
            editButton.setOnClickListener(v -> {
                // Enable editing for all input fields
                enableEditFields(true);

                // Show the Save button, hide the Edit button
                editButton.setVisibility(View.GONE);
                saveButton.setVisibility(View.VISIBLE);
            });

            // Set onClickListener for Save button
            saveButton.setOnClickListener(v -> saveEconomicActivityData());

        }
        else
        {
            editButton.setVisibility(View.GONE);
            saveButton.setVisibility(View.GONE);

        }

        String responseData = getIntent().getStringExtra("response_data");

        if (responseData != null) {
            fetchEconomicActivityData(responseData);
        } else {
            fetchEconomicActivityData("");
        }
    }


    private void fetchEconomicActivityData(String responseData) {
        if (!responseData.isEmpty()) {
            try {
                processEconomicActivityData(new JSONObject(responseData));
            } catch (JSONException e) {
                Log.e("EconomicActivity", "Error parsing JSON: " + e.getMessage());
                Toast.makeText(EconomicActivitiesActivity.this, "Failed to load economic activity data", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            String apiUrl = ApiUtils.getCurrentUrl() + ApiUtils.API_PATH + "economic_activity" + "?client=android";

            RequestQueue queue = MyApplication.getRequestQueue();
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, apiUrl, null,
                    response -> {
                        try {
                            processEconomicActivityData(response);
                        } catch (JSONException e) {
                            Log.e("EconomicActivity", "Error parsing JSON: " + e.getMessage());
                            Toast.makeText(EconomicActivitiesActivity.this, "Failed to load economic activity data", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        Log.e("EconomicActivity", "VolleyError: " + error.getMessage());
                        Toast.makeText(EconomicActivitiesActivity.this, "Failed to load economic activity data", Toast.LENGTH_SHORT).show();
                    });

            queue.add(request);
        }
    }

    private void processEconomicActivityData(JSONObject response) throws JSONException {
        if (response.has("message")) {
            Toast.makeText(EconomicActivitiesActivity.this, response.getString("message"), Toast.LENGTH_LONG).show();
        } else {
            businessTypeEditText.setText(response.optString("type", "N/A"));
            businessNameEditText.setText(response.optString("name", "N/A"));
            businessAddressEditText.setText(response.optString("address", "N/A"));
            monthlyRevenueEditText.setText(String.valueOf(response.optInt("monthly_revenue", 0)));
            annualRevenueEditText.setText(String.valueOf(response.optInt("annual_revenue", 0)));
            monthlyExpensesEditText.setText(String.valueOf(response.optInt("monthly_expense", 0)));
            annualExpensesEditText.setText(String.valueOf(response.optInt("annual_expense", 0)));
            profitMarginEditText.setText(String.valueOf(response.optInt("profit_margin", 0)));
        }
    }


    private void saveEconomicActivityData() {

        String apiUrl = ApiUtils.getCurrentUrl() + ApiUtils.API_PATH + "economic_activity" + "?client=android";

        // Create JSON object with the data
        Map<String, Object> businessData = new HashMap<>();
        businessData.put("type", businessTypeEditText.getText().toString());
        businessData.put("name", businessNameEditText.getText().toString());
        businessData.put("address", businessAddressEditText.getText().toString());
        businessData.put("monthly_revenue", Integer.parseInt(monthlyRevenueEditText.getText().toString()));
        businessData.put("annual_revenue", Integer.parseInt(annualRevenueEditText.getText().toString()));
        businessData.put("monthly_expense", Integer.parseInt(monthlyExpensesEditText.getText().toString()));
        businessData.put("annual_expense", Integer.parseInt(annualExpensesEditText.getText().toString()));
        businessData.put("profit_margin", Integer.parseInt(profitMarginEditText.getText().toString()));

        // Use JsonObjectRequest for sending JSON data
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, apiUrl, new JSONObject(businessData),
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            // Data saved successfully
                            Toast.makeText(EconomicActivitiesActivity.this, "Economic activities updated successfully.", Toast.LENGTH_SHORT).show();

                            // Disable editing for all input fields
                            enableEditFields(false);

                            // Hide the Save button, show the Edit button
                            editButton.setVisibility(View.VISIBLE);
                            saveButton.setVisibility(View.GONE);
                        } else {
                            // Handle error from the server
                            String errorMessage = response.optString("error", "Failed to save data.");
                            Toast.makeText(EconomicActivitiesActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(EconomicActivitiesActivity.this, "Error processing response.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("EconomicActivity", "VolleyError: " + error.getMessage());
                    Toast.makeText(EconomicActivitiesActivity.this, "Failed to update economic activities.", Toast.LENGTH_SHORT).show();
                });

        RequestQueue queue = MyApplication.getRequestQueue();
        queue.add(request);
    }

    private void enableEditFields(boolean enabled) {
        businessNameEditText.setEnabled(enabled);
        businessAddressEditText.setEnabled(enabled);
        businessTypeEditText.setEnabled(enabled);
        monthlyRevenueEditText.setEnabled(enabled);
        annualRevenueEditText.setEnabled(enabled);
        monthlyExpensesEditText.setEnabled(enabled);
        annualExpensesEditText.setEnabled(enabled);
        profitMarginEditText.setEnabled(enabled);
    }

}