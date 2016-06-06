package com.example.mosta.wakana;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ManageSounds extends AppCompatActivity {

    //GET all sounds from data base
    private List<String> listLabels = new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_sounds);

        DatabaseHelper db = new DatabaseHelper(getApplicationContext());
        listLabels = db.getListLabels();

        ArrayAdapter adapter = new ArrayAdapter<>(this, R.layout.sounds_list_component, listLabels);

        ListView listView = (ListView) findViewById(R.id.sounds_list);
        listView.setAdapter(adapter);
    }


}
