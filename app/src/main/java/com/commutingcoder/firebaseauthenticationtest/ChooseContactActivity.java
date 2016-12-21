package com.commutingcoder.firebaseauthenticationtest;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by bignamic on 16/12/16.
 */

// TODO: remember usage of fragments here!
public class ChooseContactActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_contact);

        // Wire UI elements
        mRecyclerView = (RecyclerView) findViewById(R.id.contact_list_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Set adapter
        ContactAdapter contactAdapter = new ContactAdapter();
        mRecyclerView.setAdapter(contactAdapter);

    }

    private class ContactHolder extends RecyclerView.ViewHolder {

        public TextView mContactName;
        public TextView mContactPhone;
        public ImageView mContactStatusImageView;

        ContactHolder(View itemView) {
            super(itemView);
//            mContactName = (TextView) itemView;
            mContactName = (TextView) itemView.findViewById(R.id.contact_name_text_view);
            mContactPhone = (TextView) itemView.findViewById(R.id.contact_phone_text_view);
            mContactStatusImageView = (ImageView) itemView.findViewById(R.id.contact_status_image);
        }
    }

    private class ContactAdapter extends RecyclerView.Adapter<ContactHolder> {

        // TODO: do we need a ctor and pass him the contact list?

        @Override
        public void onBindViewHolder(ContactHolder holder, int position) {
            holder.mContactName.setText(Users.get().getUserData(position).getName());
            holder.mContactPhone.setText(Users.get().getUserData(position).getPhoneNumber());
            if(Users.get().getUserData(position).getStatus() == true) {
                holder.mContactStatusImageView.setImageDrawable(
                        getResources().getDrawable(R.drawable.user_status_online));
            } else {
                holder.mContactStatusImageView.setImageDrawable(
                        getResources().getDrawable(R.drawable.user_status_offline));
            }
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
