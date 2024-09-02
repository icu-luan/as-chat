package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.Socket;

public class SocketActivity extends AppCompatActivity {
    private static final String TAG = "cai";
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_socket);
        imageView = findViewById(R.id.imageView);

        Log.d(TAG, "SocketActivity1 ");
        new ReceiveImageTask().execute();
    }

    //AsyncTask 现在没人用了

    private class ReceiveImageTask extends AsyncTask<Void, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(Void... voids) {
            Bitmap bitmap = null;
            try {
                // 连接到服务器
                Socket socket = new Socket("192.168.1.4", 8765);
                Log.d(TAG, "连接成功 ");
                // 获取输入流
                InputStream inputStream = socket.getInputStream();

                // 读取图片数据
                bitmap = BitmapFactory.decodeStream(inputStream);

                // 保存图片到本地
//                saveImageToExternalStorage(bitmap);

                // 保存图片到相册
                saveImageToGallery(bitmap);

                // 关闭连接
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            // 显示图片
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            }
        }
        private void saveImageToGallery(Bitmap bitmap) {
            if (bitmap == null) return;

            // 获取 ContentResolver
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, "received_image.png");
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/MyApp");

            // 插入到 MediaStore
            Context context = getApplicationContext();
            try (FileOutputStream fos = (FileOutputStream) context.getContentResolver().openOutputStream(
                    context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values))) {
                if (fos != null) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void saveImageToExternalStorage(Bitmap bitmap) {
            if (bitmap == null) return;

            FileOutputStream fos = null;
            try {
                // 获取外部存储目录
                File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyApp");
                if (!directory.exists()) {
                    directory.mkdirs();
                }

                // 创建文件
                File file = new File(directory, "received_image.png");
                fos = new FileOutputStream(file);

                // 将图片写入文件
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);

                //图片保存成功
                Log.d(TAG, "图片保存成功");

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}