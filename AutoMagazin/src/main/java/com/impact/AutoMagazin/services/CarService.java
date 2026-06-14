package com.impact.AutoMagazin.services;

import com.impact.AutoMagazin.models.Car;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CarService {

    private static final String FILE_PATH = "cars.json";
    private final ObjectMapper mapper;

    public CarService() {
        this.mapper = new ObjectMapper();
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @PostConstruct
    void init() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            try {
                file.createNewFile();
                mapper.writeValue(file, new ArrayList<Car>());
            } catch (IOException e) {
                throw new RuntimeException("Failed to create cars.json", e);
            }
        }
    }

    public List<Car> findAll() {
        try {
            return mapper.readValue(new File(FILE_PATH), new TypeReference<List<Car>>() {});
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    public Optional<Car> findById(Long id) {
        return findAll().stream().filter(c -> c.getId().equals(id)).findFirst();
    }

    public synchronized Car save(Car car) {
        List<Car> cars = findAll();
        if (car.getId() == null) {
            car.setId(nextId(cars));
        } else {
            cars.removeIf(c -> c.getId().equals(car.getId()));
        }
        cars.add(car);
        writeAll(cars);
        return car;
    }

    public synchronized Car patch(Long id, Car updates) {
        List<Car> cars = findAll();
        Car existing = cars.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Car not found"));

        if (updates.getBrand() != null) existing.setBrand(updates.getBrand());
        if (updates.getModel() != null) existing.setModel(updates.getModel());
        if (updates.getName() != null) existing.setName(updates.getName());
        if (updates.getPrice() != null) existing.setPrice(updates.getPrice());
        if (updates.getVolume() != null) existing.setVolume(updates.getVolume());
        if (updates.getPower() != null) existing.setPower(updates.getPower());

        writeAll(cars);
        return existing;
    }

    public synchronized void deleteById(Long id) {
        List<Car> cars = findAll();
        cars.removeIf(c -> c.getId().equals(id));
        writeAll(cars);
    }

    private Long nextId(List<Car> cars) {
        return cars.stream().mapToLong(Car::getId).max().orElse(0) + 1;
    }

    private void writeAll(List<Car> cars) {
        try {
            mapper.writeValue(new File(FILE_PATH), cars);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write cars.json", e);
        }
    }
}
