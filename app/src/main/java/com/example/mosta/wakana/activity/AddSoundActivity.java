package com.example.mosta.wakana.activity;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mosta.wakana.helper.DatabaseHelper;
import com.example.mosta.wakana.R;
import com.example.mosta.wakana.service.TremorElaborator;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddSoundActivity extends AppCompatActivity {

    private String TAG = "AddSoundActivity";

    private Button btn_record;

    private Button btn_stoprecord;

    private Button btn_save;

    private EditText sound_label;

    private TextView txtview_log;

    private boolean isRecording = false;

    private final int bufferSize = 4096;//AudioRecord.getMinBufferSize(44100,AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

    final AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, 44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);

    final byte[] buffer = new byte[bufferSize];

    ByteArrayOutputStream out = new ByteArrayOutputStream();

    int contatore = 0;

    private int elements;

    private final int MAX_HASHES_PER_SOUND = 9;

    ArrayList<String> Hashes = new ArrayList<>();

    Map<String, Integer> map = new HashMap<>();

    ExecutorService executor = Executors.newFixedThreadPool(2);

    private String sound_name;

    private DatabaseHelper database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_sound);
        //SET UI ELEMENTS
        txtview_log = (TextView) findViewById(R.id.txtview_log);
        btn_record = (Button) findViewById(R.id.btn_record);
        btn_stoprecord = (Button) findViewById(R.id.btn_stoprecord);
        btn_save = (Button) findViewById(R.id.btn_save);
        sound_label = (EditText) findViewById(R.id.txtfield_soundlabel);
        //Create DB
        database = new DatabaseHelper(getBaseContext());

        btn_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isRecording) {
                    isRecording = true;
                    deleteOldHashes();
                    recorder.startRecording();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                while (isRecording) {
                                    int count = recorder.read(buffer, 0, bufferSize);
                                    if (count > 0) {
                                        out.write(buffer, 0, count);
                                    }
                                    //If Size of the Buffer is more than 176kb
                                    if (out.size() > 176000) {
                                        contatore++;
                                        byte audio[] = out.toByteArray();
                                        Runnable taskOne = new TremorElaborator("TASK" + contatore, audio, getBaseContext());
                                        executor.execute(taskOne);
                                        //elaborate();
                                        out.reset();
                                        out.flush();
                                        out.close();
                                    }
                                }

                                out.close();
                            } catch (IOException e) {
                                System.err.println("I/O problems: " + e);
                                System.exit(-1);
                            }
                        }

                    }).start();
                }
            }
        });

        btn_stoprecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRecording) {
                    //Stop recording
                    isRecording = false;
                    recorder.stop();
                    recorder.release();
                    executor.shutdown();

                    //Get data from file and add them to a list
                    String root = Environment.getExternalStorageDirectory().getPath() + "/Yuri";
                    File file = new File(root, "HASHES.txt");
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(file));
                        String line;

                        while ((line = br.readLine()) != null) {
                            Hashes.add(line);
                        }
                        br.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    //GET DUPLICATES
                    //Map<String, Integer> map ;//= new HashMap<String, Integer>();
                    for (String temp : Hashes) {
                        Integer count = map.get(temp);
                        map.put(temp, (count == null) ? 1 : count + 1);
                    }
                }
            }
        });

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sound_name = sound_label.getText().toString();
                if (!isRecording && sound_name != "" && !database.soundExist(sound_name)) {
                    //SORT MAP
                    Set<Map.Entry<String, Integer>> set = map.entrySet();
                    List<Map.Entry<String, Integer>> list = new ArrayList<Map.Entry<String, Integer>>(set);
                    Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
                        public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                            return (o2.getValue()).compareTo(o1.getValue());
                        }
                    });

                    //GET TOP 10 POINTS AND ADD THEM TO DATABASE
                    elements = 0;
                    for (Map.Entry<String, Integer> entry : list) {
                        txtview_log.append(entry.getKey() + " ==== " + entry.getValue() + "\n");
                        if (entry.getKey().length() > 3) {
                            database.createSample(entry.getKey(), sound_name);
                            elements++;
                        }
                        if (elements > MAX_HASHES_PER_SOUND)
                            break;
                    }
                    toast("ADDED SOUND TO DB");
                } else if (database.soundExist(sound_name)) {
                    toast("ERROR: Change sound name");
                } else if (sound_name == "") {
                    toast("ERROR: Add sound label ");
                }
                databasetoString();
                finish();
            }
        });

    }

    private void deleteOldHashes() {
        try {
            String root = Environment.getExternalStorageDirectory().getPath();
            File file = new File(root, "HASHES.txt");
            if (file.exists())
                file.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void toast(String text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }

    public void databasetoString() {
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
}
