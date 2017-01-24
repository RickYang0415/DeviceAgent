package company.rick.deviceagent;

import android.app.ActivityManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.List;

import static android.content.ContentValues.TAG;
import static android.content.Context.ACTIVITY_SERVICE;
import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.content.Context.WIFI_SERVICE;

/**
 * Created by rick.yang on 2017/1/3.
 */

public class DeviceInformation {
    Context m_context;
    //DeviceInfo deviceInfo = new DeviceInfo();

    public DeviceInformation(Context context) {
        this.m_context = context;
    }

    DeviceInfo GetDeviceInfo() {
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.serialNumber = Build.SERIAL;
        deviceInfo.modelName = Build.MODEL;
        deviceInfo.memory = getMemoryInfo();
        deviceInfo.address = GetDeviceConnectedAddress();
        deviceInfo.cpu = getCpuInfo();
        return deviceInfo;
    }

    String GetDeviceConnectedAddress() {
        ConnectivityManager manager = (ConnectivityManager) m_context.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
        if (activeNetwork != null) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                return GetWifiConnectedAddr();
            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                return GetCellularAddr();
            }
        }
        return "";
    }

    String GetWifiConnectedAddr() {
        String ipAddress = "";
        try {
            WifiManager wifiMgr = (WifiManager) m_context.getSystemService(WIFI_SERVICE);
            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
            int ip = wifiInfo.getIpAddress();
            ipAddress = String.format("%d.%d.%d.%d", (ip & 0xff), (ip >> 8 & 0xff), (ip >> 16 & 0xff), (ip >> 24 & 0xff));
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
        return ipAddress;
    }

    String GetCellularAddr() {
        String ipAddr = "";
        try {
            List<NetworkInterface> networkInterfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface networkInterface : networkInterfaces) {
                List<InetAddress> inetAddresses = Collections.list(networkInterface.getInetAddresses());
                for (InetAddress inetAddress : inetAddresses) {
                    if (!inetAddress.isLoopbackAddress()) {
                        String addr = inetAddress.getHostAddress().toUpperCase();
                        ipAddr = addr;
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e(TAG, ex.toString());
        }
        return ipAddr;
    }

    public String getMemoryInfo() {
        try {
            ActivityManager actManager = (ActivityManager) m_context.getSystemService(ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
            actManager.getMemoryInfo(memInfo);
            //deviceInfo.memory = String.valueOf(memInfo.availMem);
            return String.valueOf(memInfo.availMem);
        } catch (Exception e) {
            Log.e(TAG, "GetMemoryInfo " + e.getMessage());
            return "";
        }
    }

    public String getCpuInfo() {
        String cpu = "";
        String[] cpuInfos = null;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream("/proc/stat")), 1000);
            String load = reader.readLine();
            reader.close();
            cpuInfos = load.split(" ");
        } catch (IOException ex) {
            Log.e(TAG, ex.getMessage());
            return "";
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception ex) {
                    Log.e(TAG, ex.getMessage());
                }
            }
        }
        long totalCpu = Long.parseLong(cpuInfos[2])
                + Long.parseLong(cpuInfos[3]) + Long.parseLong(cpuInfos[4])
                + Long.parseLong(cpuInfos[6]) + Long.parseLong(cpuInfos[5])
                + Long.parseLong(cpuInfos[7]) + Long.parseLong(cpuInfos[8]);
        return String.valueOf(totalCpu);
    }

    private static String getStringFromInputStream(InputStream is) {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line = null;
        try {
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
        } catch (IOException e) {
            Log.e(TAG, "------ getStringFromInputStream " + e.getMessage());
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    Log.e(TAG, "------ getStringFromInputStream " + e.getMessage());
                }
            }
        }
        return sb.toString();
    }
}

class DeviceInfo {
    String serialNumber;
    String modelName;
    String address;
    String cpu;
    String memory;
}
