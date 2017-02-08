package com.commutingcoder.firebaseauthenticationtest;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;

// TODO: how to hide activities like this to other app???
public class DialogWithContactActivity extends AppCompatActivity {

    private static final String TAG = "DialogWithContact";
    private static final String KEY_MY_AUDIO_STATUS = "my_audio_status";
    private static final int MY_PERMISSIONS_RECORD_AUDIO = 1;
    private TextView mMyStatus;
    private TextView mOtherStatus;
    private String mContactFirebaseUid;
    private DatabaseReference mMyContactReference;
    private DatabaseReference mOtherContactReference;
    private DatabaseReference mMyContactDialogReference;
    private DatabaseReference mOtherContactDialogReference;
    private Query mContactStatusQuery;
    private ValueEventListener mContactStatusListener;

    private Position mMyPosition;
    private Position mOtherPosition;

    private EditText mMyLatitudeEditText;
    private EditText mMyLongitudeEditText;
    private Button mUpdateMyPositionButton;
    private TextView mOtherLatitudeTextView;
    private TextView mOtherLongitudeTextView;
    private Button mRecordButton;
    private Button mPlayMyRecordButton;
    private Button mSendButton;
    private Button mPlayOtherRecordButton;
    private Button mReceiveButton;
    private boolean mIsMyAudioAvailable;// TODO rename variable, state that new audio must be sent
    private MediaRecorder mMediaRecorder;
    private String mMyAudioFileName;
    private String mOtherAudioFileName;
    private final String mPreferencesFileName = "preferences";
    private MediaPlayer mMyMediaPlayer;// TODO: is it better to use local variable or data members?
    private MediaPlayer mOtherMediaPlayer;// TODO: is it better to use local variable or data members?
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog_with_contact);

        // TODO: remove
        mMyPosition = new Position(0.0,0.0);
        mOtherPosition = new Position(0.0,0.0);

        // We set this to true every time a new recorded message to be sent is available
        mIsMyAudioAvailable = false;
        SharedPreferences preferences = getSharedPreferences(mPreferencesFileName,0);
        if (preferences.contains(KEY_MY_AUDIO_STATUS)) {
            Log.d(TAG, "My audio is available in preferences after recreation");
            mIsMyAudioAvailable = preferences.getBoolean(KEY_MY_AUDIO_STATUS, false);
            if (mIsMyAudioAvailable) {
                Log.d(TAG, "My audio is available is true");
            }
        }


        // Wire UI widgets
        mMyStatus = (TextView) findViewById(R.id.my_status_text);
        mOtherStatus = (TextView) findViewById(R.id.other_status_text);
        mMyLatitudeEditText = (EditText) findViewById(R.id.my_latitude_text);
        mMyLongitudeEditText = (EditText) findViewById(R.id.my_longitude_text);
        mUpdateMyPositionButton = (Button) findViewById(R.id.update_my_position_button);
        mOtherLatitudeTextView = (TextView) findViewById(R.id.other_latitude_text);
        mOtherLongitudeTextView = (TextView) findViewById(R.id.other_longitude_text);
        mRecordButton = (Button) findViewById(R.id.record_button);
        mPlayMyRecordButton = (Button) findViewById(R.id.play_my_record_button);
        mSendButton = (Button) findViewById(R.id.send_button);
        mPlayOtherRecordButton = (Button) findViewById(R.id.play_other_record_button);
        mReceiveButton = (Button) findViewById(R.id.receive_button);
        mReceiveButton.setEnabled(false);

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
        mMyContactDialogReference.setValue("true");// TODO: one of these is useless
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


        // Create dialog session name
        String dialogName = new String("dialog_");
        if(isInvitedDialog == true) {
            dialogName = dialogName
                    + mContactFirebaseUid + "_"
                    + Users.get().getmMyFirebaseDBUid();
        } else {
            dialogName = dialogName
                    + Users.get().getmMyFirebaseDBUid() + "_"
                    + mContactFirebaseUid;
        }

        DatabaseReference trackingReference = database.getReference(dialogName);
        final DatabaseReference myAudioStatusReference = trackingReference.child("audio_status").child(Users.get().getmMyFirebaseDBUid());
        final DatabaseReference otherAudioStatusReference = trackingReference.child("audio_status").child(mContactFirebaseUid);
        final DatabaseReference myPositionReference = trackingReference.child("positions").child(Users.get().getmMyFirebaseDBUid());
        DatabaseReference otherPositionReference = trackingReference.child("positions").child(mContactFirebaseUid);

        myAudioStatusReference.setValue(false);
        myPositionReference.child("lat").setValue(1.0);
        myPositionReference.child("lon").setValue(1.0);

        // Firebase initialization and reference retrieval stuff
        FirebaseApp.initializeApp(this);
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference topStorageRef = storage.getReferenceFromUrl("gs://fir-authenticationtest-e7571.appspot.com");
        final StorageReference storageRefUp = topStorageRef.child(dialogName+Users.get().getmMyFirebaseDBUid());
        final StorageReference storageRefDown = topStorageRef.child(dialogName+mContactFirebaseUid);

        // Upload/Download file instances
        final File myAudioFile = new File(getApplicationContext().getFilesDir(), Users.get().getmMyFirebaseDBUid());
        final File otherAudioFile = new File(getApplicationContext().getFilesDir(), mContactFirebaseUid);

        // Ask required permissions
        // TODO: these should be asked only when needed, find better design solution
        // TODO: check for best way to handle this, particularly first run aftewe install fails!!
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.RECORD_AUDIO)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.RECORD_AUDIO},
                        MY_PERMISSIONS_RECORD_AUDIO);
            }
        }



        // Setup UI objects
        mMyLatitudeEditText.setText(String.valueOf(mMyPosition.getLatitude()));
        mMyLatitudeEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO: check if better procedure exist
                try {
                    Double d = Double.parseDouble(s.toString());
                    mMyPosition.setLatitude(Double.valueOf(s.toString()));
                } catch (NumberFormatException ex) {
                    // Do something smart here...
                    mMyPosition.setLatitude(0.0);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        // TODO: check if better procedure exist
        mMyLatitudeEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus == false) {
                    mMyLatitudeEditText.setText(String.valueOf(mMyPosition.getLatitude()));
                }
            }
        });

        mMyLongitudeEditText.setText(String.valueOf(mMyPosition.getLongitude()));
        mMyLongitudeEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    Double d = Double.parseDouble(s.toString());
                    mMyPosition.setLongitude(Double.valueOf(s.toString()));
                } catch (NumberFormatException ex) {
                    // Do something smart here...
                    mMyPosition.setLongitude(0.0);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        // TODO: check if better procedure exist
        mMyLongitudeEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus == false) {
                    mMyLongitudeEditText.setText(String.valueOf(mMyPosition.getLongitude()));
                }
            }
        });
        mUpdateMyPositionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Set value for my position
                // TODO: have a look to custom java object usage
                Log.d(TAG, "My position is: " + mMyPosition.getLatitude() + " " + mMyPosition.getLongitude());
                myPositionReference.child("lat").setValue(mMyPosition.getLatitude());
                myPositionReference.child("lon").setValue(mMyPosition.getLongitude());

            }
        });

        mOtherLatitudeTextView.setText(String.valueOf(mOtherPosition.getLatitude()));
        mOtherLongitudeTextView.setText(String.valueOf(mOtherPosition.getLongitude()));

        mRecordButton.setOnClickListener(new View.OnClickListener() {

            private boolean mIsRecordingActive = false;

            @Override
            public void onClick(View v) {
                if(mIsRecordingActive == false) {

                    // TODO: isn't file creation performed by mediarecorder?
                    // TODO: do this create a new file on disk every time?
                    if (!myAudioFile.exists()) {
                        try {
                            myAudioFile.createNewFile();
                        } catch (IOException ioException) {
                            Log.e(TAG, "Record exception (file creation): " + ioException.toString());
                        }
                    }

                    // TODO: don't know why but we need to set mediarecorder to null and reinstantiate it to avoid segfault
                    // TODO: which format and encoder?
                    mMediaRecorder = new MediaRecorder();
                    mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                    mMediaRecorder.setOutputFile(myAudioFile.getAbsolutePath());
                    mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                    try {
                        Log.d(TAG, "Prepare record");
                        // TODO: when use prepare?
                        mMediaRecorder.prepare();
                        Log.d(TAG, "Start record");
                        mMediaRecorder.start();
                        Log.d(TAG, "Record started");
                    } catch (IOException ioException) {
                        Log.e(TAG, "Record exception: " + ioException.toString());
                    } catch (IllegalStateException isException)  {
                        Log.e(TAG, "Record exception: " + isException.toString());
                    }
                    mIsRecordingActive = true;
                } else {
                    Log.d(TAG, "Stop record");
                    mIsRecordingActive = false;
                    try {
                        mMediaRecorder.stop();
                    } catch (RuntimeException runtimeException)  {
                        // TODO:  Note that a RuntimeException is intentionally thrown to the application.. etc, fix code for this case!
                        Log.e(TAG, "Record stop exception: " + runtimeException.toString());
                    } finally {
                        mMediaRecorder.release();
                        Log.d(TAG, "Record stopped");
                        mMediaRecorder = null;
                        mIsMyAudioAvailable = true; // TODO: this must be false in case of exception!
                    }
                }
            }
        });

        mPlayMyRecordButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (mMyMediaPlayer ==null) {

                    // TODO: I don't know if this is the best solution, I'm not using the mp pointer
                    // TODO: Check again lifecycle steps of media player, are we correctly managing the resoources?
                    mMyMediaPlayer = new MediaPlayer();
                    Log.d(TAG, "MediaPlayer is null");
                    mMyMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            Log.d(TAG, "MediaPlayer OnCompletionListener");
                            myMediaPlayerStopAndRelease();
                        }
                    });
                }

                if (!mMyMediaPlayer.isPlaying()) {

                    if (myAudioFile.exists()) {
                        Log.d(TAG, "File exist, setup reproduction");
                        try {
                            mMyMediaPlayer.setDataSource(myAudioFile.getAbsolutePath());
                            mMyMediaPlayer.prepare();// TODO: have a look to documentation for asyncronous preparation step
                        } catch (IOException ioException) {
                            // TODO: should we release and nullify mMyMediaPlayer here?
                            Log.e(TAG, "Play my audio exception: " + ioException.getMessage());
                        } catch (IllegalStateException isException) {
                            Log.e(TAG, "Play my audio exception: " + isException.getMessage());
                        }
                        Log.d(TAG, "Start reproduction");
                        mMyMediaPlayer.start();
                    } else {
                        Toast.makeText(getApplicationContext(),
                                "My audio file not available", Toast.LENGTH_LONG).show();
                    }
                } else {
                    myMediaPlayerStopAndRelease();
                }
            }
        });

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mIsMyAudioAvailable == true) {

                    Uri file = Uri.fromFile(myAudioFile);
                    UploadTask uploadTask = storageRefUp.putFile(file);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG,"Upload failed");
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Log.d(TAG,"Upload completed");
                        }
                    });

                    myAudioStatusReference.setValue(true);
                    mIsMyAudioAvailable = false;

                } else {
                    Toast.makeText(getApplicationContext(),"No new audio available", Toast.LENGTH_LONG).show();
                    Log.d(TAG,"Send audio: no new audio available");
                }
            }
        });

        mPlayOtherRecordButton.setOnClickListener(new View.OnClickListener() {

            // TODO: refactor playing in a single class
            @Override
            public void onClick(View v) {

                if (mOtherMediaPlayer ==null) {

                    // TODO: I don't know if this is the best solution, I'm not using the mp pointer
                    // TODO: Check again lifecycle steps of media player, are we correctly managing the resoources?
                    mOtherMediaPlayer = new MediaPlayer();
                    Log.d(TAG, "MediaPlayer is null");
                    mOtherMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            Log.d(TAG, "MediaPlayer OnCompletionListener");
                            myMediaPlayerStopAndRelease();
                        }
                    });
                }

                if (!mOtherMediaPlayer.isPlaying()) {

                    if (otherAudioFile.exists()) {
                        Log.d(TAG, "File exist, setup reproduction");
                        try {
                            mOtherMediaPlayer.setDataSource(otherAudioFile.getAbsolutePath());
                            mOtherMediaPlayer.prepare();// TODO: have a look to documentation for asyncronous preparation step
                        } catch (IOException ioException) {
                            // TODO: should we release and nullify mMyMediaPlayer here?
                            Log.e(TAG, "Play other audio exception: " + ioException.getMessage());
                        } catch (IllegalStateException isException) {
                            Log.e(TAG, "Play other audio exception: " + isException.getMessage());
                        }
                        Log.d(TAG, "Start reproduction");
                        mOtherMediaPlayer.start();
                    } else {
                        Toast.makeText(getApplicationContext(),
                                "Other audio file not available", Toast.LENGTH_LONG).show();
                    }
                } else {
                    otherMediaPlayerStopAndRelease();
                }
            }
        });

        mReceiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // TODO: add check for remote file availability?
                try {
                    // TODO: I don't like to put everything within Exc catching block
                    // TODO use uri instead?
                    if (!otherAudioFile.exists()) {
                        otherAudioFile.createNewFile();
                    }

                    storageRefDown.getFile(otherAudioFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            Log.d(TAG,"Download completed in " + otherAudioFile.getAbsoluteFile());
                            otherAudioStatusReference.setValue(false); // TODO: this is a bug, both users could modify it at the same time, use two flags!
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG,"Download failed");
                        }
                    });

                } catch (IOException ioException) {
                    Log.d(TAG,"Exception: " + ioException.toString());
                }

            }
        });


        // TODO: remove listener
        otherPositionReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                // TODO: implement correct check here (currently it only avoids to have null value or no child, like at very first dialog between two users)
                if( (dataSnapshot.hasChild("lat") == true) && (dataSnapshot.child("lat").getValue() != null) ) {
                    mOtherPosition.setLatitude((long) (dataSnapshot.child("lat").getValue()));
                    mOtherLatitudeTextView.setText(String.valueOf(mOtherPosition.getLatitude()));
                }
                if( (dataSnapshot.hasChild("lon") == true) && (dataSnapshot.child("lon").getValue() != null) ) {
                    mOtherPosition.setLongitude((long) (dataSnapshot.child("lon").getValue()));
                    mOtherLongitudeTextView.setText(String.valueOf(mOtherPosition.getLongitude()));
                }

                Log.d(TAG, "Other position is: " + mOtherPosition.getLatitude() + " " + mOtherPosition.getLongitude());
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        // TODO: remove listener
        otherAudioStatusReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                Boolean value = dataSnapshot.getValue(Boolean.class);
                if ( value != null) {
                    if (value == true) {
                        mReceiveButton.setEnabled(true);
                    } else {
                        mReceiveButton.setEnabled(false);
                    }
                    Log.d(TAG, "Other audio status value is: " + value);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });



    }

    private void myMediaPlayerStopAndRelease() {
        Log.d(TAG, "Interrupting my reproduction");
        mMyMediaPlayer.stop();
        mMyMediaPlayer.release();
        mMyMediaPlayer = null;
    }

    // TODO: refactor these two methods
    private void otherMediaPlayerStopAndRelease() {
        Log.d(TAG, "Interrupting other reproduction");
        mOtherMediaPlayer.stop();
        mOtherMediaPlayer.release();
        mOtherMediaPlayer = null;
    }


    @Override
    protected void onPause() {
        super.onPause();
        mContactStatusQuery.removeEventListener(mContactStatusListener);
        // TODO: this is not the right place, debug only!
        mMyContactDialogReference.setValue("false");
        mOtherContactDialogReference.setValue("false");

        // Save new my audio availability status
        SharedPreferences.Editor editor = getSharedPreferences(mPreferencesFileName,0).edit();
        editor.putBoolean(KEY_MY_AUDIO_STATUS,mIsMyAudioAvailable);
        editor.commit();

        // Stop and release audio
        // TODO: this is not the best solution, we could let the audio play during rotation..
        if(mMyMediaPlayer !=null) {
            myMediaPlayerStopAndRelease();
        }
        if(mOtherMediaPlayer !=null) {
            otherMediaPlayerStopAndRelease();
        }


    }


    private class Position {

        public double mLatitude;
        public double mLongitude;

        public Position(double latitude, double longitude) {
            mLatitude = latitude;
            mLongitude = longitude;
        }

        public void setLatitude(double latitude) {
            mLatitude = latitude;
        }

        public void setLongitude(double longitude) {
            mLongitude = longitude;
        }

        public double getLatitude() {
            return mLatitude;
        }

        public double getLongitude() {
            return mLongitude;
        }
    }


}
