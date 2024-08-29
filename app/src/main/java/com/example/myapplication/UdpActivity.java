package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

public class UdpActivity extends AppCompatActivity {
    private static final String TAG = "cai";

    private static final int UDP_PORT = 12345;
    private static final int TCP_PORT = 12346;
    private String serverIp;
    private Socket socket;
    private OutputStream outputStream;

    private TextView ipTextView;
    private EditText messageEditText;
    private Button sendButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_udp);

        ipTextView = findViewById(R.id.ipTextView);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);

        new Thread(this::discoverServer).start();

        sendButton.setOnClickListener(v -> sendMessage());
    }
    private void discoverServer() {
        try {
            DatagramSocket udpSocket = new DatagramSocket();
            udpSocket.setBroadcast(true);

            byte[] sendData = "DISCOVER_SERVER".getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
                    InetAddress.getByName("255.255.255.255"), UDP_PORT);
            udpSocket.send(sendPacket);

            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            udpSocket.receive(receivePacket);
            serverIp = new String(receivePacket.getData(), 0, receivePacket.getLength());

            runOnUiThread(() -> ipTextView.setText("Server IP: " + serverIp));

            udpSocket.close();

            socket = new Socket(serverIp, TCP_PORT);
            outputStream = socket.getOutputStream();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage() {
        if (socket != null && outputStream != null) {
            try {
                String message = messageEditText.getText().toString();
                outputStream.write(message.getBytes());
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}