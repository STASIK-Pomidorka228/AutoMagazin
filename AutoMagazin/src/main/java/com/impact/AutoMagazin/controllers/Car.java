package com.impact.AutoMagazin.controllers;

public class Car {
    private int id;
    private String brand;
    private String model;
    private double volume;

    public Car(int id, String brand, String model, double volume){
        this.id =  id;
        this.brand = brand;
        this.model = model;
        this.volume = volume;
    }
    public int getId(){
        return id;
    }
    public String getBrand(){
        return brand;
    }
    public String getModel(){
        return model;
        }
        public double getVolume(){
            return  volume;
        }
    }

