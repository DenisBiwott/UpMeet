package com.android.buukrides;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class MyVenuesActivity extends AppCompatActivity {
    private DatabaseReference DatabaseRef;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private String userID;
    private RecyclerView mRecMyVenues;
    private Query venuesOwnedByUser;
    private TextView mTxtNoVenues;
    private RecyclerView.LayoutManager layoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_venues);

        FloatingActionButton floating_b = findViewById(R.id.floating_b);
        floating_b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MyVenuesActivity.this, AddVenueActivity.class));
            }
        });

        //mLstMyVenues = findViewById(R.id.lstMyVenues);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        userID = user.getUid();
        DatabaseRef = FirebaseDatabase.getInstance().getReference().child("Venues");
        DatabaseRef.keepSynced(true);
        mRecMyVenues =   findViewById(R.id.RecMyVenues);
        mTxtNoVenues = findViewById(R.id.txtNoVenues);

        mRecMyVenues.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        mRecMyVenues.setLayoutManager(layoutManager);

        venuesOwnedByUser = DatabaseRef.equalTo(userID).orderByChild("UserID");
        venuesOwnedByUser.keepSynced(true);
                
        venuesOwnedByUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    mTxtNoVenues.setVisibility(View.GONE);
                }else{
                    mTxtNoVenues.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Friends, VenueViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Friends, VenueViewHolder>(

                Friends.class,
                R.layout.blog_row_friends,
                VenueViewHolder.class,
                venuesOwnedByUser
        )
        {
            @Override
            protected void populateViewHolder(final VenueViewHolder viewHolder, Friends model, int position) {

                //final String post_key = getRef(position).getKey();

                // ---- VENUE ID ------
                final String venue_id = getRef(position).getKey();

                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent intent = new Intent(MyVenuesActivity.this, VenueDetailsActivity.class);
                        intent.putExtra("venue_id", venue_id);
                        startActivity(intent);
                        
                        
                    }
                });

                DatabaseRef.child(venue_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String desc = (String) dataSnapshot.child("Description").getValue();
                        String name = (String) dataSnapshot.child("PlaceName").getValue();

                        viewHolder.mTxtDesc.setText(desc);
                        viewHolder.mTxtName.setText(name);
                        
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            
            }
        };

        mRecMyVenues.setAdapter(firebaseRecyclerAdapter);


    }

    public static class VenueViewHolder extends RecyclerView.ViewHolder {

        View mView;
        TextView mTxtDesc, mTxtName;


        public VenueViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

            mTxtDesc = mView.findViewById(R.id.txtDesc);
            mTxtName = mView.findViewById(R.id.txtName);


        }
    }
}