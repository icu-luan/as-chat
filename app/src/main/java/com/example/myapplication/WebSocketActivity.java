package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class WebSocketActivity extends AppCompatActivity {

    private OkHttpClient client;
    private WebSocket webSocket;
    private EditText inputMessage;
    private Button sendButton;
    private TextView messagesView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_socket);
        // Initialize UI elements
        inputMessage = findViewById(R.id.input_message);
        sendButton = findViewById(R.id.send_button);
        messagesView = findViewById(R.id.messages_view);

        // Initialize OkHttpClient
        client = new OkHttpClient();

        // Create WebSocket request
        Request request = new Request.Builder()
                .url("ws://192.168.26.140:8765")
                .build();

        // Create WebSocket listener
        WebSocketListener listener = new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                WebSocketActivity.this.webSocket = webSocket;
                runOnUiThread(() -> {
                    appendMessage("WebSocket connected");
                });
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                runOnUiThread(() -> {
                    appendMessage("Received: " + text);
                });
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                runOnUiThread(() -> {
                    appendMessage("Error: " + t.getMessage());
                });
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                runOnUiThread(() -> {
                    appendMessage("WebSocket closing: " + reason);
                });
                webSocket.close(code, reason);
            }
        };

        // Connect to WebSocket server
        webSocket = client.newWebSocket(request, listener);

        // Set up send button click listener
        sendButton.setOnClickListener(v -> {
            String message = inputMessage.getText().toString();
            if (!message.isEmpty() && webSocket != null) {
                webSocket.send(message);
                appendMessage("Sent: " + message);
                inputMessage.setText(""); // Clear input field
            }
        });
    }

    // Append message to TextView
    private void appendMessage(String message) {
        messagesView.append(message + "\n");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webSocket != null) {
            webSocket.close(1000, "Activity destroyed");
        }
    }
}