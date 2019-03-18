package com.example.videostreaming;

import android.content.Intent;
import android.database.Cursor;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;

public class FileSelectActivity extends AppCompatActivity {
    Button btnSend, btnReceive;
    OutputStream outputStream;
    private String file_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_select);
        btnSend = findViewById(R.id.send);
        btnReceive = findViewById(R.id.receive);
        
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Streaming file", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                startActivityForResult(intent, 7);
            }
        });

        btnReceive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), StreamingActivity.class));
            }
        });

        Socket socket = SocketHandler.getSocket();

        try {
            outputStream = socket.getOutputStream();
            Log.e("OUTPUT_SOCKET", "SUCCESS");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case 7:
                if (resultCode == RESULT_OK) {
                    final String[] split = data.getData().getPath().split(":");//split the path.
                    file_name = split[1];
                    Log.e("FiLEPATHkjhhhhhh", file_name );
                    Toast.makeText(FileSelectActivity.this, file_name, Toast.LENGTH_LONG).show();
                    sendVideoFile();
                }
                break;
        }
    }

    private void sendVideoFile() {
//        String internal_storage_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        String full_file_path = file_name;
        Log.e("FILE_PATH", full_file_path);
        File file = new File(full_file_path);
        int size = (int) file.length();
        final byte[] bytes = new byte[size];
        file_name = "";
        try {
            Log.e("FILE_READ", "File read start");
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, 0, bytes.length);
            buf.close();

            Log.e("FILE_READ", "File read complete");

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        int off = 0, len = 1024000;
                        while (off < bytes.length) {
                            outputStream.write(bytes, off, Math.min(len, (bytes.length - off)));
                            outputStream.flush();
                            off += len;
                            Log.e("FILE_READ", "Output stream writing");
                        }
                        Log.e("FILE_READ", "Output stream write complete");
                    } catch (IOException e) {
                        Log.e("FILE_READ", e.toString() );
                        e.printStackTrace();
                    } catch (Exception e){
                        Log.e("FILE_READ", e.toString() );
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
        } catch (FileNotFoundException e) {
            Log.e("FILE_READ", e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("FILE_READ", e.getMessage());
            e.printStackTrace();
        } catch (Exception e){
            Log.e("FILE_READ", e.getMessage());
            e.printStackTrace();
        }
    }
}
