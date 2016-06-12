package com.example.mosta.wakana;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ManageSound extends AppCompatActivity {

    private Button btn_delete;
    private String label ;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_sound);
        Intent intent = getIntent();
        label = intent.getStringExtra("LABEL");
        btn_delete = (Button) findViewById(R.id.btn_delete);
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
