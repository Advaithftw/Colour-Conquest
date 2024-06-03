package com.example.deltafirsttask;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.GridLayout;
import android.widget.TextView;

public class GameBoard {
    private int numRows;
    private int numCols;
    private Context context;
    private GridLayout grid;
    
    private tile[][] tiles;

    public GameBoard(Context context, GridLayout grid, int numRows, int numCols) {
        this.context = context;
        this.grid = grid;
        this.numRows = numRows;
        this.numCols = numCols;
        tiles = new tile[numRows][numCols];
        setboard();
    }


    private void setboard() {
        grid.removeAllViews();
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                TextView tile = new TextView(context);
                tile.setBackgroundColor(Color.WHITE);
                tile.setTextColor(Color.BLACK);
                tile.setTextSize(18);
                tile.setGravity(Gravity.CENTER);
                tile.setBackgroundResource(R.drawable.outline);

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = 0;
                params.height = 0;
                params.rowSpec = GridLayout.spec(row, 1, 1f);
                params.columnSpec = GridLayout.spec(col, 1, 1f);
                tile.setLayoutParams(params);

                grid.addView(tile);
                tiles[row][col] = new tile(tile, 0, Color.WHITE);
            }
        }
    }


    public tile gettile(int row, int col) {
        
        return tiles[row][col];
    }

    public static class tile {
        TextView textView;
        public int points;
        public int color;

        public tile(TextView textView, int points, int color) {
            this.textView = textView;
            this.points = points;
            this.color = color;
        }

        public void setColor(int color) {
            this.color = color;
            textView.setBackgroundColor(color);
        }

        public void setPoints(int points) {
            this.points = points;
            textView.setText(String.valueOf(points));
        }

        public void removePoints(int points) {
            this.points = points;
            if (textView != null) {
                textView.setText("");
            }
        }
    }
}
