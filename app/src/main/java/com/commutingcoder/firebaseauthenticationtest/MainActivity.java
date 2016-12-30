package com.commutingcoder.firebaseauthenticationtest;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.google.firebase.database.OnDisconnect;
import com.google.firebase.database.ValueEventListener;

// TODO: online-offline status in user data-db: what about a singleton like in CriminalIntent
// TODO: use a local sqlite db for current list of available contacts??

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mUsersReference;
    private DatabaseReference mConnectionReference;


    private EditText mEmailEditText;
    private EditText mPhoneEditText;
    private EditText mPasswordEditText;
    private Button mSignUpButton;
    private Button mSignInButton;
    private Button mSignOutButton;
    private Button mClearButton;
    private Button mCheckStatusButton;
    private TextView mUserStatusText;
    private Button mChooseContactButton;
    private boolean mIsEmailValid;
    private boolean mIsPhoneValid;
    private boolean mIsPasswordValid;
    private boolean mIsUserSignedIn;
    private boolean mIsUserConnected;
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
                    Log.i(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.i(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };

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


        // UI elements setup
        // TODO: factorize button enabling code in method (and define rules...)
        mSignUpButton.setEnabled(false);
        mSignInButton.setEnabled(false);
        mChooseContactButton.setEnabled(false);

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
                if(mIsUserSignedIn) {
                    mAuth.signOut();
                    Toast.makeText(getApplicationContext(),"Signed out",Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(),"Already not signed in!",Toast.LENGTH_LONG).show();
                }
                checkAuthenticationStatus();
                checkConnectionStatus();
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
                checkAuthenticationStatus();
                checkConnectionStatus();
            }
        });

        mChooseContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // TODO: not sure about using MainActivity.this for context
                // TODO: move in right place
                Intent intent = new Intent(MainActivity.this, ChooseContactActivity.class);
                startActivity(intent);
            }
        });

        // TODO: find final place for this call
        checkAuthenticationStatus();
        checkConnectionStatus();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);

        // Check current user status
        checkAuthenticationStatus();
        checkConnectionStatus();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG,"onStop");
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }

        // Set the user as not signedIn // TODO: temporary solution, use IntentService or similar
        mIsUserSignedIn = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG,"onDestroy");
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
                    Log.i(TAG, "createUserWithEmail:onComplete:" + task.getException());
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


                    // TODO: is the creation task asynchronous? In this case checkAuthenticationStatus must be here
                    // Check current user status
                    checkAuthenticationStatus();
                    checkConnectionStatus();

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
                    Log.i(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());
                    if (!task.isSuccessful()) {
                        Toast.makeText(getApplicationContext(),
                                "Sign in failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(),
                                "Successfully signed in", Toast.LENGTH_LONG).show();
                    }

                    // ...

                    // TODO: is the creation task asynchronous? In this case checkAuthenticationStatus must be here
                    // Check current user status
                    checkAuthenticationStatus();
                    checkConnectionStatus();

                }
            });
    }


    private void checkAuthenticationStatus() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String email = user.getEmail();

            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getToken() instead.
            mFireBaseUid = user.getUid(); // TODO: this assignement is already done in createAccount
            Log.i(TAG, "User signed in with email: " + email);
            mUserStatusText.setText("Signed in with email " + email);
            mIsUserSignedIn = true;
        } else {
            mUserStatusText.setText("Not signed in");
            Log.i(TAG, "Not signed in");
            mIsUserSignedIn = false;
        }
    }

    private void checkConnectionStatus() {

        // Setup reference to rt database
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        if (database != null) {
            mConnectionReference = database.getReference(".info");
            mUsersReference = database.getReference("users");
            mIsUserConnected = true;
            if(mIsUserSignedIn == true) {
                Log.i(TAG, "Update user status on db");
                mUsersReference.child(mFireBaseUid).child("status").setValue(mIsUserSignedIn);

                // TODO: don't do this every time
                mConnectionReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        boolean connected = dataSnapshot.child("connected").getValue(Boolean.class);
                        if (connected) {
                            Log.i(TAG, "User connected");
                        } else {
                            Log.i(TAG, "User disconnected");
                        }

                        OnDisconnect onDisconnect = mUsersReference.child(mFireBaseUid).child("status").onDisconnect();
                        onDisconnect.setValue(false);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                mChooseContactButton.setEnabled(true);

            }
            Log.i(TAG, "User available");

        } else {
            Log.i(TAG, "User not available");
            mUsersReference = null;
            mIsUserConnected = false;
            mChooseContactButton.setEnabled(false);
        }

    }
}
