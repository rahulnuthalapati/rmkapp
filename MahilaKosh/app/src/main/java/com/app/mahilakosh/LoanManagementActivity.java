package com.app.mahilakosh;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.squareup.picasso.Picasso;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class LoanManagementActivity extends AppCompatActivity {

    private RecyclerView loanRequestsRecyclerView;
    private LoanRequestAdapter loanRequestAdapter;
    private List<LoanRequest> loanRequestList;
    private ProgressBar loadingProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loan_management);

        loanRequestsRecyclerView = findViewById(R.id.loanRequestsRecyclerView);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);

        loanRequestList = new ArrayList<>();
        loanRequestAdapter = new LoanRequestAdapter(this, loanRequestList);
        loanRequestsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        loanRequestsRecyclerView.setAdapter(loanRequestAdapter);

        fetchLoanRequests();
    }

    private void fetchLoanRequests() {
        String apiUrl = ApiUtils.getCurrentUrl() + ApiUtils.API_PATH + "get_all_loans?client=android";

        RequestQueue queue = MyApplication.getRequestQueue();
        StringRequest request = new StringRequest(Request.Method.GET, apiUrl,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getBoolean("loan_requests_exists")) {
                            JSONArray loansArray = jsonObject.getJSONArray("loans");
                            for (int i = 0; i < loansArray.length(); i++) {
                                JSONObject loanObject = loansArray.getJSONObject(i);
                                int loanId = loanObject.getInt("loan_id");
                                String email = loanObject.getString("email");
                                double loanAmount = loanObject.getDouble("loan_amount");
                                String loanStatus = loanObject.getString("loan_status");
                                String profilePicPath = loanObject.getString("profilePicPath");

                                loanRequestList.add(new LoanRequest(loanId, email, loanAmount, loanStatus, profilePicPath));
                            }
                            loanRequestAdapter.notifyDataSetChanged();
                        } else {
                            // Handle the case where there are no loan requests
                            Toast.makeText(LoanManagementActivity.this, "No loan requests found.", Toast.LENGTH_SHORT).show();
                        }

                        // Hide the loading progress bar after data is loaded
                        loadingProgressBar.setVisibility(View.GONE);

                    } catch (JSONException e) {
                        Log.e("LoanManagementActivity", "Error parsing JSON: " + e.getMessage());
                        Toast.makeText(LoanManagementActivity.this, "Error fetching loan requests", Toast.LENGTH_SHORT).show();
                        loadingProgressBar.setVisibility(View.GONE);
                    }
                },
                error -> {
                    Log.e("LoanManagementActivity", "VolleyError: " + error.getMessage());
                    Toast.makeText(LoanManagementActivity.this, "Error fetching loan requests", Toast.LENGTH_SHORT).show();
                    loadingProgressBar.setVisibility(View.GONE);
                });
        queue.add(request);
    }

    // LoanRequest data class to store loan request information
    public static class LoanRequest {
        int loanId;
        String email;
        double loanAmount;
        String loanStatus;
        String profilePicPath;

        public LoanRequest(int loanId, String email, double loanAmount, String loanStatus, String profilePicPath) {
            this.loanId = loanId;
            this.email = email;
            this.loanAmount = loanAmount;
            this.loanStatus = loanStatus;
            this.profilePicPath = profilePicPath;
        }
    }

    // RecyclerView Adapter for Loan Requests
    public static class LoanRequestAdapter extends RecyclerView.Adapter<LoanRequestAdapter.LoanRequestViewHolder> {
        private final Context context;
        private final List<LoanRequest> loanRequests;

        public LoanRequestAdapter(Context context, List<LoanRequest> loanRequests) {
            this.context = context;
            this.loanRequests = loanRequests;
        }

        @Override
        public LoanRequestViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.loan_request_item, parent, false);
            return new LoanRequestViewHolder(view);
        }

        @Override
        public void onBindViewHolder(LoanRequestViewHolder holder, int position) {
            LoanRequest loanRequest = loanRequests.get(position);
            holder.loanIdTextView.setText("Loan ID: " + loanRequest.loanId);
            holder.emailTextView.setText(loanRequest.email);
            holder.loanAmountTextView.setText("Amount: ₹" + loanRequest.loanAmount);

            // Set the status icon based on loan status
            int statusIcon = R.drawable.ic_pending; // Default icon
            switch (loanRequest.loanStatus) {
                case "approved":
                    statusIcon = R.drawable.ic_approved;
                    break;
                case "rejected":
                    statusIcon = R.drawable.ic_rejected;
                    break;
                case "completed":
                    statusIcon = R.drawable.ic_completed;
                    break;
                case "disbursed":
                    statusIcon = R.drawable.ic_disbursed;
                    break;
            }
            holder.statusIconImageView.setImageResource(statusIcon);

            // Load profile picture using Picasso
            Picasso.get()
                    .load(loanRequest.profilePicPath)
                    .placeholder(R.drawable.default_profile)
                    .error(R.drawable.default_profile)
                    .into(holder.profilePicImageView);

            // Set click listener to open LoanDetailsActivity
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, LoanDetailsActivity.class);
                intent.putExtra("email", loanRequest.email);
                intent.putExtra("loanId", loanRequest.loanId);
                context.startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return loanRequests.size();
        }

        public static class LoanRequestViewHolder extends RecyclerView.ViewHolder {
            ImageView profilePicImageView;
            TextView loanIdTextView;
            TextView emailTextView;
            TextView loanAmountTextView;
            ImageView statusIconImageView;

            public LoanRequestViewHolder(View itemView) {
                super(itemView);
                profilePicImageView = itemView.findViewById(R.id.profilePicImageView);
                loanIdTextView = itemView.findViewById(R.id.loanIdTextView);
                emailTextView = itemView.findViewById(R.id.beneficiaryEmailTextView);
                loanAmountTextView = itemView.findViewById(R.id.loanAmountTextView);
                statusIconImageView = itemView.findViewById(R.id.statusIconImageView);
            }
        }
    }
}