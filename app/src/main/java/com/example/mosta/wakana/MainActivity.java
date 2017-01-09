package com.example.mosta.wakana;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper database;

    private ProgressBar mybar;

    private TextView Yuri;

    private View shapee;

    private static final String TAG = "CORE";

    private boolean Tremor=false;

    public String lastnotify = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        database = new DatabaseHelper(getBaseContext());
        CreateYuriDirectory();

        mybar = (ProgressBar) findViewById(R.id.progressBar);
        mybar.setVisibility(View.INVISIBLE);
        //LOAD DEAFAULT SOUND
        loadAmbulance();
        loadBell();

        shapee = (View) findViewById(R.id.myRectangleView);

        //Tremor Switch
        Switch tremorSwitch = (Switch) findViewById(R.id.tremorSwitch);
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

    @Override
    public void onResume(){
        super.onResume();

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("my-event"));
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Extract data included in the Intent
            String message = intent.getStringExtra("SOUND");
            if (!message.equals(lastnotify))
            {
                lastnotify = message;
                Notify(message);
            }
        }
    };

    @Override
    protected void onPause(){
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        //GET NOTIFICATION when app in background
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("my-event"));
        super.onPause();
    }


    @Override
    protected void onStop(){
        super.onStop();
        System.out.println("STOP CLOSED");
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        System.out.println("CLOSED");

    }
    // Yuri start
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


    //Tremor is most important part of the app , it take care of capturing all microphone data
    public void startTremor(){
        startService(new Intent(this, TremorService.class));
    }

    public void stopTremor(){
        stopService(new Intent(this, TremorService.class));
    }

    // THIS IS THE MENU SETUP
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sounds:
                Intent i = new Intent(getApplicationContext(),ManageSounds.class);
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

    public void Notify(String label){
        if (label==null)
            onDestroy();
        PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(), 0);
        Notification notification = new NotificationCompat.Builder(this)
                .setTicker("Yuri")
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("Yuri")
                .setContentText(label)
                .setContentIntent(pi)
                .setAutoCancel(true)
                .build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        notificationManager.notify(0, notification);
    }

    public void CreateYuriDirectory(){
        File dir = new File(Environment.getExternalStorageDirectory().getPath()+"/Yuri");
        try{
            if(dir.mkdir()) {
                System.out.println("Directory created");
            } else {
                System.out.println("Directory is not created");
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
