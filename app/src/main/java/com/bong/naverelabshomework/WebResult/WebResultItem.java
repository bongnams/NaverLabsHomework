package com.bong.naverelabshomework.WebResult;

public class WebResultItem {
    private String mTitle;
    private String mDescription;
    private String mLink;

    public WebResultItem(String title, String description, String link) {
        mTitle = title;
        mDescription = description;
        mLink = link;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getLink() {
        return mLink;
    }
}
