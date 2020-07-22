package com.nalsasupport.nalsaacademy.model;

import java.io.Serializable;

public class Standard implements Serializable {
    private String id, name;

    public Standard(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public Standard() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
