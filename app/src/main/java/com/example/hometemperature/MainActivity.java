package com.example.hometemperature;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;

public class MainActivity extends AppCompatActivity {

    EditText portET, ipET;
    TextView replyTV;
    Button sendData;
    public static int port = 8888;
    public static String ipAddress = "192.168.0.13";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        portET = findViewById((R.id.server_port));
        ipET = findViewById((R.id.server_ip));
        portET.setText("Enter port");
        ipET.setText("Enter IP");
        replyTV = findViewById((R.id.Reply));
        sendData = findViewById((R.id.SendData));
        sendData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int port = Integer.parseInt(portET.getText().toString());
                String IP = ipET.getText().toString();
                new Thread(new ClientSend("Check Temperature", port, IP)).start();
                ClientListen listener = new ClientListen(port);
                new Thread(listener).start();
            }
        });
    }

    class ClientSend implements Runnable {
        String textToSend = "";
        int port = 0;
        String IP = "";
        ClientSend(String _textToSend, int _port, String _IP){
            textToSend = _textToSend;
            port = _port;
            IP = _IP;
        }
        @Override
        public void run() {
            try {
                DatagramSocket udpSocket = new DatagramSocket(null);
                udpSocket.setReuseAddress(true);
                udpSocket.bind(new InetSocketAddress(port));
                InetAddress serverAddress = InetAddress.getByName(IP);
                byte[] buf = textToSend.getBytes();
                DatagramPacket packet = new DatagramPacket(buf, buf.length, serverAddress, MainActivity.port);
                udpSocket.send(packet);
                Log.d("UDP", "Sent data");
            } catch (SocketException e) {
                Log.e("Udp:", "Socket Error:", e);
            } catch (IOException e) {
                Log.e("Udp Send:", "IO Error:", e);
            }
        }
    }

    class ClientListen implements Runnable {

        String temperature;
        int port;
        ClientListen(int _port) {
            port = _port;
        }
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
                            replyTV.setText(temperature);
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