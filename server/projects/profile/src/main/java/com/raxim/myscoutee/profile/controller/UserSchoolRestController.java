package com.raxim.myscoutee.profile.controller;

import java.util.ArrayList;
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
import org.springframework.web.multipart.MultipartFile;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.raxim.myscoutee.common.config.firebase.dto.FirebasePrincipal;
import com.raxim.myscoutee.common.util.CommonUtil;
import com.raxim.myscoutee.profile.data.document.mongo.School;
import com.raxim.myscoutee.profile.data.dto.rest.PageDTO;
import com.raxim.myscoutee.profile.data.dto.rest.SchoolDTO;
import com.raxim.myscoutee.profile.repository.mongo.SchoolRepository;
import com.raxim.myscoutee.profile.service.ProfileService;

@RepositoryRestController
@RequestMapping("user")
public class UserSchoolRestController {
    private final ProfileService profileService;
    private final SchoolRepository schoolRepository;

    public UserSchoolRestController(ProfileService profileService, SchoolRepository schoolRepository) {
        this.profileService = profileService;
        this.schoolRepository = schoolRepository;
    }

    // TODO: school fix - discreet group - isBusiness/isSchool event - discreet level
    @GetMapping("/schools")
    public ResponseEntity<PageDTO<SchoolDTO>> getSchools(
            Authentication auth,
            @RequestParam(value = "step", required = false) Integer step,
            @RequestParam(value = "offset", required = false) String[] offset) {
        FirebasePrincipal principal = (FirebasePrincipal) auth.getPrincipal();
        UUID profileId = principal.getUser().getProfile().getId();

        Object[] tOffset = (offset != null && offset.length == 3)
                ? new Object[] { CommonUtil.decode(offset[0]), CommonUtil.decode(offset[1]),
                        CommonUtil.decode(offset[2]) }
                : new Object[] { "a", "1900-01-01", "1900-01-01" };

        if (profileId == null) {
            return ResponseEntity.notFound().build();
        } else {
            List<SchoolDTO> schools = profileService.getSchools(profileId, step, tOffset);

            List<Object> lOffset = schools.isEmpty() ? Arrays.asList(tOffset)
                    : schools.get(schools.size() - 1).getOffset();

            return ResponseEntity.ok(new PageDTO<>(schools, lOffset));
        }
    }

    @PostMapping("/schools")
    @Transactional
    public ResponseEntity<List<SchoolDTO>> saveSchool(
            Authentication auth,
            @RequestBody List<School> schools) {
        FirebasePrincipal principal = (FirebasePrincipal) auth.getPrincipal();
        UUID profileId = principal.getUser().getProfile().getId();

        if (profileId == null) {
            return ResponseEntity.notFound().build();
        } else {
            List<SchoolDTO> schoolsDto = profileService.saveSchools(profileId, schools);
            return ResponseEntity.status(HttpStatus.CREATED).body(schoolsDto);
        }
    }

    @PatchMapping("/schools/{id}")
    @Transactional
    public ResponseEntity<List<SchoolDTO>> patchSchool(
            Authentication auth,
            @PathVariable String id,
            @RequestBody List<School> schools) {
        FirebasePrincipal principal = (FirebasePrincipal) auth.getPrincipal();
        UUID profileId = principal.getUser().getProfile().getId();

        if (profileId == null) {
            return ResponseEntity.notFound().build();
        } else {
            List<SchoolDTO> schoolsDto = profileService.saveSchools(profileId, schools);
            return ResponseEntity.ok(schoolsDto);
        }
    }

    @DeleteMapping("/schools/{id}")
    public ResponseEntity<?> deleteSchool(
            Authentication auth,
            @PathVariable String id) {
        FirebasePrincipal principal = (FirebasePrincipal) auth.getPrincipal();
        UUID profileId = principal.getUser().getProfile().getId();

        if (profileId == null) {
            return ResponseEntity.notFound().build();
        } else {
            //TODO: school fix why we need new method findById is fine for schoolRepository
            Optional<School> school = profileService
                    .getSchoolByProfile(profileId, UUID.fromString(id));
            if (school.isPresent()) {
                School schoolT = school.get();
                schoolT.setStatus("D");
                schoolRepository.save(schoolT);
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        }
    }

    @PostMapping("/schools/parse")
    public ResponseEntity<List<School>> upload(
            Authentication auth,
            @RequestParam("file") MultipartFile file) {
        if (file != null && !file.isEmpty()) {
            try {
                PdfReader pdfReader = new PdfReader(file.getBytes());
                List<School> schools = new ArrayList<>();

                for (int i = 1; i <= pdfReader.getNumberOfPages(); i++) {
                    String cnt = PdfTextExtractor.getTextFromPage(pdfReader, i);
                    String[] lines = cnt.split("\n");

                    for (String line : lines) {
                        try {
                            java.util.regex.Matcher matcher = CommonUtil.dateRegex.matcher(line);
                            if (matcher.find()) {
                                String str = matcher.group(1);
                                School school = new School(CommonUtil.parseRange(str));
                                schools.add(school);
                            }
                        } catch (Exception ex) {
                            System.out.println(ex);
                        }
                    }
                }

                return ResponseEntity.ok(schools);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } else {
            return ResponseEntity.badRequest().build();
        }
    }
}
