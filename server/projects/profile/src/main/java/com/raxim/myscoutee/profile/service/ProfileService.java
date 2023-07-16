package com.raxim.myscoutee.profile.service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raxim.myscoutee.common.util.CommonUtil;
import com.raxim.myscoutee.common.util.JsonUtil;
import com.raxim.myscoutee.profile.data.document.mongo.Car;
import com.raxim.myscoutee.profile.data.document.mongo.Event;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.document.mongo.School;
import com.raxim.myscoutee.profile.data.dto.rest.CarDTO;
import com.raxim.myscoutee.profile.data.dto.rest.GroupDTO;
import com.raxim.myscoutee.profile.data.dto.rest.PageParam;
import com.raxim.myscoutee.profile.data.dto.rest.ProfileDTO;
import com.raxim.myscoutee.profile.data.dto.rest.SchoolDTO;
import com.raxim.myscoutee.profile.repository.mongo.CarEventHandler;
import com.raxim.myscoutee.profile.repository.mongo.CarRepository;
import com.raxim.myscoutee.profile.repository.mongo.EventRepository;
import com.raxim.myscoutee.profile.repository.mongo.ProfileEventHandler;
import com.raxim.myscoutee.profile.repository.mongo.ProfileRepository;
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
    private final EventRepository eventRepository;
    private final ProfileEventHandler profileEventHandler;
    private final CarRepository carRepository;
    private final CarEventHandler carEventHandler;
    private final SchoolRepository schoolRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final SettingsService settingsService;

    public ProfileService(
            ProfileRepository profileRepository,
            EventRepository eventRepository,
            ProfileEventHandler profileEventHandler,
            CarRepository carRepository,
            CarEventHandler carEventHandler,
            SchoolRepository schoolRepository,
            UserRepository userRepository,
            ObjectMapper objectMapper,
            SettingsService settingsService) {
        this.profileRepository = profileRepository;
        this.eventRepository = eventRepository;
        this.profileEventHandler = profileEventHandler;
        this.carRepository = carRepository;
        this.carEventHandler = carEventHandler;
        this.schoolRepository = schoolRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
        this.settingsService = settingsService;
    }

    public List<ProfileDTO> getProfiles(PageParam pageParam, Profile profile, String pKey, double direction,
            int score) {

        Optional<String> optMet = this.settingsService.getValue(profile.getId(), pKey, "met");
        boolean met = optMet.isPresent() ? Boolean.parseBoolean(optMet.get()) : false;

        String gender = profile.getGender().equals("m") ? "w" : "m";

        // $met variable is a filter for findProfile on the list screen,
        // can be true or false (events are not even generated, but both liked)
        List<ProfileDTO> profileDTOs = profileRepository.findProfile(
                pageParam,
                CommonUtil.point(profile.getPosition()),
                gender,
                profile.getGroup(),
                direction, score, met);
        return profileDTOs;
    }

    public List<GroupDTO> getGroupsByProfile(UUID profileId, PageParam pageParam) {
        return this.profileRepository.findGroupsByProfile(profileId, pageParam);
    }

    public Optional<ProfileDTO> saveProfile(String profileId,
            Profile pProfile) {
        UUID pProfileUuid = UUID.fromString(profileId);
        Optional<Profile> dbProfile = profileRepository.findById(pProfileUuid);

        if (dbProfile.isPresent()) {
            Profile profile = dbProfile.get();
            // ??? - default is U, and admin of the group can change it
            profile.setRole(pProfile.getRole());
            profile.setStatus(pProfile.getStatus());

            List<Event> events = this.eventRepository.findAll();

            List<Event> sEvents = null;
            // friends only
            if ("F".equals(pProfile.getStatus())) {
                sEvents = changeStatus(profile, events.stream()
                        .filter(event -> "P".equals(event.getStatus()) && "A".equals(event.getAccess())), "LF");
            } else if ("I".equals(pProfile.getStatus())) {
                sEvents = changeStatus(profile, events.stream()
                        .filter(event -> "P".equals(event.getStatus()) && !"P".equals(event.getAccess())), "LI");
            } else if ("S".equals(pProfile.getStatus())) {
                sEvents = changeStatus(profile, events.stream()
                        .filter(event -> "P".equals(event.getStatus())), "LS");
            } else if ("L".equals(pProfile.getStatus())) {
                sEvents = changeStatus(profile, events.stream(), "LG");
            }
            this.eventRepository.saveAll(sEvents);

            Profile profileSaved = profileRepository.save(profile);
            return Optional.of(new ProfileDTO(profileSaved));
        }
        return Optional.empty();
    }

    private List<Event> changeStatus(Profile profile, Stream<Event> sEvents, String toStatus) {
        return sEvents.map(event -> {
            event.getMembers().stream().filter(member -> profile.getId().equals(member.getProfile().getId()))
                    .map(member -> {
                        member.setStatus(toStatus); // friends only leave
                        return member;
                    });
            return event;
        }).toList();
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
