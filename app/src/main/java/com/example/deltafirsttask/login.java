package com.example.deltafirsttask;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.view.WindowManager;

public class login extends AppCompatActivity {

    private EditText pl1name, pl2name, rows, cols, bestof;
    private Button playbtn;

    private MediaPlayer mediaPlayer;
    private CheckBox timed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        pl1name = findViewById(R.id.pl1name);
        pl2name = findViewById(R.id.pl2name);
        rows = findViewById(R.id.rows);
        cols = findViewById(R.id.columns);
        bestof = findViewById(R.id.Bestof);
        playbtn = findViewById(R.id.buttonPlay);
        timed = findViewById(R.id.checkBoxTimed);

        playbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get player names from EditText fields
                String pl1name = login.this.pl1name.getText().toString().trim();
                String pl2name = login.this.pl2name.getText().toString().trim();
                String rows = login.this.rows.getText().toString().trim();
                String cols = login.this.cols.getText().toString().trim();
                String bestof = login.this.bestof.getText().toString().trim();

                if (mediaPlayer != null) {
                    mediaPlayer.release();
                }
                mediaPlayer = MediaPlayer.create(login.this, R.raw.click);
                mediaPlayer.start();


                if (!pl1name.isEmpty() && !pl2name.isEmpty() && !rows.isEmpty() && !cols.isEmpty() && !bestof.isEmpty()) {
                    int numrows = Integer.parseInt(rows);
                    int numcols = Integer.parseInt(cols);
                    int numMatches = Integer.parseInt(bestof);
                    if (numrows < 1 || numcols < 1 ) {
                        Toast.makeText(login.this, "Rows and columns must be at least 1", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Intent i;
                    if (timed.isChecked()) {
                        i = new Intent(login.this, TimedActivity.class);

                    } else {
                        i = new Intent(login.this, MainActivity.class);
                    }
                    i.putExtra("PLAYER1_NAME", pl1name);
                    i.putExtra("PLAYER2_NAME", pl2name);
                    i.putExtra("NUM_ROWS", numrows);
                    i.putExtra("NUM_COLS", numcols);
                    i.putExtra("NUM_MATCHES", numMatches);
                    startActivity(i);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.silde_out_right);
                } else {

                    Toast.makeText(login.this, "Please enter player names, board size, and number of matches", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
