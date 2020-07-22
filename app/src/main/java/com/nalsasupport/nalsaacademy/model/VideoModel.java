package com.nalsasupport.nalsaacademy.model;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class VideoModel implements Serializable {

    private String title, thumbnailUrl, publishedTime, videoId;

    public VideoModel() {
    }

    public VideoModel(String title, String thumbnailUrl, String publishedTime, String videoId) {
        this.title = title;
        this.thumbnailUrl = thumbnailUrl;
        this.publishedTime = publishedTime;
        this.videoId = videoId;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getPublishedTime() {
        return publishedTime;
    }

    public void setPublishedTime(String publishedTime) {
        this.publishedTime = publishedTime;
    }

    @NonNull
    @Override
    public String toString() {
        return "1. Thumbnail url : " + thumbnailUrl + "\n" +
                "2. Title : " + title + "\n" +
                "3. Published At : " + publishedTime + "\n" +
                "4. Video Id : " + videoId;
    }
}
