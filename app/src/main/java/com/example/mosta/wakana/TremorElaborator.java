package com.example.mosta.wakana;


import android.app.NotificationManager;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by mosta on 15/05/16.
 */
public class TremorElaborator implements Runnable {
    final String TAG = "TREMOR-ELABORATOR";
    private String myName;
    public final int[] RANGE = new int[] { 40, 80, 120, 180, 301 };
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final int CHUNK_SIZE = 4096;
    private static final int FUZ_FACTOR = 2;
    double highscores[][];
    long points[][];
    public byte Audio[];
    public String HASHES ="";
    private DatabaseHelper database;
    private Context mContext;




    TremorElaborator(String name , byte[] audio , Context context){
        this.myName = name;
        this.Audio = audio;
        mContext = context;
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(mContext)
                        .setSmallIcon(R.drawable.ic_settings)
                        .setContentTitle("ALLERT WAKANA")
                        .setContentText("SOUND DETECTED");

    }

    @Override
    public void run(){
        database = new DatabaseHelper(mContext);
        elaborate();
        System.out.println(myName + ")Finished");
    }
    public void elaborate(){

        final int totalSize = Audio.length;
        Log.i(TAG,"Total Size Of data: "+totalSize);
        int amountPossible = totalSize/CHUNK_SIZE;
        Log.i(TAG,"Amount of CHUNKS: "+amountPossible);
        //When turning into frequency domain we'll need complex numbers:
        Complex[][] results = new Complex[amountPossible][];

        //For all the chunks:
        for(int times = 0;times < amountPossible; times++) {
            Complex[] complex = new Complex[CHUNK_SIZE];
            for(int i = 0;i < CHUNK_SIZE;i++) {
                //Put the time domain data into a complex number with imaginary part as 0:
                complex[i] = new Complex(Audio[(times*CHUNK_SIZE)+i], 0);
            }
            //Perform FFT analysis on the chunk:
            results[times] = FFT.fft(complex);
        }
        highscores = new double[results.length][5];
        points = new long[results.length][5];
        for (int t = 0 ; t < results.length ; t++){
            for (int freq = 30; freq < 300-1; freq++) {
                //Get the magnitude:
                double mag = Math.log(results[t][freq].abs() + 1);

                //Find out which range we are in:
                int index = getIndex(freq);

                //Save the highest magnitude and corresponding frequency:
                if (mag > highscores[t][index]) {
                    highscores[t][index] = mag;
                    points[t][index] = freq;
                }
            }
            long h = hash(points[t][0], points[t][1], points[t][2], points[t][3]);
            //Hashes.add(h);
            String hash = ""+h;
            if (database.hashExist(hash))
            {
                System.out.println("NOTIFY:"+database.getLabel(hash));

            }
            HASHES += ""+h+"\n";
        }
        try {
            File myFile = new File(Environment.getExternalStorageDirectory().getPath(),"HASHES.txt");
            FileOutputStream fOut = new FileOutputStream(myFile,true);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append(HASHES);
            myOutWriter.close();
            fOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //Log.d(TAG, HASHES);
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
}
