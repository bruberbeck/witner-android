package com.kreon.android.witner.models;

public class ReplyTrack {
    //region Fields

    private int mPriority;
    private String mColor;
    private String mText;

    //endregion

    public ReplyTrack() { }

    //region Properties

    public int getPriority() {
        return mPriority;
    }

    public void setPriority(int mPriority) {
        this.mPriority = mPriority;
    }

    // Provides lower case color name.
    public String getColor() {
        return mColor;
    }

    public void setColor(String mColor) {
        this.mColor = mColor.toLowerCase();
    }

    public String getText() {
        return mText;
    }

    public void setText(String mText) {
        this.mText = mText;
    }

    //endregion
}
