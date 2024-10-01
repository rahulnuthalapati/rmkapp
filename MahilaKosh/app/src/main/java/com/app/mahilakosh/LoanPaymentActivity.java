package com.app.mahilakosh;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class LoanPaymentActivity extends AppCompatActivity {

    private RecyclerView paymentRecyclerView;
    private PaymentAdapter paymentAdapter;
    private List<Payment> paymentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loan_payment); // Use the correct layout

        paymentRecyclerView = findViewById(R.id.paymentHistoryRecyclerView);
        paymentRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        paymentList = new ArrayList<>();
        paymentAdapter = new PaymentAdapter(this, paymentList);
        paymentRecyclerView.setAdapter(paymentAdapter);

        // Get the loan ID from the Intent
        int loanId = getIntent().getIntExtra("loanId", -1);

        if (loanId != -1) {
            fetchPaymentHistory(loanId);
        } else {
            Toast.makeText(this, "Loan ID is missing.", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchPaymentHistory(int loanId) {
        String apiUrl = ApiUtils.getCurrentUrl() + ApiUtils.API_PATH +"get_loan_payments?loan_id=" + loanId + "&client=android";

        RequestQueue queue = MyApplication.getRequestQueue();
        StringRequest request = new StringRequest(Request.Method.GET, apiUrl,
                response -> {
                    try {
                        JSONArray paymentsArray = new JSONArray(response);
                        for (int i = 0; i < paymentsArray.length(); i++) {
                            JSONObject paymentObject = paymentsArray.getJSONObject(i);
                            int paymentId = paymentObject.getInt("payment_id");
                            double amount = paymentObject.getDouble("amount");
                            String paymentDate = paymentObject.getString("payment_date");
                            paymentList.add(new Payment(paymentId, amount, paymentDate));
                        }
                        paymentAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        Log.e("LoanPaymentActivity", "Error parsing JSON: " + e.getMessage());
                        Toast.makeText(LoanPaymentActivity.this, "Error fetching payment history.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("LoanPaymentActivity", "VolleyError: " + error.getMessage());
                    Toast.makeText(LoanPaymentActivity.this, "Error fetching payment history.", Toast.LENGTH_SHORT).show();
                });
        queue.add(request);
    }


    // Payment Data Class
    public static class Payment {
        int paymentId;
        double amount;
        String paymentDate;

        public Payment(int paymentId, double amount, String paymentDate) {
            this.paymentId = paymentId;
            this.amount = amount;
            this.paymentDate = paymentDate;
        }
    }

    // Payment Adapter
    public static class PaymentAdapter extends RecyclerView.Adapter<PaymentAdapter.PaymentViewHolder> {
        private Context context;
        private List<Payment> payments;

        public PaymentAdapter(Context context, List<Payment> payments) {
            this.context = context;
            this.payments = payments;
        }

        @Override
        public PaymentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.payment_item, parent, false);
            return new PaymentViewHolder(view);
        }

        @Override
        public void onBindViewHolder(PaymentViewHolder holder, int position) {
            Payment payment = payments.get(position);
            holder.paymentIdTextView.setText("Payment ID: " + payment.paymentId);
            holder.paymentAmountTextView.setText("Amount: ₹" + payment.amount);
            holder.paymentDateTextView.setText("Date: " + payment.paymentDate);
        }

        @Override
        public int getItemCount() {
            return payments.size();
        }

        public static class PaymentViewHolder extends RecyclerView.ViewHolder {
            TextView paymentIdTextView, paymentAmountTextView, paymentDateTextView;

            public PaymentViewHolder(View itemView) {
                super(itemView);
                paymentIdTextView = itemView.findViewById(R.id.paymentIdTextView);
                paymentAmountTextView = itemView.findViewById(R.id.paymentAmountTextView);
                paymentDateTextView = itemView.findViewById(R.id.paymentDateTextView);
            }
        }
    }
}