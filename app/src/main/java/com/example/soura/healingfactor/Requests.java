package com.example.soura.healingfactor;

public class Requests
{
    public String name;
    public String request_type;

    public Requests(){}

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDate() {
        return request_type;
    }

    public void setDate(String request_type) {
        this.request_type = request_type;
    }
}
