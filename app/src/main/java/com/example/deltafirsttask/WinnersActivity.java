package com.example.deltafirsttask;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;
import android.view.WindowManager;

public class WinnersActivity extends AppCompatActivity {

    private RecyclerView recyclerview;
    private WinnersAdapter adapter;
    private List<String> winners;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_winners);
        recyclerview = findViewById(R.id.winnersrecycler);
        recyclerview.setLayoutManager(new LinearLayoutManager(this));
        winners = new ArrayList<>();
        adapter = new WinnersAdapter(winners);
        recyclerview.setAdapter(adapter);
        db = FirebaseFirestore.getInstance();
        fetchWinnersFromFirebase();
    }
    private void fetchWinnersFromFirebase() {
        db.collection("winners")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String winnerName = document.getString("name");
                                winners.add(winnerName);
                            }
                            adapter.notifyDataSetChanged();
                        } else {
                        }
                    }
                });
    }
}
