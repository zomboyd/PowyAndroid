package com.example.alex.powy;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

//public class bluetoothServer extends AppCompatActivity {
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.UUID;

/**
 * Created by Sylvain on 18/02/16.
 */
public class bluetoothServer extends Activity {
    private static final int DISCOVERABLE_REQUEST_CODE = 0x1;
    private boolean CONTINUE_READ_WRITE = true;
    private TextView txt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_server);
        //Always make sure that Bluetooth server is discoverable during listening...
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        startActivityForResult(discoverableIntent, DISCOVERABLE_REQUEST_CODE);
        txt  = (TextView) findViewById(R.id.servertxt);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        txt.setText("TrackingFlow, Creating thread to start listening...");
        android.util.Log.e("TrackingFlow", "Creating thread to start listening...");
        new Thread(reader).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(socket != null){
            try{
                is.close();
                os.close();
                socket.close();
            }catch(Exception e){}
            CONTINUE_READ_WRITE = false;
        }
    }

    private BluetoothSocket socket;
    private InputStream is;
    private OutputStreamWriter os;
    private Runnable reader = new Runnable() {
        public void run() {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            UUID uuid = UUID.fromString("4e5d48e0-75df-11e3-981f-0800200c9a66");
            try {
                BluetoothServerSocket serverSocket = adapter.listenUsingRfcommWithServiceRecord("BLTServer", uuid);
                //txt.setText("Listening...");
                android.util.Log.e("TrackingFlow", "Listening...");
                socket = serverSocket.accept();
                //txt.setText("Socket accepted...");
                android.util.Log.e("TrackingFlow", "Socket accepted...");
                is = socket.getInputStream();
                os = new OutputStreamWriter(socket.getOutputStream());
                new Thread(writter).start();

                int bufferSize = 1024;
                int bytesRead = -1;
                byte[] buffer = new byte[bufferSize];
                //Keep reading the messages while connection is open...
                while(CONTINUE_READ_WRITE){
                    final StringBuilder sb = new StringBuilder();
                    bytesRead = is.read(buffer);
                    if (bytesRead != -1) {
                        String result = "";
                        while ((bytesRead == bufferSize) && (buffer[bufferSize-1] != 0)){
                            result = result + new String(buffer, 0, bytesRead - 1);
                            bytesRead = is.read(buffer);
                        }
                        result = result + new String(buffer, 0, bytesRead - 1);
                        sb.append(result);
                    }
                    //txt.setText("READ: " + sb.toString());
                    android.util.Log.e("TrackingFlow", "Read: " + sb.toString());
                    //Show message on UIThread
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(bluetoothServer.this, sb.toString(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            } catch (IOException e) {e.printStackTrace();}
        }
    };

    private Runnable writter = new Runnable() {

        @Override
        public void run() {
            int index = 0;
            while(CONTINUE_READ_WRITE){
                try {
                    os.write("Message From Server" + (index++) + "\n");
                    os.flush();
                    Thread.sleep(2000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };
}
