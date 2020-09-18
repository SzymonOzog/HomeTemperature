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
import java.net.SocketException;

public class MainActivity extends AppCompatActivity {

    TextView temperatureTV;
    Button checkTemperatureBtn;
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
            }
        });
    }
}

class ClientSend implements Runnable{
    @Override
    public void run(){
        try {
            DatagramSocket udpSocket = new DatagramSocket(2390);
            InetAddress serverAddress = InetAddress.getByName("192.168.0.13");
            byte[] buf = ("The String to Send").getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length,serverAddress, 2390);
            udpSocket.send(packet);
        } catch (SocketException e) {
            Log.e("Udp:", "Socket Error:", e);
        } catch (IOException e) {
            Log.e("Udp Send:", "IO Error:", e);
        }
    }
}