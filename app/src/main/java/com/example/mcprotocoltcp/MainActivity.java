package com.example.mcprotocoltcp;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    private TextView textView;
    private Thread plcThread;
    private volatile boolean running = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.textView);
        TextView ipView = findViewById(R.id.IPView);

        WifiManager manager = (WifiManager)getSystemService(WIFI_SERVICE);
        WifiInfo info = manager.getConnectionInfo();
        int ipAdr = info.getIpAddress();
        String ip = String.format("IP Adrress : %02d.%02d.%02d.%02d", (ipAdr>>0)&0xff, (ipAdr>>8)&0xff, (ipAdr>>16)&0xff, (ipAdr>>24)&0xff);

        ipView.setText("IP Address: " + ip);

       plcThread = new Thread(new PLCCommunicationTask("192.168.0.200", 1024));
        // スレッド開始
        plcThread.start();
    }



    protected void onDestroy() {
        super.onDestroy();
        running = false;
        if (plcThread != null) {
            plcThread.interrupt();
        }
    }

    private class PLCCommunicationTask implements Runnable {
        private String plcAddress;
        private int plcPort;

        public PLCCommunicationTask(String address, int port) {
            plcAddress = address;
            plcPort = port;
        }

        @Override
        public void run() {
            try {
                ConnectionParameter cp = new ConnectionParameter();
                cp.remote_address=plcAddress;
                cp.local_address="127.0.0.1";
                cp.port=plcPort;
                cp.recieve_timeout=5000;
                cp.send_timeout=5000;
                cp.max_receive_length=256;
                MCProtocolBinary protocol = new MCProtocolBinary(TcpOrUdp.UDP, cp);

                if (protocol.connect()) {
                    // 接続成功
                    runOnUiThread(() -> textView.setText("Connected"));
                } else {
                    // 接続失敗
                    runOnUiThread(() -> textView.setText("Connection failed"));
                    return;
                }
                int readCount=10;

                while (running) {
                    // データ送信
                    int deviceType =MCProtocolBinary.DeviceType.D.ordinal();
                    int[] data=protocol.ReadData(deviceType, 0, readCount);
                    //textViewにint配列を表示
                    runOnUiThread(() -> textView.setText("Data: " + data[0] + ", " + data[1] + ", " + data[2] + ", " + data[3] + ", " + data[4] + ", " + data[5] + ", " + data[6] + ", " + data[7] + ", " + data[8] + ", " + data[9]));
                    // 一定時間待機 (例: 1秒)
                    Thread.sleep(1000);
                }
                protocol.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }




}