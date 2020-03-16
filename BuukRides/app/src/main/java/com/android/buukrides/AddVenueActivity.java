package com.android.buukrides;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

public class AddVenueActivity extends AppCompatActivity {

    private EditText mEdtFarrmDesc, mEdtVenueSpace, mEdtVenuePrice;
    private TextView mTxtSearchLoc;
    private Button mBtnSaveLoc;
    private LatLng farmLocation;
    int AUTOCOMPLETE_REQUEST_CODE = 1;
    private static final int REQUEST_READ_EXTERNAL = 1234;
    PlacesClient placesClient;
    List<Place.Field> placeFields = Arrays.asList(Place.Field.ID,
            Place.Field.LAT_LNG,
            Place.Field.NAME,
            Place.Field.ADDRESS);

    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;
    private String userID, placeName;
    private ProgressBar loading;
    private FirebaseAuth mauth;
    private FirebaseUser user;
    private ImageView imgAddVenue;
    private Uri resultUri;
    private String key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Places.initialize(AddVenueActivity.this, getString(R.string.api_key));
        placesClient = Places.createClient(AddVenueActivity.this);
        setContentView(R.layout.activity_add_venue);


        user = FirebaseAuth.getInstance().getCurrentUser();

        // Set the fields to specify which types of place data to
// return after the user has made a selection.
        final List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);

        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        user = FirebaseAuth.getInstance().getCurrentUser();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Venues");
        key = mUserDatabase.push().toString();

        mBtnSaveLoc = findViewById(R.id.btnSaveLoc);
        mEdtFarrmDesc = findViewById(R.id.edtFarmDesc);
        mEdtVenueSpace = findViewById(R.id.edtSpace);
        mEdtVenuePrice = findViewById(R.id.edtPrice);
        mTxtSearchLoc = findViewById(R.id.txtSearchLoc);
        imgAddVenue = findViewById(R.id.imgAddVenue);
        loading = findViewById(R.id.loadingAddLoc);

        mTxtSearchLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Start the autocomplete intent.
                Intent intent = new Autocomplete.IntentBuilder(
                        AutocompleteActivityMode.FULLSCREEN, fields)
                        .setCountry("KE")
                        .build(AddVenueActivity.this);
                startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);

            }
        });

        imgAddVenue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissions();
            }
        });
        
        mBtnSaveLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
                saveFarm();
            }
        });
    }

    private void saveFarm() {

        loading.setVisibility(View.VISIBLE);

        final String desc = mEdtFarrmDesc.getText().toString().trim();
        final String space = mEdtVenueSpace.getText().toString().trim();
        final String price = mEdtVenuePrice.getText().toString().trim();


        if (farmLocation == null){
            loading.setVisibility(View.GONE);
            Toast.makeText(this, "Enter Location", Toast.LENGTH_SHORT).show();
        }
        else if (desc.isEmpty()){
            loading.setVisibility(View.GONE);
            Toast.makeText(this, "Enter Description", Toast.LENGTH_SHORT).show();
        }
        else if (space.isEmpty()){
            loading.setVisibility(View.GONE);
            Toast.makeText(this, "Enter Space Number", Toast.LENGTH_SHORT).show();
        }
        else if (price.isEmpty()){
            loading.setVisibility(View.GONE);
            Toast.makeText(this, "Enter Venue Price", Toast.LENGTH_SHORT).show();
        }
        else if (resultUri == null){
            loading.setVisibility(View.GONE);
            Toast.makeText(this, "Add Venue Image", Toast.LENGTH_SHORT).show();
        }
        else if (placeName.isEmpty()){
            placeName = desc;
        }
        else  {


                final StorageReference filePath = FirebaseStorage.getInstance().getReference().child("venue_images").child(key);
                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), resultUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
                byte[] data = baos.toByteArray();
                final UploadTask uploadTask = filePath.putBytes(data);


                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //Uri url = taskSnapshot.getDownloadUrl();

                        final Map newImage = new HashMap();

                        filePath.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {

                                // ------------ SAVE IMAGE TO STORAGE & DB -----------------

                                newImage.put("venueImageUrl", task.getResult().toString());
                                mUserDatabase.child(key).updateChildren(newImage);
                                mUserDatabase.child(key).child("UserID").setValue(user.getUid());
                                mUserDatabase.child(key).child("UserID").setValue(user.getUid());
                                mUserDatabase.child(key).child("Longitude").setValue(farmLocation.longitude);
                                mUserDatabase.child(key).child("Latitude").setValue(farmLocation.latitude);
                                mUserDatabase.child(key).child("PlaceName").setValue(placeName);
                                mUserDatabase.child(key).child("Space").setValue(space);
                                mUserDatabase.child(key).child("Price").setValue(price);
                                mUserDatabase.child(key).child("Description").setValue(desc).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            Toast.makeText(AddVenueActivity.this, "Location Saved", Toast.LENGTH_SHORT).show();
                                            loading.setVisibility(View.GONE);
                                            finish();
                                        }
                                    }
                                });


                                Toast.makeText(AddVenueActivity.this,
                                        "Venue Saved", Toast.LENGTH_SHORT).show();

                            }
                        });


                    }
                });
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AddVenueActivity.this, "Error: "+ e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


        }
    }
    private void checkPermissions() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){

            if ((ContextCompat.checkSelfPermission(AddVenueActivity.this, READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) ){

                // Permission is not granted


                ActivityCompat.requestPermissions(AddVenueActivity.this, new String[]{READ_EXTERNAL_STORAGE }, REQUEST_READ_EXTERNAL);



            }else {

                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 2);

            }



        }else {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, 2);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // --------------- PLACES RESULT ---------------
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                farmLocation = place.getLatLng();
                placeName = place.getName();
                mTxtSearchLoc.setText(place.getName());

                //Toast.makeText(this, "Place: " + place.getName(), Toast.LENGTH_SHORT).show();
                //Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());
            } else if (resultCode == RESULT_CANCELED) {
                //mEdtFarrmDesc.setText("");
                // The user canceled the operation.
            }
        }
        // --------------- VENUE IMAGE RESULT ------------
        if(requestCode == 2 && resultCode == Activity.RESULT_OK){
            final Uri imageUri = data.getData();
            resultUri = imageUri;
            imgAddVenue.setImageURI(resultUri);
        }
    }
}
