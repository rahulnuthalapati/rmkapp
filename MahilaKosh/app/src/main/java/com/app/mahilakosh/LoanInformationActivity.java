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
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class LoanInformationActivity extends AppCompatActivity {

    private RecyclerView loanRecyclerView;
    private LoanAdapter loanAdapter;
    private List<Loan> loanList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loan_information);

        loanRecyclerView = findViewById(R.id.loanRecyclerView);
        loanRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        loanList = new ArrayList<>();
        loanAdapter = new LoanAdapter(this, loanList);
        loanRecyclerView.setAdapter(loanAdapter);

        String responseData = getIntent().getStringExtra("response_data");
        if (responseData != null) {
            fetchLoanInformation(responseData);
        } else {
            fetchLoanInformation("");
        }
    }

    private void fetchLoanInformation(String responseData) {
        if ( !responseData.isEmpty()) {
            try {
                processLoanInformation(responseData);
            }
            catch (JSONException e) {
                Log.e("LoanInfoActivity", "Error parsing JSON: " + e.getMessage());
                Toast.makeText(LoanInformationActivity.this, "Error fetching loan information.", Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            String apiUrl = ApiUtils.getCurrentUrl() + ApiUtils.API_PATH + "loan_information" + "?client=android";

            RequestQueue queue = MyApplication.getRequestQueue();
            StringRequest request = new StringRequest(Request.Method.GET, apiUrl,
                    response -> {
                        try {
                            processLoanInformation(response);
                        }
                        catch (JSONException e) {
                            Log.e("LoanInfoActivity", "Error parsing JSON: " + e.getMessage());
                            Toast.makeText(LoanInformationActivity.this, "Error fetching loan information.", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        Log.e("LoanInfoActivity", "VolleyError: " + error.getMessage());
                        Toast.makeText(LoanInformationActivity.this, "Error fetching loan information.", Toast.LENGTH_SHORT).show();
                    });
            queue.add(request);
        }
    }

    private void processLoanInformation(String response) throws JSONException
    {
        JSONObject jsonObject = new JSONObject(response);
        if (jsonObject.getBoolean("loan_exists")) {
            JSONArray loansArray = jsonObject.getJSONArray("loans");
            for (int i = 0; i < loansArray.length(); i++) {
                JSONObject loanObject = loansArray.getJSONObject(i);
                int loanId = loanObject.getInt("loan_id");
                double loanAmount = loanObject.getDouble("loan_amount");
                String loanStatus = loanObject.getString("loan_status");
                boolean paymentExists = loanObject.getBoolean("payment_exists");
                loanList.add(new Loan(loanId, loanAmount, loanStatus, paymentExists));
            }
            loanAdapter.notifyDataSetChanged();
        }
        else {
            Toast.makeText(LoanInformationActivity.this, "No loan information found.", Toast.LENGTH_SHORT).show();
        }
    }

    public static class Loan {
        int loanId;
        double loanAmount;
        String loanStatus;
        boolean paymentExists;

        public Loan(int loanId, double loanAmount, String loanStatus, boolean paymentExists) {
            this.loanId = loanId;
            this.loanAmount = loanAmount;
            this.loanStatus = loanStatus;
            this.paymentExists = paymentExists;
        }
    }

    public static class LoanAdapter extends RecyclerView.Adapter<LoanAdapter.LoanViewHolder> {
        private Context context;
        private List<Loan> loans;

        public LoanAdapter(Context context, List<Loan> loans) {
            this.context = context;
            this.loans = loans;
        }

        @Override
        public LoanViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.loan_item, parent, false);
            return new LoanViewHolder(view);
        }

        @Override
        public void onBindViewHolder(LoanViewHolder holder, int position) {
            Loan loan = loans.get(position);
            holder.loanIdTextView.setText("Loan ID: " + loan.loanId);
            holder.loanAmountTextView.setText("Amount: ₹" + loan.loanAmount);
            holder.loanStatusTextView.setText("Status: " + loan.loanStatus);

            holder.viewDetailsButton.setOnClickListener(v -> {
                Intent intent = new Intent(context, LoanPaymentActivity.class);
                intent.putExtra("loanId", loan.loanId);
                context.startActivity(intent);
            });

            if (loan.paymentExists) {
                holder.viewDetailsButton.setVisibility(View.VISIBLE);
                holder.viewDetailsButton.setOnClickListener(v -> {
                    Intent intent = new Intent(context, LoanPaymentActivity.class);
                    intent.putExtra("loanId", loan.loanId);
                    context.startActivity(intent);
                });
            } else {
                holder.viewDetailsButton.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return loans.size();
        }

        public static class LoanViewHolder extends RecyclerView.ViewHolder {
            TextView loanIdTextView, loanAmountTextView, loanStatusTextView;
            ImageButton viewDetailsButton;

            public LoanViewHolder(View itemView) {
                super(itemView);
                loanIdTextView = itemView.findViewById(R.id.loanIdTextView);
                loanAmountTextView = itemView.findViewById(R.id.loanAmountTextView);
                loanStatusTextView = itemView.findViewById(R.id.loanStatusTextView);
                viewDetailsButton = itemView.findViewById(R.id.viewDetailsButton);
            }
        }
    }
}