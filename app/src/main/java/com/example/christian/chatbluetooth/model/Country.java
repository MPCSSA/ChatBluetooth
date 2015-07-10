package com.example.christian.chatbluetooth.model;


public class Country {

    private String country;
    private int position;

    public String getCountry() {
        return country;
    }
    public void setCountry(String country) {
        this.country = country;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public Country(String country, int position) {
        setCountry(country);
        setPosition(position);
    }
}
