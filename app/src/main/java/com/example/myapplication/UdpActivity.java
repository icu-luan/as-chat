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

    private void getMessages(InputStream inputStream) throws IOException {
        Log.d(TAG, "接收服务端数据 " + "socket:"+socket + "inputStream:"+inputStream);
        if (socket != null && inputStream != null) {
            Log.d(TAG, "数据不为空");
            // 读取数据
            response = new StringBuilder();
                //........................................ readLine
                // 获取输入流
            byte[] data2 = new byte[1024];
            //这里就可以直接读取了
            inputStream.read(data2);
            //比如：
            if (data2[0] == 0x01){
                //表示这个是个String
                //下一位表示长度
                int len = data2[1];
                //那么结果就是：
                //data[第一位是协议啊0x01，第二位是长度啊,0xaa,内容。。。。。。。。。。。]
                //所以字符串要从第二位开始解析啊，这个协议我怎么确定，固定吗
                //协议 就是你和大金硬件部门讨论确定的啊，我指的是我现在从，socket收到的数据
                //下班了 听不懂话你说啥没用的死丫头
                //活生生蠢死
                //明天用Android 改写服务端
                //简单点 不要要UDP寻址了退下吧
                //我要吃饭了蠢死
                String stringx = new String(data2,2,len);
            }else if (data2[0] == 0x02){
                //表示这个是个图
            }


                BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
                String serverMessage;
                char[] data = new char[1024];
                //ok了么？readline不行吗
                //我和你说了多少次了。。。我说的话记一下
                //socket 传输数据本质上是传输 char[] or byte[] 一个意思
                //readline 重点是line 意味着一个消息以'\n'作为分界线
                //服务端发送的消息 没有\n
                //意味着 客户端以为服务端没法送完，在等一个\n作为结尾
                //大姐，这和我记住上面的话一点关系都没有
                //socket 作为底层数据传送 传输的不是string 因为会有编码问题各种问题
                //socket再任何平台都用int = read（参数是内存块），int是读取到的数据长度

            //这段代码你整理下吧 我下了
            //累死，退下吧
                while (true){
                    int len = in.read(data);
                    String string = new String(data,0,len);
                    Log.d(TAG, "获取到服务端数据 "+ string);
//                    response.append(serverMessage).append("\n");
                    runOnUiThread(() -> appendMessage("Server Message: " + string));
                }
//                try {
//                    Log.d(TAG, "in:" + in);
//                    serverMessage = in.readLine();
//                    Log.d(TAG, "serverMessage");
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    Log.e(TAG, "IOException:"+e);
//
//                    throw new RuntimeException(e);
//                }
//                Log.d(TAG, "获取到服务端数据 "+ serverMessage);
//                response.append(serverMessage).append("\n");
//                runOnUiThread(() -> appendMessage("Server Message: " + response));

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

            //这里是UDP结束
            udpSocket.close();
            //TCP开始
            socket = new Socket(serverIp, TCP_PORT);
            outputStream = socket.getOutputStream();
            InputStream inputStream = socket.getInputStream();
            getMessages(inputStream);
//            new Thread(() -> getMessages(inputStream)).start();
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