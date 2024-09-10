package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.myapplication.databinding.ActivityChatClientBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

class ChatClientActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatClientBinding
    private lateinit var socket: Socket
    private lateinit var writer: PrintWriter
    private lateinit var reader: BufferedReader
    //初学者不要用奇技淫巧 老老实实写onClickListener 不要套用variable
    var message: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat_client)
        binding.client = this
        connectToServer()
    }
    private fun connectToServer() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                socket = Socket("192.168.0.101", 8899) // 替换为服务端实际IP

                writer = PrintWriter(socket.getOutputStream(), true)
                reader = BufferedReader(InputStreamReader(socket.getInputStream()))
                withContext(Dispatchers.Main) {
                    binding.statusTextView.text = "Connected to server"
                    Toast.makeText(this@ChatClientActivity, "Connected to server", Toast.LENGTH_SHORT).show()
                }
                listenForMessages()

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ChatClientActivity, "Failed to connect to server", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun listenForMessages() {
        withContext(Dispatchers.IO) {
            try {
                val inputStream = socket.getInputStream()
                val buffer = StringBuilder()
                val byteBuffer = ByteArray(1024)
                var bytesRead: Int

                while (true) {
                    bytesRead = inputStream.read(byteBuffer)
                    if (bytesRead == -1) break // Stream end
                    // 将读取的字节转换为字符串并添加到缓冲区中
                    buffer.append(String(byteBuffer, 0, bytesRead, Charsets.UTF_8))

                    while (true) {
                        val message = buffer.toString()

                        if (message.startsWith("MSG:")) {
                            // 处理文本消息
                            val endOfMessage = message.indexOf('\n')
                            if (endOfMessage != -1) {
                                val textMessage = message.substring(4, endOfMessage).trim()
                                updateMessages("Server: $textMessage")
                                buffer.delete(0, endOfMessage + 1) // 移除已处理的消息
                            } else {
                                break // 消息不完整，继续读取
                            }
                        } else if (message.startsWith("IMG:")) {
                            // 处理图片消息
                            val imgHeaderEnd = message.indexOf('\n')
                            if (imgHeaderEnd != -1) {
                                val imageSizeLine = message.substring(4, imgHeaderEnd).trim()
                                val imageSize = imageSizeLine.toInt()

                                // 确认是否收到了完整的图片数据
                                if (message.length >= imgHeaderEnd + 1 + imageSize) {
                                    val imageBytes = message.substring(imgHeaderEnd + 1, imgHeaderEnd + 1 + imageSize).toByteArray()
                                    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageSize)
                                    updateImage(bitmap)
                                    buffer.delete(0, imgHeaderEnd + 1 + imageSize) // 移除已处理的图片数据
                                } else {
                                    break // 图片数据不完整，继续读取
                                }
                            } else {
                                break // 图片头部不完整，继续读取
                            }
                        } else {
                            break // 数据格式不匹配，继续读取
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    //在协程内部 也就是suspend方法内 withContext是一定顺序执行的
    //线程开启是无序的
    fun onSendClick() {
        CoroutineScope(Dispatchers.IO).launch {
        val msg = binding.messageEditText.text.toString()
        if (msg.isNotBlank()) {
            //这个是协程 你看上去像是线性运行的 其实类似于thread
            //这里不是协程吗
            //1.打开协程
                //你在这里开了个线程啊
            //没事了，就是相当于开线程就是无序 对的

            //--------------------------
                //3. 再走这里发送 ok了
                writer.println(msg)
                updateMessages("客户端: $msg")
            }
            //2. 先走这里 message置空

            message = ""
            binding.messageEditText.text.clear()
        }
    }
    fun onSendImageClick() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        this.startActivityForResult(intent, 100)
    }


    private  fun updateMessages(message: String) {
        CoroutineScope(Dispatchers.Main).launch{
            binding.messagesTextView.append("$message\n")
        }
    }
    private fun updateImage(bitmap: Bitmap) {
        CoroutineScope(Dispatchers.Main).launch{
            binding.receivedImageView.setImageBitmap(bitmap)
        }
    }
    private fun sendImageToServer(imagePath: String) {
        val imageFile = File(imagePath)
        val imageSize = imageFile.length().toInt()
        val imageBytes = ByteArray(imageSize)
        FileInputStream(imageFile).use { it.read(imageBytes) }

        CoroutineScope(Dispatchers.IO).launch {
            writer.println("IMG:")
            writer.println(imageSize)
            socket.getOutputStream().write(imageBytes)
            socket.getOutputStream().flush()
        }
        updateMessages("Client: Image sent to server")
    }


    private fun showToast(message: String) {
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(this@ChatClientActivity, message, Toast.LENGTH_SHORT).show()
        }
    }
    override  fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            val uri = data?.data
            if (uri != null) {
                val path = Utils.getPath(this, uri)
                if (path != null) {
                    this.sendImageToServer(path)
                }
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        CoroutineScope(Dispatchers.IO).launch {
            socket.close()
        }
    }
}