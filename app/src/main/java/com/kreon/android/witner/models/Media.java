package com.kreon.android.witner.models;

import java.util.List;

public class Media {

    //region Fields

    private String mExpandedUrl;
    private List<String> mMediaUrls;

    //endregion

    //region Properties

    public String getExpandedUrl() {
        return mExpandedUrl;
    }

    public void setExpandedUrl(String expandedUrl) {
        this.mExpandedUrl = expandedUrl;
    }

    public List<String> getMediaUrls() {
        return mMediaUrls;
    }

    public void setMediaUrls(List<String> mediaUrls) {
        this.mMediaUrls = mediaUrls;
    }

    //endregion

    //region Constructor

    public Media() { }

    //endregion
}
