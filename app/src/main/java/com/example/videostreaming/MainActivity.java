package com.example.videostreaming;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    Button mDiscoverButton;
    ListView mListView;
    TextView mConnectionStatusText;
    WifiManager mWifiManger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialSetup();
        mDiscoverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkWifiState();
            }
        });
    }

    private void initialSetup() {
        mDiscoverButton = findViewById(R.id.btn_discover);
        mListView = findViewById(R.id.peer_listview);
        mConnectionStatusText = findViewById(R.id.text_connection_status);
        mWifiManger = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    private void checkWifiState() {
        if (!mWifiManger.isWifiEnabled()) {
            mWifiManger.setWifiEnabled(true);
            mConnectionStatusText.setText("Wi-Fi On!!!");
        }
    }
}
