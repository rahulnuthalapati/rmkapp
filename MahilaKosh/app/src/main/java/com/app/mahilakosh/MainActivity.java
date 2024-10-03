package com.app.mahilakosh;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button beneficiaryButton = findViewById(R.id.beneficiaryButton);
        Button managerButton = findViewById(R.id.managerButton);
        Button organizerButton = findViewById(R.id.organizerButton);

        beneficiaryButton.setOnClickListener(v -> startLoginActivity("beneficiary"));

        managerButton.setOnClickListener(v -> startLoginActivity("manager"));

        organizerButton.setOnClickListener(v -> startLoginActivity("organizer"));
    }

    private void startLoginActivity(String role) {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("role", role);
        ApiUtils.currentRole = role;
        startActivity(intent);
    }
}