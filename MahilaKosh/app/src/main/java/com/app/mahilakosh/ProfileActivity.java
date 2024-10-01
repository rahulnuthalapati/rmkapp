package com.app.mahilakosh;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.textfield.TextInputEditText;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private ImageView profileImageView;
    private ImageButton changeProfileImageButton;
    private TextInputEditText nameEditText, dobEditText, aadhaarEditText, phoneEditText, bankAccountEditText;
    private Button editProfileButton, saveProfileButton;

    private Uri selectedImageUri;
    private static final int PICK_IMAGE_REQUEST = 1;

    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            selectedImageUri = result.getData().getData();
                            profileImageView.setImageURI(selectedImageUri);
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize views
        profileImageView = findViewById(R.id.profileImageView);
        changeProfileImageButton = findViewById(R.id.changeProfileImageButton);
        nameEditText = findViewById(R.id.nameEditText);
        dobEditText = findViewById(R.id.dobEditText);
        aadhaarEditText = findViewById(R.id.aadhaarEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        bankAccountEditText = findViewById(R.id.bankAccountEditText);
        editProfileButton = findViewById(R.id.editProfileButton);
        saveProfileButton = findViewById(R.id.saveProfileButton);

        changeProfileImageButton.setOnClickListener(v -> openImagePicker());
        editProfileButton.setOnClickListener(v -> {
            enableEditFields(true);
            editProfileButton.setVisibility(View.GONE);
            saveProfileButton.setVisibility(View.VISIBLE);
        });

        saveProfileButton.setOnClickListener(v -> saveProfileData());

        fetchProfileData();
    }

    // Method to enable/disable editing of profile fields
    private void enableEditFields(boolean enabled) {
        nameEditText.setEnabled(enabled);
        dobEditText.setEnabled(enabled);
        aadhaarEditText.setEnabled(enabled);
        phoneEditText.setEnabled(enabled);
        bankAccountEditText.setEnabled(enabled);
        changeProfileImageButton.setVisibility(enabled ? View.VISIBLE : View.GONE);
    }

    // Method to fetch profile data from the server
    private void fetchProfileData() {
        String apiUrl = ApiUtils.getCurrentUrl() + ApiUtils.API_PATH + "profileinfo" + "?client=android";

        RequestQueue queue = MyApplication.getRequestQueue();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, apiUrl, null,
                response -> {
                    try {
                        nameEditText.setText(response.optString("name"));
                        phoneEditText.setText(String.valueOf(response.optLong("phone")));
                        dobEditText.setText(response.optString("dob", "N/A"));
                        aadhaarEditText.setText(String.valueOf(response.optLong("aadharno")));
                        bankAccountEditText.setText(String.valueOf(response.optLong("bankaccno")));

                        // Fetch and decode the Base64 profile image
                        String base64Image = response.optString("profilePic", "");
                        byte[] imageBytes = Base64.decode(base64Image, Base64.DEFAULT);
                        Bitmap decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                        profileImageView.setImageBitmap(decodedImage);

                    } catch (Exception e) {
                        Log.e("ProfileActivity", "JSON Parsing Error: " + e.getMessage());
                        Toast.makeText(this, "Failed to fetch profile data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    // Handle Volley error
                    Log.e("ProfileActivity", "VolleyError: " + error.getMessage());
                    Toast.makeText(this, "Failed to fetch profile data", Toast.LENGTH_SHORT).show();
                });

        queue.add(request);
    }

    // Method to save the updated profile data to the server
    private void saveProfileData() {
        String apiUrl = ApiUtils.getCurrentUrl() + ApiUtils.API_PATH + "update_profile" + "?client=android";

        // Collect data from the input fields
        String name = nameEditText.getText().toString();
        String dob = dobEditText.getText().toString();
        String aadhaar = aadhaarEditText.getText().toString();
        String phone = phoneEditText.getText().toString();
        String bankAccount = bankAccountEditText.getText().toString();

        // Create a RequestQueue
        RequestQueue queue = MyApplication.getRequestQueue();

        // Create a StringRequest to send data to the server
        StringRequest request = new StringRequest(Request.Method.POST, apiUrl,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getBoolean("success")) {
                            // Profile updated successfully
                            Toast.makeText(ProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                            // ... you might want to update SharedPreferences here as well ...
                            // Disable editing of input fields
                            enableEditFields(false);
                            // Show Edit Profile button and hide Save Changes button
                            editProfileButton.setVisibility(View.VISIBLE);
                            saveProfileButton.setVisibility(View.GONE);
                        } else {
                            // Handle error response from the server
                            String errorMessage = jsonResponse.getString("error");
                            Toast.makeText(ProfileActivity.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(ProfileActivity.this, "Error processing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    // Handle Volley error
                    Log.e("ProfileActivity", "Error updating profile: " + error.toString());
                    Toast.makeText(ProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                // Prepare the data to be sent in the request body
                Map<String, String> params = new HashMap<>();
                params.put("name", name);
                params.put("dob", dob);
                params.put("aadharno", aadhaar);
                params.put("phone", phone);
                params.put("bankaccno", bankAccount);

                // Add the profile picture data if a new image was selected
                if (selectedImageUri != null) {
                    params.put("profilePic", convertImageToBase64(selectedImageUri));
                }
                return params;
            }
        };

        // Add the request to the RequestQueue.
        queue.add(request);
    }

    // Method to open the image picker for selecting a profile picture
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);
    }

    // Method to convert a Bitmap image to a Base64 encoded string
    private String convertImageToBase64(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            return Base64.encodeToString(byteArray, Base64.DEFAULT);
        } catch (IOException e) {
            Log.e("ProfileActivity", "Error converting image: " + e.getMessage());
            return null; // Or handle the error appropriately
        }
    }
}