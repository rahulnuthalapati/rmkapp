package com.app.mahilakosh;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.textfield.TextInputEditText;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private TextInputEditText nameEditText, addressEditText;
    private Spinner bankSpinner; // Bank selection Spinner
    private Button editButton, updateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        nameEditText = findViewById(R.id.nameEditText);
        addressEditText = findViewById(R.id.addressEditText);
        bankSpinner = findViewById(R.id.bankSpinner); // Initialize the Spinner
        editButton = findViewById(R.id.editButton);
        updateButton = findViewById(R.id.updateButton);

        enableEditFields(false);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.bank_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bankSpinner.setAdapter(adapter);

        editButton.setOnClickListener(v -> {
            enableEditFields(true);
            editButton.setVisibility(View.GONE);
            updateButton.setVisibility(View.VISIBLE);
        });

        updateButton.setOnClickListener(v -> saveProfileData());

        fetchProfileData();
    }

    private void enableEditFields(boolean enabled) {
        nameEditText.setEnabled(enabled);
        addressEditText.setEnabled(enabled);
        bankSpinner.setEnabled(enabled);
    }

    private void fetchProfileData() {
        String apiUrl = ApiUtils.getCurrentUrl() + ApiUtils.API_PATH + "profile" + "?client=android";

        RequestQueue queue = MyApplication.getRequestQueue();
        StringRequest request = new StringRequest(Request.Method.GET, apiUrl,
                response -> {
                    try {
                        JSONObject profileData = new JSONObject(response);
                        nameEditText.setText(profileData.getString("name"));

                        // Set the bank selection in the Spinner
                        String currentBank = profileData.getString("bank");
                        int spinnerPosition = ((ArrayAdapter<String>) bankSpinner.getAdapter()).getPosition(currentBank);
                        bankSpinner.setSelection(spinnerPosition);

                        addressEditText.setText(profileData.getString("address"));

                    } catch (JSONException e) {
                        Log.e("EditProfileActivity", "JSON Parsing Error: " + e.getMessage());
                        Toast.makeText(this, "Failed to fetch profile data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("EditProfileActivity", "VolleyError: " + error.getMessage());
                    Toast.makeText(this, "Failed to fetch profile data", Toast.LENGTH_SHORT).show();
                });
        queue.add(request);
    }

    private void saveProfileData() {
        String apiUrl = ApiUtils.getCurrentUrl() + ApiUtils.API_PATH + "profile" + "?client=android";

        // Get the updated profile data
        String name = nameEditText.getText().toString();
        String bank = bankSpinner.getSelectedItem().toString();
        String address = addressEditText.getText().toString();

        // Create a RequestQueue
        RequestQueue queue = MyApplication.getRequestQueue();

        // Create a StringRequest to send data to the server
        StringRequest request = new StringRequest(Request.Method.POST, apiUrl,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getBoolean("success")) {
                            // Profile updated successfully
                            Toast.makeText(EditProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();

                            // Disable editing of input fields
                            enableEditFields(false);

                            // Show Edit button and hide Update button
                            editButton.setVisibility(View.VISIBLE);
                            updateButton.setVisibility(View.GONE);

                        } else {
                            // Handle error response from server
                            String errorMessage = jsonResponse.getString("error");
                            Toast.makeText(this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error processing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    // Handle Volley error
                    Log.e("EditProfileActivity", "Error updating profile: " + error.toString());
                    Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("name", name);
                params.put("bank", bank);
                params.put("address", address);
                return params;
            }
        };

        // Add the request to the RequestQueue
        queue.add(request);
    }
}