package com.android.buukrides;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class MyVenuesActivity extends AppCompatActivity {
    private DatabaseReference DatabaseRef;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private String userID;
    private RecyclerView mFriendList;
    private Query venuesOwnedByUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_venues);

        FloatingActionButton floating_b = findViewById(R.id.floating_b);
        floating_b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MyVenuesActivity.this, AddVenueActivity.class));
            }
        });

        //mLstMyVenues = findViewById(R.id.lstMyVenues);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        userID = user.getUid();
        DatabaseRef = FirebaseDatabase.getInstance().getReference();
        mFriendList =   findViewById(R.id.blog_list_friends);

        mFriendList.setHasFixedSize(true);
        mFriendList.setLayoutManager(new LinearLayoutManager(MyVenuesActivity.this));

        venuesOwnedByUser = DatabaseRef
                .child("Venues")
                .child("UserID")
                .equalTo(userID);
        DatabaseRef.keepSynced(true);


        venuesOwnedByUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Toast.makeText(MyVenuesActivity.this, "Exists", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Friends, FriendsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(

                Friends.class,
                R.layout.blog_row_friends,
                FriendsViewHolder.class,
                venuesOwnedByUser
        )
        {
            @Override
            protected void populateViewHolder(final FriendsViewHolder viewHolder, Friends model, int position) {

                //final String post_key = getRef(position).getKey();

                final String list_user_id = getRef(position).getKey();
                viewHolder.mLastseen.setText(list_user_id);
            }
        };

        mFriendList.setAdapter(firebaseRecyclerAdapter);


    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder {

        View mView;
        TextView mLastseen;


        public FriendsViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

            mLastseen = (TextView) mView.findViewById(R.id.textViewFriends);



        }
    }
}