package com.impact.AutoMagazin.Database;

import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

public class CarDatabase {
    @GetMapping
    public List<Car> getCars(){
        return List.of(
                new Car(1, "Renault", "Megane", 1.6, 113, 4500),
                new Car(2, "Volkswagen", "Golf", 1.9, 105, 5500),
                new Car(3, "BMW", "E39", 2.5, 192, 7000),
                new Car(4, "Mersedes-Benz", "W124", 3.0, 220, 8500),
                new Car(5, "Audi", "A4", 2.0, 130, 6500),
                new Car(6, "Skoda", "Octavia", 1.8, 150, 6000),
                new Car(7, "Toyota", "Corolla", 1.4, 97, 5000),
                new Car(8, "Honda", "Civic", 2.0, 155, 7600),
                new Car(9, "Ford", "focus", 1.6, 100, 5200),
                new Car(10, "Nissan", "Qashqai", 2.0, 141, 9000)
        );
    }
}
