package com.example.mosta.wakana.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.mosta.wakana.R;
import com.example.mosta.wakana.helper.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class ManageSoundsActivity extends AppCompatActivity {

    //GET all sounds from data base
    private List<String> listLabels = new ArrayList<>();

    private ArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_sounds);

        ListView soundsList = (ListView) findViewById(R.id.sounds_list);
        adapter = new ArrayAdapter<>(this, R.layout.sounds_list_component, listLabels);
        soundsList.setAdapter(adapter);

        soundsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String label = listLabels.get(position);

                Intent i = new Intent(getApplicationContext(), ManageSoundDetailActivity.class).putExtra("LABEL", label);
                startActivity(i);
            }
        });
    }

    public void toast(String text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        DatabaseHelper db = new DatabaseHelper(getApplicationContext());
        listLabels = db.getListLabels();

        adapter.clear();
        for (String label : listLabels) {
            adapter.add(label);
        }
    }
}
