package com.example.whatsapp.helper;

public class Contacts {
    public String name , status , image , uid ,timeuploaded,valid;
    public Contacts()
    {

    }

    public Contacts(String name, String status, String image, String uid, String timeuploaded,String valid) {
        this.name = name;
        this.status = status;
        this.image = image;
        this.uid = uid;
        this.timeuploaded = timeuploaded;
        this.valid = valid;

    }

    public String getValid() {
        return valid;
    }

    public void setValid(String valid) {
        this.valid = valid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getuid() {
        return uid;
    }

    public void setuid(String uid) {
        this.uid = uid;
    }

    public String getTimeuploaded() {
        return timeuploaded;
    }

    public void setTimeuploaded(String timeuploaded) {
        this.timeuploaded = timeuploaded;
    }
}
