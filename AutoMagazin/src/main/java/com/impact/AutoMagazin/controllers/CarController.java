package com.impact.AutoMagazin.controllers;

import java.util.List;
import com.impact.AutoMagazin.models.Car;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/cars")

public class CarController {

    @GetMapping
    public List<Car> getCars() {
        return List.of(
                new Car(1, "Renault", "Megane", 1.5),
                new Car(2, "Dacia", "Duster", 1.6),
                new Car(3, "Ford", "Fiesta", 1.0),
                new Car(4, "Skoda", "Octavia", 1.4),
                new Car(5, "Skoda", "Superb", 2.0),
                new Car(6, "Audi", "QuatroA7", 3.0),
                new Car(7, "Audi", "RS3", 2.5),
                new Car(8, "Audi", "A6", 2.0),
                new Car(9, "Audi","RS7", 4.0)
        );
    }
}
