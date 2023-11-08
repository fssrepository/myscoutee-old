package com.raxim.myscoutee.profile.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.raxim.myscoutee.profile.data.document.mongo.Car;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.dto.rest.CarDTO;
import com.raxim.myscoutee.profile.data.dto.rest.PageParam;
import com.raxim.myscoutee.profile.repository.mongo.CarRepository;
import com.raxim.myscoutee.profile.repository.mongo.ProfileRepository;

@Service
public class CarService {

    private final CarRepository carRepository;
    private final ProfileRepository profileRepository;

    public CarService(CarRepository carRepository, ProfileRepository profileRepository) {
        this.carRepository = carRepository;
        this.profileRepository = profileRepository;
    }

    public Optional<CarDTO> saveCar(UUID profileId, Car pCar) throws CloneNotSupportedException {
        Optional<Profile> optProfile = this.profileRepository.findById(profileId);
        if (optProfile.isPresent()) {
            Profile profile = optProfile.get();
            Optional<Car> optCar = profile.getCars().stream().filter(car -> car.getId().equals(pCar.getId()))
                    .findFirst();

            Car lCar = null;
            if (optCar.isPresent()) {
                lCar = (Car) pCar.clone();
                Car dbCar = optCar.get();
                lCar.setId(dbCar.getId());
                lCar.setStatus(pCar.getStatus());
            } else {
                lCar = (Car) pCar.clone();
                lCar.setId(UUID.randomUUID());
            }

            Car respCar = this.carRepository.save(lCar);
            profile.getCars().add(lCar);
            this.profileRepository.save(profile);

            return Optional.of(new CarDTO(respCar));
        }
        return Optional.empty();
    }

    public List<CarDTO> getCars(PageParam pageParam) {
        return this.profileRepository.findCarsByProfile(pageParam);
    }
}
