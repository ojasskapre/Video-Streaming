package com.example.videostreaming;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.Toast;

public class WifiDirectBroadcastReceiver extends BroadcastReceiver {
    private WifiP2pManager mWifiP2pManager;
    private WifiP2pManager.Channel mChannel;
    private MainActivity mainActivity;

    public WifiDirectBroadcastReceiver(WifiP2pManager mWifiP2pManager, WifiP2pManager.Channel mChannel, MainActivity mainActivity) {
        this.mWifiP2pManager = mWifiP2pManager;
        this.mChannel = mChannel;
        this.mainActivity = mainActivity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                Toast.makeText(context, "Wifi P2P is On", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Wifi P2p is Off", Toast.LENGTH_SHORT).show();
            }
        }else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            if (mWifiP2pManager != null) {
                mWifiP2pManager.requestPeers(mChannel, mainActivity.peerListListener);
            }
        }else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            if (mWifiP2pManager == null) {
                return;
            }

            NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if (networkInfo != null && networkInfo.isConnected()) {
                mWifiP2pManager.requestConnectionInfo(mChannel, mainActivity.connectionInfoListener);
            } else {
                mainActivity.mConnectionStatusText.setText("Device Disconnected");
            }
        }else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {

        }
    }
}
