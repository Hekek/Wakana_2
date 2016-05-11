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

    static final int PICK_CONTACT_REQUEST = 1;
    public static String EXTRA_DEVICE_ADDRESS;
    public static String BLUETOOTH_DEVICE_MAC = null;

    private Switch senseSwitch;
    private Switch tremorSwitch;


    private static final String TAG = "CORE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Enable Wakana to start service
        senseSwitch = (Switch) findViewById(R.id.senseSwitch);
        senseSwitch.setChecked(false);
        senseSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {

                if (isChecked) {
                    toast("Sense ON");
                    if (BLUETOOTH_DEVICE_MAC == null) {
                        toast("No Bluetooth Device Selected!");
                    } else {
                        startCore();
                        toast("Sense ON");
                    }

                } else {
                    toast("Sense OFF");
                    stopCore();
                }

            }
        });

        //Tremor Switch
        tremorSwitch = (Switch) findViewById(R.id.tremorSwitch);
        tremorSwitch.setChecked(false);
        tremorSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (isChecked) {
                    toast("Tremor ON");
                    startTremor();

                } else {
                    toast("Tremor OFF");
                    stopTremor();
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

    public void startTremor(){
        startService(new Intent(this, TremorService.class));
    }

    public void stopTremor(){
        stopService(new Intent(this, TremorService.class));
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


}
