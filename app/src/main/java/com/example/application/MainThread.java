package com.example.application;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;


public class MainThread extends Thread {

    public final MainActivity activity;
    private final BluetoothDevice blueDevice;
    private BluetoothSocket blueServiceSocket;
    private char toSend = 'p';

    public MainThread(MainActivity activity, BluetoothDevice bt) {
        this.blueDevice = bt;
        this.activity = activity;
    }

    public synchronized void changeToSend(char c) {
        this.toSend = c;
    }

    private void cancel() {
        try {
            if (this.blueServiceSocket != null) {
                this.blueServiceSocket.close();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void run() {
        BluetoothSocket bts = null;
        try {
            System.out.println("attente socket de service");
            bts = this.blueDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        System.out.println("connexion...");
        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
        try {
            bts.connect();
            this.activity.findViewById(R.id.spinner).post(new Runnable() {
                public void run() {
                    MainThread.this.activity.enableButtons(true);
                    MainThread.this.activity.findViewById(R.id.monboutonoff).setEnabled(true);
                }
            });
            System.out.println("ok");
            this.blueServiceSocket = bts;
            manageConnectedSocket();
        } catch (IOException ioe2) {
            ioe2.printStackTrace();
            System.out.println("unable to connect");
            this.activity.findViewById(R.id.spinner).post(new Runnable() {
                public void run() {
                    Toast.makeText(MainThread.this.activity, "unable to connect", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void manageConnectedSocket() {
        char localToSend;
        while (!Thread.currentThread().isInterrupted()) {
            synchronized (this) {
                localToSend = this.toSend;
            }
            try {
                this.blueServiceSocket.getOutputStream().write(localToSend);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ioe) {
                    ioe.printStackTrace();
                }
            } catch (IOException ioe2) {
                ioe2.printStackTrace();
            }
        }
        cancel();
    }
}