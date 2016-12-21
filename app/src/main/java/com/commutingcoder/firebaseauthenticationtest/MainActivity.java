package com.commutingcoder.firebaseauthenticationtest;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

// TODO: online-offline status in user data-db: what about a singleton like in CriminalIntent
// TODO: use a local sqlite db for current list of available contacts??

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";
    final static int MY_PERMISSIONS_READ_CONTACTS = 0;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mUsersReference;

    private EditText mEmailEditText;
    private EditText mPhoneEditText;
    private EditText mNameEditText;
    private EditText mPasswordEditText;
    private Button mSignUpButton;
    private Button mSignInButton;
    private Button mSignOutButton;
    private Button mClearButton;
    private Button mCheckStatusButton;
    private TextView mUserStatusText;
    private Button mChooseContactButton;
    private TextView mContactChosenText;
    private boolean mIsEmailValid;
    private boolean mIsPhoneValid;
    private boolean mIsPasswordValid;
    private boolean mIsUserLoggedIn;
    private String mFireBaseUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up auth stuff
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };


        // Setup reference to rt database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mUsersReference = database.getReference("users");

        // Users data setup
        // TODO: add SharedPreferences retrieval
        mIsEmailValid = false;
        mIsPhoneValid = false;
        mIsPasswordValid = false;

        // UI elements wiring
        mEmailEditText = (EditText) findViewById(R.id.email_edit_text);
        mPhoneEditText = (EditText) findViewById(R.id.phone_edit_text);
        mPasswordEditText = (EditText) findViewById(R.id.password_edit_text);
        mSignUpButton = (Button) findViewById(R.id.signup_button);
        mSignInButton = (Button) findViewById(R.id.signin_button);
        mSignOutButton = (Button) findViewById(R.id.signout_button);
        mClearButton = (Button) findViewById(R.id.clear_button);
        mCheckStatusButton = (Button) findViewById(R.id.check_status_button);
        mUserStatusText = (TextView) findViewById(R.id.user_status_text);
        mChooseContactButton = (Button) findViewById(R.id.choose_contact_button);
        mContactChosenText = (TextView) findViewById(R.id.chosen_contact_text);


        // UI elements setup
        // TODO: factorize button enabling code in method (and define rules...)
        mSignUpButton.setEnabled(false);
        mSignInButton.setEnabled(false);
        mChooseContactButton.setEnabled(false);

        // Check current user status
        checkStatus();

        // User data insert
        mEmailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO: add missing check if needed
                mIsEmailValid = true;
                if(mIsEmailValid && mIsPasswordValid) {
                    mSignInButton.setEnabled(true);
                    if(mIsPhoneValid) {
                        mSignUpButton.setEnabled(true);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mPhoneEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO: add missing check if needed
                mIsPhoneValid = true;
                if(mIsEmailValid && mIsPhoneValid && mIsPasswordValid) {
                    mSignInButton.setEnabled(false);
                    mSignUpButton.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mPasswordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO: add missing check if needed
                mIsPasswordValid = true;
                if(mIsEmailValid && mIsPasswordValid) {// TODO: refactor the button activation code
                    mSignInButton.setEnabled(true);
                    if(mIsPhoneValid) {
                        mSignUpButton.setEnabled(true);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // Signing operations
        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAccount(mEmailEditText.getText().toString(),
                        mPhoneEditText.getText().toString(),
                        mPasswordEditText.getText().toString());
            }
        });
        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn(mEmailEditText.getText().toString(),
                        mPasswordEditText.getText().toString());
            }
        });
        mSignOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mIsUserLoggedIn) {
                    mAuth.signOut();
                    Toast.makeText(getApplicationContext(),"Signed out",Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(),"Already not logged int!",Toast.LENGTH_LONG).show();
                }
                checkStatus();
            }
        });
        mClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEmailEditText.setText("");
                mPasswordEditText.setText("");
                mSignUpButton.setEnabled(false);
                mSignInButton.setEnabled(false);
            }
        });
        mCheckStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkStatus();
            }
        });

        mChooseContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // TODO: not sure about using MainActivity.this for context
                // TODO: move in right place
                Intent intent = new Intent(MainActivity.this, ChooseContactActivity.class);
                startActivity(intent);

                // Access list of app users
                Query userQuery = mUsersReference.orderByKey();
                userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        // TODO: very bad piece of code
                        List<String> appUsersPhones = new ArrayList<String>();
                        List<Boolean> appUsersStatus = new ArrayList<Boolean>();
                        for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                            appUsersPhones.add(singleSnapshot.child("phone").getValue().toString());
                            appUsersStatus.add((Boolean) singleSnapshot.child("status").getValue());
                        }

                        // TODO: check for best way to handle this, particularly first run aftewe install fails!!
                        // Here, thisActivity is the current activity
                        // TODO: use new requestPermissions
                        if (ContextCompat.checkSelfPermission(getBaseContext(),
                                android.Manifest.permission.READ_CONTACTS)
                                != PackageManager.PERMISSION_GRANTED) {

                            // Should we show an explanation?
                            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,// TODO: I'm not sure about MainActivity.this
                                    android.Manifest.permission.READ_CONTACTS)) {

                                // Show an expanation to the user *asynchronously* -- don't block
                                // this thread waiting for the user's response! After the user
                                // sees the explanation, try again to request the permission.

                            } else {

                                // No explanation needed, we can request the permission.

                                ActivityCompat.requestPermissions(MainActivity.this,// TODO: I'm not sure about MainActivity.this
                                        new String[]{android.Manifest.permission.READ_CONTACTS},
                                        MY_PERMISSIONS_READ_CONTACTS);

                                // MY_PERMISSIONS_READ_CONTACTS is an
                                // app-defined int constant. The callback method gets the
                                // result of the request.
                            }
                        }

                        String[] queryFields = new String[] {
                                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                                ContactsContract.CommonDataKinds.Phone.NUMBER};

                        Cursor cursor = MainActivity.this.getContentResolver()// TODO: I'm not sure about MainActivity.this
                                .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                        queryFields,null,null,null);

                        // TODO: avoid multiple delete
                        Users users = Users.get();// TODO: add save/retrieval code accross lifetime steps (Local db?)
                        users.deleteAll();
                        // TODO: find best way to do this search and avoid multiple search
                        for(int userIndex=0;userIndex<appUsersPhones.size();++userIndex) {
                            final String currentUserPhone = new String(appUsersPhones.get(userIndex));
                            Log.d(TAG,"out");
                            for (cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext()) {
                                Log.d(TAG,"in");
                                Log.d(TAG,"cursor.getString(1) " + cursor.getString(1));
                                if(cursor.getString(1).equals(currentUserPhone)) {
                                    users.addUserData(new UserData(cursor.getString(1),
                                        cursor.getString(0), appUsersStatus.get(userIndex)));
                                }
                            }
                        }

                        // TODO: Debug only
                        for (int userIndex=0;userIndex<users.getNumberUsers();++userIndex) {
                            Log.d(TAG,"Contact number: " + userIndex +
                                    " name: " + users.getUserData(userIndex).getName() +
                                    " phone: " + users.getUserData(userIndex).getPhoneNumber() +
                                    " status: " + users.getUserData(userIndex).getStatus());
                        }


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });




            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    // TODO: input pars not needed, can be obtained from data member
    private void createAccount(String email, final String phoneNumber, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {


                    // If sign in fails, display a message to the user. If sign in succeeds
                    // the auth state listener will be notified and logic to handle the
                    // signed in user can be handled in the listener.
                    Log.d(TAG, "createUserWithEmail:onComplete:" + task.getException());
                    if (!task.isSuccessful()) {
                        Toast.makeText(getApplicationContext(),
                                "Account creation error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();

                    } else {

                        // Update user database
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        mFireBaseUid = user.getUid();
                        mUsersReference.child(mFireBaseUid).child("email").setValue(user.getEmail());// TODO: is Uid fine as node?
                        // TODO: is there a way to get my phone number from contact list??
                        mUsersReference.child(mFireBaseUid).child("phone").setValue(phoneNumber);
                        Toast.makeText(getApplicationContext(),
                                "Account successfully created", Toast.LENGTH_LONG).show();
                    }


                    // TODO: is the creation task asynchronous? In this case checkStatus must be here
                    // Check current user status
                    checkStatus();

                }
            });
    }

    private void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    // If sign in fails, display a message to the user. If sign in succeeds
                    // the auth state listener will be notified and logic to handle the
                    // signed in user can be handled in the listener.
                    Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());
                    if (!task.isSuccessful()) {
                        Toast.makeText(getApplicationContext(),
                                "Sign in failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(),
                                "Successfully signed in", Toast.LENGTH_LONG).show();
                    }

                    // ...

                    // TODO: is the creation task asynchronous? In this case checkStatus must be here
                    // Check current user status
                    checkStatus();

                }
            });
    }


    private void checkStatus() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser(); // TODO: bug, questo funziona anche senza connessione..
        if (user != null) {
            String email = user.getEmail();

            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getToken() instead.
            mFireBaseUid = user.getUid(); // TODO: this assignement is already done in createAccount
            Log.d(TAG, "User logged in with email: " + email);
            mUserStatusText.setText("Logged in with email " + email);
            mIsUserLoggedIn = true;
            mChooseContactButton.setEnabled(true);
        } else {
            mUserStatusText.setText("Not logged in as");
            Log.d(TAG, "Not logged in");
            mIsUserLoggedIn = false;
            mChooseContactButton.setEnabled(false);
        }
        if(mFireBaseUid!=null) {
            mUsersReference.child(mFireBaseUid).child("status").setValue(mIsUserLoggedIn);
        }
    }
}
