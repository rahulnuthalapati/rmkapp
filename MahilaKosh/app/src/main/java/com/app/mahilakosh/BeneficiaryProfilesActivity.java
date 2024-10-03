package com.app.mahilakosh;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.squareup.picasso.Picasso;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class BeneficiaryProfilesActivity extends AppCompatActivity {

    private RecyclerView beneficiaryRecyclerView;
    private BeneficiaryAdapter beneficiaryAdapter;
    private List<Beneficiary> beneficiaryList;
    private EditText searchEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beneficiary_profiles);

        beneficiaryRecyclerView = findViewById(R.id.beneficiaryRecyclerView);
        searchEditText = findViewById(R.id.searchEditText);

        beneficiaryList = new ArrayList<>();
        beneficiaryAdapter = new BeneficiaryAdapter(this, beneficiaryList);
        beneficiaryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        beneficiaryRecyclerView.setAdapter(beneficiaryAdapter);

        fetchBeneficiaries();

        // Set up search functionality using TextWatcher
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                filter(s.toString());
            }
        });
    }

    private void fetchBeneficiaries() {
        String apiUrl = ApiUtils.getCurrentUrl() + ApiUtils.API_PATH + "beneficiary_profiles" + "?client=android";

        RequestQueue queue = MyApplication.getRequestQueue();
        StringRequest request = new StringRequest(Request.Method.GET, apiUrl,
                response -> {
                    try {
                        JSONArray beneficiariesArray = new JSONArray(response);
                        for (int i = 0; i < beneficiariesArray.length(); i++) {
                            JSONObject beneficiaryObject = beneficiariesArray.getJSONObject(i);
                            String name = beneficiaryObject.getString("name");
                            String email = beneficiaryObject.getString("email");
                            String phone = beneficiaryObject.getString("phone");
                            String dob = beneficiaryObject.getString("dob");
                            String aadharno = beneficiaryObject.getString("aadharno");
                            String bankaccno = beneficiaryObject.getString("bankaccno");
                            String profilePicPath = beneficiaryObject.getString("profilePicPath");

                            beneficiaryList.add(new Beneficiary(name, email, phone, dob, aadharno, bankaccno, profilePicPath));
                        }
                        beneficiaryAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        Log.e("BeneficiaryProfiles", "JSON Parsing Error: " + e.getMessage());
                        Toast.makeText(BeneficiaryProfilesActivity.this, "Failed to fetch beneficiaries", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("BeneficiaryProfiles", "VolleyError: " + error.getMessage());
                    Toast.makeText(BeneficiaryProfilesActivity.this, "Failed to fetch beneficiaries", Toast.LENGTH_SHORT).show();
                });
        queue.add(request);
    }

    private void filter(String text) {
        List<Beneficiary> filteredList = new ArrayList<>();
        for (Beneficiary item : beneficiaryList) {
            if (item.name.toLowerCase().contains(text.toLowerCase()) || item.email.toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }
        beneficiaryAdapter.filterList(filteredList);
    }

    // Data class to hold beneficiary information
    public static class Beneficiary {
        String name;
        String email;
        String phone;
        String dob;
        String aadharno;
        String bankaccno;
        String profilePicPath;

        public Beneficiary(String name, String email, String phone, String dob, String aadharno, String bankaccno, String profilePicPath) {
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.dob = dob;
            this.aadharno = aadharno;
            this.bankaccno = bankaccno;
            this.profilePicPath = profilePicPath;
        }
    }

    // RecyclerView Adapter for Beneficiaries
    public static class BeneficiaryAdapter extends RecyclerView.Adapter<BeneficiaryAdapter.BeneficiaryViewHolder> {
        private Context context;
        private List<Beneficiary> beneficiaries;

        public BeneficiaryAdapter(Context context, List<Beneficiary> beneficiaries) {
            this.context = context;
            this.beneficiaries = beneficiaries;
        }

        @Override
        public BeneficiaryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.beneficiary_item, parent, false);
            return new BeneficiaryViewHolder(view);
        }

        @Override
        public void onBindViewHolder(BeneficiaryViewHolder holder, int position) {
            Beneficiary beneficiary = beneficiaries.get(position);
            holder.nameTextView.setText(beneficiary.name);
            holder.emailTextView.setText(beneficiary.email);

            // Load the profile picture using Picasso
            Picasso.get().load(beneficiary.profilePicPath)
                    .placeholder(R.drawable.default_profile)
                    .error(R.drawable.default_profile)
                    .into(holder.profilePicImageView);

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, BeneficiaryLinkActivity.class); // Replace BeneficiaryDetailsActivity
                intent.putExtra("beneficiary_email", beneficiary.email); // Use a suitable key
                context.startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return beneficiaries.size();
        }

        // Method to filter the list based on search query
        public void filterList(List<Beneficiary> filteredList) {
            beneficiaries = filteredList;
            notifyDataSetChanged();
        }

        public static class BeneficiaryViewHolder extends RecyclerView.ViewHolder {
            ImageView profilePicImageView;
            TextView nameTextView;
            TextView emailTextView;

            public BeneficiaryViewHolder(View itemView) {
                super(itemView);
                profilePicImageView = itemView.findViewById(R.id.profilePicImageView);
                nameTextView = itemView.findViewById(R.id.beneficiaryNameTextView);
                emailTextView = itemView.findViewById(R.id.beneficiaryEmailTextView);
            }
        }
    }
}