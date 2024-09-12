package eu.learning.voiceassistance;


import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.UUID;

public class MainActivity2 extends AppCompatActivity {

    private static final String TAG = "ObstacleAvoidance";
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    InputStream inputStream;
    // TextView dataTextView;

    TextToSpeech textToSpeech;



    // Replace this with the MAC address of module
    private static final String MAC_ADDRESS = "FC:B4:67:4E:56:7E"; //[new sensor 00:22:03:01:60:B9 ]  [old sensor 00:23:02:35:14:DB ]
//esp32 mac FC:B4:67:4E:56:7E

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        ImageView img = findViewById(R.id.obsdet);
        img.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // Handle long click event
                showTextPopup("Obstacle Detection Mode");
                return true; // Consume the long click event
            }
        });
        //     dataTextView = findViewById(R.id.dataTextView);


        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                // Handle back button press
                Intent intent = new Intent();
                intent.putExtra("booleanKey2", false); // Put your boolean value here
                setResult(RESULT_OK, intent);
                finish();
            }
        };
        // Register the callback
        getOnBackPressedDispatcher().addCallback(this, callback);


        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // creating an object textToSpeech and adding features into it
        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                // if No error is found then only it will run
                if(status!=TextToSpeech.ERROR){
                    // To Choose language of speech
                    textToSpeech.setLanguage(Locale.ENGLISH);
                }
            }
        });

        textToSpeech.speak("Starting Obstacle Detection.",TextToSpeech.QUEUE_FLUSH,null);
        while (textToSpeech.isSpeaking()){

        }
        new ConnectBluetoothTask().execute();


    }
    private void showTextPopup(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .setCancelable(false);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private class ConnectBluetoothTask extends AsyncTask<Void, Void, Boolean> {

        @SuppressLint("MissingPermission")
        @Override
        protected Boolean doInBackground(Void... params) {
            if (bluetoothAdapter == null) {
                return false; // Device does not support Bluetooth
            }

            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(MAC_ADDRESS);

            try {
                // UUID for Serial Port Profile (SPP)
                UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
                //Exception not handled
                bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid);
                bluetoothSocket.connect();
                inputStream = bluetoothSocket.getInputStream();
                return true;
            } catch (IOException e) {
                Log.e(TAG, "Error connecting to Bluetooth device", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean isConnected) {
            if (isConnected) {
                textToSpeech.speak("Connected Successfully",TextToSpeech.QUEUE_FLUSH,null);
                while (textToSpeech.isSpeaking()){

                }
                new ReadDataTask().execute();
            } else {
                textToSpeech.speak("Failed to connect",TextToSpeech.QUEUE_FLUSH,null);
                Log.e(TAG, "Failed to connect to Bluetooth device");
            }
        }
    }

    private class ReadDataTask extends AsyncTask<Void, String, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            byte[] buffer = new byte[1024];
            int bytes;

            while (true) {
                try {
                    bytes = inputStream.read(buffer);
                    String data = new String(buffer, 0, bytes);
                    publishProgress(data);
                } catch (IOException e) {
                    Log.e(TAG, "Error reading data from Bluetooth device", e);
                    break;
                }
            }
            return null;
        }


        @Override
        protected void onProgressUpdate(String... values) {


            String data = values[0];
            String val;
            //String data;
            val=data.toString();
            if (val.contains("\n")) {

                int index = val.indexOf("\n");
                data = val.substring(0,index);

            }



            int distance;
            distance=Integer.parseInt(data);
            //    dataTextView.setText(data);

            if(distance == 10){
                textToSpeech.setSpeechRate(1.5f);
                textToSpeech.speak("Stop, obstacle is very close",TextToSpeech.QUEUE_FLUSH,null);
                while (textToSpeech.isSpeaking()){

                }
            }
            else if(distance == 20){
                textToSpeech.setSpeechRate(1.5f);
                textToSpeech.speak("Obstacle is detected in front of you, be careful.",TextToSpeech.QUEUE_FLUSH,null);
                while (textToSpeech.isSpeaking()){

                }
            }

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        try {
            if (bluetoothSocket != null) {
                bluetoothSocket.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error closing Bluetooth socket", e);
        }


    }
}