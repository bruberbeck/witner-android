package com.kreon.android.witner.models;

import java.util.List;

public class ReplyStats {
    //region Fields

    private List<String> mReplies;
    private QualifiedReply mCurrentQualifiedStatus;
    private List<QualifiedReply> mQualifiedReplyReplies;

    //endregion

    //region Properties

    public List<String> getReplies() {
        return mReplies;
    }

    public void setReplies(List<String> replies) {
        this.mReplies = replies;
    }

    public QualifiedReply getCurrentQualifiedStatus() {
        return mCurrentQualifiedStatus;
    }

    public void setCurrentQualifiedStatus(QualifiedReply currentQualifiedStatus) {
        this.mCurrentQualifiedStatus = currentQualifiedStatus;
    }

    public List<QualifiedReply> getQualifiedReplies() {
        return mQualifiedReplyReplies;
    }

    public void setQualifiedReplies(List<QualifiedReply> qualifiedReplyReplies) {
        this.mQualifiedReplyReplies = qualifiedReplyReplies;
    }

    //endregion
}
