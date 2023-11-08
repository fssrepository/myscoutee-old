package com.raxim.myscoutee.profile.controller;

import java.util.List;
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
import org.springframework.web.bind.annotation.RestController;

import com.raxim.myscoutee.common.config.firebase.dto.FirebasePrincipal;
import com.raxim.myscoutee.common.util.CommonUtil;
import com.raxim.myscoutee.common.util.ControllerUtil;
import com.raxim.myscoutee.profile.data.document.mongo.Car;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.dto.rest.CarDTO;
import com.raxim.myscoutee.profile.data.dto.rest.PageDTO;
import com.raxim.myscoutee.profile.data.dto.rest.PageParam;
import com.raxim.myscoutee.profile.handler.CarParamHandler;
import com.raxim.myscoutee.profile.handler.ParamHandlers;
import com.raxim.myscoutee.profile.service.CarService;

@RestController
@RequestMapping("user")
public class UserCarRestController {
    private final CarService carService;
    private final ParamHandlers paramHandlers;

    public UserCarRestController(CarService carService, ParamHandlers paramHandlers) {
        this.carService = carService;
        this.paramHandlers = paramHandlers;
    }

    @GetMapping("/cars")
    public ResponseEntity<PageDTO<CarDTO>> getCars(
            PageParam pageParam,
            Authentication auth) {
        Profile profile = ((FirebasePrincipal) auth.getPrincipal()).getUser().getProfile();

        pageParam = paramHandlers.handle(profile, pageParam, CarParamHandler.TYPE);

        List<CarDTO> carDTOs = this.carService.getCars(pageParam);
        List<Object> lOffset = CommonUtil.offset(carDTOs, pageParam.getOffset());

        return ResponseEntity.ok(new PageDTO<>(carDTOs, lOffset));
    }

    @PostMapping("/cars")
    @Transactional
    public ResponseEntity<CarDTO> addCar(
            Authentication auth,
            @RequestBody Car car) {
        Profile profile = ((FirebasePrincipal) auth.getPrincipal()).getUser().getProfile();

        return ControllerUtil.handle((i, c) -> carService.saveCar(i, c),
                profile.getId(), car, HttpStatus.CREATED);
    }

    @PatchMapping("/cars/{id}")
    @Transactional
    public ResponseEntity<CarDTO> patchCar(
            Authentication auth,
            @PathVariable String id,
            @RequestBody Car car) {
        Profile profile = ((FirebasePrincipal) auth.getPrincipal()).getUser().getProfile();

        return ControllerUtil.handle((i, c) -> carService.saveCar(i, c),
                profile.getId(), car, HttpStatus.OK);
    }

    @DeleteMapping("cars/{id}")
    public ResponseEntity<CarDTO> deleteCar(
            Authentication auth,
            @PathVariable String id) {
        Profile profile = ((FirebasePrincipal) auth.getPrincipal()).getUser().getProfile();

        Car car = new Car(UUID.fromString(id));
        car.setStatus("D");

        return ControllerUtil.handle((i, c) -> carService.saveCar(i, c),
                profile.getId(), car, HttpStatus.NO_CONTENT);

    }
}
