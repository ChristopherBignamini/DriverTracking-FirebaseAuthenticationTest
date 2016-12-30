package com.commutingcoder.firebaseauthenticationtest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

// TODO: how to hide activities like this to other app???
public class DialogWithContactActivity extends AppCompatActivity {

    private static final String TAG = "DialogWithContact";
    private TextView mMyStatus;
    private TextView mOtherStatus;
    private DatabaseReference mContactReference;
    private Query mContactStatusQuery;
    private ValueEventListener mContactStatusListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog_with_contact);

        // Wire UI widgets
        mMyStatus = (TextView) findViewById(R.id.my_status_text);
        mOtherStatus = (TextView) findViewById(R.id.other_status_text);


        // Retrieve intent data
        final int contactIndex = ChooseContactActivity.getContactIndex(getIntent());

        // Create reference to chosen contact
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        // TODO: we need also here some connection check: copy function from main activity
        // and implement the missing behavior for connection status change
        mContactReference = database.getReference("users")
                .child(Users.get().getUserData(contactIndex).getFirebaseDBId());
        Log.i(TAG,"Chosen contact id " + Users.get().getUserData(contactIndex).getFirebaseDBId());

        // Set listener for chosen contact
        // TODO: this is already listened by the ChooseContact activity, we should use a single listener (backgroud service?)
        mContactStatusQuery = mContactReference.child("status");
        mContactStatusListener = mContactStatusQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                boolean status = (Boolean) dataSnapshot.getValue();
                if (status == true) {
                    mOtherStatus.setText(Users.get().getUserData(contactIndex).getName() +
                            " is available");
                } else {
                    mOtherStatus.setText(Users.get().getUserData(contactIndex).getName() +
                            " is not available");
                }
                Log.i(TAG,"Still listening to " + Users.get().getUserData(contactIndex).getName() + " status ");

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // TODO : what about my status?

    }

    @Override
    protected void onPause() {
        super.onPause();
        mContactStatusQuery.removeEventListener(mContactStatusListener);
    }

}
