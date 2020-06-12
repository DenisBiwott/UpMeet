package com.android.buukrides;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class FacilityDetailsActivity extends AppCompatActivity {
    private String facility_id, venue_id, owner_id, user_id, facility_reference;
    private TextView mTxtCapacity, mTxtOpeningTime, mTxtClosingTime, mTxtPrice, mTxtFacilityName;
    private Button mBtnBookFacility, mBtnViewFacilityBookings;
    private ImageView mImgFacilityImage;
    private DatabaseReference facDatabaseRef, mDtbCheckAvailability;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private DatePickerDialog.OnDateSetListener mDateSetListener;

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
        mBtnViewFacilityBookings = findViewById(R.id.btnViewFacilityBookings);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        user_id = user.getUid();

        venue_id = getIntent().getStringExtra("venue_id");
        facility_id = getIntent().getStringExtra("facility_id");
        facility_reference = venue_id +'/'+facility_id;
        facDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Venues").child(venue_id);
        mDtbCheckAvailability = FirebaseDatabase.getInstance().getReference().child("Bookings").child(facility_id);

        //Hide Book button if user owns venue
        facDatabaseRef.child("UserID").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                owner_id = (String)dataSnapshot.getValue();
                if (owner_id.equals(user_id)){
                    mBtnBookFacility.setVisibility(View.GONE);
                    mBtnViewFacilityBookings.setVisibility(View.VISIBLE);
                }
                else {
                    mBtnBookFacility.setVisibility(View.VISIBLE);
                    mBtnViewFacilityBookings.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        mBtnViewFacilityBookings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FacilityDetailsActivity.this, BookingsActivity.class);
                intent.putExtra("venue_id", venue_id);
                intent.putExtra("facility_id", facility_id);
                startActivity(intent);
            }
        });
        
        final SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        final Date date = new Date();

        mBtnBookFacility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        FacilityDetailsActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mDateSetListener,
                        year, month, day);
                dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });
        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month + 1;
                final String booking_date = dayOfMonth + "-" + month + "-" + year;
                Toast.makeText(FacilityDetailsActivity.this, "You are reserving on: "+ date,
                        Toast.LENGTH_SHORT).show();
                mDtbCheckAvailability.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(booking_date)){
                            Toast.makeText(FacilityDetailsActivity.this,
                                    "Sorry, This facility is reserved on this day",
                                    Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Intent intent = new Intent(FacilityDetailsActivity.this, FinalizeBookingActivity.class);
                            intent.putExtra("booking_date", booking_date);
                            intent.putExtra("venue_id", venue_id);
                            intent.putExtra("facility_id", facility_id);
                            intent.putExtra("user_id", user_id);
                            intent.putExtra("owner_id", owner_id);
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        };


        //Populate Facility Details Activity
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
