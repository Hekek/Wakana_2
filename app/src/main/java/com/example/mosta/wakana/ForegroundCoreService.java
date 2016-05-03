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
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.UUID;

/**
 * Created by mosta on 30/04/16.
 */
public class ForegroundCoreService extends Service {
    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;

    private static final String TAG = "CORE";

    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private OutputStream outStream = null;
    private String btAdress= null;

    private boolean connected = false ;
    private boolean running = true ;

    public static String EXTRA_DEVICE_ADDRESS;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public Visualizer audioOutput = null;
    AudioTrack visualizedTrack = null;

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            // Normally we would do some work here, like download a file.
            // For our sample, we just sleep for 5 seconds.
            while (!connected) {
                Log.i(TAG, "Core Started");
                btAdapter = BluetoothAdapter.getDefaultAdapter();
                checkBTState();
                try {
                    Thread.sleep(1000);
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

                        sendData("WaKaNa Connected");
                    }
                    catch (InterruptedException e) {}
                }
            /*final Thread mThread = new Thread(){
                @Override
                public void run(){
                    final int minBufferSize = AudioTrack.getMinBufferSize(Visualizer.getMaxCaptureRate(), AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_8BIT);
                    visualizedTrack = new AudioTrack(AudioManager.STREAM_MUSIC, Visualizer.getMaxCaptureRate(), AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_8BIT, minBufferSize, AudioTrack.MODE_STREAM);
                    visualizedTrack.play();
                    audioOutput = new Visualizer(0); // get output audio stream

                    audioOutput.setEnabled(false);
                    audioOutput.setCaptureSize(Visualizer.getCaptureSizeRange()[0]);
                    toast("Capture size range: "+Arrays.toString(Visualizer.getCaptureSizeRange()));
                    audioOutput.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
                        @Override
                        public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {
                            //visualizedTrack.write(waveform, 0, waveform.length);
                        }

                        @Override
                        public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {
                            Log.i(TAG, Arrays.toString(fft));
                            //generateData(fft);
                        }
                    }, Visualizer.getMaxCaptureRate(), false, true); // waveform not freq data

                    startVisualizer();
                }
            };
            mThread.start();*/
            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
            stopSelf(msg.arg1);
        }
    }


    @Override
    public void onCreate() {
        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

        btAdress = intent.getStringExtra(EXTRA_DEVICE_ADDRESS);

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        mServiceHandler.sendMessage(msg);

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
    }

    private void startVisualizer(){
        audioOutput.setEnabled(true);
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

    }

    public void toast(String text){
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }
}
