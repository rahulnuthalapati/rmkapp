package com.app.mahilakosh;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MessagingActivity extends AppCompatActivity {

    private RecyclerView messagesRecyclerView;
    private MessageAdapter messageAdapter;
    private List<Message> messageList;
    private EditText messageEditText;
    private Button sendButton;
    private String receiverEmail, receiverName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);

        // Get receiver details from Intent
        Intent intent = getIntent();
        receiverEmail = intent.getStringExtra("receiverEmail");
        receiverName = intent.getStringExtra("receiverName");

        // Set activity title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Chat with " + receiverName);
        }

        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messageList);
        messagesRecyclerView.setAdapter(messageAdapter);

        messagesRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom != oldBottom) {
                    scrollToBottom();
                    messagesRecyclerView.removeOnLayoutChangeListener(this);
                }
            }
        });

        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);

        sendButton.setOnClickListener(v -> {
            String messageText = messageEditText.getText().toString().trim();
            if (!messageText.isEmpty()) {
                sendMessage(messageText);
            } else {
                Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show();
            }
        });

        fetchMessages();
    }


    private void fetchMessages() {
        String apiUrl = ApiUtils.getCurrentUrl() + ApiUtils.API_PATH + "get_messages/" + receiverEmail + "?client=android";

        RequestQueue queue = MyApplication.getRequestQueue();
        StringRequest request = new StringRequest(Request.Method.GET, apiUrl,
                response -> {
                    try {
                        JSONArray messagesArray = new JSONArray(response);
                        messageList.clear(); // Clear previous messages

                        for (int i = 0; i < messagesArray.length(); i++) {
                            JSONObject messageObject = messagesArray.getJSONObject(i);
                            String sender = messageObject.getString("sender");
                            String message = messageObject.getString("message");
                            String timestamp = messageObject.getString("timestamp");

                            messageList.add(new Message(sender, message, timestamp));
                        }

                        messageAdapter.notifyDataSetChanged();
                        scrollToBottom(); // Scroll to the bottom to show the latest message
                    } catch (JSONException e) {
                        Log.e("MessagingActivity", "JSON Parsing Error: " + e.getMessage());
                        Toast.makeText(MessagingActivity.this, "Failed to fetch messages", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("MessagingActivity", "VolleyError: " + error.getMessage());
                    Toast.makeText(MessagingActivity.this, "Failed to fetch messages", Toast.LENGTH_SHORT).show();
                });
        queue.add(request);
    }

    private void sendMessage(String messageText) {
        String apiUrl = ApiUtils.getCurrentUrl() + ApiUtils.API_PATH + "send_message" + "?client=android";

        RequestQueue queue = MyApplication.getRequestQueue();
        StringRequest request = new StringRequest(Request.Method.POST, apiUrl,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getBoolean("success")) {
                            messageEditText.setText("");
                            fetchMessages();
                        } else {
                            Toast.makeText(MessagingActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("MessagingActivity", "JSON Parsing Error: " + e.getMessage());
                        Toast.makeText(MessagingActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("MessagingActivity", "VolleyError: " + error.getMessage());
                    Toast.makeText(MessagingActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json; charset=UTF-8");
                return headers;
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                JSONObject jsonBody = new JSONObject();
                try {
                    jsonBody.put("receiver_email", receiverEmail);
                    jsonBody.put("message_text", messageText);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return jsonBody.toString().getBytes();
            }
        };

        queue.add(request);
    }

    private void markMessagesRead() {
        String apiUrl = ApiUtils.getCurrentUrl() + ApiUtils.API_PATH + "mark_messages_read/" + receiverEmail + "?client=android";
        RequestQueue queue = MyApplication.getRequestQueue();
        StringRequest request = new StringRequest(Request.Method.POST, apiUrl,
                response -> {
                    // Handle success (no action needed here, messages are marked as read on server)
                },
                error -> {
                    Log.e("MessagingActivity", "VolleyError marking messages read: " + error.getMessage());
                });
        queue.add(request);
    }

    @Override
    protected void onResume() {
        super.onResume();
        markMessagesRead(); // Mark messages as read when the chat is opened
        fetchMessages();
    }


    private void scrollToBottom() {
        messagesRecyclerView.postDelayed(() -> messagesRecyclerView.smoothScrollToPosition(messageAdapter.getItemCount() - 1), 100);
    }

    // Message class to store individual messages
    public static class Message {
        String sender;
        String message;
        String timestamp;

        public Message(String sender, String message, String timestamp) {
            this.sender = sender;
            this.message = message;
            this.timestamp = timestamp;
        }
    }

    public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int VIEW_TYPE_SENT = 1;
        private static final int VIEW_TYPE_RECEIVED = 2;

        private Context context;
        private List<Message> messages;

        public MessageAdapter(Context context, List<Message> messages) {
            this.context = context;
            this.messages = messages;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            if (viewType == VIEW_TYPE_SENT) {
                view = LayoutInflater.from(context).inflate(R.layout.message_item_sent, parent, false);
                return new SentMessageViewHolder(view);
            } else {
                view = LayoutInflater.from(context).inflate(R.layout.message_item_received, parent, false);
                return new ReceivedMessageViewHolder(view);
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            Message message = messages.get(position);

            // Format the timestamp (replace with your desired format)
            SimpleDateFormat inputFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.getDefault()); // Correct format
            SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            try {
                Date date = inputFormat.parse(message.timestamp);
                String formattedTimestamp = outputFormat.format(date);
                if (holder instanceof SentMessageViewHolder) {
                    ((SentMessageViewHolder) holder).sentMessageTextView.setText(message.message);
                    ((SentMessageViewHolder) holder).sentMessageTimestamp.setText(formattedTimestamp);
                } else if (holder instanceof ReceivedMessageViewHolder) {
                    ((ReceivedMessageViewHolder) holder).receivedMessageTextView.setText(message.message);
                    ((ReceivedMessageViewHolder) holder).receivedMessageTimestamp.setText(formattedTimestamp);
                }
            } catch (Exception e) {
                Log.e("MessagingActivity", "Error formatting timestamp: " + e.getMessage());
                // Handle the error (e.g., display the original timestamp)
            }
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }

        @Override
        public int getItemViewType(int position) {
            Message message = messages.get(position);
            SharedPreferences sharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE);
            String userEmail = sharedPreferences.getString("email", "");
            if (message.sender.equals(userEmail)) {
                return VIEW_TYPE_SENT;
            } else {
                return VIEW_TYPE_RECEIVED;
            }
        }

        public class SentMessageViewHolder extends RecyclerView.ViewHolder {
            TextView sentMessageTextView;
            TextView sentMessageTimestamp;

            public SentMessageViewHolder(View itemView) {
                super(itemView);
                sentMessageTextView = itemView.findViewById(R.id.sentMessageTextView);
                sentMessageTimestamp = itemView.findViewById(R.id.sentMessageTimestamp);
            }
        }

        public class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
            TextView receivedMessageTextView;
            TextView receivedMessageTimestamp;

            public ReceivedMessageViewHolder(View itemView) {
                super(itemView);
                receivedMessageTextView = itemView.findViewById(R.id.receivedMessageTextView);
                receivedMessageTimestamp = itemView.findViewById(R.id.receivedMessageTimestamp);
            }
        }
    }
}