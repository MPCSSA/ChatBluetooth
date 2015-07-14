package com.example.christian.chatbluetooth.model;

public class Country {

    private String country; //Country name
    private int position; //Flag position in flag drawable

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

    //Constructor
    public Country(String country, int position) {

        setCountry(country);
        setPosition(position);
    }
}
