package com.kreon.android.witner.models;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.firebase.database.Exclude;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;

public class Config {
    //region Fields

    private static HashMap<String, BitmapDescriptor> sColorToDescriptorMap;

    private List<String> mTracks;
    private HashMap<String, ReplyTrack> mReplyTracks;

    //endregion

    //region Blocks

    static {
        sColorToDescriptorMap = new HashMap<>();
        Field[] fields = BitmapDescriptorFactory.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                if (field.get(null).getClass() == Float.class) {
                    sColorToDescriptorMap.put(extractColorName(field.getName()),
                            BitmapDescriptorFactory.defaultMarker(field.getFloat(null)));
                }
            }
            catch (Exception ignored) { }
        }
    }

    //endregion

    public Config() { }

    //region Properties

    public List<String> getTracks() {
        return mTracks;
    }

    public void setTracks(List<String> mTracks) {
        this.mTracks = mTracks;
    }

    public HashMap<String, ReplyTrack> getReplyTracks() {
        return mReplyTracks;
    }

    public void setReplyTracks(HashMap<String, ReplyTrack> mReplyTracks) {
        this.mReplyTracks = mReplyTracks;
    }

    @Exclude
    public BitmapDescriptor getDescriptor(Witneet eet) {
        String tag;
        if (eet == null
            || (tag = eet.getTag()) == null
            || !mReplyTracks.containsKey(tag))
            return BitmapDescriptorFactory.defaultMarker();

        ReplyTrack track = mReplyTracks.get(tag);
        if (!sColorToDescriptorMap.containsKey(track.getColor()))
            return null;

        return sColorToDescriptorMap.get(track.getColor());
    }

    //endregion

    //region Private Interface

    private static String extractColorName(String fieldName) {
        // Default colors are names in the format,
        // HUE_XXX.
        return fieldName.substring(4).toLowerCase();
    }

    //endregion
}
