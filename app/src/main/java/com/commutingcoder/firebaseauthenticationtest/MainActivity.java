package com.commutingcoder.firebaseauthenticationtest;

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

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private EditText mEmailEditText;
    private EditText mPasswordEditText;
    private Button mSignUpButton;
    private Button mSignInButton;
    private Button mSignOutButton;
    private Button mClearButton;
    private Button mCheckStatusButton;
    private TextView mUserStatusText;
    private boolean mIsEmailValid;
    private boolean mIsPasswordValid;
    private boolean mIsUserLoggedIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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


        // User data setup
        // TODO: add SharedPreferences retrieval
        mIsEmailValid = false;
        mIsPasswordValid = false;

        // UI elements wiring
        mEmailEditText = (EditText) findViewById(R.id.email_edit_text);
        mPasswordEditText = (EditText) findViewById(R.id.password_edit_text);
        mSignUpButton = (Button) findViewById(R.id.signup_button);
        mSignInButton = (Button) findViewById(R.id.signin_button);
        mSignOutButton = (Button) findViewById(R.id.signout_button);
        mClearButton = (Button) findViewById(R.id.clear_button);
        mCheckStatusButton = (Button) findViewById(R.id.check_status_button);
        mUserStatusText = (TextView) findViewById(R.id.user_status_text);

        // UI elements setup
        mSignUpButton.setEnabled(false);
        mSignInButton.setEnabled(false);

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
                if(mIsEmailValid && mIsPasswordValid) {
                    mSignInButton.setEnabled(true);
                    mSignUpButton.setEnabled(true);
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

    private void createAccount(String email, String password) {
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
                        Toast.makeText(getApplicationContext(),
                                "Account successfully created", Toast.LENGTH_LONG).show();
                    }

                    // ...

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
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String email = user.getEmail();

            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getToken() instead.
            String uid = user.getUid();
            Log.d(TAG, "User logged in with email: " + email);
            mUserStatusText.setText("Logged in with email " + email);
            mIsUserLoggedIn = true;
        } else {
            mUserStatusText.setText("Not logged in as");
            Log.d(TAG, "Not logged in");
            mIsUserLoggedIn = false;
        }
    }
}
