package com.app.mahilakosh; // Make sure this matches your package name
import java.io.Serializable; // Import Serializable

public interface UnreadMessageListener {
    void onUnreadMessageCountChanged(String senderEmail, int unreadCount);
}