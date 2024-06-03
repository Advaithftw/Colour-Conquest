package com.example.deltafirsttask;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;

import android.media.MediaPlayer;
import android.view.WindowManager;
import android.graphics.Color;
import android.os.Bundle;
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

public class MainActivity extends AppCompatActivity {

    private GameBoard gameBoard;
    private boolean p1turn = true;
    private boolean p1firstturn = true;
    private boolean p2firstturn = true;
    private String p1name;
    private String p2name;
    private TextView currentPlayerTextView;
    private Button p1points;
    private Button p2points;
    private FirebaseFirestore db;
    private int rows = 5;
    private int cols = 5;

    private MediaPlayer mp;

    private int totalmatches;
    private int matchesToWin;
    private int player1Wins;
    private int player2Wins;
    private int matchesPlayed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();

        GridLayout gridLayout = findViewById(R.id.gameboard);
        p1name = getIntent().getStringExtra("PLAYER1_NAME");
        p2name = getIntent().getStringExtra("PLAYER2_NAME");
        rows = getIntent().getIntExtra("NUM_ROWS", 5);
        cols = getIntent().getIntExtra("NUM_COLS", 5);
        totalmatches = getIntent().getIntExtra("NUM_MATCHES", 3);

        matchesToWin = (totalmatches / 2) + 1;

        gridLayout.setRowCount(rows);
        gridLayout.setColumnCount(cols);
        gameBoard = new GameBoard(this, gridLayout, rows, cols);

        currentPlayerTextView = findViewById(R.id.turn);

        p1points = findViewById(R.id.player1score);
        p2points = findViewById(R.id.player2score);
        Button resetButton = findViewById(R.id.reset);

        if (p1name != null && p2name != null) {
            p1turn = true;
            updateCurrentPlayerDisplay();
        } else {
            Log.e("MainActivity", "Player names are null");
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
            // Initialize and play the click sound
            if (mp != null) {
                mp.release();
            }
            mp = MediaPlayer.create(this, R.raw.pop);
            mp.start();

            playerTurn(tile, row, col, playerColor, isFirstTurn);
            if (p1turn) {
                p1firstturn = false;
            } else {
                p2firstturn = false;
            }
            p1turn = !p1turn;
            updateCurrentPlayerDisplay();
            updatePlayerScores();
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
        if (currentPlayerTextView == null) {
            Log.e("MainActivity", "currentPlayerTextView is null");
            return;
        }
        if (p1turn) {
            currentPlayerTextView.setText(p1name + "'s Turn (Blue)");
            currentPlayerTextView.setBackgroundColor(ContextCompat.getColor(this, R.color.blue));
            currentPlayerTextView.setTextColor(BLACK);
            layout.setBackgroundColor(ContextCompat.getColor(this, R.color.blue));
        } else {
            currentPlayerTextView.setText(p2name + "'s Turn (Red)");
            currentPlayerTextView.setBackgroundColor(ContextCompat.getColor(this, R.color.redd));
            currentPlayerTextView.setTextColor(BLACK);
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

        p1points.setText(p1name + "'s Score: " + player1Score);
        p2points.setText(p2name + "'s Score: " + player2Score);
    }

    private void checkForWin() {
        boolean playerOneHasTiles = false;
        boolean playerTwoHasTiles = false;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                GameBoard.tile tile = gameBoard.gettile(row, col);
                if (tile.color == ContextCompat.getColor(this, R.color.blue)) {
                    playerOneHasTiles = true;
                } else if (tile.color == ContextCompat.getColor(this, R.color.redd)) {
                    playerTwoHasTiles = true;
                }
            }
        }

        if (!playerOneHasTiles || !playerTwoHasTiles) {
            String winnerName = playerOneHasTiles ? p1name : p2name;
            if (winnerName.equals(p1name)) {
                player1Wins++;
            } else {
                player2Wins++;
            }

            matchesPlayed++;

            if (player1Wins >= matchesToWin || player2Wins >= matchesToWin) {
                if (player1Wins == player2Wins) {
                    showGameDrawDialog();
                } else {
                    saveWinnerToFirestore(winnerName);  // Save winner to Firestore
                    showGameWinDialog(winnerName);
                }
            } else {
                showMatchWinDialog(winnerName);
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
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                GameBoard.tile tile = gameBoard.gettile(row, col);
                tile.removePoints(0);
                tile.setColor(Color.WHITE);
                tile.textView.setBackgroundResource(R.drawable.outline);
            }
        }
        if (mp != null) {
            mp.release();
        }
        mp = MediaPlayer.create(this, R.raw.reset);
        mp.start();
        p1turn = true;
        p1firstturn = true;
        p2firstturn = true;
        updateCurrentPlayerDisplay();
        updatePlayerScores();
    }

    private void showMatchWinDialog(String winnerName) {
        new AlertDialog.Builder(this)
                .setTitle("Match Over")
                .setMessage(winnerName + " has won this match!")
                .setNegativeButton("Home", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish(); // Finish the current activity to prevent going back to the dialog
                    }
                })
                .setPositiveButton("Next Match", (dialog, which) -> resetGameBoard())
                .show();
    }

    private void showGameDrawDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Game Over")
                .setMessage("It's a Draw!")
                .setNegativeButton("Home", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish(); // Finish the current activity to prevent going back to the dialog
                    }
                })
                .setPositiveButton("Restart Game", (dialog, which) -> {
                    player1Wins = 0;
                    player2Wins = 0;
                    matchesPlayed = 0;
                    resetGameBoard();
                })
                .show();
    }

    private void showGameWinDialog(String winnerName) {
        new AlertDialog.Builder(this)
                .setTitle("Game Over")
                .setMessage(winnerName + " has won the Game!")
                .setNegativeButton("Home", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish(); // Finish the current activity to prevent going back to the dialog
                    }
                })
                .setPositiveButton("Restart Game", (dialog, which) -> {
                    player1Wins = 0;
                    player2Wins = 0;
                    matchesPlayed = 0;
                    resetGameBoard();
                })
                .show();
    }
}
