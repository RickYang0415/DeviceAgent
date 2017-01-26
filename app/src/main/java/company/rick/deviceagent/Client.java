package company.rick.deviceagent;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

/**
 * Created by rick.yang on 2017/1/10.
 */

enum Command {
    Connect,
    Connect_Success,
    Disconnect,
    Observe,
    Stop_Observe,
    Observing
}

interface UpdateCallBack {
    void UpdateUIText(String str);
}

public class Client implements Runnable {
    String m_ip;
    int m_port;
    DatagramSocket m_socket;
    DeviceInformation deviceInformation;
    ObserverDevice observerThread;
    Context m_contex;
    UpdateCallBack updateCallBack;
    private boolean m_stop;

    public Client(Context context, UpdateCallBack callBack) {
        m_contex = context;
        updateCallBack = callBack;
    }

    @Override
    public void run() {
        m_stop = false;
        try {
            m_socket = new DatagramSocket();
            InetAddress IPAddress = InetAddress.getByName(m_ip);
            deviceInformation = new DeviceInformation(m_contex);
            DeviceInfo deviceInfo = deviceInformation.GetDeviceInfo();
            String msg = String.format("%d|%s,%s", Command.Connect.ordinal(), deviceInfo.modelName, deviceInfo.serialNumber);
            DatagramPacket sendPacket = new DatagramPacket(msg.getBytes(), msg.getBytes().length, IPAddress, m_port);
            m_socket.send(sendPacket);
            ((Activity) m_contex).runOnUiThread(new UpdateUIRunnable("Connect to server ..."));
            while (!m_stop) {
                byte[] recbuf = new byte[512];
                DatagramPacket recpacket = null;
                try {
                    recpacket = new DatagramPacket(recbuf,
                            recbuf.length);
                    m_socket.setSoTimeout(3000);
                    m_socket.receive(recpacket);
                } catch (SocketTimeoutException ex) {
                    Log.d("LOG", ex.toString());
                    continue;
                }
                int length = recpacket.getLength();
                String rev = new String(recbuf, 0, length);
                String[] arg = rev.split("\\|", -1);
                if (Integer.valueOf(arg[0]) == Command.Observe.ordinal()) {
                    Log.d("LOG", "Start Observing");
                    observerThread = new ObserverDevice();
                    new Thread(observerThread).start();
                    ((Activity) m_contex).runOnUiThread(new UpdateUIRunnable("Server observe\r\nSending..."));
                } else if (Integer.valueOf(arg[0]) == Command.Stop_Observe.ordinal()) {
                    Log.d("LOG", "Stop Observing");
                    observerThread.SetStopFlag();
                    ((Activity) m_contex).runOnUiThread(new UpdateUIRunnable("Server stop observe"));
                } else if (Integer.valueOf(arg[0]) == Command.Connect_Success.ordinal()) {
                    ((Activity) m_contex).runOnUiThread(new UpdateUIRunnable("Connect Success"));
                }
            }
            ((Activity) m_contex).runOnUiThread(new UpdateUIRunnable("Disconnect..."));
            msg = String.format("%d|%s", Command.Disconnect.ordinal(), deviceInfo.serialNumber);
            sendPacket = new DatagramPacket(msg.getBytes(), msg.getBytes().length, IPAddress, m_port);
            m_socket.send(sendPacket);
        } catch (Exception ex) {
            Log.d("LOG", ex.toString());
            return;
        } finally {
            if (m_socket != null)
                m_socket.close();
        }
    }

    void Stop() {
        if (observerThread != null)
            observerThread.SetStopFlag();
        m_stop = true;
    }

    class UpdateUIRunnable implements Runnable {
        String msg = "";

        public UpdateUIRunnable(String str) {
            msg = str;
        }

        @Override
        public void run() {
            if (updateCallBack != null) {
                updateCallBack.UpdateUIText(msg);
            }
        }
    }

    class ObserverDevice implements Runnable {
        private boolean stopThread = false;

        @Override
        public void run() {
            while (!stopThread) {
                try {
                    DeviceInfo deviceInfo = deviceInformation.GetDeviceInfo();
                    String msg = String.format("%d|%s,%s,%s,%s,%s", Command.Observing.ordinal(), deviceInfo.serialNumber, deviceInfo.modelName, deviceInfo.cpu, deviceInfo.memory, deviceInfo.address);
                    Log.d("Send", msg);
                    InetAddress IPAddress = InetAddress.getByName(m_ip);
                    DatagramPacket sendPacket = new DatagramPacket(msg.getBytes(), msg.getBytes().length, IPAddress, m_port);
                    m_socket.send(sendPacket);
                    Thread.sleep(2000);
                } catch (Exception ex) {
                    Log.d("LOG", ex.toString());
                }
            }
            Log.d("LOG", "ObserverDevice stop");
        }

        void SetStopFlag() {
            stopThread = true;
        }
    }
}
