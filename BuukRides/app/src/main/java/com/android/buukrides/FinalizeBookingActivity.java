package com.android.buukrides;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class FinalizeBookingActivity extends AppCompatActivity {
    private DatabaseReference mDtbFacilityBookings, mDtbNotifications,mDtbCheckAvailability, DatabaseRef;
    private String facility_id, venue_id, booking_date, owner_id, user_id;
    private Button mBtnConfirmBooking;
    private TextView mTxtReservationDate, mTxtReservationFacility, mTxtReservationVenue,
            mTxtReservationOwner, mTxtReservationOwnerNumber, mTxtReservationCost;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finalize_booking);
        facility_id = getIntent().getStringExtra("facility_id");
        venue_id = getIntent().getStringExtra("venue_id");
        booking_date = getIntent().getStringExtra("booking_date");
        owner_id = getIntent().getStringExtra("owner_id");
        user_id = getIntent().getStringExtra("user_id");

        mTxtReservationDate = findViewById(R.id.txtReservationDate);
        mTxtReservationDate.setText(booking_date);
        mTxtReservationVenue = findViewById(R.id.txtReservationVenue);
        mTxtReservationOwner = findViewById(R.id.txtReservationOwner);
        mTxtReservationFacility = findViewById(R.id.txtReservationFacility);
        mTxtReservationCost = findViewById(R.id.txtReservationPrice);
        mTxtReservationOwnerNumber = findViewById(R.id.txtReservationOwnerPhone);

        mDtbFacilityBookings = FirebaseDatabase.getInstance().getReference().child("Bookings").push();
        mDtbCheckAvailability = FirebaseDatabase.getInstance().getReference().child("Bookings").child(facility_id);
        DatabaseRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference mDtbOwnerInfo = DatabaseRef.child("Users").child(owner_id);
        DatabaseReference mDtbVenueInfo = DatabaseRef.child("Venues").child(venue_id);
        mDtbOwnerInfo.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String owner_name = (String) dataSnapshot.child("username").getValue();
                String owner_phone = (String) dataSnapshot.child("phone").getValue();

                mTxtReservationOwner.setText(owner_name);
                mTxtReservationOwnerNumber.setText(owner_phone);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        mDtbVenueInfo.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String venue_name = (String) dataSnapshot.child("PlaceName").getValue();
                mTxtReservationVenue.setText(venue_name);
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
                mTxtReservationCost.setText(cost);
                mTxtReservationFacility.setText(facility_name);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        mBtnConfirmBooking = findViewById(R.id.btnConfirmBooking);
        mBtnConfirmBooking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Map bookMap = new HashMap();
                bookMap.put("userID", user_id);
                bookMap.put("ownerID", owner_id);
                bookMap.put("venue_id", venue_id);
                bookMap.put("facility_id", facility_id);
                bookMap.put("date", booking_date);
                mDtbFacilityBookings.updateChildren(bookMap);

                //Push same data to notifications child
                mDtbNotifications = FirebaseDatabase.getInstance().getReference().child("Notifications").child(owner_id);
                mDtbNotifications.child(user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        mDtbNotifications.child(user_id).setValue(true);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                mDtbCheckAvailability.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        mDtbCheckAvailability.child(booking_date).setValue(true);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                Toast.makeText(FinalizeBookingActivity.this, "Successfully Booked!", Toast.LENGTH_SHORT).show();

            }
        });
    }
}
