package com.example.deltafirsttask;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class WinnersAdapter extends RecyclerView.Adapter<WinnersAdapter.WinnerViewHolder> {

    private List<String> winnerlist;

    public WinnersAdapter(List<String> winnerlist) {
        this.winnerlist = winnerlist;
    }

    @NonNull
    @Override
    public WinnerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new WinnerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WinnerViewHolder holder, int position) {
        if (winnerlist.isEmpty()) {
            holder.bindNoWinnersMessage();
        } else {
            String winner = winnerlist.get(position);
            holder.bindWinner(winner);
        }
    }

    @Override
    public int getItemCount() {
        if (winnerlist.isEmpty()) {
            return 1;
        } else {
            return winnerlist.size();
        }
    }

    static class WinnerViewHolder extends RecyclerView.ViewHolder {
        TextView winnerTextView;
        WinnerViewHolder(@NonNull View itemView) {
            super(itemView);
            winnerTextView = itemView.findViewById(android.R.id.text1);
        }void bindWinner(String winner) {
            winnerTextView.setText(winner);
        }void bindNoWinnersMessage() {
            winnerTextView.setText("No winners yet");
        }
    }
}
