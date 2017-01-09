package com.example.mosta.wakana;



import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

/**
 * Created by mosta on 15/05/16.
 */
public class TremorElaborator implements Runnable {
    final String TAG = "TREMOR-ELABORATOR";
    private String myName;
    public final int[] RANGE = new int[] { 40, 80, 120, 180, 300 };
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final int CHUNK_SIZE = 4096;
    private static final int FUZ_FACTOR = 2;
    double highscores[][];
    long points[][];
    public byte Audio[];
    public String HASHES ="";
    private DatabaseHelper database;
    public Context mContext;




    TremorElaborator(String name , byte[] audio , Context context){
        this.myName = name;
        this.Audio = audio;
        mContext = context;

    }

    @Override
    public void run(){
        database = new DatabaseHelper(mContext);
        elaborate();
        //System.out.println(myName + ")Finished");
    }
    public void elaborate(){

        final int totalSize = Audio.length;
        int amountPossible = totalSize/CHUNK_SIZE;
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
            for (int freq = 30; freq < 300; freq++) {
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
            String hash = ""+h;
            if (database.hashExist(hash))
            {
                //ANOTHER THREAD SHOULD START HERE FOR THE SPECIFIED SOUND - NEED TO BE DONE
                String label = database.getLabel(hash);
                System.out.println("NOTIFY:"+label);
                Intent intent=new Intent(mContext,Notifications.class).putExtra("LABEL",label);
                mContext.startService(intent);
                mContext.stopService(intent);
            }
            HASHES += ""+h+"\n";
            database.close();
        }
        try {
            File myFile = new File(Environment.getExternalStorageDirectory().getPath()+"/Yuri","HASHES.txt");
            FileOutputStream fOut = new FileOutputStream(myFile,true);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append(HASHES);
            myOutWriter.close();
            fOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
