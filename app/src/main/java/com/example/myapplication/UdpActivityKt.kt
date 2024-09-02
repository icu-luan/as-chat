package com.example.myapplication

import android.os.Bundle
import android.view.Display
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.OutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.Socket

class UdpActivityKt : AppCompatActivity() {
    private var serverIp: String? = null
    private var socket: Socket? = null
    private var outputStream: OutputStream? = null

    private var ipTextView: TextView? = null
    private var messageEditText: EditText? = null
    private var sendButton: Button? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_udp)

        ipTextView = findViewById(R.id.ipTextView)
        messageEditText = findViewById(R.id.messageEditText)
        sendButton = findViewById(R.id.sendButton)

        //        new Thread(this::discoverServer).start();
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                discoverServer();
//            }
//        }).start();
        //一般这样
        Thread { discoverServer() }.start()

        sendButton?.setOnClickListener(View.OnClickListener { v: View? -> sendMessage() })
    }

    private fun discoverServer() {
        try {
            val udpSocket = DatagramSocket()
            udpSocket.broadcast = true

            val sendData = "DISCOVER_SERVER".toByteArray()
            val sendPacket = DatagramPacket(
                sendData, sendData.size,
                InetAddress.getByName("255.255.255.255"), UDP_PORT
            )
            udpSocket.send(sendPacket)

            val receiveData = ByteArray(1024)
            val receivePacket = DatagramPacket(receiveData, receiveData.size)
            udpSocket.receive(receivePacket)
            serverIp = String(receivePacket.data, 0, receivePacket.length)

            runOnUiThread { ipTextView!!.text = "Server IP: $serverIp" }

            udpSocket.close()

            socket = Socket(serverIp, TCP_PORT)
            outputStream = socket!!.getOutputStream()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun sendMessage() {

        if (socket != null && outputStream != null) {
            try {
                val message = messageEditText!!.text.toString()
                //outputStream 是网络流  write等同于向网络发送二进制数据
                //setOnClickListener 一般在主线程
                //参考把项目移动到 studio-2024 这个就是用2024打开的
//                outputStream!!.write(message.toByteArray())
//                outputStream!!.flush()
                //GlobalScope 默认是io线程等同于
                GlobalScope.launch(Dispatchers.IO) {
                    outputStream!!.write(message.toByteArray())
                    outputStream!!.flush()
                    //回调主线程
                    withContext(Dispatchers.Main){
//                        showToast("消息已发送")
                    }
                }
                //或者
                GlobalScope.launch() {
                    withContext(Dispatchers.IO){
                        outputStream!!.write(message.toByteArray())
                        outputStream!!.flush()
                    }
                }
                //或者
                lifecycleScope.launch() {
                    withContext(Dispatchers.IO){
                        outputStream!!.write(message.toByteArray())
                        outputStream!!.flush()
                    }
                }

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun send(message: String) {
    }

    companion object {
        private const val TAG = "cai"

        private const val UDP_PORT = 12345
        private const val TCP_PORT = 12346
    }
}