package com.example.mosta.wakana;

import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.audiofx.PresetReverb;
import android.media.audiofx.Visualizer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_CONTACT_REQUEST = 1;
    public static String EXTRA_DEVICE_ADDRESS;
    public static String BLUETOOTH_DEVICE_MAC = null;

    private Switch senseSwitch;
    private Switch tremorSwitch;

    private DatabaseHelper database;

    private ProgressBar mybar;

    private TextView Yuri;

    private View shapee;

    private static final String TAG = "CORE";

    private boolean Tremor=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        database = new DatabaseHelper(getBaseContext());

        mybar = (ProgressBar) findViewById(R.id.progressBar);
        //mybar.setIndeterminate(true);
        /*mybar.getIndeterminateDrawable().setColorFilter(
                getResources().getColor(R.color.PBar),
                android.graphics.PorterDuff.Mode.SRC_IN);*/
        mybar.setVisibility(View.INVISIBLE);
        //LOAD DEAFAULT SOUND
        loadAmbulance();
        loadBell();

        shapee = (View) findViewById(R.id.myRectangleView);


        //Enable Wakana to start service
        senseSwitch = (Switch) findViewById(R.id.senseSwitch);
        senseSwitch.setChecked(false);
        senseSwitch.setVisibility(View.INVISIBLE);
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
        tremorSwitch.setVisibility(View.INVISIBLE);
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

    public void yuri_action(View v){
        Yuri = (TextView) findViewById(R.id.btn_yuri);
        if (!Tremor) {
            startTremor();
            mybar.setVisibility(View.VISIBLE);
            Tremor = true;
            Yuri.setText("Stop");
            shapee.setVisibility(View.INVISIBLE);

        } else {
            stopTremor();
            mybar.setVisibility(View.INVISIBLE);
            Tremor=false;
            Yuri.setText("Start");
            shapee.setVisibility(View.VISIBLE);
        }
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
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                Intent i = new Intent(getApplicationContext(),DeviceListActivity.class);
                startActivityForResult(i,PICK_CONTACT_REQUEST);
                return true;
            /*
            case R.id.action_favorite:
                // User chose the "Favorite" action, mark the current item
                // as a favorite...
                i = new Intent(getApplicationContext(),AddSoundActivity.class);
                startActivity(i);
                return true;*/

            case R.id.action_sounds:
                i = new Intent(getApplicationContext(),ManageSounds.class);
                startActivity(i);
                return true;

            case R.id.action_addsounds:
                i = new Intent(getApplicationContext(),AddSoundActivity.class);
                startActivity(i);
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

    public void loadAmbulance(){
        if (!database.soundExist("Ambulance")){
            database.createSample("12811007436","Ambulance");
            database.createSample("15409206230","Ambulance");
            database.createSample("12809207436","Ambulance");
            database.createSample("12811007236","Ambulance");
            database.createSample("14611007436","Ambulance");
            database.createSample("14609207436","Ambulance");
            database.createSample("12809207236","Ambulance");
            database.createSample("12811005436","Ambulance");
            database.createSample("14611007236","Ambulance");
            database.createSample("12809205436","Ambulance");
            databasetoString();
        }
    }

    public void loadBell(){
        if (!database.soundExist("Bell")){
            database.createSample("16211407032","Bell");
            database.createSample("16211407232","Bell");
            database.createSample("16211407034","Bell");
            database.createSample("16211407234","Bell");
            database.createSample("16211407030","Bell");
            database.createSample("16211407230","Bell");
            database.createSample("16211407036","Bell");
            database.createSample("16211207032","Bell");
            database.createSample("16211207232","Bell");
            database.createSample("16211407236","Bell");
            databasetoString();
        }
    }


    public void databasetoString(){
        //PRINT DATABASE
        HashMap<String,String> samples = database.getAllSamples();
        System.out.println("Size: " + samples.size());
        Iterator<String> iterator = samples.keySet().iterator();

        while (iterator.hasNext()) {
            String key = iterator.next();

            String value = samples.get(key).toString();

            System.out.println(key + " " + value);
        }
    }

    public void toast(String text){
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }
}
