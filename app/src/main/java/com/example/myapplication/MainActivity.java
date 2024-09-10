package com.example.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "cai";
    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "MainActivity onCreate");
        findViewById(R.id.btn_web_socket).setOnClickListener(this);
        findViewById(R.id.btn_socket).setOnClickListener(this);
        findViewById(R.id.btn_udp).setOnClickListener(this);
        findViewById(R.id.btn_client).setOnClickListener(this);

        // 请求权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        } else {
            // 权限已授予
//            new ReceiveImageTask().execute();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // 权限已授予
//            new ReceiveImageTask().execute();
        }
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent();
        switch (view.getId()) {
            case R.id.btn_web_socket:
                intent.setClass(MainActivity.this, WebSocketActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_socket:
                intent.setClass(MainActivity.this, SocketActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_udp:
                intent.setClass(MainActivity.this, UdpActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_client:
                intent.setClass(MainActivity.this, ChatClientActivity.class);
                startActivity(intent);
                break;
            default:


        }
    }
}