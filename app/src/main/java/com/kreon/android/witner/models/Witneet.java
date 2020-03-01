package com.kreon.android.witner.models;

import com.firebase.geofire.GeoLocation;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

// 'Witneet' is the current domain specific abstraction for
// Twitter event objects encapsulating actual tweets.
@IgnoreExtraProperties
public class Witneet {

    //region Fields

    private static HashMap<String, BitmapDescriptor> sColorToDescriptorMap;
    private static int sTestWitneetTweetIdCounter = 0;
    private static TimeZone sUTCTimeZone = TimeZone.getTimeZone("UTC");
    private static SimpleDateFormat sCreatedAtDateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z YYYY", Locale.US);
    private static Witneet sTestWitneet = new Witneet();

    private String mTweetId;
    private String mText;
    private boolean mGeoparsed;
    private double mLatitude;
    private double mLongitude;
    private List<String> mHashtags;
    private String mCreatedAt;
    private long mTwitterTimeStamp;
    private long mSystemTimeStamp;
    private User mUser;
    private Poi mPoi;
    private Media mMedia;
    private ReplyStats mReplyStats;

    //endregion

    //region Blocks

    static {
        sCreatedAtDateFormat.setTimeZone(sUTCTimeZone);

        sTestWitneet.mHashtags = Arrays.asList("witner", "test");
        sTestWitneet.mUser = User.getTestUser();
        sTestWitneet.mPoi = null;
        sTestWitneet.mMedia = null;
    }
    static {
        Field[] fields = BitmapDescriptorFactory.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                if (field.get(null).getClass() == Float.class)
                    sColorToDescriptorMap.put(field.getName(),
                            BitmapDescriptorFactory.defaultMarker(field.getFloat(null)));
            }
            catch (Exception ignored) { }
        }
    }

    //endregion

    //region Properties

    public String getTweetId() {
        return mTweetId;
    }

    public void setTweetId(String idStr) {
        mTweetId = idStr;
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        this.mText = text;
    }

    public boolean getGeoparsed() {
        return mGeoparsed;
    }

    public void setGeoparsed(boolean geoparsed) {
        this.mGeoparsed = geoparsed;
    }

    public List<Double> getCoordinates() {
        return Arrays.asList(mLatitude, mLongitude);
    }

    public void setCoordinates(List<Double> coordinates) {
        mLatitude = coordinates.get(0);
        mLongitude = coordinates.get(1);
    }

    public List<String> getHashtags() {
        return mHashtags;
    }

    public void setHashtags(List<String> hashtags) {
        this.mHashtags = hashtags;
    }

    public String getCreatedAt() {
        return mCreatedAt;
    }

    public void setCreatedAt(String createdAt) {
        this.mCreatedAt = createdAt;
    }

    public long getTwitterTimeStamp() {
        return mTwitterTimeStamp;
    }

    public void setTwitterTimeStamp(long twitterTimeStamp) {
        mTwitterTimeStamp = twitterTimeStamp;
    }

    public long getSystemTimeStamp() {
        return mSystemTimeStamp;
    }

    public void setSystemTimeStamp(long serverTimeStamp) {
        mSystemTimeStamp = serverTimeStamp;
    }

    public User getUser() {
        return mUser;
    }

    public void setUser(User user) {
        this.mUser = user;
    }

    public Poi getPoi() {
        return mPoi;
    }

    public void setPoi(Poi poi) {
        this.mPoi = poi;
    }

    public Media getMedia() {
        return mMedia;
    }

    public void setMedia(Media media) {
        this.mMedia = media;
    }

    public ReplyStats getReplyStats() {
        return mReplyStats;
    }

    public void setReplyStats(ReplyStats mReplyStats) {
        this.mReplyStats = mReplyStats;
    }

    //endregion

    //region Custom Properties

    @Exclude
    public double getLatitude() {
        return mLatitude;
    }

    @Exclude
    public void setLatitude(double latitude) {
        mLatitude = latitude;
    }

    @Exclude
    public double getLongitude() {
        return mLongitude;
    }

    @Exclude
    public void setLongitude(double longitude) {
        mLongitude = longitude;
    }

    @Exclude
    public LatLng getPosition() {
        return new LatLng(mLatitude, mLongitude);
    }

    @Exclude
    public GeoLocation getGeolocation() {
        return new GeoLocation(mLatitude, mLongitude);
    }

    @Exclude
    public String getDateString() {
        return DateFormat.getDateTimeInstance().format(new Date(mTwitterTimeStamp));
    }

    @Exclude
    public static Witneet getTestWitneet(double latitude, double longitude) {
        sTestWitneet.mTweetId = Integer.toString(++sTestWitneetTweetIdCounter);
        sTestWitneet.mText = String.format("Test tweet with id <%s>", sTestWitneet.getTweetId());
        sTestWitneet.mLatitude = latitude;
        sTestWitneet.mLongitude = longitude;

        Date createdDate = Calendar.getInstance(sUTCTimeZone).getTime();
        sTestWitneet.mCreatedAt = sCreatedAtDateFormat.format(createdDate);
        sTestWitneet.mTwitterTimeStamp = createdDate.getTime();
        sTestWitneet.mSystemTimeStamp = sTestWitneet.mTwitterTimeStamp;

        return sTestWitneet;
    }

    @Exclude
    public String getTag() {
        if (mReplyStats != null
            && mReplyStats.getCurrentQualifiedStatus() != null)
            return mReplyStats.getCurrentQualifiedStatus().getTag();
        return null;
    }

    //endregion

    //region Overridden Methods

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof Witneet)) {
            return false;
        }

        Witneet p = (Witneet) obj;
        return Objects.equals(mTweetId, p.mTweetId);
    }

    @Override
    public int hashCode() {
        return mTweetId.hashCode();
    }

    @Override
    public String toString() {
        return null;// String.format(Locale.US, TO_STRING, mTweetId, mLatitude, mLongitude);
    }

    //endregion
}
