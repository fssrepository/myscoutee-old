package com.raxim.myscoutee.profile.controller;

import java.io.File;
import java.io.IOException;

import org.springframework.data.rest.webmvc.BasePathAwareController;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.raxim.myscoutee.common.util.FileUtil;
import com.raxim.myscoutee.profile.data.document.mongo.School;

import jakarta.servlet.http.HttpServletResponse;

//@BasePathAwareController
@RestController
@RequestMapping("/")
public class BaseOverrideRestController {
    @RequestMapping(path = {
            "schools", "schools/{id}", "cars", "cars/{id}", "items", "items/{id}",
            "feedbacks", "feedbacks/{id}", "setting", "setting/{id}"
    })
    public ResponseEntity<School> doRequest() {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }

    @GetMapping(path = {
            "profiles/{id}/images/{name}",
            "user/profile/images/{name}",
            "user/cars/{id}/images/{name}",
            "user/cars/images/{name}",
            "user/groups/images/{name}",
            "user/groups/{id}/images/{name}",
            "user/groups/{id}/profiles/{pId}/images/{name}",
            "profiles/{profileId}/cars/{id}/images/{name}",
            "profiles/{profileId}/cars/images/{name}",
            "games/{type}/{id}/images/{name}",
            "activity/events/{eventId}/members/{id}/images/{name}",
            "activity/events/{eventId}/items/{itemId}/members/{id}/images/{name}",
            "activity/invitations/{eventId}/members/{id}/images/{name}",
            "promotions/{promoId}/events/{eventId}/members/{id}/images/{name}",
            "promotions/{promoId}/members/{id}/images/{name}",
            "ideas/people/{id}/images/{name}",
            "games/rate_double/{selected}/{type}/{id}/images/{name}"
    })
    public void download(@PathVariable String name, HttpServletResponse response) throws IOException {
        String[] pName = name.split("_orig");
        String sub = (pName.length > 1) ? "_orig" : "";

        Pair<String, String> pathInfo = FileUtil.tempToPath(pName[0], false);
        String separator = pathInfo.getFirst();
        String tmpDir = pathInfo.getSecond();
        File file = new File(tmpDir + separator + "_" + pName[0] + sub);

        if (!file.exists()) {
            Pair<String, String> fullDirInfo = FileUtil.uuidToPath(pName[0], false);
            String fullDir = fullDirInfo.getSecond();
            file = new File(fullDir + separator + "_" + pName[0] + sub);
        }
        FileUtil.copyFileToHttp(response, file);
    }

    @GetMapping(path = {
            "profiles/{id}/voice/{name}"
    })
    public void voice(@PathVariable String id, @PathVariable String name, HttpServletResponse response)
            throws IOException {
        Pair<String, String> pathInfo = FileUtil.uuidToPath(name, false);
        String separator = pathInfo.getFirst();
        String fullDir = pathInfo.getSecond();
        File file = new File(fullDir + separator + "_" + name + "_voice");

        FileUtil.copyFileToHttp(response, file);
    }
}
