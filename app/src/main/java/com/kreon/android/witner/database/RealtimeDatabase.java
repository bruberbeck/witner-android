package com.kreon.android.witner.database;

import com.firebase.geofire.GeoFire;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

// A class for holding Firebase references
public class RealtimeDatabase {

    //
    //
    //region Fields

    private static final String TAG = "RealtimeDatabase";

    private static final String FIREBASE_CONFIG = "/config/";
    private static final String FIREBASE_TESTWEETS = "/test/testweets/";
    private static final String FIREBASE_GEOFIRE_TESTWEETS = "/test/geofire/testweets/";
    private static final String FIREBASE_WITNEETS = "/weets/witneets/";
    private static final String FIREBASE_GEOFIRE_WITNEETS = "/weets/geofire/witneets/";
    private static final String FIREBASE_USERS_TRACK = "/users/%s/track/";
    private static final String FIREBASE_USERS_GEOFIRE_TRACK = "/users/%s/geofire/track/";

    // Realtime database references

    private String mUserId;
    private DatabaseReference mConfigRef;
    private DatabaseReference mTestweetsRef;
    private DatabaseReference mTestweetsGeoFireRef;
    private GeoFire mTestweetsGeoFire;
    private DatabaseReference mWitneetsRef;
    private GeoFire mWitneetsGeoFire;
    private DatabaseReference mUserTrackRef;
    private GeoFire mUserTrackGeoFire;

    //endregion

    //region Constructors

    private RealtimeDatabase() {
        FirebaseDatabase fireDatabase = FirebaseDatabase.getInstance();
        // This line is required so that when offline, listeners can produce
        // immediate results.
        // fireDatabase.setPersistenceEnabled(true);

        mConfigRef = fireDatabase.getReference(FIREBASE_CONFIG);
        mTestweetsRef = fireDatabase.getReference(FIREBASE_TESTWEETS);
        mTestweetsGeoFireRef = fireDatabase.getReference(FIREBASE_GEOFIRE_TESTWEETS);
        mTestweetsGeoFire = new GeoFire(mTestweetsGeoFireRef);
        mWitneetsRef = fireDatabase.getReference(FIREBASE_WITNEETS);
        mWitneetsGeoFire = new GeoFire(fireDatabase.getReference(FIREBASE_GEOFIRE_WITNEETS));
    }

    //endregion

    //region Properties

    public DatabaseReference getConfigRef() {
        return mConfigRef;
    }

    public static RealtimeDatabase getInstance() { return Holder.Instance; }

    public DatabaseReference getTestweetsRef(String tweetId) { return mTestweetsRef.child(tweetId); }

    public DatabaseReference getTestweetsRef() { return mTestweetsRef; }

    public DatabaseReference getTestweetsGeoFireRef() { return mTestweetsGeoFireRef; }

    public GeoFire getTestweetsGeoFire() { return mTestweetsGeoFire; }

    public DatabaseReference getWitneetsRef() { return mWitneetsRef; }

    public DatabaseReference getWitneetRef(String tweetId) { return mWitneetsRef.child(tweetId); }

    public GeoFire getWitneetsGeoFire() { return mWitneetsGeoFire; }

    public DatabaseReference getUserTrackRef(FirebaseUser user)
    {
        if (mUserId == null || !user.getUid().equals(mUserId)) {
            mUserId = user.getUid();
            mUserTrackRef = null;
        }

        if (mUserTrackRef == null)
            mUserTrackRef = FirebaseDatabase.getInstance()
                    .getReference(String.format(FIREBASE_USERS_TRACK, mUserId));

        return mUserTrackRef;
    }

    public GeoFire getUserTrackGeoFire(FirebaseUser user)
    {
        if (mUserId == null || !user.getUid().equals(mUserId)) {
            mUserId = user.getUid();
            mUserTrackGeoFire = null;
        }

        if (mUserTrackGeoFire == null)
            mUserTrackGeoFire = new GeoFire(FirebaseDatabase.getInstance()
                    .getReference(String.format(FIREBASE_USERS_GEOFIRE_TRACK, mUserId)));

        return mUserTrackGeoFire;
    }

    //endregion

    //region Holder

    private static class Holder {
        static final RealtimeDatabase Instance = new RealtimeDatabase();
    }

    //endregion
}
