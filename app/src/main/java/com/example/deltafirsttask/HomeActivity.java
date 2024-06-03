package com.example.deltafirsttask;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import android.content.DialogInterface;

import java.util.ArrayList;
import java.util.List;
import android.view.WindowManager;

public class HomeActivity extends AppCompatActivity {

    private List<String> winners;
    private FirebaseFirestore db;

    private MediaPlayer mp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        db = FirebaseFirestore.getInstance();

        winners = new ArrayList<>();

        retrievewinner();

        findViewById(R.id.help).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mp != null) {
                    mp.release();
                }
                mp = MediaPlayer.create(HomeActivity.this, R.raw.click);
                mp.start();
                showhelp();
            }
        });


        findViewById(R.id.play).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mp != null) {
                    mp.release();
                }
                mp = MediaPlayer.create(HomeActivity.this, R.raw.click);
                mp.start();
                Intent intent = new Intent(HomeActivity.this, login.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
            }
        });


        findViewById(R.id.winners).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mp != null) {
                    mp.release();
                }
                mp = MediaPlayer.create(HomeActivity.this, R.raw.click);
                mp.start();
                Intent intent = new Intent(HomeActivity.this, WinnersActivity.class);
                intent.putStringArrayListExtra("winnersList", (ArrayList<String>) winners);
                startActivity(intent);
            }
        });
    }

    private void retrievewinner() {
        db.collection("winners")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String winnerName = document.getString("name");
                                winners.add(winnerName);
                            }

                        }
                    }
                });
    }

    private void showhelp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.activity_help, null);
        TextView helpTextView = dialogLayout.findViewById(R.id.helptext);

        helpTextView.setText("Rules:\n\n"
                + "1. Each player takes turns placing their tiles on the gameboard.\n"
                + "2. On your first turn, you can place your tile anywhere on the board and it will have 3 points.\n"
                + "3. On subsequent turns, you can place your tile on your own tile or an empty space.\n"
                + "4. If you place your tile on your own tile, its points increase by 1.\n"
                + "5. If your tile reaches 4 points, it expands to adjacent tiles (up,down,right and left).\n"
                + "6. The tile that is expanded will disappear and the adjacent tiles will become yours by adding one point to each tile.\n"
                + "7. The game ends when one player has no more tiles left or your opponent runs out of time.\n"
                + "8. The player with the most points wins.");

        builder.setView(dialogLayout)
                .setPositiveButton("Got it!", (dialog, which) -> dialog.dismiss())
                .show();
    }

    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Exit")
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        HomeActivity.super.onBackPressed();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
}
