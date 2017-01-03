package com.commutingcoder.firebaseauthenticationtest;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bignamic on 16/12/16.
 */

// TODO: remember usage of fragments here!
public class ChooseContactActivity extends AppCompatActivity {

    private static final String TAG = "ChooseContactActivity";
    public static final String CONTACT_CHOSEN =
            "com.commutingcoder.android.firebaseauthenticationtest.contact_chosen";
    private final static int MY_PERMISSIONS_READ_CONTACTS = 0;
    private RecyclerView mRecyclerView;
    private DatabaseReference mUsersReference;
    private ContactAdapter mContactAdapter;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_contact);

        // Setup reference to rt database
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        if (database != null) {
            mUsersReference = database.getReference("users");
        }

        // Access list of app users
        Query userQuery = mUsersReference.orderByKey();
        // TODO: this works but is highly inefficient, temporary only
        // TODO: cosa succede a questo listener quando questa activity viene stoppata? Sembra continuare
        // TODO: rimuovere l'event listener quando si cambia activity
        userQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // TODO: very bad piece of code, we probably don't need to retrieve all these 3 info
                List<String> appUsersPhones = new ArrayList<String>();
                List<String> appUsersFirebaseUid = new ArrayList<String>();
                List<Boolean> appUsersStatus = new ArrayList<Boolean>();
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    appUsersPhones.add(singleSnapshot.child("phone").getValue().toString());
                    appUsersFirebaseUid.add(singleSnapshot.getKey());
                    appUsersStatus.add((Boolean) singleSnapshot.child("status").getValue());
                }

                // TODO: check for best way to handle this, particularly first run aftewe install fails!!
                // Here, thisActivity is the current activity
                // TODO: use new requestPermissions
                if (ContextCompat.checkSelfPermission(getBaseContext(),
                        android.Manifest.permission.READ_CONTACTS)
                        != PackageManager.PERMISSION_GRANTED) {

                    // Should we show an explanation?
                    if (ActivityCompat.shouldShowRequestPermissionRationale(ChooseContactActivity.this,// TODO: I'm not sure about MainActivity.this
                            android.Manifest.permission.READ_CONTACTS)) {

                        // Show an expanation to the user *asynchronously* -- don't block
                        // this thread waiting for the user's response! After the user
                        // sees the explanation, try again to request the permission.

                    } else {

                        // No explanation needed, we can request the permission.

                        ActivityCompat.requestPermissions(ChooseContactActivity.this,// TODO: I'm not sure about MainActivity.this
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

                Cursor cursor = ChooseContactActivity.this.getContentResolver()// TODO: I'm not sure about MainActivity.this
                        .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                queryFields,null,null,null);

                // TODO: avoid multiple delete
                Users users = Users.get();// TODO: add save/retrieval code accross lifetime steps (Local db?)
                users.deleteAll();
                // TODO: find best way to do this search and avoid multiple search
                for(int userIndex=0;userIndex<appUsersPhones.size();++userIndex) {
                    final String currentUserPhone = new String(appUsersPhones.get(userIndex));
                    for (cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext()) {
                        Log.i(TAG,"cursor.getString(1) " + cursor.getString(1));
                        if(cursor.getString(1).equals(currentUserPhone)) {
                            users.addUserData(new UserData(cursor.getString(1),
                                    cursor.getString(0),
                                    appUsersFirebaseUid.get(userIndex),
                                    appUsersStatus.get(userIndex)));
                        }
                    }
                }

                // TODO: Debug only
                Log.i(TAG,"List of available contacts");
                for (int userIndex=0;userIndex<users.getNumberUsers();++userIndex) {
                    Log.i(TAG,"Contact number: " + userIndex +
                            " name: " + users.getUserData(userIndex).getName() +
                            " phone: " + users.getUserData(userIndex).getPhoneNumber() +
                            " status: " + users.getUserData(userIndex).getStatus());
                }

                updateUI();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // Wire UI elements
        mRecyclerView = (RecyclerView) findViewById(R.id.contact_list_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Set adapter
        mContactAdapter = new ContactAdapter();
        mRecyclerView.setAdapter(mContactAdapter);
    }

    private void updateUI() {
        // TODO: implement efficient reloading, only for changed items
        mContactAdapter.notifyDataSetChanged();
    }

    static public int getContactIndex(Intent intent) {
        return intent.getIntExtra(CONTACT_CHOSEN,0);
    }

    // TODO: is this a safe solution (the name of the class as string would be enough)
    static public boolean isLaunchingActivity(Intent intent) {
        return intent.hasExtra(CONTACT_CHOSEN);
    }

    // What we see in the rec. view is the itemView of the ViewHolder. And we access its widgets
    // by calling its find...ById method.
    private class ContactHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener {

        // TODO change into private
        public TextView mContactName;
        public TextView mContactPhone;
        public ImageView mContactStatusImageView;
        public int mContactIndex;
        public boolean mContactStatus;

        ContactHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
//            mContactName = (TextView) itemView;
            mContactName = (TextView) itemView.findViewById(R.id.contact_name_text_view);
            mContactPhone = (TextView) itemView.findViewById(R.id.contact_phone_text_view);
            mContactStatusImageView = (ImageView) itemView.findViewById(R.id.contact_status_image);
        }

        @Override
        public void onClick(View v) {

            // TODO: temporary, for debug only. Final flow to be defined
//            if (mContactStatus == true) {
//                Intent intent = new Intent();
//                intent.putExtra(CONTACT_CHOSEN, mContactIndex);
//                setResult(RESULT_OK, intent);
//                ChooseContactActivity.this.finish();
//            } else {
//                Toast.makeText(ChooseContactActivity.this,
//                        "Please choose a connected contact", Toast.LENGTH_SHORT).show();
//            }

            Intent intent = new Intent(ChooseContactActivity.this, DialogWithContactActivity.class);
            intent.putExtra(CONTACT_CHOSEN, mContactIndex);
            startActivity(intent);
        }
    }

    private class ContactAdapter extends RecyclerView.Adapter<ContactHolder> {

        // TODO: do we need a ctor and pass him the contact list?

        @Override
        public void onBindViewHolder(ContactHolder holder, int position) {
            holder.mContactName.setText(Users.get().getUserData(position).getName());
            holder.mContactPhone.setText(Users.get().getUserData(position).getPhoneNumber());
            holder.mContactStatus = Users.get().getUserData(position).getStatus();
            if(holder.mContactStatus) {
                holder.mContactStatusImageView.setImageDrawable(
                        getResources().getDrawable(R.drawable.user_status_online));
            } else {
                holder.mContactStatusImageView.setImageDrawable(
                        getResources().getDrawable(R.drawable.user_status_offline));
            }
            holder.mContactIndex = position;
        }

        @Override
        public ContactHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(ChooseContactActivity.this);// TODO: not sure about ChooseContactActivity.this
//            View view = layoutInflater
//                    .inflate(android.R.layout.simple_list_item_1, parent, false);// TODO: perche' non e' una text view? Perche' qua si aspetta un intero layout!
            View view = layoutInflater
                    .inflate(R.layout.contact_list_item, parent, false);
            return new ContactHolder(view);
        }

        @Override
        public int getItemCount() {
            return Users.get().getNumberUsers();
        }
    }

}
