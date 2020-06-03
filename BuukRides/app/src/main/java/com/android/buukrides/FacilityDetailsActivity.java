package com.android.buukrides;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class FacilityDetailsActivity extends AppCompatActivity {
    private String facility_id, venue_id;
    private TextView mTxtCapacity, mTxtOpeningTime, mTxtClosingTime, mTxtPrice, mTxtFacilityName;
    private ImageView mImgFacilityImage;
    private DatabaseReference facDatabaseRef;

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


        venue_id = getIntent().getStringExtra("venue_id");
        facility_id = getIntent().getStringExtra("facility_id");
        facDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Venues").child(venue_id).child("Facility").child(facility_id);
        facDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String FacName = (String)dataSnapshot.child("Name").getValue();
                String OpeningTime = (String)dataSnapshot.child("OpeningTime").getValue();
                String ClosingTime = (String)dataSnapshot.child("ClosingTime").getValue();
                String Capacity = (String)dataSnapshot.child("Capacity").getValue();
                String Cost = (String)dataSnapshot.child("Cost").getValue();
                final String FacilityImageUrl = (String)dataSnapshot.child("facilityImageUrl").getValue();

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

                        Picasso.get().load(FacilityImageUrl).placeholder(R.drawable.new_picture).into(mImgFacilityImage);

                    }

                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
