package com.example.mosta.wakana;

import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    //TEST VAR
    public static Integer[] frequencies;
    //

    static final int PICK_CONTACT_REQUEST = 1;
    public static String EXTRA_DEVICE_ADDRESS;
    public static String BLUETOOTH_DEVICE_MAC = null;

    private Switch wakanaSwitch;

    private Visualizer audioOutput = null;
    AudioTrack visualizedTrack = null;
    MediaPlayer mp = null;

    private static final String TAG = "CORE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Test Code
        int steps = 8;
        int range = 100;
        frequencies = new Integer[steps];
        for(int i = 0; i < steps; i++) {
            frequencies[i] = range / steps * i;
        }

        createVisualizer();

        //Enable Wakana to start service
        wakanaSwitch = (Switch) findViewById(R.id.wakanaSwitch);
        wakanaSwitch.setChecked(false);
        wakanaSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {

                if (isChecked) {
                    toast("Switch is currently ON");
                    if (BLUETOOTH_DEVICE_MAC == null) {
                        toast("No Bluetooth Device Selected!");
                        startVisualizer();
                    } else {
                        startCore();
                        toast("Wakana Enabled");
                    }

                } else {
                    stopCore();
                    stopVisualizer();
                }

            }
        });
    }

    //START BACKGROUND SERVICE
    public void startCore(){
        startService(new Intent(this, CoreService.class).putExtra(EXTRA_DEVICE_ADDRESS, BLUETOOTH_DEVICE_MAC));
    }
    //STOP BACKGROUND SERVICE
    public void stopCore(){
        stopService(new Intent(this, CoreService.class));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...


                //return true;

            case R.id.action_favorite:
                // User chose the "Favorite" action, mark the current item
                // as a favorite...
                Intent i = new Intent(getApplicationContext(),DeviceListActivity.class);
                startActivityForResult(i,PICK_CONTACT_REQUEST);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if(resultCode == RESULT_OK){
                BLUETOOTH_DEVICE_MAC = data.getStringExtra(EXTRA_DEVICE_ADDRESS);
            }
        }
    }

    //Create Menu Config.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings, menu);
        return true;
    }

    public void toast(String text){
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }

    private void createVisualizer(){
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
                //Log.i(TAG, Arrays.toString(fft));
                generateData(fft);
            }
        }, Visualizer.getMaxCaptureRate(), false, true); // waveform not freq data


    }

    private void startVisualizer(){
        audioOutput.setEnabled(true);
    }

    private void stopVisualizer(){
        audioOutput.setEnabled(false);
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
}
