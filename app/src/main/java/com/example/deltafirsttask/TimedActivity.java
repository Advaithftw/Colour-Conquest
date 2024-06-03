package com.example.deltafirsttask;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;

import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.DialogInterface;
import android.content.Intent;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;

import java.util.HashMap;
import java.util.Map;
import android.view.WindowManager;

public class TimedActivity extends AppCompatActivity {

    private GameBoard gameBoard;
    private boolean p1turn = true;
    private boolean p1firstturn = true;
    private boolean p2firstturn = true;
    private String p1name;
    private String p2name;
    private TextView currentpl;

    private TextView p1time;
    private TextView p2time;
    private Button p1points;
    private Button p2points;
    private FirebaseFirestore db;
    private CountDownTimer p1timer;
    private CountDownTimer p2timer;
    private int totalmatches;
    private int winmatches;
    private int matchesplayed;

    private long p1timeleft = turntime;
    private long p2timeleft = turntime;
    private static final long turntime = 60000;

    private int rows = 5;
    private int cols = 5;
    private int p1wins;
    private int p2wins;

    private MediaPlayer mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timed);

        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();

        Intent intent = getIntent();
        rows = intent.getIntExtra("NUM_ROWS", 5);
        cols = intent.getIntExtra("NUM_COLS", 5);

        GridLayout gridLayout = findViewById(R.id.gameboard);

        gridLayout.setRowCount(rows);
        gridLayout.setColumnCount(cols);
        gameBoard = new GameBoard(this, gridLayout, rows, cols);

        currentpl = findViewById(R.id.turn);
        p1time = findViewById(R.id.player1timer);
        p2time = findViewById(R.id.player2timer);
        p1name = intent.getStringExtra("PLAYER1_NAME");
        p2name = intent.getStringExtra("PLAYER2_NAME");
        totalmatches = getIntent().getIntExtra("NUM_MATCHES", 3);
        winmatches = (totalmatches / 2) + 1;

        p1points = findViewById(R.id.player1score);
        p2points = findViewById(R.id.player2score);
        Button resetButton = findViewById(R.id.reset);

        if (p1name != null && p2name != null) {
            p1turn = true;
            updateCurrentPlayerDisplay();
            p1time.setText(p1name + "\nTime left: " + turntime / 1000);
            p2time.setText(p2name + "\nTime left: " + turntime / 1000);
            startPlayerOneTimer();

        } else {
            Log.e("TimedActivity", "Player names are null");
        }

        settileClickListeners();

        resetButton.setOnClickListener(v -> resetGameBoard());
    }

    private void settileClickListeners() {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                GameBoard.tile tile = gameBoard.gettile(row, col);
                tile.textView.setBackgroundResource(R.drawable.outline);
                final int finalRow = row;
                final int finalCol = col;
                tile.textView.setOnClickListener(view -> ontileClicked(tile, finalRow, finalCol));
            }
        }
    }


    private void ontileClicked(GameBoard.tile tile, int row, int col) {
        int playerColor;
        if (mp != null) {
            mp.release();
        }
        mp = MediaPlayer.create(this, R.raw.pop);
        mp.start();
        if (p1turn) {
            playerColor = ContextCompat.getColor(this, R.color.blue);
        } else {
            playerColor = ContextCompat.getColor(this, R.color.redd);
        }

        boolean isFirstTurn;
        if (p1turn) {
            isFirstTurn = p1firstturn;
        } else {
            isFirstTurn = p2firstturn;
        }

        if (tile.color == Color.WHITE || tile.color == playerColor) {
            playerTurn(tile, row, col, playerColor, isFirstTurn);
            if (p1turn) {
                p1firstturn = false;
            } else {
                p2firstturn = false;
            }
            p1turn = !p1turn;
            updateCurrentPlayerDisplay();
            updatePlayerScores();
            if (p1turn) {
                startPlayerOneTimer();
            } else {
                startPlayerTwoTimer();
            }
            if (!p1firstturn && !p2firstturn) {
                checkForWin();
            }
        } else {
            Toast.makeText(this, "Invalid move", Toast.LENGTH_SHORT).show();
        }
    }

    private void playerTurn(GameBoard.tile square, int row, int col, int playerColor, boolean isFirstTurn) {
        if (isFirstTurn || square.color == playerColor || square.color == Color.WHITE) {
            if (isFirstTurn || square.color == Color.WHITE) {
                square.setPoints(3);
            } else {
                square.setPoints(square.points + 1);
            }
            square.setColor(playerColor);

            if (square.points >= 4) {
                expand(square, row, col, playerColor);
            }
        } else {
            Toast.makeText(this, "Invalid move", Toast.LENGTH_SHORT).show();
        }
    }

    private void expand(GameBoard.tile tile, int row, int col, int playerColor) {
        tile.removePoints(0);
        tile.setColor(WHITE);

        if (mp != null) {
            mp.release();
        }
        mp = MediaPlayer.create(this, R.raw.expand);
        mp.start();

        int[] deltaRow = {-1, 1, 0, 0};
        int[] deltaCol = {0, 0, -1, 1};

        for (int i = 0; i < 4; i++) {
            int newRow = row + deltaRow[i];
            int newCol = col + deltaCol[i];

            if (newRow >= 0 && newRow < rows && newCol >= 0 && newCol < cols) {
                GameBoard.tile adjacenttile = gameBoard.gettile(newRow, newCol);
                adjacenttile.setPoints(adjacenttile.points + 1);
                adjacenttile.setColor(playerColor);

                if (adjacenttile.points >= 4) {
                    expand(adjacenttile, newRow, newCol, playerColor);
                }
            }
        }
    }

    private void updateCurrentPlayerDisplay() {
        RelativeLayout layout = findViewById(R.id.layouts);
        if (currentpl == null) {
            Log.e("MainActivity", "currentPlayerTextView is null");
            return;
        }
        if (p1turn) {
            currentpl.setText(p1name + "'s Turn (Blue)");
            currentpl.setBackgroundColor(ContextCompat.getColor(this, R.color.blue));
            currentpl.setTextColor(BLACK);
            layout.setBackgroundColor(ContextCompat.getColor(this, R.color.blue));
        } else {
            currentpl.setText(p2name + "'s Turn (Red)");
            currentpl.setBackgroundColor(ContextCompat.getColor(this, R.color.redd));
            currentpl.setTextColor(BLACK);
            layout.setBackgroundColor(ContextCompat.getColor(this, R.color.redd));
        }
    }

    private void updatePlayerScores() {
        int player1Score = 0;
        int player2Score = 0;
        int player1Color = ContextCompat.getColor(this, R.color.blue);
        int player2Color = ContextCompat.getColor(this, R.color.redd);

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                GameBoard.tile tile = gameBoard.gettile(row, col);
                if (tile.color == player1Color) {
                    player1Score += tile.points;
                } else if (tile.color == player2Color) {
                    player2Score += tile.points;
                }
            }
        }

        p1points.setText("Score: " + player1Score);
        p2points.setText("Score: " + player2Score);
    }


    private void checkForWin() {
        boolean pl1tiles = false;
        boolean pl2tiles = false;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                GameBoard.tile tile = gameBoard.gettile(row, col);
                if (tile.color == ContextCompat.getColor(this, R.color.blue)) {
                    pl1tiles = true;
                } else if (tile.color == ContextCompat.getColor(this, R.color.redd)) {
                    pl2tiles = true;
                }
            }
        }

        if (!pl1tiles || !pl2tiles) {
            String winnerName = pl1tiles ? p1name : p2name;
            if (winnerName.equals(p1name)) {
                p1wins++;
            } else {
                p2wins++;
            }

            matchesplayed++;

            if (p1wins >= winmatches || p2wins >= winmatches) {
                if (p1wins == p2wins) {
                    showgamedraw();
                } else {
                    saveWinnerToFirestore(winnerName);
                    showgamewin(winnerName);
                }
            } else {
                showmatchwin(winnerName);
            }
        }
    }



    private void saveWinnerToFirestore(String winnerName) {
        Map<String, Object> winner = new HashMap<>();
        winner.put("name", winnerName);
        winner.put("timestamp", FieldValue.serverTimestamp());

        db.collection("winners")
                .add(winner)
                .addOnSuccessListener(documentReference -> Log.d("MainActivity", "Winner added with ID: " + documentReference.getId()))
                .addOnFailureListener(e -> Log.w("MainActivity", "Error adding winner", e));
    }

    private void resetGameBoard() {

        if (mp != null) {
            mp.release();
        }
        mp = MediaPlayer.create(this, R.raw.reset);
        mp.start();

        if (p1timer != null) {
            p1timer.cancel();
        }
        if (p2timer != null) {
            p2timer.cancel();
        }
        p1timeleft = turntime;
        p2timeleft = turntime;
        p1time.setText(p1name + "\nTime left: " + turntime / 1000);
        p2time.setText(p2name + "\nTime left: " + turntime / 1000);

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                GameBoard.tile tile = gameBoard.gettile(row, col);
                tile.removePoints(0);
                tile.setColor(Color.WHITE);
                tile.textView.setBackgroundResource(R.drawable.outline);
            }
        }
        p1turn = true;
        p1firstturn = true;
        p2firstturn = true;
        updateCurrentPlayerDisplay();
        updatePlayerScores();
        startPlayerOneTimer();
    }
    private void startPlayerOneTimer() {
        if (p2timer != null) {
            p2timer.cancel();
        }

        p1timer = new CountDownTimer(p1timeleft, 1000) {
            public void onTick(long millisUntilFinished) {
                p1timeleft = millisUntilFinished;
                p1time.setText(p1name + "\nTime left: " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                p2wins++;
                matchesplayed++;
                if (p2wins >= winmatches) {
                    saveWinnerToFirestore(p2name);  // Save winner to Firestore
                    showgamewin(p2name);
                } else if (matchesplayed == totalmatches && p1wins == p2wins) {
                    showgamedraw();
                } else {
                    showmatchwin(p2name);
                }
            }

        }.start();
    }

    private void startPlayerTwoTimer() {
        if (p1timer != null) {
            p1timer.cancel();
        }

        p2timer = new CountDownTimer(p2timeleft, 1000) {
            public void onTick(long millisUntilFinished) {
                p2timeleft = millisUntilFinished;
                p2time.setText(p2name + "\nTime left: " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                p1wins++;
                matchesplayed++;
                if (p1wins >= winmatches) {
                    saveWinnerToFirestore(p1name);
                    showgamewin(p1name);
                } else if (matchesplayed == totalmatches && p2wins == p1wins) {
                    showgamedraw();
                } else {
                    showmatchwin(p1name);
                }
            }
        }.start();
    }






    private void showmatchwin(String winnerName) {
        new AlertDialog.Builder(this)
                .setTitle("Match Over")
                .setMessage(winnerName + " has won this match!")
                .setNegativeButton("Home", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(TimedActivity.this, HomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish(); // Finish the current activity to prevent going back to the dialog
                    }
                })
                .setPositiveButton("Next Match", (dialog, which) -> resetGameBoard())
                .show();
    }
    private void showgamedraw()
    {
        new AlertDialog.Builder(this)
                .setTitle("Game Over")
                .setMessage("It's a Draw!")
                .setNegativeButton("Home", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(TimedActivity.this, HomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                })
                .setPositiveButton("Restart Game", (dialog, which) -> {
                    p1wins = 0;
                    p2wins = 0;
                    matchesplayed = 0;
                    resetGameBoard();
                })
                .show();

    }

    private void showgamewin(String winnerName) {
        new AlertDialog.Builder(this)
                .setTitle("Game Over")
                .setMessage(winnerName + " has won the Game!")
                .setNegativeButton("Home", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(TimedActivity.this, HomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                })
                .setPositiveButton("Restart Game", (dialog, which) -> {
                    p1wins = 0;
                    p2wins = 0;
                    matchesplayed = 0;
                    resetGameBoard();
                })
                .show();
    }
}