package com.example.mosta.wakana;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddSoundActivity extends AppCompatActivity {
    private Button btn_record;
    private Button btn_stoprecord;
    private TextView txtview_log;
    private int GFreqSize = 448;
    private int GFreq = 8;
    boolean isRecording;
    public final int[] RANGE = new int[] { 40, 80, 120, 180, 301 };
    final int bufferSize = 4096;//AudioRecord.getMinBufferSize(44100,AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
    final AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, 44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
    final byte[] buffer = new byte[bufferSize];
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    int CHUNK_SIZE = 4096;
    private static final int FUZ_FACTOR = 2;
    double highscores[][];
    long points[][];
    int contatore = 0 ;
    ArrayList<String> Hashes = new ArrayList<>();
    ExecutorService executor = Executors.newFixedThreadPool(2);

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_sound);
        txtview_log = (TextView) findViewById(R.id.txtview_log);
        btn_record = (Button) findViewById(R.id.btn_record);
        btn_stoprecord = (Button) findViewById(R.id.btn_stoprecord);

        btn_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRecording = true;
                deleteOldHashes();
                recorder.startRecording();
                new Thread(new Runnable(){
                    @Override
                    public void run() {
                        try{
                            while (isRecording) {
                                int count = recorder.read(buffer, 0, bufferSize);
                                if (count > 0) {
                                    out.write(buffer, 0, count);
                                }
                                //If Size of the Buffer is more than 176kb
                                if(out.size() > 176000)
                                {
                                    contatore ++ ;
                                    byte audio[] = out.toByteArray();
                                    Runnable taskOne = new TremorElaborator("TASK"+contatore,audio);
                                    executor.execute(taskOne);
                                    //elaborate();
                                    out.reset();
                                    out.flush();
                                    out.close();
                                }
                            }

                            out.close();
                        }catch (IOException e) {
                            System.err.println("I/O problems: " + e);
                            System.exit(-1);
                        }
                    }

                }).start();
            }
        });

        btn_stoprecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Stop recording
                isRecording = false;
                recorder.stop();
                recorder.release();

                //Get data from file and add them to a list
                String root = Environment.getExternalStorageDirectory().getPath();
                File file = new File(root,"HASHES.txt");
                try {
                    BufferedReader br = new BufferedReader(new FileReader(file));
                    String line;

                    while ((line = br.readLine()) != null) {
                        Hashes.add(line);
                    }
                    br.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }

                //GET DUPLICATES
                Map<String, Integer> map = new HashMap<String, Integer>();
                for (String temp : Hashes) {
                    Integer count = map.get(temp);
                    map.put(temp, (count == null) ? 1 : count + 1);
                }

                //SORT MAP
                Map<String, Integer> treeMap = new TreeMap<String, Integer>(map);

                for (Map.Entry<String, Integer> entry : map.entrySet()) {
                    txtview_log.append("Key : " + entry.getKey() + " Value : "
                            + entry.getValue()+"\n");
                }
            }
        });


    }
    private void deleteOldHashes(){
        try
        {
            String root = Environment.getExternalStorageDirectory().getPath();
            File file = new File(root,"HASHES.txt");
            if(file.exists())
                file.delete();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
