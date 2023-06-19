package com.raxim.myscoutee.profile.service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raxim.myscoutee.common.util.JsonUtil;
import com.raxim.myscoutee.profile.data.document.mongo.Car;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.document.mongo.Role;
import com.raxim.myscoutee.profile.data.document.mongo.School;
import com.raxim.myscoutee.profile.data.dto.rest.CarDTO;
import com.raxim.myscoutee.profile.data.dto.rest.ProfileDTO;
import com.raxim.myscoutee.profile.data.dto.rest.ProfileStatusDTO;
import com.raxim.myscoutee.profile.data.dto.rest.SchoolDTO;
import com.raxim.myscoutee.profile.repository.mongo.CarEventHandler;
import com.raxim.myscoutee.profile.repository.mongo.CarRepository;
import com.raxim.myscoutee.profile.repository.mongo.ProfileEventHandler;
import com.raxim.myscoutee.profile.repository.mongo.ProfileRepository;
import com.raxim.myscoutee.profile.repository.mongo.RoleRepository;
import com.raxim.myscoutee.profile.repository.mongo.SchoolRepository;
import com.raxim.myscoutee.profile.repository.mongo.UserRepository;
import com.raxim.myscoutee.profile.util.ProfileUtil;

//val reqGroupId = UUID.fromString(groupId)
//if (profile.group == reqGroupId) {
/*val role = this.roleRepository.findRoleByProfileAndGroup(adminEmail, groupId = reqGroupId)
    .firstOrNull { role -> role.group.id == reqGroupId && role.role == FirebaseService.ROLE_ADMIN && role.status == "A" }*/
//}

@Service
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final ProfileEventHandler profileEventHandler;
    private final CarRepository carRepository;
    private final CarEventHandler carEventHandler;
    private final SchoolRepository schoolRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ObjectMapper objectMapper;

    public ProfileService(
            ProfileRepository profileRepository,
            ProfileEventHandler profileEventHandler,
            CarRepository carRepository,
            CarEventHandler carEventHandler,
            SchoolRepository schoolRepository,
            UserRepository userRepository,
            RoleRepository roleRepository,
            ObjectMapper objectMapper) {
        this.profileRepository = profileRepository;
        this.profileEventHandler = profileEventHandler;
        this.carRepository = carRepository;
        this.carEventHandler = carEventHandler;
        this.schoolRepository = schoolRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.objectMapper = objectMapper;
    }

    public ProfileDTO saveProfileStatus(String profileId,
            ProfileStatusDTO profileStatus) {
        UUID uuid = UUID.fromString(profileId);
        return profileRepository.findById(uuid).map(profile -> {
            Profile profileToSave = JsonUtil.clone(profile, objectMapper);
            profileToSave.setStatus(profileStatus.getStatus());
            Profile profileSaved = profileRepository.save(profileToSave);

            List<Role> role = this.roleRepository.findRoleByProfile(profileSaved.getId());

            Role firstRole = JsonUtil.clone(role.get(0), objectMapper);
            firstRole.setRole(profileStatus.getRole());
            this.roleRepository.save(firstRole);

            return new ProfileDTO(profileSaved);
        }).orElse(null);
    }

    public ProfileDTO saveProfile(UUID userId, UUID profileId, UUID group,
            Profile profile, MultipartFile voice) throws IOException {

        String fileName = UUID.randomUUID().toString();

        Optional<Profile> profileResult = profileId != null ? profileRepository.findById(profileId) : Optional.empty();

        Profile clonedProfile = profile.clone();
        if (profileResult.isPresent()) {
            Profile profileEntity = profileResult.get();

            clonedProfile.setId(profileEntity.getId());
            clonedProfile.setCars(profileEntity.getCars());
            clonedProfile.setSchools(profileEntity.getSchools());
            if (profileEntity.getVoice() == null) {
                clonedProfile.setVoice(fileName);
            }
            clonedProfile.setGroup(group);
        } else {
            clonedProfile.setId(UUID.randomUUID());
            clonedProfile.setVoice(fileName);
            clonedProfile.setGroup(group);
        }
        clonedProfile.setScore(ProfileUtil.score(clonedProfile));

        ProfileUtil.saveVoice(voice, clonedProfile.getVoice());

        Profile profileSaved = this.profileRepository.save(clonedProfile);
        this.userRepository.addProfile(userId, profileSaved);

        this.profileEventHandler.handleAfterCreate(profileSaved);

        ProfileDTO profileDTO = new ProfileDTO();
        profileDTO.setProfile(profileSaved);
        return profileDTO;
    }

    public List<CarDTO> getCars(UUID profileId, Integer step, Object[] tOffset) {
        int pageSize = 20;
        int defaultStep = 5;

        return this.profileRepository.findCarsByProfile(
                profileId,
                pageSize,
                step != null ? step : defaultStep,
                tOffset);
    }

    public Optional<Car> getCarByProfile(UUID profileId, UUID carId) {
        return this.profileRepository.findCarByProfile(profileId, carId);
    }

    public List<SchoolDTO> getSchools(UUID profileId, Integer step, Object[] tOffset) {
        int pageSize = 20;
        int defaultStep = 5;

        return this.profileRepository.findSchoolsByProfile(
                profileId,
                pageSize,
                step != null ? step : defaultStep,
                tOffset);
    }

    public Optional<School> getSchoolByProfile(UUID profileId,
            UUID schoolId) {
        return this.profileRepository.findSchoolByProfile(profileId, schoolId);
    }

    public CarDTO addCar(UUID profileId, UUID carId, Car newCar) {
        return this.profileRepository.findById(profileId).map(profile -> {
            Car car;
            if (carId != null) {
                Car dbCar = profile.getCars().stream()
                        .filter(c -> c.getId().equals(carId))
                        .findFirst()
                        .orElse(null);
                car = JsonUtil.clone(newCar, objectMapper);
                car.setId(dbCar.getId());
            } else {
                car = newCar;
            }

            Car savedCar = carRepository.save(car);
            profile.getCars().add(savedCar);
            profileRepository.save(profile);
            carEventHandler.handleAfterCreate(car);
            return new CarDTO(savedCar);
        }).orElse(null);
    }

    public List<SchoolDTO> saveSchools(UUID profileId,
            List<School> schools) {
        return this.profileRepository.findById(profileId).map(profile -> {
            for (School school : schools) {
                School savedSchool = this.schoolRepository.save(school);
                boolean hasSchool = profile.getSchools().stream().anyMatch(s -> s.getId().equals(savedSchool.getId()));
                if (!hasSchool) {
                    profile.getSchools().add(savedSchool);
                }
            }
            this.profileRepository.save(profile);

            return profile.getSchools().stream().map(school -> new SchoolDTO(school)).toList();
        }).orElse(Collections.emptyList());
    }
}
