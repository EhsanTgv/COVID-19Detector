package com.taghavi.covid_19detector.fragments;

public class DataPart {
    private String fileName;
    private byte[] content;
    private String type;

    public DataPart() {
    }

    DataPart(String name, byte[] data) {
        fileName = name;
        content = data;
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getContent() {
        return content;
    }

    public String getType() {
        return type;
    }

}
