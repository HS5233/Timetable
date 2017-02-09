package com.hs5233.timetable;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Other extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other);
        init();
    }
    private void init(){
        Button timetable = (Button)findViewById(R.id.button);
        timetable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        final Button scoresInfo = (Button)findViewById(R.id.button3);
        scoresInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent _intent = new Intent(Other.this,scoresInfo.class);
                startActivity(_intent);
            }
        });
        final Button examInfo = (Button)findViewById(R.id.button4);
        examInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent _intent = new Intent(Other.this,examInfo.class);
                startActivity(_intent);
            }
        });
    }
}
