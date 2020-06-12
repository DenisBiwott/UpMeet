package com.android.buukrides;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;

import com.android.buukrides.ui.share.ShareFragment;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
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
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
    private SearchView mSearchView;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        FloatingActionButton fab = findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                startActivity(new Intent(NavigationActivity.this, AddVenueActivity.class));
//            }
//        });


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
                        break;
                    case R.id.nav_about:
                        startActivity(new Intent(NavigationActivity.this, AboutUsActivity.class));
                        break;
                    case R.id.nav_venues:
                        startActivity(new Intent(NavigationActivity.this, MyVenuesActivity.class));
                        break;
                    case R.id.nav_history:
                        startActivity(new Intent(NavigationActivity.this, HistoryActivity.class));
                        break;
                    case R.id.nav_call_us:
                        String phone = "+254790462100";
                        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null));
                        startActivity(intent);
                        break;
                    case R.id.nav_feedback:
                        String email = "denisbiwott@gmail.com";
                        String chooserTitle = "Please select email app to send UpMeet feedback";
//                        String subject = "UpMeet feedback";
//                        String body = "I think ...";
                        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + email));
//                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
//                        emailIntent.putExtra(Intent.EXTRA_TEXT, body);

                        startActivity(Intent.createChooser(emailIntent, chooserTitle));
                        break;
                }

                return true;
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
        createLocationRequest();

        // --------------- MAP ------------------
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        //---------------Search Bar-----------
        mSearchView = findViewById(R.id.sv_location);

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String location = mSearchView.getQuery().toString();
                List<Address> addressList = null;

                if (location != null || !location.equals("")){
                    Geocoder geocoder = new Geocoder(NavigationActivity.this);
                    try{
                        addressList = geocoder.getFromLocationName(location, 1);
                    
                        
                        if(addressList.isEmpty()){
                            Toast.makeText(NavigationActivity.this, "Place doesn't exist", Toast.LENGTH_SHORT).show();
                        }else{
                            Address address = addressList.get(0);
                            LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));

                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(NavigationActivity.this, "Exeption: " + e, Toast.LENGTH_SHORT).show();
                
                    }

                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

    }

    //---------- REQUEST USER PERMISSION TO USE LOCATION-----------------------

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
                            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());

                    }
                } else{
                    Toast.makeText(getApplicationContext(), "Please provide the permission", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }


    //----------------REQUEST USE TO TURN ON LOCATION ON STARTUP------------------
    protected void createLocationRequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize
                // location requests here.
                // ...
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(NavigationActivity.this,
                                REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        Toast.makeText(NavigationActivity.this, "Please try again", Toast.LENGTH_SHORT);
                        // Ignore the error.
                    }
                }
            }
        });
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
        String venue_id;

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
            venue_id = (String) singleUser.get("venue_id");
            LatLng latLng = new LatLng(lat, lng);
            //Toast.makeText(getContext(), ""+ latLng  , Toast.LENGTH_SHORT).show();
            if (mMap!=null) {
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(lat, lng))
                        .title(desc)
                        .snippet(venue_id));


                mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                    @Override
                    public View getInfoWindow(Marker marker) {
                        View window = getLayoutInflater().inflate(R.layout.custom_info_window, null);
                        TextView title = window.findViewById(R.id.title);
                        title.setText(marker.getTitle());

                        return window;
                    }

                    @Override
                    public View getInfoContents(Marker marker) {

                        return null;

                    }

                });
                mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(Marker marker) {
                        Intent intent = new Intent(NavigationActivity.this, VenueDetailsActivity.class);
                        intent.putExtra("venue_id", marker.getSnippet());
                        startActivity(intent);

                    }
                });
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

                    }else {
                        Snackbar.make(findViewById(android.R.id.content), "Hi, Add your venue to show on the map!", Snackbar.LENGTH_LONG).show();

                    }


                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }else{
            checkLocationPermission();
//            Toast.makeText(NavigationActivity.this, "Went to else", Toast.LENGTH_SHORT).show();
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
