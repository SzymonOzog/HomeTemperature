package com.example.hometemperature;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;

public class MainActivity extends AppCompatActivity {

    TextView temperatureTV;
    Button checkTemperatureBtn;
    public static int port = 2390;
    public static String ipAddress = "192.168.0.13";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        temperatureTV = findViewById((R.id.temperatureTV));
        checkTemperatureBtn = findViewById((R.id.checkTempBtn));

        checkTemperatureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new ClientSend()).start();
                ClientListen listener = new ClientListen();
                new Thread(listener).start();
            }
        });
    }

    public void setTemperatureTV(String temperature) {
        temperatureTV.setText(temperature);
    }

    class ClientSend implements Runnable {
        @Override
        public void run() {
            try {
                DatagramSocket udpSocket = new DatagramSocket(null);
                udpSocket.setReuseAddress(true);
                udpSocket.bind(new InetSocketAddress(MainActivity.port));
                InetAddress serverAddress = InetAddress.getByName(MainActivity.ipAddress);
                byte[] buf =("GetTemperature").getBytes();
                DatagramPacket packet = new DatagramPacket(buf, buf.length, serverAddress, MainActivity.port);
                udpSocket.send(packet);
            } catch (SocketException e) {
                Log.e("Udp:", "Socket Error:", e);
            } catch (IOException e) {
                Log.e("Udp Send:", "IO Error:", e);
            }
        }
    }

    class ClientListen implements Runnable {

        String temperature;

        @Override
        public void run() {
            while (true) {
                try {
                    DatagramSocket udpSocket = new DatagramSocket(null);
                    udpSocket.setReuseAddress(true);
                    udpSocket.bind(new InetSocketAddress(port));
                    byte[] message = new byte[8000];
                    DatagramPacket packet = new DatagramPacket(message, message.length);
                    Log.i("UDP client: ", "about to wait to receive");
                    udpSocket.receive(packet);
                    temperature = new String(message, 0, packet.getLength());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            temperatureTV.setText(temperature);
                        }
                    });
                    Log.d("Received data", temperature);
                } catch (IOException e) {
                    Log.e("Client has IOException", "error: ", e);
                    break;
                }
            }
        }
    }
}