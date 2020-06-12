package com.android.buukrides;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class VenueDetailsActivity extends AppCompatActivity {

    private DatabaseReference VenDatabaseRef;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private String userID;
    private RecyclerView RecMyFacilities;
    private Query facilitiesOwnedByVenue;
    private String venue_id;
    private TextView mTxtVenueName, mTxtLocation;
    private CollapsingToolbarLayout mcollapsing_toolbar;
    private ImageView mImgVenueImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_venue_details);

        venue_id = getIntent().getStringExtra("venue_id");

        RecMyFacilities =   findViewById(R.id.facilities_list);
        RecMyFacilities.setHasFixedSize(true);
        RecMyFacilities.setLayoutManager(new LinearLayoutManager(VenueDetailsActivity.this));

        mTxtVenueName = findViewById(R.id.venue_name);
        mTxtLocation = findViewById(R.id.venue_location);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        userID = user.getUid();
        VenDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Venues").child(venue_id);
     
        facilitiesOwnedByVenue = VenDatabaseRef.child("Facility").orderByChild("Name");
        facilitiesOwnedByVenue.keepSynced(true);
        mcollapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        mImgVenueImage = findViewById(R.id.expanded_venue_image);
        final FloatingActionButton mFltNewFacility = findViewById(R.id.new_facility);
        mFltNewFacility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VenueDetailsActivity.this, FacilityActivity.class);
                intent.putExtra("Key", venue_id);
                startActivity(intent);
            }
        });
        VenDatabaseRef.child("UserID").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String owner_id = (String)dataSnapshot.getValue();
                if (owner_id.equals(userID)){
                    mFltNewFacility.setVisibility(View.VISIBLE);
                }
                else {
                    mFltNewFacility.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //Populate venue details and location
        VenDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = (String) dataSnapshot.child("Description").getValue();
                String location = (String) dataSnapshot.child("PlaceName").getValue();
                final String mVenueImageUrl = (String) dataSnapshot.child("venueImageUrl").getValue();
                // Toast.makeText(VenueDetailsActivity.this, ""+ name, Toast.LENGTH_SHORT).show();

                mcollapsing_toolbar.setTitle(name);
                mTxtVenueName.setText(name);
                mTxtLocation.setText(location);

                Picasso.get().load(mVenueImageUrl).networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.drawable.new_picture).into(mImgVenueImage, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Exception e) {

                        Picasso.get().load(mVenueImageUrl).placeholder(R.drawable.new_picture).into(mImgVenueImage);

                    }

                });

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
                R.layout.layout_facilities,
                VenueViewHolder.class,facilitiesOwnedByVenue
        )
        {
            @Override
            protected void populateViewHolder(final VenueViewHolder viewHolder, Friends model, int position) {

                //final String post_key = getRef(position).getKey();

                // ---- Facility ID ------
                final String facility_id = getRef(position).getKey();

                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent intent = new Intent(VenueDetailsActivity.this, FacilityDetailsActivity.class);
                        intent.putExtra("facility_id", facility_id);
                        intent.putExtra("venue_id", venue_id);
                        startActivity(intent);


                    }
                });

                VenDatabaseRef.child("Facility").child(facility_id).addValueEventListener(new ValueEventListener() {
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

        RecMyFacilities.setAdapter(firebaseRecyclerAdapter);


    }

    public static class VenueViewHolder extends RecyclerView.ViewHolder {

        private View mView;
        private TextView mTxtCapacity, mTxtName;


        public VenueViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

            mTxtCapacity = mView.findViewById(R.id.txtFacilityCapacity);
            mTxtName = mView.findViewById(R.id.txtFacilityName);


        }
    }
}
