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

public class ChatActivity extends AppCompatActivity {

    private RecyclerView usersRecyclerView;
    private UserAdapter userAdapter;
    private List<User> userList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        usersRecyclerView = findViewById(R.id.usersRecyclerView);
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        userList = new ArrayList<>();
        userAdapter = new UserAdapter(this, userList);
        usersRecyclerView.setAdapter(userAdapter);

        fetchUsers();
    }

    private void fetchUsers() {
        String apiUrl = ApiUtils.getCurrentUrl() + ApiUtils.API_PATH + "get_users_for_chat" + "?client=android";

        RequestQueue queue = MyApplication.getRequestQueue();
        StringRequest request = new StringRequest(Request.Method.GET, apiUrl,
                response -> {
                    try {
                        JSONArray usersArray = new JSONArray(response);
                        for (int i = 0; i < usersArray.length(); i++) {
                            JSONObject userObject = usersArray.getJSONObject(i);
                            String email = userObject.getString("email");
                            String name = userObject.getString("name");
                            String profilePicPath = userObject.getString("profilePicPath");
                            boolean hasUnreadMessages = userObject.getBoolean("has_unread_messages");
                            userList.add(new User(email, name, profilePicPath, hasUnreadMessages));
                        }
                        userAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        Log.e("ChatActivity", "JSON Parsing Error: " + e.getMessage());
                        Toast.makeText(ChatActivity.this, "Failed to fetch users", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("ChatActivity", "VolleyError: " + error.getMessage());
                    Toast.makeText(ChatActivity.this, "Failed to fetch users", Toast.LENGTH_SHORT).show();
                });

        queue.add(request);
    }

    // User class to hold user data for the list
    public static class User {
        String email;
        String name;
        String profilePicPath;
        boolean hasUnreadMessages;

        public User(String email, String name, String profilePicPath, boolean hasUnreadMessages) {
            this.email = email;
            this.name = name;
            this.profilePicPath = profilePicPath;
            this.hasUnreadMessages = hasUnreadMessages;
        }
    }

    // RecyclerView Adapter for the user list
    public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
        private Context context;
        private List<User> users;

        public UserAdapter(Context context, List<User> users) {
            this.context = context;
            this.users = users;
        }

        @Override
        public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.user_list_item, parent, false);
            return new UserViewHolder(view);
        }

        @Override
        public void onBindViewHolder(UserViewHolder holder, int position) {
            User user = users.get(position);
            holder.userNameTextView.setText(user.name);

            // Use Picasso to load the profile picture
            Picasso.get().load(user.profilePicPath)
                    .placeholder(R.drawable.default_profile)
                    .error(R.drawable.default_profile)
                    .into(holder.profilePicImageView);

            // Show/hide unread message indicator
            holder.unreadMessageIndicator.setVisibility(user.hasUnreadMessages ? View.VISIBLE : View.GONE);

            // Set click listener to open MessagingActivity
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, MessagingActivity.class);
                intent.putExtra("receiverEmail", user.email);
                intent.putExtra("receiverName", user.name);
                context.startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return users.size();
        }

        public class UserViewHolder extends RecyclerView.ViewHolder {
            ImageView profilePicImageView;
            TextView userNameTextView;
            TextView unreadMessageIndicator;

            public UserViewHolder(View itemView) {
                super(itemView);
                profilePicImageView = itemView.findViewById(R.id.profilePicImageView);
                userNameTextView = itemView.findViewById(R.id.userNameTextView);
                unreadMessageIndicator = itemView.findViewById(R.id.unreadMessageIndicator);
            }
        }
    }
}