package com.raxim.myscoutee.profile.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.document.mongo.School;
import com.raxim.myscoutee.profile.data.dto.rest.PageParam;
import com.raxim.myscoutee.profile.data.dto.rest.SchoolDTO;
import com.raxim.myscoutee.profile.repository.mongo.ProfileRepository;
import com.raxim.myscoutee.profile.repository.mongo.SchoolRepository;

@Service
public class SchoolService {

    private final SchoolRepository schoolRepository;
    private final ProfileRepository profileRepository;

    public SchoolService(SchoolRepository schoolRepository, ProfileRepository profileRepository) {
        this.schoolRepository = schoolRepository;
        this.profileRepository = profileRepository;
    }

    public List<SchoolDTO> saveSchools(UUID profileId, List<School> pSchools) throws CloneNotSupportedException {
        Optional<Profile> optProfile = this.profileRepository.findById(profileId);
        if (optProfile.isPresent()) {
            Profile profile = optProfile.get();

            List<School> lSchools = pSchools.stream().map(pSchool -> {
                try {
                    Optional<School> optSchool = profile.getSchools().stream()
                            .filter(school -> school.getId().equals(pSchool.getId()))
                            .findFirst();

                    School lSchool = (School) pSchool.clone();
                    if (optSchool.isPresent()) {
                        School dbCar = optSchool.get();
                        lSchool.setId(dbCar.getId());
                        lSchool.setStatus(pSchool.getStatus());
                    } else {
                        lSchool.setId(UUID.randomUUID());
                    }
                    return lSchool;
                } catch (CloneNotSupportedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                return null;
            }).filter(school -> school != null).toList();

            List<School> rSchools = this.schoolRepository.saveAll(lSchools);
            profile.getSchools().addAll(lSchools);
            this.profileRepository.save(profile);

            List<SchoolDTO> schoolDTOs = rSchools.stream().map(rSchool -> new SchoolDTO(rSchool)).toList();
            return schoolDTOs;
        }
        return List.of();
    }

    public List<SchoolDTO> getSchools(UUID profileId, PageParam pageParam) {
        return this.profileRepository.findSchoolsByProfile(profileId, pageParam);
    }
}
