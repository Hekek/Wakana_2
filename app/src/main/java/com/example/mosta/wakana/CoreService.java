package com.example.mosta.wakana;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.audiofx.Visualizer;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.UUID;


/**
 * Created by mosta on 02/05/16.
 */
public class CoreService extends Service {

    //Tag for terminal output
    private static final String TAG = "CORE";

    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private OutputStream outStream = null;
    private String btAdress= null;

    private boolean connected = false ;

    public static String EXTRA_DEVICE_ADDRESS;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public Visualizer audioOutput = null;
    AudioTrack visualizedTrack = null;

    @Override
    public void onCreate() {
        super.onCreate();
        final int minBufferSize = AudioTrack.getMinBufferSize(Visualizer.getMaxCaptureRate(), AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_8BIT);
        visualizedTrack = new AudioTrack(AudioManager.STREAM_MUSIC, Visualizer.getMaxCaptureRate(), AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_8BIT, minBufferSize, AudioTrack.MODE_STREAM);
        visualizedTrack.play();
        audioOutput = new Visualizer(0); // get output audio stream

        audioOutput.setEnabled(false);
        audioOutput.setCaptureSize(Visualizer.getCaptureSizeRange()[0]);
        audioOutput.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
            @Override
            public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {
                //We don't need Waveform data so i disabled it
                //visualizedTrack.write(waveform, 0, waveform.length);
            }

            @Override
            public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {

                Float mean = 0.0f;
                Float absoluteMean = 0.0f;
                Float [] Mean = new Float[8];
                Float [] AbsMean = new Float[8];
                String var = Arrays.toString(fft);
                String[] vars = var.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll(" ","").split(",");
                for (int j = 0 ; j < 8 ; j++){
                    mean = 0.0f;
                    absoluteMean = 0.0f;
                    for (int i = 0 ; i < 16 ; i++){
                        mean += Integer.parseInt(vars[i+j]);
                        absoluteMean += Math.abs(Integer.parseInt(vars[i+j]));
                        //System.out.println("Group("+j+") Freq:"+vars[i+j]);
                    }
                    Mean[j] = mean/16.f;
                    AbsMean[j] = absoluteMean/16.f;
                }
                Log.i(TAG, Arrays.toString(AbsMean));
                sendData(Arrays.toString(AbsMean).replaceAll(" ","").replaceAll("\\[","@").replaceAll("\\]","!")+"\n");
            }
        }, Visualizer.getMaxCaptureRate() / 4, false, true); // waveform not freq data , divide the capture rate by 4 to get less data
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
        btAdress = intent.getStringExtra(EXTRA_DEVICE_ADDRESS);
        while (!connected) {
            Log.i(TAG, "Core Started");
            btAdapter = BluetoothAdapter.getDefaultAdapter();
            checkBTState();
            try {
                Thread.sleep(10);
                //newAddress = intent.getStringExtra(EXTRA_DEVICE_ADDRESS);
                Log.i(TAG, "BT DEVICE ADRESS:" + btAdress);
                BluetoothDevice device = btAdapter.getRemoteDevice(btAdress);
                //Attempt to create a bluetooth socket for comms
                try {
                    btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
                } catch (IOException e1) {
                    Log.v(TAG, "ERROR - Could not create Bluetooth socket");
                }

                // Establish the connection.
                try {
                    btSocket.connect();
                    connected = true;
                } catch (IOException e) {
                    try {
                        btSocket.close();        //If IO exception occurs attempt to close socket
                    } catch (IOException e2) {
                        Log.v(TAG, "ERROR - Could not create Bluetooth socket");
                    }
                }

                // Create a data stream so we can talk to the device
                try {
                    outStream = btSocket.getOutputStream();
                } catch (IOException e) {
                    Log.v(TAG, "ERROR - Could not create bluetooth outstream");
                }
                sendData("WaKaNa Connected"+"\n");
            }
            catch (InterruptedException e) {}
        }
        new Thread(new Runnable(){
            @Override
            public void run() {
                audioOutput.setEnabled(true);
            }
        }).start();

        // If we get killed, after returning from here, restart
        return super.onStartCommand(intent , flags , startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        audioOutput.setEnabled(false);
        audioOutput.release();
        audioOutput = null;
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
    }


    //same as in device list activity
    private void checkBTState() {
        // Check device has Bluetooth and that it is turned on
        if(btAdapter==null) {
            Toast.makeText(getBaseContext(), "ERROR - Device does not support bluetooth", Toast.LENGTH_SHORT).show();
            //finish();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                //startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    // Method to send data
    public void sendData(String message) {
        byte[] msgBuffer = message.getBytes();

        try {
            //attempt to place data on the outstream to the BT device
            outStream.write(msgBuffer);
        } catch (IOException e) {
            //if the sending fails this is most likely because device is no longer there
            Toast.makeText(getBaseContext(), "ERROR - Device not found", Toast.LENGTH_SHORT).show();
            //finish();
        }
    }

    private void generateData(byte[] fft){
        Float mean = 0.0f;
        Float absoluteMean = 0.0f;
        Float [] Mean = new Float[8];
        Float [] AbsMean = new Float[8];
        String var = Arrays.toString(fft);
        String[] vars = var.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll(" ","").split(",");
        for (int j = 0 ; j < 8 ; j++){
            mean = 0.0f;
            absoluteMean = 0.0f;
            for (int i = 0 ; i < 16 ; i++){
                mean += Integer.parseInt(vars[i+j]);
                absoluteMean += Math.abs(Integer.parseInt(vars[i+j]));
                //System.out.println("Group("+j+") Freq:"+vars[i+j]);
            }
            Mean[j] = mean/16.f;
            AbsMean[j] = absoluteMean/16.f;
        }
        Log.i(TAG, Arrays.toString(AbsMean));
        sendData(Arrays.toString(AbsMean));
    }

    public void toast(String text){
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }
}
