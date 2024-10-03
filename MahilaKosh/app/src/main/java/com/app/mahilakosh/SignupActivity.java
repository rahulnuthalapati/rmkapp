package com.app.mahilakosh;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private EditText nameEditText;
    private EditText emailEditText;
    private EditText phoneEditText;
    private EditText passwordEditText;
    private Spinner bankSpinner; // Bank selection Spinner
    private Button signupButton;
    private String role;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        bankSpinner = findViewById(R.id.bankSpinner);
        signupButton = findViewById(R.id.signupButton);

        // Get the user role from the Intent
        Intent intent = getIntent();
        if (intent != null) {
            role = intent.getStringExtra("role");

            // Make bankSpinner visible only if the role is "organizer"
            if (role.equals("organizer")) {
                bankSpinner.setVisibility(View.VISIBLE);

                // Set up the ArrayAdapter for the bankSpinner
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                        this, R.array.bank_options, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                bankSpinner.setAdapter(adapter);
            }
        }

        signupButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String phone = phoneEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            // Get the selected bank (only if the role is "organizer")
            String bank = role.equals("organizer")
                    ? bankSpinner.getSelectedItem().toString()
                    : "";

            // Basic input validation (for all roles)
            if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
                Toast.makeText(SignupActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Additional validation for organizer role
            if (role.equals("organizer") && bank.equals("Select a bank")) {
                Toast.makeText(SignupActivity.this, "Please select a bank", Toast.LENGTH_SHORT).show();
                return;
            }

            // Call the signup method to send the request to the server
            signup(name, email, phone, password, bank);
        });
    }

    private void signup(String name, String email, String phone, String password, String bank) {
        String apiUrl = ApiUtils.getCurrentUrl() + ApiUtils.API_PATH + "signup?client=android"; // Corrected API endpoint

        RequestQueue queue = MyApplication.getRequestQueue();

        StringRequest request = new StringRequest(Request.Method.POST, apiUrl,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getBoolean("success")) {
                            // Signup successful, show a message and redirect to LoginActivity
                            Toast.makeText(SignupActivity.this, "Signup successful! Please log in.", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                            intent.putExtra("role", role);
                            startActivity(intent);
                            finish();
                        } else {
                            // Signup failed, display the error message from the server
                            String errorMessage = jsonResponse.getString("message");
                            Toast.makeText(SignupActivity.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("SignupActivity", "Error parsing JSON: " + e.getMessage());
                        Toast.makeText(SignupActivity.this, "Error during signup. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("SignupActivity", "Error: " + error.toString());
                    Toast.makeText(SignupActivity.this, "Signup failed. Please try again.", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("name", name);
                params.put("email", email);
                params.put("phone", phone);
                params.put("password", password);
                if (role.equals("organizer")) {
                    params.put("bank", bank);
                }
                params.put("role", role);
                return params;
            }
        };
        queue.add(request);
    }
}