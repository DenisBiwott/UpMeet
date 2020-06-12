package com.android.buukrides;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class HistoryActivity extends AppCompatActivity {
    private String userID, user_id;
    private FirebaseUser user;
    private FirebaseAuth mAuth;
    private DatabaseReference DatabaseRef;
    private RecyclerView mRecHistory;
    private TextView mTxtNoHistory;
    private LinearLayoutManager layoutManager;
    private Query userHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        userID = user.getUid();
        DatabaseRef = FirebaseDatabase.getInstance().getReference();
        DatabaseRef.keepSynced(true);
        mRecHistory =   findViewById(R.id.RecHistory);
        mTxtNoHistory = findViewById(R.id.txtNoHistory);

        mRecHistory.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        mRecHistory.setLayoutManager(layoutManager);

        userHistory = DatabaseRef.child("Bookings").equalTo(userID).orderByChild("userID");
        userHistory.keepSynced(true);

        userHistory.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    mTxtNoHistory.setVisibility(View.GONE);
                }else{
                    mTxtNoHistory.setVisibility(View.VISIBLE);
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
                R.layout.layout_history,
                VenueViewHolder.class,
                userHistory
        )
        {
            @Override
            protected void populateViewHolder(final HistoryActivity.VenueViewHolder viewHolder, Friends model, int position) {

                final String booking_id = getRef(position).getKey();

                DatabaseRef.child("Bookings").child(booking_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String owner_id = (String) dataSnapshot.child("ownerID").getValue();
                        String date = (String) dataSnapshot.child("date").getValue();
                        String facility_id = (String) dataSnapshot.child("facility_id").getValue();
                        String venue_id = (String) dataSnapshot.child("venue_id").getValue();
                        DatabaseReference mDtbOwnerInfo = DatabaseRef.child("Users").child(owner_id);
                        DatabaseReference mDtbVenueInfo = DatabaseRef.child("Venues").child(venue_id);
                        mDtbOwnerInfo.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                String owner_name = (String) dataSnapshot.child("username").getValue();
                                String owner_phone = (String) dataSnapshot.child("phone").getValue();
                                viewHolder.mTxtOwner.setText(owner_name);
                                viewHolder.mTxtHistoryOwnerPhone.setText(owner_phone);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                        mDtbVenueInfo.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                String venue_name = (String) dataSnapshot.child("PlaceName").getValue();
                                viewHolder.mTxtVenue.setText(venue_name);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                        mDtbVenueInfo.child("Facility").child(facility_id).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                String facility_name = (String) dataSnapshot.child("Name").getValue();
                                String cost = (String) dataSnapshot.child("Cost").getValue();
                                viewHolder.mTxtFacility.setText(facility_name);
                                viewHolder.mTxtHistoryPrice.setText(cost);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                        viewHolder.mTxtDate.setText(date);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });



            }
        };

        mRecHistory.setAdapter(firebaseRecyclerAdapter);


    }

    public static class VenueViewHolder extends RecyclerView.ViewHolder {

        View mView;
        TextView mTxtDate, mTxtFacility, mTxtVenue, mTxtOwner, mTxtHistoryPrice, mTxtHistoryOwnerPhone;


        public VenueViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

            mTxtOwner = mView.findViewById(R.id.txtHistoryOwner);
            mTxtDate = mView.findViewById(R.id.txtHistoryDate);
            mTxtFacility = mView.findViewById(R.id.txtHistoryFacility);
            mTxtVenue = mView.findViewById(R.id.txtHistoryVenue);
            mTxtHistoryPrice = mView.findViewById(R.id.txtHistoryPrice);
            mTxtHistoryOwnerPhone = mView.findViewById(R.id.txtHistoryOwnerPhone);


        }
    }
}
