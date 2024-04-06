package com.example.snakegame;

import android.content.Intent;
import android.os.Bundle;


import android.app.Activity;
import android.view.View;


public class EndingActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dead_page);
    }




    public void quit(View view) {
        finishAffinity();
    }


    public void play(View view){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

}
