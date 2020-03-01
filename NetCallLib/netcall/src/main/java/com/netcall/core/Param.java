package com.netcall.core;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class Param {
    private String name;
    private String value;
    private boolean encoded;

    public Param(String name, String value, boolean encoded) {
        this.name = name;
        this.value = value;
        this.encoded = encoded;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public String getValueEncoded() {
        if (encoded) {
            try {
                return URLEncoder.encode(value, "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return value;
            }
        }
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isEncoded() {
        return encoded;
    }

    public void setEncoded(boolean encoded) {
        this.encoded = encoded;
    }
}
