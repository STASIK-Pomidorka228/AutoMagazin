package com.impact.AutoMagazin.Database;

public class Car {
    private int id;

    private String brand;
    private String model;
    private double Volume;
    private int Power;
    private int Price;


    public Car(int id, String brand, String model, double volume, int Power, int Price){
        this.id = id;
        this.brand = brand;
        this.model= model;
        this.Volume = volume;
        this.Power = Power;
        this.Price = Price;

    }
    public int getId(){
        return id;
    }
    public String getbrand(){
        return brand;
    }
    public String getModel(){
        return model;
    }
    public double getVolume(){
        return Volume;
    }
    public int getPower(){
        return Power;
    }
    public int getPrice(){
        return Price;
    }
}
