package com.example.mosta.wakana.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.mosta.wakana.R;
import com.example.mosta.wakana.helper.DatabaseHelper;

public class ManageSoundDetailActivity extends AppCompatActivity {

    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_sound);

        Intent intent = getIntent();
        final String label = intent.getStringExtra("LABEL");

        Button btn_delete = (Button) findViewById(R.id.btn_delete);

        db = new DatabaseHelper(getApplicationContext());

        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.removeSound(label);
                finish();
            }
        });
    }
}
