package com.android.buukrides;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class VenueDetailsActivity extends AppCompatActivity {

    private DatabaseReference DatabaseRef;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private String userID;
    private RecyclerView mRecylcerVenues;
    private Query venuesOwnedByUser;
    private TextView mTxtNoVenues;
    private String venue_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_venue_details);

        venue_id = getIntent().getStringExtra("venue_id");

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        userID = user.getUid();
        DatabaseRef = FirebaseDatabase.getInstance().getReference().child("Venues").child(venue_id);
        DatabaseRef.keepSynced(true);
        mRecylcerVenues =   findViewById(R.id.facilities_list);
        mTxtNoVenues = findViewById(R.id.txtNoVenues);

        mRecylcerVenues.setHasFixedSize(true);
        mRecylcerVenues.setLayoutManager(new LinearLayoutManager(VenueDetailsActivity.this));

        venuesOwnedByUser = DatabaseRef.orderByChild("Name");
        venuesOwnedByUser.keepSynced(true);
                
        // venuesOwnedByUser.addValueEventListener(new ValueEventListener() {
        //     @Override
        //     public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        //         if(dataSnapshot.exists()){
            //             mTxtNoVenues.setVisibility(View.GONE);
        //         }else{
        //             mTxtNoVenues.setVisibility(View.VISIBLE);
        //         }
        //     }

        //     @Override
        //     public void onCancelled(@NonNull DatabaseError databaseError) {

        //     }
        // });
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Friends, VenueViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Friends, VenueViewHolder>(

                Friends.class,
                R.layout.layout_facilities,
                VenueViewHolder.class,
                venuesOwnedByUser
        )
        {
            @Override
            protected void populateViewHolder(final VenueViewHolder viewHolder, Friends model, int position) {

                //final String post_key = getRef(position).getKey();

                // ---- VENUE ID ------
                final String facility_id = getRef(position).getKey();

                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent intent = new Intent(VenueDetailsActivity.this, FacilityDetailsActivity.class);
                        intent.putExtra("facility_id", facility_id);
                        startActivity(intent);
                        
                        
                    }
                });

                DatabaseRef.child(facility_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String capacity = (String) dataSnapshot.child("Capacity").getValue();
                        String name = (String) dataSnapshot.child("Name").getValue();

                        viewHolder.mTxtCapacity.setText(capacity);
                        viewHolder.mTxtName.setText(name);
                        
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            
            }
        };

        mRecylcerVenues.setAdapter(firebaseRecyclerAdapter);


    }

    public static class VenueViewHolder extends RecyclerView.ViewHolder {

        View mView;
        TextView mTxtCapacity, mTxtName;


        public VenueViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

            mTxtCapacity = (TextView) mView.findViewById(R.id.txtFacilityCapacity);
            mTxtName = (TextView) mView.findViewById(R.id.txtFacilityName);


        }
    }
}
