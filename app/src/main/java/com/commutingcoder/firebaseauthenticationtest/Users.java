package com.commutingcoder.firebaseauthenticationtest;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bignamic on 19/12/16.
 */

// TODO: temporary solution for testing purpose only
public class Users {

    private static Users sUsers;
    private List<UserData> mUsersData;
    private String mMyFirebaseDBUid;// This should not be here, why not adding my user to the list?

    public static Users get() {
        if (sUsers == null) {
            sUsers = new Users();
        }
        return sUsers;
    }

    private Users() {
        mUsersData = new ArrayList<>();
    }

    public int getNumberUsers() {
        return mUsersData.size();
    }

    public void addUserData(UserData data) {
        // TODO: add check to avoid user duplication
        mUsersData.add(data);
    }

    public UserData getUserData(int index) {
        // TODO: add check to avoid violating accesses
        return mUsersData.get(index);
    }

    public void deleteAll() {
        mUsersData.clear();
    }

    public String getmMyFirebaseDBUid() {
        return mMyFirebaseDBUid;
    }

    public void setmMyFirebaseDBUid(String myFirebaseDBUid) {
        mMyFirebaseDBUid = myFirebaseDBUid;
    }
}
