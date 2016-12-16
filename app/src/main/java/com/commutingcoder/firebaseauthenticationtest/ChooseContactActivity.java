package com.commutingcoder.firebaseauthenticationtest;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

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

    }
}
