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

public class BookingsActivity extends AppCompatActivity {
    private String facility_id, venue_id, userID, user_id;
    private FirebaseUser user;
    private FirebaseAuth mAuth;
    private DatabaseReference DatabaseRef;
    private RecyclerView mRecBookings;
    private TextView mTxtNoBookings;
    private LinearLayoutManager layoutManager;
    private Query facilityBookings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookings);
        facility_id = getIntent().getStringExtra("facility_id");
        venue_id = getIntent().getStringExtra("venue_id");
        
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        userID = user.getUid();
        DatabaseRef = FirebaseDatabase.getInstance().getReference().child("Bookings");
        DatabaseRef.keepSynced(true);
        mRecBookings =   findViewById(R.id.RecBookings);
        mTxtNoBookings = findViewById(R.id.txtNoBookings);

        mRecBookings.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        mRecBookings.setLayoutManager(layoutManager);

        facilityBookings = DatabaseRef.equalTo(facility_id).orderByChild("facility_id");
        facilityBookings.keepSynced(true);

        facilityBookings.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    mTxtNoBookings.setVisibility(View.GONE);
                }else{
                    mTxtNoBookings.setVisibility(View.VISIBLE);
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
                R.layout.layout_bookings,
                VenueViewHolder.class,
                facilityBookings
        )
        {
            @Override
            protected void populateViewHolder(final BookingsActivity.VenueViewHolder viewHolder, Friends model, int position) {

                //final String post_key = getRef(position).getKey();

                // ---- VENUE ID ------
                final String booking_date = getRef(position).getKey();

//                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//
//                        Intent intent = new Intent(BookingsActivity.this, VenueDetailsActivity.class);
//                        intent.putExtra("venue_id", venue_id);
//                        startActivity(intent);
//
//
//                    }
//                });

                DatabaseRef.child(booking_date).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        user_id = (String) dataSnapshot.child("userID").getValue();
                        String booking_date = (String) dataSnapshot.child("date").getValue();

                        DatabaseReference userDatabaseRef = FirebaseDatabase.getInstance()
                                .getReference().child("Users").child(user_id);
                        userDatabaseRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                String user_name = (String) dataSnapshot.child("username").getValue();
                                String phone_number = (String) dataSnapshot.child("phone").getValue();
                                viewHolder.mTxtUser.setText(user_name);
                                viewHolder.mTxtPhone.setText(phone_number);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        viewHolder.mTxtDate.setText(booking_date);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });



            }
        };

        mRecBookings.setAdapter(firebaseRecyclerAdapter);


    }

    public static class VenueViewHolder extends RecyclerView.ViewHolder {

        View mView;
        TextView mTxtUser, mTxtDate, mTxtPhone;


        public VenueViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

            mTxtUser = mView.findViewById(R.id.txtBookingUserId);
            mTxtDate = mView.findViewById(R.id.txtBookingDate);
            mTxtPhone = mView.findViewById(R.id.txtPhoneNumber);


        }
    }
}
