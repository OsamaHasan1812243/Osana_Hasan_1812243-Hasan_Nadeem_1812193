package com.example.project.Models;

public class ModelChat {

    String message, sender, reciever,timestamp, type;
    boolean isSeen;

    public ModelChat() {
    }

    public ModelChat(String message, String sender, String reciever, String timestamp, String type, boolean isSeen) {
        this.message = message;
        this.sender = sender;
        this.reciever = reciever;
        this.timestamp = timestamp;
        this.type = type;
        this.isSeen = isSeen;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReciever() {
        return reciever;
    }

    public void setReciever(String reciever) {
        this.reciever = reciever;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isSeen() {
        return isSeen;
    }

    public void setSeen(boolean seen) {
        isSeen = seen;
    }
}
