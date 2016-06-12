package com.example.mosta.wakana;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
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

        ListView soundsList = (ListView) findViewById(R.id.sounds_list);

        DatabaseHelper db = new DatabaseHelper(getApplicationContext());
        listLabels = db.getListLabels();

        ArrayAdapter adapter = new ArrayAdapter<>(this, R.layout.sounds_list_component, listLabels);

        ListView listView = (ListView) findViewById(R.id.sounds_list);
        listView.setAdapter(adapter);

        soundsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String label = ((TextView) view).getText().toString();
                Intent i = new Intent(getApplicationContext(),ManageSound.class).putExtra("LABEL",label);
                startActivity(i);
            }
        });
    }
    public void toast(String text){
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume(){
        super.onResume();
        ListView soundsList = (ListView) findViewById(R.id.sounds_list);
        DatabaseHelper db = new DatabaseHelper(getApplicationContext());
        listLabels = db.getListLabels();
        ArrayAdapter adapter = new ArrayAdapter<>(this, R.layout.sounds_list_component, listLabels);
        ListView listView = (ListView) findViewById(R.id.sounds_list);
        listView.setAdapter(adapter);
        soundsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String label = ((TextView) view).getText().toString();
                Intent i = new Intent(getApplicationContext(),ManageSound.class).putExtra("LABEL",label);
                startActivity(i);
            }
        });
    }
}
