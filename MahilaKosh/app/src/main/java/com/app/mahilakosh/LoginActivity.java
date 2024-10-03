package com.app.mahilakosh;

import androidx.appcompat.app.AppCompatActivity;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private Button signupButton;
    private String role;
    private ImageView loginIcon; // Added for the icon
    private TextView loginTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        signupButton = findViewById(R.id.signupButton);
        loginTitle = findViewById(R.id.loginTitle);

        // Get the role from the intent
        Intent intent = getIntent();
        if (intent != null) {
            role = intent.getStringExtra("role");

            String titleText = "Beneficiary Login";
            switch (role) {
                case "manager":
                    titleText = "Manager Login";
                    break;
                case "organizer":
                    titleText = "Organizer Login";
                    break;
            }

            loginTitle.setText(titleText);

        }

        signupButton.setOnClickListener(v -> {
            Intent signupIntent = new Intent(LoginActivity.this, SignupActivity.class);
            signupIntent.putExtra("role", role); // Pass the role to SignupActivity
            startActivity(signupIntent);
        });


        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            login(email, password);
        });
    }


    private void login(String email, String password) {
        String apiUrl = ApiUtils.getCurrentUrl() + ApiUtils.API_PATH + "login" + "?client=android";

        RequestQueue queue = MyApplication.getRequestQueue();
        StringRequest request = new StringRequest(Request.Method.POST, apiUrl,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response); // Parse JSON response
                        if (jsonResponse.getBoolean("success")) {
                            // Login successful
                            String role = jsonResponse.getString("role");
                            ApiUtils.user = jsonResponse.get("user");
                            handleLoginSuccess(role); // Call handleLoginSuccess
                        } else {
                            // Login failed, display error message from JSON
                            String message = jsonResponse.getString("message");
                            Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("LoginActivity", "JSON Parsing Error: " + e.getMessage());
                        Toast.makeText(LoginActivity.this, "Invalid response from server", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    if (error.networkResponse != null && error.networkResponse.data != null) {

                        String errorResponseBody = new String(error.networkResponse.data);
                        try {
                            JSONObject errorJson = new JSONObject(errorResponseBody);
                            String message = errorJson.getString("message");
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            Log.e("LoginActivity", "JSON Parsing Error: " + e.getMessage());
                            Toast.makeText(this, "Invalid error response from server", Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Log.e("LoginActivity", "Login Error: " + error.toString());
                        Toast.makeText(LoginActivity.this, "Login Failed: " + error.toString(), Toast.LENGTH_SHORT).show();
                    }
                }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError { // Add getParams if you need to send POST parameters
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("password", password);
                params.put("role", role);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }
        };


        request.setShouldCache(false);
        queue.add(request);
    }

    // Separate method to handle successful login and start the appropriate activity
    private void handleLoginSuccess(String role) {
        Intent intent;
        switch (role) {
            case "beneficiary":
                intent = new Intent(this, BeneficiaryActivity.class);
                break;
            case "manager":
                intent = new Intent(this, ManagerActivity.class);
                break;
            case "organizer":
                intent = new Intent(this, OrganizerActivity.class);
                break;
            default:
                // Handle unexpected role (e.g., display an error)
                Toast.makeText(this, "Unexpected role received from server", Toast.LENGTH_SHORT).show();
                return; // Do not start any activity
        }
        startActivity(intent);
    }



}