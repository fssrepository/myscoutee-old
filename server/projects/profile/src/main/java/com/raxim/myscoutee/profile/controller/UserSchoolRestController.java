package com.raxim.myscoutee.profile.controller;

import java.util.ArrayList;
import java.util.List;

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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.raxim.myscoutee.common.config.firebase.dto.FirebasePrincipal;
import com.raxim.myscoutee.common.util.CommonUtil;
import com.raxim.myscoutee.common.util.ControllerUtil;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.document.mongo.School;
import com.raxim.myscoutee.profile.data.dto.rest.PageDTO;
import com.raxim.myscoutee.profile.data.dto.rest.PageParam;
import com.raxim.myscoutee.profile.data.dto.rest.SchoolDTO;
import com.raxim.myscoutee.profile.handler.ParamHandlers;
import com.raxim.myscoutee.profile.handler.SchoolParamHandler;
import com.raxim.myscoutee.profile.service.SchoolService;

@RestController
@RequestMapping("user")
public class UserSchoolRestController {
    private final SchoolService schoolService;
    private final ParamHandlers paramHandlers;

    public UserSchoolRestController(SchoolService schoolService, ParamHandlers paramHandlers) {
        this.schoolService = schoolService;
        this.paramHandlers = paramHandlers;
    }

    // TODO: school fix - discreet group - isBusiness/isSchool event - discreet
    // level
    @GetMapping("/schools")
    public ResponseEntity<PageDTO<SchoolDTO>> getSchools(@PathVariable String id, PageParam pageParam,
            Authentication auth) {
        FirebasePrincipal firebasePrincipal = (FirebasePrincipal) auth.getPrincipal();
        Profile profile = firebasePrincipal.getUser().getProfile();

        pageParam = paramHandlers.handle(profile, pageParam, SchoolParamHandler.TYPE);

        List<SchoolDTO> schoolDTOs = schoolService.getSchools(profile.getId(), pageParam);
        List<Object> lOffset = CommonUtil.offset(schoolDTOs, pageParam.getOffset());

        return ResponseEntity.ok(new PageDTO<>(schoolDTOs, lOffset));
    }

    @PostMapping("/schools")
    @Transactional
    public ResponseEntity<List<SchoolDTO>> saveSchool(
            Authentication auth,
            @RequestBody List<School> schools) {
        Profile profile = ((FirebasePrincipal) auth.getPrincipal()).getUser().getProfile();

        return ControllerUtil.handleList((i, s) -> schoolService.saveSchools(i, s),
                profile.getId(), schools, HttpStatus.CREATED);
    }

    @PatchMapping("/schools/{id}")
    @Transactional
    public ResponseEntity<List<SchoolDTO>> patchSchool(
            Authentication auth,
            @PathVariable String id,
            @RequestBody List<School> schools) {
        Profile profile = ((FirebasePrincipal) auth.getPrincipal()).getUser().getProfile();

        return ControllerUtil.handleList((i, s) -> schoolService.saveSchools(i, s),
                profile.getId(), schools, HttpStatus.OK);
    }

    @DeleteMapping("/schools/{id}")
    public ResponseEntity<?> deleteSchool(
            Authentication auth,
            @PathVariable String id) {
        Profile profile = ((FirebasePrincipal) auth.getPrincipal()).getUser().getProfile();

        School school = new School();
        school.setStatus("D");
        List<School> schools = List.of(school);

        return ControllerUtil.handleList((i, s) -> schoolService.saveSchools(i, s),
                profile.getId(), schools, HttpStatus.NO_CONTENT);
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
