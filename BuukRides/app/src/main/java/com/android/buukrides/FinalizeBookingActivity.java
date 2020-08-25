package com.android.buukrides;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
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
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

public class FinalizeBookingActivity extends AppCompatActivity {
    private DatabaseReference mDtbFacilityBookings, mDtbNotifications,mDtbCheckAvailability, DatabaseRef;
    private String facility_id, venue_id, booking_date, owner_id, user_id;
    private Button mBtnConfirmBooking;
    private TextView mTxtReservationDate, mTxtReservationFacility, mTxtReservationVenue,
            mTxtReservationOwner, mTxtReservationOwnerNumber, mTxtReservationCost;
    private String stkUrl = "https://us-central1-buuk-rides.cloudfunctions.net/koskiRequest";
    private String stk_result_code, stk_merchant_request_id, stk_checkout_request_id, mPhone, mUsername, cost;

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
        DatabaseReference mDtbUserInfo = DatabaseRef.child("Users").child(user_id);
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
        mDtbUserInfo.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsername = (String) dataSnapshot.child("username").getValue();
                mPhone = (String) dataSnapshot.child("phone").getValue();
                mPhone = mPhone.substring(1);
            

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
                cost = (String) dataSnapshot.child("Cost").getValue();
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

            RequestParams params = new RequestParams();
            params.put("client_number",mPhone);
            params.put("client_id",user_id);
            params.put("amount",cost);

            AsyncHttpClient client = new AsyncHttpClient();
            client.post(stkUrl, params, new TextHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

                    Toast.makeText(FinalizeBookingActivity.this, responseString, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String responseString) {

                    Toast.makeText(FinalizeBookingActivity.this, responseString, Toast.LENGTH_SHORT).show();
                    try {

                        JSONObject jsonObjectR = new JSONObject(responseString);
                        stk_result_code = jsonObjectR.getString("ResponseCode");
                        stk_merchant_request_id = jsonObjectR.getString("MerchantRequestID");
                        stk_checkout_request_id = jsonObjectR.getString("CheckoutRequestID");

                        if(stk_result_code.equal("0")){
                            final Map bookMap = new HashMap();
                            bookMap.put("userID", user_id);
                            bookMap.put("ownerID", owner_id);
                            bookMap.put("venue_id", venue_id);
                            bookMap.put("facility_id", facility_id);
                            bookMap.put("date", booking_date);
                            bookMap.put("CheckoutRequestID", stk_checkout_request_id);
                            mDtbFacilityBookings.updateChildren(bookMap);
                        }
                        else{
                            Toast.makeText(FinalizeBookingActivity.this, "Payment Failed", Toast.LENGTH_LONG).show();
                        }
                

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(FinalizeBookingActivity.this, "JSONException: " + e, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onStart() {
                    super.onStart();
                    Toast.makeText(FinalizeBookingActivity.this, "Processing...", Toast.LENGTH_SHORT).show();
                }
            });

            }
        });

        // mBtnConfirmBooking.setOnClickListener(new View.OnClickListener() {
        //     @Override
        //     public void onClick(View v) {

        //         final Map bookMap = new HashMap();
        //         bookMap.put("userID", user_id);
        //         bookMap.put("ownerID", owner_id);
        //         bookMap.put("venue_id", venue_id);
        //         bookMap.put("facility_id", facility_id);
        //         bookMap.put("date", booking_date);
        //         bookMap.put("CheckoutRequestID", stk_checkout_request_id);
        //         mDtbFacilityBookings.updateChildren(bookMap);

        //         //Push same data to notifications child
        //         mDtbNotifications = FirebaseDatabase.getInstance().getReference().child("Notifications").child(owner_id);
        //         mDtbNotifications.child(user_id).addValueEventListener(new ValueEventListener() {
        //             @Override
        //             public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        //                 mDtbNotifications.child(user_id).setValue(true);

        //             }

        //             @Override
        //             public void onCancelled(@NonNull DatabaseError databaseError) {

        //             }
        //         });
        //         mDtbCheckAvailability.addValueEventListener(new ValueEventListener() {
        //             @Override
        //             public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        //                 mDtbCheckAvailability.child(booking_date).setValue(true);
        //             }

        //             @Override
        //             public void onCancelled(@NonNull DatabaseError databaseError) {

        //             }
        //         });
        //         Toast.makeText(FinalizeBookingActivity.this, "Successfully Booked!", Toast.LENGTH_SHORT).show();
        //         startActivity(new Intent(FinalizeBookingActivity.this, HistoryActivity.class));

        //     }
        // });
    }
}
