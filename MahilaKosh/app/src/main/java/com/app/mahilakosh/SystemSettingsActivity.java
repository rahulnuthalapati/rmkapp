package com.app.mahilakosh;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class SystemSettingsActivity extends AppCompatActivity {

    private Button editProfileButton;
    private Button changePasswordButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_settings);

        editProfileButton = findViewById(R.id.editProfileButton);
        changePasswordButton = findViewById(R.id.changePasswordButton);

        editProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(SystemSettingsActivity.this, EditProfileActivity.class);
            startActivity(intent);
        });

        changePasswordButton.setOnClickListener(v -> {
            Intent intent = new Intent(SystemSettingsActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
        });
    }
}