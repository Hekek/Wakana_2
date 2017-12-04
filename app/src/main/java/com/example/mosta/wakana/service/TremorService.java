package com.example.mosta.wakana.service;

import android.app.Service;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by mosta on 10/05/16.
 */
public class TremorService extends Service {

    public String TAG = "TREMOR SERVICE";

    private static int GFreqSize = 448;

    private static int GFreq = 8;

    private boolean isRecording = true;

    public final int[] RANGE = new int[]{40, 80, 120, 180, 301};

    private final int bufferSize = 4096;//AudioRecord.getMinBufferSize(44100,AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

    private final AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, 44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);

    private final byte[] buffer = new byte[bufferSize];

    private ByteArrayOutputStream out = new ByteArrayOutputStream();

    private static int CHUNK_SIZE = 4096;

    private static final int FUZ_FACTOR = 2;

    private double highscores[][];

    private long points[][];

    private int contatore = 0;

    //TEST
    private ExecutorService executor = Executors.newFixedThreadPool(2);

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Tremor Started");
        Log.i(TAG, "BUFF:" + bufferSize);
        //DELETE OLD SAVED HASHES
        try {
            String root = Environment.getExternalStorageDirectory().getPath() + "/Yuri";
            File file = new File(root, "HASHES.txt");
            if (file.exists())
                file.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                        if (out.size() > 22000)//176000)
                        {
                            contatore++;
                            byte audio[] = out.toByteArray();
                            Log.i(TAG, "Analyze(" + contatore + ")");
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
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRecording = false;
        recorder.stop();
        recorder.release();
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    // find out in which range is frequency
    public int getIndex(int freq) {
        int i = 0;
        while (RANGE[i] < freq)
            i++;
        return i;
    }

    private long hash(long p1, long p2, long p3, long p4) {
        return (p4 - (p4 % FUZ_FACTOR)) * 100000000 + (p3 - (p3 % FUZ_FACTOR))
                * 100000 + (p2 - (p2 % FUZ_FACTOR)) * 100
                + (p1 - (p1 % FUZ_FACTOR));
    }

    public long FileSize(String fileName) {
        File file = new File(fileName);
        long filesize = file.length();
        return filesize / 1024;
    }
}
