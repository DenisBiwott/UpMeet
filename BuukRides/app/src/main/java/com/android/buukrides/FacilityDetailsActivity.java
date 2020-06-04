package com.android.buukrides;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class FacilityDetailsActivity extends AppCompatActivity {
    private String facility_id, venue_id, user_id;
    private TextView mTxtCapacity, mTxtOpeningTime, mTxtClosingTime, mTxtPrice, mTxtFacilityName,
    mBtnBookFacility;
    private ImageView mImgFacilityImage;
    private DatabaseReference facDatabaseRef;
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facility_details);

        mTxtCapacity = findViewById(R.id.txtCapacity);
        mImgFacilityImage = findViewById(R.id.imgFacilityImage);
        mTxtOpeningTime = findViewById(R.id.txtOpeningTime);
        mTxtClosingTime = findViewById(R.id.txtClosingTime);
        mTxtPrice = findViewById(R.id.txtPrice);
        mTxtFacilityName = findViewById(R.id.txtFacilityName);
        mBtnBookFacility = findViewById(R.id.btnBookFacility);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        user_id = user.getUid();

        venue_id = getIntent().getStringExtra("venue_id");
        facility_id = getIntent().getStringExtra("facility_id");
        facDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Venues").child(venue_id);

        //Hide Book button if user owns venue
        facDatabaseRef.child("UserID").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String UserID = (String)dataSnapshot.getValue();
                if (UserID.equals(user_id)){
                    mBtnBookFacility.setVisibility(View.GONE);
                }
                else {
                    mBtnBookFacility.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //Populate Facility Activity
        facDatabaseRef.child("Facility").child(facility_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String FacName = (String)dataSnapshot.child("Name").getValue();
                String OpeningTime = (String)dataSnapshot.child("OpeningTime").getValue();
                String ClosingTime = (String)dataSnapshot.child("ClosingTime").getValue();
                String Capacity = (String)dataSnapshot.child("Capacity").getValue();
                String Cost = (String)dataSnapshot.child("Cost").getValue();
                final String FacilityImageUrl = (String)dataSnapshot.child("facilityImageUrl")
                        .getValue();

                mTxtFacilityName.setText(FacName);
                mTxtOpeningTime.setText(OpeningTime);
                mTxtClosingTime.setText(ClosingTime);
                mTxtPrice.setText(Cost);
                mTxtCapacity.setText(Capacity);

                Picasso.get().load(FacilityImageUrl).networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.drawable.new_picture).into(mImgFacilityImage, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Exception e) {

                        Picasso.get().load(FacilityImageUrl).placeholder(R.drawable.new_picture)
                                .into(mImgFacilityImage);

                    }

                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
