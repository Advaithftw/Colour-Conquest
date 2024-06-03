package com.example.deltafirsttask;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import androidx.appcompat.app.AppCompatActivity;

public class splash extends AppCompatActivity {
    private MediaPlayer mp;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        if (mp != null) {
            mp.release();
        }
        mp = MediaPlayer.create(splash.this, R.raw.logo);
        mp.start();
        Handler handler =new Handler();
        handler.postDelayed(new Runnable(){
            @Override
            public void run() {

                Intent i =new Intent(splash.this,HomeActivity.class);
                startActivity(i);
                overridePendingTransition(R.anim.pop_in, R.anim.fade_out);

                finish();
            }
        },5000);
    }
}
