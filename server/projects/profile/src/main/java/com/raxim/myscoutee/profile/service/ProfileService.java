package com.raxim.myscoutee.profile.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.threeten.bp.Instant;

import com.raxim.myscoutee.algo.dto.Range;
import com.raxim.myscoutee.common.util.CommonUtil;
import com.raxim.myscoutee.common.util.SettingUtil;
import com.raxim.myscoutee.profile.data.document.mongo.Event;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.document.mongo.RangeLocal;
import com.raxim.myscoutee.profile.data.dto.rest.ISODateRange;
import com.raxim.myscoutee.profile.data.dto.rest.GroupDTO;
import com.raxim.myscoutee.profile.data.dto.rest.PageParam;
import com.raxim.myscoutee.profile.data.dto.rest.ProfileDTO;
import com.raxim.myscoutee.profile.data.dto.rest.SettingDTO;
import com.raxim.myscoutee.profile.exception.MessageException;
import com.raxim.myscoutee.profile.repository.mongo.EventRepository;
import com.raxim.myscoutee.profile.repository.mongo.ProfileEventHandler;
import com.raxim.myscoutee.profile.repository.mongo.ProfileRepository;
import com.raxim.myscoutee.profile.repository.mongo.UserRepository;
import com.raxim.myscoutee.profile.util.AppConstants;
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
    private final UserRepository userRepository;
    private final SettingsService settingsService;

    public ProfileService(
            ProfileRepository profileRepository,
            EventRepository eventRepository,
            ProfileEventHandler profileEventHandler,
            UserRepository userRepository,
            SettingsService settingsService) {
        this.profileRepository = profileRepository;
        this.eventRepository = eventRepository;
        this.profileEventHandler = profileEventHandler;
        this.userRepository = userRepository;
        this.settingsService = settingsService;
    }

    public List<ProfileDTO> getProfiles(PageParam pageParam, Profile profile, String pKey, double direction,
            int score)
            throws MessageException {

        if (profile.getPosition() == null) {
            throw new MessageException(AppConstants.ERR_NO_PROFILE);
        }

        if ("F".equals(profile.getStatus())) {
            throw new MessageException(AppConstants.ERR_FRIENDS_ONLY);
        }

        if ("I".equals(profile.getStatus())) {
            throw new MessageException(AppConstants.ERR_INVISIBLE_PROFILE);
        }

        // gender,met,direction as filter criteria
        // based on pKey it can be different, maybe create ParamParserForDouble etc.
        Optional<SettingDTO> optSetting = this.settingsService.getSetting(profile.getId(), pKey);

        Optional<String> optMet = SettingUtil.getValue(optSetting, "met");
        boolean met = optMet.isPresent() ? Boolean.parseBoolean(optMet.get()) : false;

        Optional<Range> ageRangeOpt = SettingUtil.getRange(optSetting, "age");
        Range ageRange = ageRangeOpt.isPresent() ? ageRangeOpt.get() : new Range(18, 100);

        LocalDateTime bStart = LocalDateTime.now().minus(ageRange.getMax(), ChronoUnit.YEARS);
        LocalDateTime bEnd = LocalDateTime.now().minus(ageRange.getMin(), ChronoUnit.YEARS);

        RangeLocal rangeLocal = new RangeLocal(bStart, bEnd);

        Optional<String> optGender = SettingUtil.getValue(optSetting, "gender");

        Optional<String> optDirection = SettingUtil.getValue(optSetting, "direction");
        direction = optDirection.isPresent() ? Double.parseDouble(optDirection.get()) : direction;

        String gender = null;
        UUID selectUuid = pageParam.getSelectedId() != null ? UUID.fromString(pageParam.getSelectedId()) : null;
        if (selectUuid != null) {
            Optional<Profile> selected = profileRepository.findById(selectUuid);
            if (!selected.isPresent()) {
                throw new MessageException(AppConstants.ERR_NO_PROFILE);
            }

            Profile sProfile = selected.get();
            gender = sProfile.getGender().equals("m") ? "w" : "m";
        } else {
            gender = optGender.isPresent() ? optGender.get() : profile.getGender().equals("m") ? "w" : "m";
        }

        // $met variable is a filter for findProfile on the list screen,
        // can be true or false (events are not even generated, but both liked)
        // isDouble = true, direction is ignored
        List<ProfileDTO> profileDTOs = profileRepository.findProfile(
                pageParam,
                CommonUtil.point(profile.getPosition()),
                gender,
                profile.getGroup(),
                direction, score, met, selectUuid, rangeLocal);
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

        if (clonedProfile.getVoice() != null) {
            ProfileUtil.saveVoice(voice, clonedProfile.getVoice());
        }

        Profile profileSaved = this.profileRepository.save(clonedProfile);
        this.userRepository.addProfile(userId, profileSaved);

        ProfileDTO profileDTO = new ProfileDTO();
        profileDTO.setProfile(profileSaved);
        return profileDTO;
    }
}
