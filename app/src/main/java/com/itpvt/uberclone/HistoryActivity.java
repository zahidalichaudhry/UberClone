package com.itpvt.uberclone;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.itpvt.uberclone.historyRecyclerView.HistoryAdapter;
import com.itpvt.uberclone.historyRecyclerView.HistoryObject;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {
    private String customerOrDriver, userId;
    private RecyclerView mHistoryRecyclerView;
    private RecyclerView.Adapter mHistoryAdapter;
    private RecyclerView.LayoutManager mHistoryLayoutManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        mHistoryRecyclerView = (RecyclerView) findViewById(R.id.historyRecyclerView);
        mHistoryRecyclerView.setNestedScrollingEnabled(false);
        mHistoryRecyclerView.setHasFixedSize(true);
        mHistoryLayoutManager=new LinearLayoutManager(HistoryActivity.this);
        mHistoryRecyclerView.setLayoutManager(mHistoryLayoutManager);
        mHistoryAdapter=new HistoryAdapter(getDataSetHistory(),HistoryActivity.this);
        mHistoryRecyclerView.setAdapter(mHistoryAdapter);

        customerOrDriver = getIntent().getExtras().getString("customerOrDriver");
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        getUserHistoryIds();
    }

    private void getUserHistoryIds()
    {
        DatabaseReference userHistoryDatabase = FirebaseDatabase.getInstance().getReference()
                .child("Users").child(customerOrDriver).child(userId).child("history");
        userHistoryDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot history : dataSnapshot.getChildren()){
                        FetchRideInformation(history.getKey());
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void FetchRideInformation(String ridekey)
    {
        DatabaseReference historyDatabase = FirebaseDatabase.getInstance().getReference()
                .child("history").child(ridekey);
        historyDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String rideId= dataSnapshot.getKey();
                    Long timestamp=0L;
                    if(dataSnapshot.child("timestamp").getValue() != null){
                        timestamp = Long.valueOf(dataSnapshot.child("timestamp").getValue().toString());
                    }
                    HistoryObject obj=new HistoryObject(rideId, getDate(timestamp));
                    resultHistory.add(obj);
                    mHistoryAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private String getDate(Long timestamp)
    {
        Calendar cal=Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(timestamp*1000);
        String data= DateFormat.format("dd-MM-yyyy hh:mm",cal).toString();

       return data;
    }

    private ArrayList resultHistory=new ArrayList<HistoryObject>();
    private ArrayList<HistoryObject> getDataSetHistory()
    {
        return resultHistory;
    }
}
