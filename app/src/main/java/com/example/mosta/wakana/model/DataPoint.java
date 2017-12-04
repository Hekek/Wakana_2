package com.example.mosta.wakana.model;

/**
 * Created by mosta on 16/05/16.
 */
public class DataPoint {
    private int time;
    private int songId;

    public DataPoint(int songId, int time) {
        this.songId = songId;
        this.time = time;
    }

    public int getTime() {
        return time;
    }
    public int getSongId() {
        return songId;
    }
}

