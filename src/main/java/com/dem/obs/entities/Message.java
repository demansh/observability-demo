package com.dem.obs.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record Message(String id, String text) {

    public static Message defaultMessage() {
        return new Message("-1", "none");
    }

    @JsonIgnore
    public boolean isDefault() {
        return id.equals("-1") && text.equals("none");
    }
}
