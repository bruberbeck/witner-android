package com.kreon.android.witner.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {

    //region Fields

    private static User sTestUser = new User();
    static {
        sTestUser.mUserId = "-1" ;
        sTestUser.mName = "Test";
        sTestUser.mScreenName = "Test";
    }

    private String mUserId;
    private String mName;
    private String mScreenName;

    //endregion

    //region Properties

    public String getUserId() {
        return mUserId;
    }

    public void setUserId(String userId) {
        mUserId = userId;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getScreenName() {
        return mScreenName;
    }

    public void setScreenName(String screenName) {
        mScreenName = screenName;
    }

    @Exclude
    public static User getTestUser() {
        return sTestUser;
    }

    //endregion

    //region Constructors

    User() { }

    //endregion
}
