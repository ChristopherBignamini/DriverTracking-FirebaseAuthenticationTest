package com.commutingcoder.firebaseauthenticationtest;

/**
 * Created by bignamic on 15/12/16.
 */

// TODO: temporary design, find minimal set of data
class UserData {

    private String mName;
    private String mPhoneNumber;
    private String mFirebaseDBId;
    private Boolean mStatus;

    public UserData(String phoneNumber, String name, String firebaseDBId, Boolean status) {
        mPhoneNumber = phoneNumber;
        mName = name;
        mFirebaseDBId = firebaseDBId;
        mStatus = status;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getFirebaseDBId() {
        return mFirebaseDBId;
    }

    public void setFirebaseDBId(String firebaseDBId) {
        mFirebaseDBId = firebaseDBId;
    }

    public String getPhoneNumber() {
        return mPhoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        mPhoneNumber = phoneNumber;
    }

    public Boolean getStatus() {
        return mStatus;
    }

    public void setStatus(Boolean status) {
        mStatus = status;
    }
}
