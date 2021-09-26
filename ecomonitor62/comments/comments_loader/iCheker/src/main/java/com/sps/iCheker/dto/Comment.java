package com.sps.iCheker.dto;

public class Comment {
    private long id;
    private long time;
    private String text;
    private long userId;
    private String userFIO;
    private double latitude;
    private double longitude;

    public Comment(long id, long time, String text, long userId, String userFIO, double latitude, double longitude) {
        this.id = id;
        this.time = time;
        this.text = text;
        this.userId = userId;
        this.userFIO = userFIO;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Comment() {
    }

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }

    public long getTime() {
        return time;
    }
    public void setTime(long time) {
        this.time = time;
    }

    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }

    public long getUserId() {
        return userId;
    }
    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getUserFIO() {
        return userFIO;
    }
    public void setUserFIO(String userFIO) {
        this.userFIO = userFIO;
    }

    public double getLatitude() {
        return latitude;
    }
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }


}
