package com.app.mahilakosh;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeedbackActivity extends AppCompatActivity {

    private RecyclerView feedbackRecyclerView;
    private FeedbackAdapter feedbackAdapter;
    private List<Feedback> feedbackList;
    private Button newFeedbackButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        feedbackRecyclerView = findViewById(R.id.feedbackRecyclerView);
        newFeedbackButton = findViewById(R.id.newFeedbackButton);

        feedbackList = new ArrayList<>();
        feedbackAdapter = new FeedbackAdapter(this, feedbackList);
        feedbackRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        feedbackRecyclerView.setAdapter(feedbackAdapter);

        if(!ApiUtils.currentRole.equals("beneficiary")) {
            newFeedbackButton.setVisibility(View.GONE);
        }

        fetchFeedback();

        newFeedbackButton.setOnClickListener(v -> showNewFeedbackDialog());
    }

    private void fetchFeedback() {
        String apiUrl = ApiUtils.getCurrentUrl() + ApiUtils.API_PATH + "feedback" + "?client=android";

        RequestQueue queue = MyApplication.getRequestQueue();
        StringRequest request = new StringRequest(Request.Method.GET, apiUrl,
                response -> {
                    try {
                        JSONArray feedbackArray = new JSONArray(response);
                        for (int i = 0; i < feedbackArray.length(); i++) {
                            JSONObject feedbackObject = feedbackArray.getJSONObject(i);
                            int feedbackId = feedbackObject.getInt("feedback_id");
                            String feedbackDate = feedbackObject.getString("feedback_date");
                            String feedbackText = feedbackObject.getString("feedback_text");
                            String feedbackSummary = feedbackObject.getString("feedback_summary");
                            feedbackList.add(new Feedback(feedbackId, feedbackDate, feedbackSummary, feedbackText));
                        }
                        feedbackAdapter.notifyDataSetChanged();

                    } catch (JSONException e) {
                        Log.e("FeedbackActivity", "JSON parsing error: " + e.getMessage());
                        Toast.makeText(FeedbackActivity.this, "Error fetching feedback", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("FeedbackActivity", "VolleyError: " + error.getMessage());
                    Toast.makeText(FeedbackActivity.this, "Error fetching feedback", Toast.LENGTH_SHORT).show();
                });

        queue.add(request);
    }

    private void showNewFeedbackDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_new_feedback, null);
        builder.setView(dialogView);

        EditText feedbackEditText = dialogView.findViewById(R.id.feedbackEditText);
        Button submitButton = dialogView.findViewById(R.id.submitFeedbackButton);

        AlertDialog dialog = builder.create();
        dialog.show();

        submitButton.setOnClickListener(v -> {
            String feedbackText = feedbackEditText.getText().toString().trim();
            if (!feedbackText.isEmpty()) {
                submitFeedback(feedbackText, dialog);
            } else {
                Toast.makeText(FeedbackActivity.this, "Please enter your feedback", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void submitFeedback(String feedbackText, AlertDialog dialog) {
        String apiUrl = ApiUtils.getCurrentUrl() + ApiUtils.API_PATH + "feedback" + "?client=android";

        Map<String, String> params = new HashMap<>();
        params.put("feedback_text", feedbackText);

        RequestQueue queue = MyApplication.getRequestQueue();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, apiUrl, new JSONObject(params),
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            Toast.makeText(FeedbackActivity.this, "Feedback submitted successfully", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            fetchFeedback();
                        } else {
                            Toast.makeText(FeedbackActivity.this, "Error: " + response.getString("error"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("FeedbackActivity", "JSON parsing error: " + e.getMessage());
                        Toast.makeText(FeedbackActivity.this, "Error submitting feedback", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("FeedbackActivity", "VolleyError: " + error.getMessage());
                    Toast.makeText(FeedbackActivity.this, "Error submitting feedback", Toast.LENGTH_SHORT).show();
                });

        queue.add(request);
    }

    // Feedback class to store feedback data
    public static class Feedback {
        int feedbackId;
        String feedbackDate;
        String feedbackSummary;
        String feedbackText;

        public Feedback(int feedbackId, String feedbackDate, String feedbackSummary, String feedbackText) {
            this.feedbackId = feedbackId;
            this.feedbackDate = feedbackDate;
            this.feedbackSummary = feedbackSummary;
            this.feedbackText = feedbackText;
        }
    }

    // RecyclerView Adapter
    public static class FeedbackAdapter extends RecyclerView.Adapter<FeedbackAdapter.FeedbackViewHolder> {
        private Context context;
        private List<Feedback> feedbacks;

        public FeedbackAdapter(Context context, List<Feedback> feedbacks) {
            this.context = context;
            this.feedbacks = feedbacks;
        }

        @Override
        public FeedbackViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.feedback_item, parent, false);
            return new FeedbackViewHolder(view);
        }

        @Override
        public void onBindViewHolder(FeedbackViewHolder holder, int position) {
            Feedback feedback = feedbacks.get(position);
            holder.feedbackDateTextView.setText("Date: " + feedback.feedbackDate);
            holder.feedbackSummaryTextView.setText(feedback.feedbackSummary);

            // Set OnClickListener to show full feedback in a dialog
            holder.itemView.setOnClickListener(v -> {
                new AlertDialog.Builder(context)
                        .setTitle("Feedback Details")
                        .setMessage(feedback.feedbackText)
                        .setPositiveButton("OK", null)
                        .show();
            });
        }

        @Override
        public int getItemCount() {
            return feedbacks.size();
        }

        public static class FeedbackViewHolder extends RecyclerView.ViewHolder {
            TextView feedbackDateTextView, feedbackSummaryTextView;

            public FeedbackViewHolder(View itemView) {
                super(itemView);
                feedbackDateTextView = itemView.findViewById(R.id.feedbackDateTextView);
                feedbackSummaryTextView = itemView.findViewById(R.id.feedbackSummaryTextView);
            }
        }
    }

}