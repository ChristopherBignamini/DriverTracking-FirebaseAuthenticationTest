package com.commutingcoder.firebaseauthenticationtest;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bignamic on 16/12/16.
 */

// TODO: remember usage of fragments here!
public class ChooseContactActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private List<String> mContacts;//TODO: debug only

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_contact);

        // Wire UI elements
        mRecyclerView = (RecyclerView) findViewById(R.id.contact_list_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Retrieve contact list
        mContacts = new ArrayList<>();
        mContacts.add("q");
        mContacts.add("w");
        mContacts.add("e");
        mContacts.add("r");
        mContacts.add("t");
        mContacts.add("y");
        mContacts.add("u");
        mContacts.add("i");
        mContacts.add("o");
        mContacts.add("p");
        mContacts.add("a");
        mContacts.add("s");
        mContacts.add("d");
        mContacts.add("f");
        mContacts.add("g");
        mContacts.add("h");
        mContacts.add("j");
        mContacts.add("k");
        mContacts.add("l");
        mContacts.add("z");
        mContacts.add("x");
        mContacts.add("c");
        mContacts.add("v");
        mContacts.add("b");
        mContacts.add("n");

        // Set adapter
        ContactAdapter contactAdapter = new ContactAdapter();
        mRecyclerView.setAdapter(contactAdapter);

    }

    private class ContactHolder extends RecyclerView.ViewHolder {

        public TextView mContactName;

        ContactHolder(View itemView) {
            super(itemView);
            mContactName = (TextView) itemView;
        }
    }

    private class ContactAdapter extends RecyclerView.Adapter<ContactHolder> {

        // TODO: do we need a ctor and pass him the contact list?

        @Override
        public void onBindViewHolder(ContactHolder holder, int position) {
            holder.mContactName.setText(mContacts.get(position));
        }

        @Override
        public ContactHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(ChooseContactActivity.this);// TODO: not sure about ChooseContactActivity.this
            View view = layoutInflater
                    .inflate(android.R.layout.simple_list_item_1, parent, false);// TODO: perche' nonne' una text view?
            return new ContactHolder(view);
        }

        @Override
        public int getItemCount() {
            return mContacts.size();
        }
    }

}
