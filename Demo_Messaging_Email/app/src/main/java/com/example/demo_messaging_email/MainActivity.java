package com.example.demo_messaging_email;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

public class MainActivity extends AppCompatActivity {
    private TextView txtInfo, txtIPInfo, txtMsg;
    private TextView test;
    private ServerSocket serverSocket;
    private String msg = "";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Socket Demo
        txtInfo = findViewById(R.id.txtInfo);
        txtIPInfo = findViewById(R.id.txtInfoIP);
        txtMsg = findViewById(R.id.txtMsg);

        test = findViewById(R.id.txtTest);




        txtIPInfo.setText(getIpAddress());
        Thread socketServerThread = new Thread(new SocketServerThread());
        socketServerThread.start();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class SocketServerReplyThread extends Thread{
        private Socket hostThreadSocket;
        int cnt;


        public SocketServerReplyThread(Socket hostThreadSocket, int cnt) {
            this.hostThreadSocket = hostThreadSocket;
            this.cnt = cnt;
        }

        @Override
        public void run() {
            OutputStream os;
            String msReply = "Networking, # " + cnt;
            try {
                os = hostThreadSocket.getOutputStream();
                PrintStream ps = new PrintStream(os);
                ps.println(msReply);
                ps.close();

                msg += "Replayed: " + msReply + "\n";
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        txtMsg.setText(msg);
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
                msg += "Something is wrong! " + e.toString() + "\n";
            }
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    txtMsg.setText(msg);
                }
            });
        }
    }

    private class SocketServerThread extends Thread {
        private static final int SocketServerPort = 8080;
        private int count = 0;

        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(SocketServerPort);
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        txtInfo.setText("Waiting Port: " + serverSocket.getLocalPort());

                    }
                });
                while (true) {
                    Socket socket = serverSocket.accept();
                    count++;
                    msg +="#" + count + "from " + socket.getInetAddress() + ":" + socket.getPort() + "\n";
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            txtMsg.setText(msg);
                        }
                    });
                    SocketServerReplyThread replyThread = new SocketServerReplyThread(socket, count);
                    replyThread.run();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getIpAddress(){
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNWInterface = NetworkInterface.getNetworkInterfaces();
            while (enumNWInterface.hasMoreElements()) {
                NetworkInterface nwInterface = enumNWInterface.nextElement();
//                test.setText(nwInterface.toString() + "\n");

                Enumeration<InetAddress> enumInetAddr = nwInterface.getInetAddresses();
//                test.setText(enumInetAddr.toString());
                while (enumInetAddr.hasMoreElements()) {
                    InetAddress inet = enumInetAddr.nextElement();
                    if(inet.isSiteLocalAddress()) {
                        ip += "Site Local Address " + inet.getHostAddress() + "\n";
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
            ip += "Something is wrong! " + e.toString() + "\n";
        }
        return ip;
    }














    //Email
    private void sendEmail (String[] address, String[] cc, String subject, String msg) {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.putExtra(Intent.EXTRA_EMAIL, address);
        emailIntent.putExtra(Intent.EXTRA_CC, cc);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, msg);
        emailIntent.setType("message/rfc822");
        startActivity(Intent.createChooser(emailIntent, "Email"));

    }

    public void clickToSendEmail(View view) {
        String[] to = {"newconan1995@gmail.com"};
        String[] cc = {"khoapcse141096@fpt.edu.vn"};
        sendEmail(to,cc, "Email From Android", "Test of Email Msg \n");
    }




}