package com.example.nadavspitzer.imageserviceapplication;

import android.os.StrictMode;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class Communication implements ICommunication {
    // members
    private final int port;
    private Socket socket;
    private InetAddress serverAddr;
    private final String ip = "10.0.2.2";

    // constructor
    public Communication() {
        this.port = 8002;
        try {
            // set to computer I.P
            this.serverAddr = InetAddress.getByName(this.ip);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void Start() {
        try {
            // create a socket for communication
            this.socket = new Socket(this.serverAddr, this.port);
            if (this.socket == null) {
                Log.e("TCP", "Socket is null");
            } else {
                Log.e("TCP", "Socket is alright");
            }
        } catch (Exception e) {
            Log.e("TCP", "C: Error! Could not open ip or create socket on start", e);
        }
    }

    @Override
    public void Stop() {
        try {
            if(this.socket != null){
                this.socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void Send(byte[] pic) {
        OutputStream outputStream = null;
        try {
            if(this.socket != null) {
                outputStream = this.socket.getOutputStream();
                outputStream.write(pic, 0, pic.length);
                outputStream.flush();
            }
        } catch (Exception e) {
            Log.e("TCP", "S: Error! Could not start connection or connection was aborted", e);
        }
    }
}
