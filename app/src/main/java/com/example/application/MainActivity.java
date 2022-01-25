package com.example.application;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.io.IOException;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static char direction = 'p';
    private Button button1;
    private Button button2;
    private Button button3;
    private Button button4;
    private Button buttonStart;
    private Button buttonOff;
    private Spinner spinner1;
    private Thread mainThread;
    private HashMap spinnerMap = new HashMap<String, BluetoothDevice>();
    private BluetoothDevice bt;

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        if (position != 0) {
            String name = parent.getItemAtPosition(position).toString();
            this.bt = (BluetoothDevice) this.spinnerMap.get(name);
            if (this.mainThread != null) {
                this.mainThread.interrupt();
            }
            this.mainThread = new Thread(mainRunnable);
            this.buttonStart.setEnabled(true);
            this.buttonOff.setEnabled(false);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    Runnable mainRunnable = new Runnable() {
        @Override
        public void run() {
            BluetoothSocket bs1 = null;
            try {
                bs1 = bt.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                bs1.connect();
                while (!Thread.currentThread().isInterrupted()) {
                    if (direction != 'p') {
                        bs1.getOutputStream().write(direction);
                    }
                    Thread.sleep(100);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.button1 = findViewById(R.id.monbouton);
        this.button2 = findViewById(R.id.monbouton2);
        this.button3 = findViewById(R.id.monbouton3);
        this.button4 = findViewById(R.id.monbouton4);
        this.buttonStart = findViewById(R.id.monboutonstart);
        this.buttonOff = findViewById(R.id.monboutonoff);

        this.button1.setText(R.string.Button_1);
        this.button2.setText(R.string.Button_2);
        this.button3.setText(R.string.Button_3);
        this.button4.setText(R.string.Button_4);
        this.buttonStart.setText(R.string.Button_start);
        this.buttonOff.setText(R.string.Button_off);

        this.enableButtons(false);
        this.buttonStart.setEnabled(false);
        this.buttonOff.setEnabled(false);

        this.buttonOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.mainThread.interrupt();
                MainActivity.this.buttonStart.setEnabled(true);
                MainActivity.this.buttonOff.setEnabled(false);
                MainActivity.this.enableButtons(false);
                spinner1.setEnabled(true);
            }
        });

        this.buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainThread.start();
                spinner1.setEnabled(false);
                MainActivity.this.buttonStart.setEnabled(false);
                MainActivity.this.buttonOff.setEnabled(true);
                MainActivity.this.startClient();
                MainActivity.this.enableButtons(true);
            }
        });

        this.button1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        MainActivity.direction = 'u';
                        return true;
                    case MotionEvent.ACTION_UP:
                        MainActivity.direction = 'p';
                        break;
                }
                return true;
            }
        });

        this.button2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        MainActivity.direction = 'l';
                        return true;
                    case MotionEvent.ACTION_UP:
                        MainActivity.direction = 'p';
                        break;
                }
                return true;
            }
        });

        this.button3.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        MainActivity.direction = 'r';
                        return true;
                    case MotionEvent.ACTION_UP:
                        MainActivity.direction = 'p';
                        break;
                }
                return true;
            }
        });

        this.button4.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        MainActivity.direction = 'd';
                        return true;
                    case MotionEvent.ACTION_UP:
                        MainActivity.direction = 'p';
                        break;
                }
                return true;
            }
        });
        this.startClient();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != 0) {
            return;
        }
        if (resultCode != -1) {
            startActivityForResult(new Intent("android.bluetooth.adapter.action.REQUEST_ENABLE"), 0);
        } else {
            startClient();
        }
    }

    public void enableButtons(boolean enabled) {
        this.button1.setEnabled(enabled);
        this.button4.setEnabled(enabled);
        this.button2.setEnabled(enabled);
        this.button3.setEnabled(enabled);
    }

    public void startClient() {
        spinner1 = findViewById(R.id.spinner);
        BluetoothAdapter vBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = vBluetoothAdapter.getBondedDevices();
        List<String> spinnerArray = new ArrayList<String>();

        for (BluetoothDevice bt : pairedDevices) {
            this.spinnerMap.put(bt.getName(), bt);
            spinnerArray.add(bt.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,spinnerArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.spinner1.setAdapter(adapter);
        this.spinner1.setOnItemSelectedListener(MainActivity.this);
    }


}