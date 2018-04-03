package com.hti.graduationproject.wificalling.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Log;

import com.hti.graduationproject.wificalling.utils.Constants;
import com.hti.graduationproject.wificalling.utils.NetworkUtils;

import java.util.List;

public class WifiNetworkReceiver extends BroadcastReceiver {

    private WifiManager wifiManager;
    Runnable runnable = () -> wifiManager.startScan();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (this.wifiManager == null)
            wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        assert wifiManager != null;
        List<ScanResult> wifiScanList = wifiManager.getScanResults();
        for (ScanResult item : wifiScanList) {
            Log.i(Constants.TAG, "New wifi in region it's ssid is: " + item.SSID);


            if (Constants.getCurrentWifiSSID() != null && !Constants.getCurrentWifiSSID().equals(item.SSID)
                    && Constants.getCurrentWifiLevel() < item.level
                    && item.SSID.contains(Constants.getWifiSSIDParam())) {
                NetworkUtils.addNetwork(context, item.SSID);
            }

        }
        new Handler().postDelayed(runnable, 60000);
    }
}
