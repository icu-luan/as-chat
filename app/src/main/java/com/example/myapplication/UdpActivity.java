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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
    private InputStream inputStream;

    private TextView ipTextView;
    private TextView messageView;
    private EditText messageEditText;
    private Button sendButton;
    private StringBuilder response;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_udp);

        ipTextView = findViewById(R.id.ipTextView);
        messageView = findViewById(R.id.messageView);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);

//        new Thread(this::discoverServer).start();
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                discoverServer();
//            }
//        }).start();
        //一般这样
        new Thread(() -> discoverServer()).start();

        sendButton.setOnClickListener(v -> sendMessage());

    }

    private void getMessages() {
        Log.d(TAG, "接收服务端数据 " + "socket:"+socket + "inputStream:"+inputStream);
        if (socket != null && inputStream != null) {

            // 读取数据
            response = new StringBuilder();
            
                // 获取输入流
                BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
                String serverMessage;
                try {
                    serverMessage = in.readLine();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                Log.d(TAG, "获取到服务端数据 "+ serverMessage);
                response.append(serverMessage).append("\n");
                runOnUiThread(() -> appendMessage("Client Message: " + response));

        }
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
            inputStream = socket.getInputStream();
            new Thread(() -> getMessages()).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage() {
        if (socket != null && outputStream != null) {
            String message = messageEditText.getText().toString();
            //outputStream 是网络流  write等同于向网络发送二进制数据
            //setOnClickListener 一般在主线程
            //参考把项目移动到 studio-2024 这个就是用2024打开的
            new Thread(() -> send(message)).start();
//                outputStream.write(message.getBytes());
//                outputStream.flush();
            appendMessage("Client Message: " + message);
        }
    }
    private void send(String message){
        try {
            outputStream.write(message.getBytes());
            outputStream.flush();
//            runOnUiThread(() -> appendMessage("Client Message: " + message));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    // Append message to TextView
    private void appendMessage(String message) {
        messageView.append(message + "\n");
    }
}