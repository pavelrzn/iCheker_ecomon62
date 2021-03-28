package com.sps.iCheker.model;

import java.util.HashMap;
import java.util.Map;

public class CommentsPack {
    private Map [] content;
    private long totalElements;
    private long totalPages;
    private boolean last;
    private long numberOfElements;
    private Map[] sort;
    private boolean first;
    private long size;
    private long number;

    private Map user;

    public Map[] getContent() {
        return content;
    }
    public void setContent(Map[] content) {
        this.content = content;
    }
    public long getTotalElements() {
        return totalElements;
    }
    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }
    public long getTotalPages() {
        return totalPages;
    }
    public void setTotalPages(long totalPages) {
        this.totalPages = totalPages;
    }
    public void setLast(boolean last) {
        this.last = last;
    }
    public long getNumberOfElements() {
        return numberOfElements;
    }
    public void setNumberOfElements(long numberOfElements) {
        this.numberOfElements = numberOfElements;
    }
    public void setSort(Map[] sort) {
        this.sort = sort;
    }
    public void setFirst(boolean first) {
        this.first = first;
    }
    public void setSize(long size) {
        this.size = size;
    }
    public void setNumber(long number) {
        this.number = number;
    }

    public long getCommentId (int commentNumber) {
        return Long.parseLong(content[commentNumber].get("id").toString());
    }
    public String getCommentText (int commentNumber) {
        return ((String) content[commentNumber].get("text"))
                .replaceAll("</div>", ". ")
                .replaceAll("<div>", " ")
                .replaceAll("<div class=\\\"for-text\\\">", " ")
                .replaceAll("\n|\r\n", ".");
    }

    public double getLatitude (int commentNumber) {
        return (double) content[commentNumber].get("latitude");
    }

    public double getLongitude (int commentNumber) {
        return (double) content[commentNumber].get("longitude");
    }

    public long getCommentTime (int commentNumber) {
        return Long.parseLong(content[commentNumber].get("time").toString());
    }

    public long getUserId (int commentNumber) {
        user = (HashMap)content[commentNumber].get("user");
        return Long.parseLong(user.get("id").toString());
    }
    public String getUserFIO (int commentNumber) {
        user = (HashMap)content[commentNumber].get("user");
        return user.get("fio").toString();
    }
    public int getContentArrayLength () {
        return content.length;
    }
}
