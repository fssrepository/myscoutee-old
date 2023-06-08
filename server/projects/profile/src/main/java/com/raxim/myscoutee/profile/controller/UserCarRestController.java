package com.raxim.myscoutee.profile.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.raxim.myscoutee.common.config.firebase.dto.FirebasePrincipal;
import com.raxim.myscoutee.common.util.CommonUtil;
import com.raxim.myscoutee.profile.data.document.mongo.Car;
import com.raxim.myscoutee.profile.data.dto.rest.CarDTO;
import com.raxim.myscoutee.profile.data.dto.rest.PageDTO;
import com.raxim.myscoutee.profile.repository.mongo.CarRepository;
import com.raxim.myscoutee.profile.service.ProfileService;

@RepositoryRestController
@RequestMapping("user")
public class UserCarRestController {
    private final ProfileService profileService;
    private final CarRepository carRepository;

    public UserCarRestController(ProfileService profileService, CarRepository carRepository) {
        this.profileService = profileService;
        this.carRepository = carRepository;
    }

    @GetMapping("/cars")
    public ResponseEntity<PageDTO<CarDTO>> getCars(
            Authentication auth,
            @RequestParam(value = "step", required = false) Integer step,
            @RequestParam(value = "offset", required = false) String[] offset) {
        FirebasePrincipal principal = (FirebasePrincipal) auth.getPrincipal();
        UUID profileId = principal.getUser().getProfile().getId();

        Object[] tOffset = (offset != null && offset.length == 1) ? new Object[] { CommonUtil.decode(offset[0]) }
                : new Object[] { "1900-01-01" };

        if (profileId == null) {
            return ResponseEntity.notFound().build();
        } else {
            List<CarDTO> cars = profileService.getCars(profileId, step, tOffset);

            List<Object> lOffset = cars.isEmpty() ? Arrays.asList(tOffset) : cars.get(cars.size() - 1).getOffset();

            return ResponseEntity.ok(new PageDTO<>(cars, lOffset));
        }
    }

    @PostMapping("/cars")
    @Transactional
    public ResponseEntity<CarDTO> addCar(
            Authentication auth,
            @RequestBody Car car) {
        FirebasePrincipal principal = (FirebasePrincipal) auth.getPrincipal();
        UUID profileId = principal.getUser().getProfile().getId();

        if (profileId == null) {
            return ResponseEntity.notFound().build();
        } else {
            CarDTO carDto = profileService.addCar(profileId, null, car);
            return ResponseEntity.status(HttpStatus.CREATED).body(carDto);
        }
    }

    @PatchMapping("/cars/{id}")
    @Transactional
    public ResponseEntity<CarDTO> patchCar(
            Authentication auth,
            @PathVariable String id,
            @RequestBody Car car) {
        FirebasePrincipal principal = (FirebasePrincipal) auth.getPrincipal();
        UUID profileId = principal.getUser().getProfile().getId();

        if (profileId == null) {
            return ResponseEntity.notFound().build();
        } else {
            CarDTO carDto = profileService.addCar(profileId, UUID.fromString(id), car);
            return ResponseEntity.ok(carDto);
        }
    }

    @DeleteMapping("cars/{id}")
    public ResponseEntity<?> deleteCar(
            Authentication auth,
            @PathVariable String id) {
        FirebasePrincipal principal = (FirebasePrincipal) auth.getPrincipal();
        UUID profileId = principal.getUser().getProfile().getId();

        if (profileId == null) {
            return ResponseEntity.notFound().build();
        } else {
            Optional<Car> car = profileService.getCarByProfile(profileId, UUID.fromString(id));
            if (car.isPresent()) {
                Car carT = car.get();
                carT.setStatus("D");
                carRepository.save(carT);
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        }
    }
}
