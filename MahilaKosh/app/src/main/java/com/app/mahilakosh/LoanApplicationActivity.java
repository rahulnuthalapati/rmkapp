package com.app.mahilakosh;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import android.app.DatePickerDialog;
import android.widget.DatePicker;
import java.util.Calendar;

public class LoanApplicationActivity extends AppCompatActivity {

    private EditText nameEditText, contactEditText, businessEditText, amountEditText;
    private ImageView aadhaarImageView, panImageView, passbookImageView, passportImageView;
    private Button aadhaarButton, panButton, passbookButton, passportButton, submitButton;

    private Uri aadhaarImageUri, panImageUri, passbookImageUri, passportImageUri;

    private TextInputEditText dobEditText;
    private Calendar calendar;
    private int year, month, day;

    private ActivityResultLauncher<Intent> aadhaarImagePickerLauncher;
    private ActivityResultLauncher<Intent> panImagePickerLauncher;
    private ActivityResultLauncher<Intent> passbookImagePickerLauncher;
    private ActivityResultLauncher<Intent> passportImagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loan_application);

        initializeViews();
        setupDatePicker();
        setupImagePickers();
        setupButtonListeners();
    }

    private void initializeViews() {
        nameEditText = findViewById(R.id.nameEditText);
        dobEditText = findViewById(R.id.dobEditText);
        contactEditText = findViewById(R.id.contactEditText);
        businessEditText = findViewById(R.id.businessEditText);
        amountEditText = findViewById(R.id.amountEditText);

        aadhaarImageView = findViewById(R.id.aadhaarImageView);
        panImageView = findViewById(R.id.panImageView);
        passbookImageView = findViewById(R.id.passbookImageView);
        passportImageView = findViewById(R.id.passportImageView);

        aadhaarButton = findViewById(R.id.aadhaarButton);
        panButton = findViewById(R.id.panButton);
        passbookButton = findViewById(R.id.passbookButton);
        passportButton = findViewById(R.id.passportButton);
        submitButton = findViewById(R.id.submitLoanRequestButton);
    }

    private void setupDatePicker() {
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        dobEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(LoanApplicationActivity.this, dateSetListener, year, month, day).show();
            }
        });
    }

    private DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            dobEditText.setText(String.format(Locale.getDefault(), "%04d-%02d-%02d", year, monthOfYear + 1, dayOfMonth));
        }
    };

    private void setupImagePickers() {
        aadhaarImagePickerLauncher = registerImagePicker(aadhaarImageView, new ImageUriSetter() {
            @Override
            public void setUri(Uri uri) {
                aadhaarImageUri = uri;
            }
        });

        panImagePickerLauncher = registerImagePicker(panImageView, new ImageUriSetter() {
            @Override
            public void setUri(Uri uri) {
                panImageUri = uri;
            }
        });

        passbookImagePickerLauncher = registerImagePicker(passbookImageView, new ImageUriSetter() {
            @Override
            public void setUri(Uri uri) {
                passbookImageUri = uri;
            }
        });

        passportImagePickerLauncher = registerImagePicker(passportImageView, new ImageUriSetter() {
            @Override
            public void setUri(Uri uri) {
                passportImageUri = uri;
            }
        });
    }

    private ActivityResultLauncher<Intent> registerImagePicker(final ImageView imageView, final ImageUriSetter uriSetter) {
        return registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            Uri selectedImageUri = result.getData().getData();
                            imageView.setImageURI(selectedImageUri);
                            uriSetter.setUri(selectedImageUri);
                        }
                    }
                }
        );
    }

    private void setupButtonListeners() {
        aadhaarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePicker(aadhaarImagePickerLauncher);
            }
        });

        panButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePicker(panImagePickerLauncher);
            }
        });

        passbookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePicker(passbookImagePickerLauncher);
            }
        });

        passportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePicker(passportImagePickerLauncher);
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitLoanApplication();
            }
        });
    }

    private void openImagePicker(ActivityResultLauncher<Intent> launcher) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        launcher.launch(intent);
    }

    private interface ImageUriSetter {
        void setUri(Uri uri);
    }

    private void submitLoanApplication() {
        String name = nameEditText.getText().toString();
        String dob = dobEditText.getText().toString();
        String contact = contactEditText.getText().toString();
        String business = businessEditText.getText().toString();
        String amount = amountEditText.getText().toString();

        if (name.isEmpty() || dob.isEmpty() || contact.isEmpty() || business.isEmpty() || amount.isEmpty() ||
                aadhaarImageUri == null || panImageUri == null || passbookImageUri == null || passportImageUri == null) {
            Toast.makeText(this, "Please fill all fields and upload all images", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convert images to Base64
        String aadhaarImageBase64 = convertImageToBase64(aadhaarImageUri);
        String panImageBase64 = convertImageToBase64(panImageUri);
        String passbookImageBase64 = convertImageToBase64(passbookImageUri);
        String passportImageBase64 = convertImageToBase64(passportImageUri);

        // Send data to Flask server using Volley
        String apiUrl = ApiUtils.getCurrentUrl() + ApiUtils.API_PATH + "loanapplication";
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest request = new StringRequest(Request.Method.POST, apiUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            if (jsonResponse.getBoolean("success")) {
                                Toast.makeText(LoanApplicationActivity.this, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                                finish(); // Finish the LoanApplicationActivity
                            } else {
                                Toast.makeText(LoanApplicationActivity.this, "Error: " + jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Log.e("LoanAppActivity", "Error parsing JSON response: " + e.getMessage());
                            Toast.makeText(LoanApplicationActivity.this, "Error submitting application", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("LoanAppActivity", "Error: " + error.getMessage());
                        Toast.makeText(LoanApplicationActivity.this, "Error submitting application", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("name", name);
                params.put("dob", dob);
                params.put("contact", contact);
                params.put("business", business);
                params.put("amount", amount);
                params.put("adhaar", aadhaarImageBase64);
                params.put("pan", panImageBase64);
                params.put("passbook", passbookImageBase64);
                params.put("passport", passportImageBase64);
                return params;
            }
        };
        queue.add(request);
    }

    private String convertImageToBase64(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            return Base64.encodeToString(byteArray, Base64.DEFAULT);
        } catch (IOException e) {
            Log.e("LoanAppActivity", "Error converting image to Base64: " + e.getMessage());
            return null;
        }
    }
}