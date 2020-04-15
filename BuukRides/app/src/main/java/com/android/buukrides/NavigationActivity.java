package com.android.buukrides;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.os.Looper;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class NavigationActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    private DrawerLayout drawer;
    private ActionBarDrawerToggle t;
    private NavigationView navigationView;

    private TextView txtUsername, txtEmail;
    private CircleImageView imgUserProf;
    private FirebaseAuth mauth;
    private DatabaseReference mCustomerDatabase, mUserDatabase;
    private String userID, placeName;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationClient;
    LocationRequest mLocationRequest;
    Location mLastLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(NavigationActivity.this, AddVenueActivity.class));
            }
        });


        drawer = findViewById(R.id.drawer_layout);
        t = new ActionBarDrawerToggle(this, drawer,toolbar,R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawer.addDrawerListener(t);
        t.syncState();

        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(NavigationActivity.this);
        t.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!drawer.isDrawerOpen(GravityCompat.START)){
                    drawer.openDrawer(GravityCompat.START);
                }
                else  {
                    drawer.closeDrawer(GravityCompat.END);
                }

            }
        });
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch(id)
                {
                    case R.id.nav_profile:
                        startActivity(new Intent(NavigationActivity.this, ProfileActivity.class));

                    default:
                        return true;
                }


            }
        });

        // ---------------- USER INFO -------------------------
        mauth = FirebaseAuth.getInstance();
        userID = mauth.getCurrentUser().getUid();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Venues");
        mUserDatabase.keepSynced(true);
        mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(userID);



        View header = navigationView.getHeaderView(0);

        imgUserProf = header.findViewById(R.id.imgUserProf);
        txtUsername = header.findViewById(R.id.txtUsername);
        txtEmail = header.findViewById(R.id.txtEmail);

        if (mauth.getCurrentUser().getDisplayName()!=null){
            txtUsername.setText(mauth.getCurrentUser().getDisplayName());
        }
        if (mauth.getCurrentUser().getEmail()!=null){
            txtEmail.setText(mauth.getCurrentUser().getEmail());
        }

        getUserInfo();


        // --------------- MAP ------------------
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);








    }

    //Requests Location Permission

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new android.app.AlertDialog.Builder(this)
                        .setCancelable(false)
                        .setTitle("Enable permission")
                        .setMessage("Enable permission to continue")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(NavigationActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                            }
                        })
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(NavigationActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);


            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case 1:{
                if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
//                        Intent cat = new Intent(NavigationActivity.this, MainActivity.class);
//                        cat.putExtra("status", "notrequested");
//                        startActivity(cat);

                            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                     

                    }
                } else{
                    Toast.makeText(getApplicationContext(), "Please provide the permission", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    private void getUserInfo(){
        mCustomerDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                    if(map.get("profileImageUrl")!=null){
                        final String mProfileImageUrl = (String) map.get("profileImageUrl");

                        Picasso.get().load(mProfileImageUrl).networkPolicy(NetworkPolicy.OFFLINE)
                                .placeholder(R.drawable.userprof).into(imgUserProf, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError(Exception e) {

                                Picasso.get().load(mProfileImageUrl).placeholder(R.drawable.userprof).into(imgUserProf);

                            }



                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.navigation, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:

                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(@NonNull Task<Void> task) {

                                if (task.isSuccessful()){
                                    startActivity(new Intent(NavigationActivity.this, SignInActivity.class));
                                    finish();
                                }
                                // ...
                            }
                        });

                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        return false;
    }

    private void collectPhoneNumbers(Map<String,Object> users) {

//        int size = (int) dataSnapshot.getChildrenCount(); //
//        Marker[] allMarkers = new Marker[size];
//        Marker mm;

        ArrayList<Double> phoneNumbers = new ArrayList<>();
        ArrayList<Double> latitude = new ArrayList<>();
        double lat, lng;
        String desc;

        //iterate through each user, ignoring their UID
        for (Map.Entry<String, Object> entry : users.entrySet()){

            //Get user map
            Map singleUser = (Map) entry.getValue();
            //Get phone field and append to list
            phoneNumbers.add((Double) singleUser.get("Longitude"));
            latitude.add((Double) singleUser.get("Latitude"));
            lat = (double) singleUser.get("Latitude");
            lng = (double) singleUser.get("Longitude");
            desc = (String) singleUser.get("Description");
            LatLng latLng = new LatLng(lat, lng);
            //Toast.makeText(getContext(), ""+ latLng  , Toast.LENGTH_SHORT).show();
            if (mMap!=null) {
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(lat, lng))
                        .title(desc));
                // mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,12));
            }
//            mMap.addMarker(new MarkerOptions()
//                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).position(latLng).title("Farm"));

        }

        //System.out.println(phoneNumbers.toString());
        //Toast.makeText(getContext(), phoneNumbers.toString(), Toast.LENGTH_SHORT).show();

    }
    @Override
    public void onMapReady(GoogleMap googleMap) {

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){

            mMap = googleMap;
            mMap.getUiSettings().setZoomGesturesEnabled(true);
            mMap.setMyLocationEnabled(true);
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(120000);
            mLocationRequest.setFastestInterval(60000);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());

            mUserDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if (dataSnapshot.exists()){
                        collectPhoneNumbers((Map<String,Object>) dataSnapshot.getValue());
                        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                            @Override
                            public void onInfoWindowClick(Marker marker) {
                                double lat = marker.getPosition().latitude;
                                double lng = marker.getPosition().longitude;


                            }
                        });

                    }else {
                        Snackbar.make(findViewById(android.R.id.content), "Add new location to view on map", Snackbar.LENGTH_LONG).show();

                    }


                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }else{
            checkLocationPermission();

        }




    }

    private Marker mUserMarker;

    LocationCallback mLocationCallback = new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for(Location location : locationResult.getLocations()){
                if(getApplicationContext()!=null){
                    mLastLocation = location;

                    LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());

                    if (mLastLocation!=null){

                    
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,13));

                         // ------ User location Marker -------------
                        if(mUserMarker != null){
                            mUserMarker.remove();
                        }else{

//                            mUserMarker = mMap.addMarker(new MarkerOptions()
//                            .position(latLng).title("My Location")
//                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

    
                        }
                        

                    }

                   

                   // pickupLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    //pickupMarker = mMap.addMarker(new MarkerOptions().position(pickupLocation)
                           //.title("Pickup Here").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));


                    //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,10));
                    //CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(pickupLocation, 15);
                    //mMap.animateCamera(yourLocation);

                    //mMap.animateCamera(CameraUpdateFactory.zoomTo(12));
                    // if(!getDriversAroundStarted)
                    //     getDriversAround();
                }
            }
        }
    };


}
