package com.android.buukrides;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

public class FacilityActivity extends AppCompatActivity {

    private EditText mEdtTime, mEdtclosingTime, mEdtName,
             mEdtCapacity, mEdtCost ;
    private Button mBtnSaveFac, mBtnFinishFac;
    private ImageView imgAddVenue;
    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;
    private FirebaseUser user;
    private String userID, key;
    private ProgressBar loading;
    private Uri resultUri;
    private String venuekey;
    private static final int REQUEST_READ_EXTERNAL = 1234;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facility);

    
        mEdtTime = findViewById(R.id.edtOpeningtime);
        mEdtclosingTime = findViewById(R.id.edtClosingtime);
        mEdtName = findViewById(R.id.edtName);
        mEdtCapacity = findViewById(R.id.edtCapacity);
        mEdtCost = findViewById(R.id.edtCost);
        mBtnSaveFac = findViewById(R.id.btnSaveFacility);
        mBtnFinishFac = findViewById(R.id.btnFinish);
        imgAddVenue = findViewById(R.id.imgAddFac);
        loading = findViewById(R.id.loadingAddFac);

        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        user = FirebaseAuth.getInstance().getCurrentUser();

        Intent intent = getIntent();
        venuekey = intent.getStringExtra("Key");

        
        mEdtTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(FacilityActivity.this, new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                               
                                mEdtTime.setText(String.format("%02d:%02d", selectedHour, selectedMinute) );
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();

            }
        });
        mEdtclosingTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(FacilityActivity.this, new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                                mEdtclosingTime.setText(String.format("%02d:%02d", selectedHour, selectedMinute) );
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();

            }
        });

        imgAddVenue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissions();
            }
        });


        mBtnSaveFac.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
                saveFarm();
            }
        });

        mBtnFinishFac.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(FacilityActivity.this, MyVenuesActivity.class));
                finish();
            }
        });
    }

    private void saveFarm() {

        loading.setVisibility(View.VISIBLE);

        final String openingTime = mEdtTime.getText().toString().trim();
        final String closingTime = mEdtclosingTime.getText().toString().trim();
        final String name = mEdtName.getText().toString().trim();
        final String capacity = mEdtCapacity.getText().toString().trim();
        final String cost = mEdtCost.getText().toString().trim();
        


        if (name.isEmpty()){
            loading.setVisibility(View.GONE);
            Toast.makeText(this, "Enter Facility Name", Toast.LENGTH_SHORT).show();
        }
        else if (capacity.isEmpty()){
            loading.setVisibility(View.GONE);
            Toast.makeText(this, "Enter Capacity", Toast.LENGTH_SHORT).show();
        }
        else if (cost.isEmpty()){
            loading.setVisibility(View.GONE);
            Toast.makeText(this, "Enter Cost", Toast.LENGTH_SHORT).show();
        }
        else if (openingTime.isEmpty()){
            loading.setVisibility(View.GONE);
            Toast.makeText(this, "Enter Opening Hours", Toast.LENGTH_SHORT).show();
        }
        else if (closingTime.isEmpty()){
            loading.setVisibility(View.GONE);
            Toast.makeText(this, "Enter Closing Hours", Toast.LENGTH_SHORT).show();
        }
        else if (resultUri == null){
            loading.setVisibility(View.GONE);
            Toast.makeText(this, "Add Facilty Image", Toast.LENGTH_SHORT).show();
        }
        else  {


                mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Venues")
                                                    .child(venuekey).child("Facility").push();
                key = mUserDatabase.getKey();

                final StorageReference filePath = FirebaseStorage.getInstance().getReference()
                                    .child("venue_images").child(key).child("facility_images");
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

                        

                        final Map facilityMap = new HashMap();

                        filePath.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {

                                // ------------ SAVE IMAGE TO STORAGE & DB -----------------

                                    
                                facilityMap.put("facilityImageUrl", task.getResult().toString());
                                facilityMap.put("Name", name);
                                facilityMap.put("Capacity", capacity);
                                facilityMap.put("Cost", cost);
                                facilityMap.put("OpeningTime", openingTime);
                                facilityMap.put("ClosingTime", closingTime);
                                mUserDatabase.updateChildren(facilityMap);
                                // mUserDatabase.child("Name").setValue(name);
                                // mUserDatabase.child("Capacity").setValue(capacity);
                                // mUserDatabase.child("Cost").setValue(cost);
                                // mUserDatabase.child("OpeningTime").setValue(openingTime);
                                // mUserDatabase.child("ClosingTime").setValue(closingTime);
                            
                                Toast.makeText(FacilityActivity.this, "Facility Saved", Toast.LENGTH_SHORT).show();
                                loading.setVisibility(View.GONE);

                                mEdtTime.setText("");
                                mEdtclosingTime.setText("");
                                mEdtclosingTime.setText("");
                                mEdtName.setText("");
                                mEdtCapacity.setText("");
                                mEdtCost.setText("");
                                //imgAddVenue.setImageURI(null);
                           

                                // Toast.makeText(AddVenueActivity.this,
                                //         "Venue Saved", Toast.LENGTH_SHORT).show();

                            }
                        });


                    }
                });
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(FacilityActivity.this, "Error: "+ e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


        }
    }

    private void checkPermissions() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){

            if ((ContextCompat.checkSelfPermission(FacilityActivity.this, READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) ){

                // Permission is not granted


                ActivityCompat.requestPermissions(FacilityActivity.this, new String[]{READ_EXTERNAL_STORAGE }, REQUEST_READ_EXTERNAL);



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
        
        // --------------- FACILITY IMAGE RESULT ------------
        if(requestCode == 2 && resultCode == Activity.RESULT_OK){
            final Uri imageUri = data.getData();
            resultUri = imageUri;
            imgAddVenue.setImageURI(resultUri);
        }
    }

}
