package com.example.mosta.wakana.activity;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mosta.wakana.helper.DatabaseHelper;
import com.example.mosta.wakana.R;
import com.example.mosta.wakana.service.TremorService;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "CORE";

    public static final String AMBULANCE = "Ambulance";

    public static final String BELL = "Bell";

    private static final int MY_PERMISSIONS_REQUEST_READ_STORAGE = 1 ;

    private DatabaseHelper database;

    private ProgressBar myBar;

    private TextView yuriTextView;

    private View circleShape;

    private boolean tremor = false;

    private String lastNotify = null;

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Extract data included in the Intent
            String message = intent.getStringExtra("SOUND");
            if (!message.equals(lastNotify)) {
                lastNotify = message;
                MainActivity.this.notify(message);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myBar = (ProgressBar) findViewById(R.id.progress_bar);
        circleShape = (View) findViewById(R.id.background_circle);
        yuriTextView = (TextView) findViewById(R.id.btn_yuri);

        yuriTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!tremor) {
                    startTremor();

                    myBar.setVisibility(View.VISIBLE);
                    circleShape.setVisibility(View.INVISIBLE);

                    tremor = true;
                    yuriTextView.setText(getString(R.string.stop));

                } else {
                    stopTremor();

                    myBar.setVisibility(View.INVISIBLE);
                    circleShape.setVisibility(View.VISIBLE);

                    tremor = false;
                    yuriTextView.setText(getString(R.string.start));
                }
            }
        });

        //Permissions
        permissions();

        database = new DatabaseHelper(getBaseContext());
        createYuriDirectory();

        //LOAD DEFAULT SOUND

        loadAmbulance();
        loadBell();
    }

    @Override
    public void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("my-event"));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);

        //TODO why?
        //GET NOTIFICATION when app in background
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("my-event"));

        super.onPause();
    }

    @Override
    protected void onStop() {
        System.out.println("STOP");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        System.out.println("CLOSED");
        stopTremor();

        super.onDestroy();
        //TODO chiudere il servizio che ascolta
    }

    // yuriTextView start
    public void yuri_action(View v) {
        yuriTextView = (TextView) findViewById(R.id.btn_yuri);
        if (!tremor) {
            startTremor();
            myBar.setVisibility(View.VISIBLE);
            tremor = true;
            yuriTextView.setText("Stop");
            circleShape.setVisibility(View.INVISIBLE);

        } else {
            stopTremor();
            myBar.setVisibility(View.INVISIBLE);
            tremor = false;
            yuriTextView.setText("Start");
            circleShape.setVisibility(View.VISIBLE);
        }
    }

    //tremor is most important part of the app , it take care of capturing all microphone data
    public void startTremor() {
        startService(new Intent(this, TremorService.class));
    }

    public void stopTremor() {
        stopService(new Intent(this, TremorService.class));
    }

    // THIS IS THE MENU SETUP
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sounds:
                Intent i = new Intent(getApplicationContext(), ManageSoundsActivity.class);
                startActivity(i);
                return true;

            case R.id.action_addsounds:
                i = new Intent(getApplicationContext(), AddSoundActivity.class);
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

    public void loadAmbulance() {
        if (!database.soundExist(AMBULANCE)) {
            database.createSample("12811007436", AMBULANCE);
            database.createSample("15409206230", AMBULANCE);
            database.createSample("12809207436", AMBULANCE);
            database.createSample("12811007236", AMBULANCE);
            database.createSample("14611007436", AMBULANCE);
            database.createSample("14609207436", AMBULANCE);
            database.createSample("12809207236", AMBULANCE);
            database.createSample("12811005436", AMBULANCE);
            database.createSample("14611007236", AMBULANCE);
            database.createSample("12809205436", AMBULANCE);
            printDatabase();
        }
    }

    public void loadBell() {
        if (!database.soundExist(BELL)) {
            database.createSample("16211407032", BELL);
            database.createSample("16211407232", BELL);
            database.createSample("16211407034", BELL);
            database.createSample("16211407234", BELL);
            database.createSample("16211407030", BELL);
            database.createSample("16211407230", BELL);
            database.createSample("16211407036", BELL);
            database.createSample("16211207032", BELL);
            database.createSample("16211207232", BELL);
            database.createSample("16211407236", BELL);
            printDatabase();
        }
    }

    public void printDatabase() {
        //PRINT DATABASE
        HashMap<String, String> samples = database.getAllSamples();
        System.out.println("Size: " + samples.size());
        Iterator<String> iterator = samples.keySet().iterator();

        while (iterator.hasNext()) {
            String key = iterator.next();
            String value = samples.get(key).toString();
            System.out.println(key + " " + value);
        }
    }

    public void toast(String text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }

    public void notify(String label) {
        if (label == null)
            onDestroy();
        PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(), 0);
        Notification notification = new NotificationCompat.Builder(this)
                .setTicker("yuriTextView")
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("yuriTextView")
                .setContentText(label)
                .setContentIntent(pi)
                .setAutoCancel(true)
                .build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        notificationManager.notify(0, notification);
    }

    public void createYuriDirectory() {
        File dir = new File(Environment.getExternalStorageDirectory().getPath() + "/Yuri");
        try {
            if (dir.mkdir()) {
                System.out.println("Directory created");
            } else {
                System.out.println("Directory is not created");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void permissions(){
        System.out.println("Requesting Permissions");
        //Control if we have the permission to Record Audio
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED)
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {
            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        1);
            }
        }

        //Control if we have the permission to Record Audio
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        1);
            }
        }
    }
}
