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
//    private int mContactIndex;
    private String mContactFirebaseUid;
    private DatabaseReference mMyContactReference;
    private DatabaseReference mOtherContactReference;
    private DatabaseReference mMyContactDialogReference;
    private DatabaseReference mOtherContactDialogReference;
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
        // Find the launching activity
        boolean isInvitedDialog = false;
        if (MainActivity.isLaunchingActivity(getIntent())) {
            mContactFirebaseUid = MainActivity.getInvitingContactFirebaseUid(getIntent());
            Log.i(TAG,"Invited by " + mContactFirebaseUid);
            isInvitedDialog = true;
        } else {
            int contactIndex = ChooseContactActivity.getContactIndex(getIntent());
            mContactFirebaseUid = Users.get().getUserData(contactIndex).getFirebaseDBId();
            Log.i(TAG,"Chosen contact id " + mContactFirebaseUid);
        }


        // Create reference to my and chosen contact
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        // TODO: we need also here some connection check: copy function from main activity
        // and implement the missing behavior for connection status change
        mMyContactReference = database.getReference("users")
                .child(Users.get().getmMyFirebaseDBUid());
        mOtherContactReference = database.getReference("users")
                .child(mContactFirebaseUid);

        if (isInvitedDialog == true) {
            // TODO: rename with invited?
            mMyContactDialogReference = mMyContactReference.child("joined_sessions")
                    .child(mContactFirebaseUid);
            mOtherContactDialogReference = mOtherContactReference.child("admin_sessions")
                    .child(Users.get().getmMyFirebaseDBUid());
        } else {
            mMyContactDialogReference = mMyContactReference.child("admin_sessions")
                    .child(mContactFirebaseUid);
            mOtherContactDialogReference = mOtherContactReference.child("joined_sessions")
                    .child(Users.get().getmMyFirebaseDBUid());
        }
        mMyContactDialogReference.setValue("true");
        mOtherContactDialogReference.setValue("true");

        // Set listener for chosen contact
        // TODO: this is already listened by the ChooseContact activity, we should use a single listener (backgroud service?)
        mContactStatusQuery = mOtherContactReference.child("status");
        // TODO: what about keepSync?
        mContactStatusListener = mContactStatusQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                boolean status = (Boolean) dataSnapshot.getValue();
                if (status == true) {
                    mOtherStatus.setText(mContactFirebaseUid +
                            " is available");

                } else {
                    mOtherStatus.setText(mContactFirebaseUid +
                            " is not available");
                }
                Log.i(TAG,"Still listening to " + mContactFirebaseUid + " status ");

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // TODO : what about my status?
        mMyStatus.setText("You are available");

    }

    @Override
    protected void onPause() {
        super.onPause();
        mContactStatusQuery.removeEventListener(mContactStatusListener);
        // TODO: this is not the right place, debug only!
        mMyContactDialogReference.setValue("false");
        mOtherContactDialogReference.setValue("false");
    }

}
