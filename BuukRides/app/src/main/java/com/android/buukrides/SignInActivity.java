package com.android.buukrides;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseUserMetadata;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.Arrays;
import java.util.List;

public class SignInActivity extends AppCompatActivity {

    private static int RC_SIGN_IN = 1;

    private FirebaseAuth mauth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;
    private DatabaseReference mUserDatabaseRef;
    private FirebaseUser current_user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        mUserDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mauth = FirebaseAuth.getInstance();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (mauth.getCurrentUser() != null){
            startActivity(new Intent(SignInActivity.this, NavigationActivity.class));
            finish();
        }else {
            List<AuthUI.IdpConfig> providers = Arrays.asList(
                    new AuthUI.IdpConfig.EmailBuilder().build());

// Create and launch sign-in intent
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .build(),
                    RC_SIGN_IN);
        }




    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                //Store user token in database
                current_user = mauth.getCurrentUser();
                final String user_id = current_user.getUid();
                String user_token = FirebaseInstanceId.getInstance().getToken();
                mUserDatabaseRef.child(user_id).child("notificationTokens").child(user_token).setValue(true);


                FirebaseInstanceId.getInstance().getInstanceId()
                        .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                            @Override
                            public void onComplete(@NonNull Task<InstanceIdResult> task) {

                                    String token = task.getResult().getToken();
                                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                    if (user!=null){
                                        FirebaseUserMetadata metadata = mauth.getCurrentUser().getMetadata();
                                        if (metadata.getCreationTimestamp() == metadata.getLastSignInTimestamp()){
                                            //New User
                                            Toast.makeText(SignInActivity.this, "Welcome to " + "UpMeet" + " !", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(SignInActivity.this, NavigationActivity.class));
                                            mUserDatabaseRef.child(user_id).child("username").setValue(user.getDisplayName());
                                            finish();
                                        }else {
                                            //Exixsting User
                                            Toast.makeText(SignInActivity.this, "Welcome Back " + user.getDisplayName() + " !", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(SignInActivity.this, NavigationActivity.class));
                                            finish();

                                        }
                                    }



                            }
                        });


                // ...
            } else {
                // Sign in failed. If response is null the userprof canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        startActivity(new Intent(SignInActivity.this, SignInActivity.class));
    }
}
