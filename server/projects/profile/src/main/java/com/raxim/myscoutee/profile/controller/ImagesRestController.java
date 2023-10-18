package com.raxim.myscoutee.profile.controller;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.UUID;

import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.raxim.myscoutee.common.util.FileUtil;
import com.raxim.myscoutee.profile.data.dto.rest.ImageDTO;

import jakarta.servlet.http.HttpServletResponse;

@RepositoryRestController("images")
public class ImagesRestController {

    @GetMapping("/{name}")
    public void download(@PathVariable String name, HttpServletResponse response) throws IOException {
        Pair<String, String> tempPath = FileUtil.tempToPath(name, false);
        String separator = tempPath.getFirst();
        String tmpDir = tempPath.getSecond();

        File file = new File(tmpDir + separator + "_" + name);
        FileUtil.copyFileToHttp(response, file);
    }

    @PostMapping(value = { "", "/{name}" })
    public ResponseEntity<ImageDTO> upload(
            @PathVariable(required = false) String name,
            @RequestParam("orig") MultipartFile orig,
            @RequestParam("edited") MultipartFile edited) {
        String fileName = name != null ? name : UUID.randomUUID().toString();
        try {
            Pair<String, String> tempPath = FileUtil.tempToPath(fileName, true);
            String separator = tempPath.getFirst();
            String tmpDir = tempPath.getSecond();

            if (edited != null && !edited.isEmpty()) {
                String completeImageData = new String(edited.getBytes(), Charset.forName("UTF-8"));
                String imageDataBytes = completeImageData.substring(completeImageData.indexOf(",") + 1);
                InputStream imgStream = new ByteArrayInputStream(Base64.getMimeDecoder().decode(imageDataBytes));

                FileUtil.save(imgStream, tmpDir + separator + "_" + fileName);
            }

            if (orig != null && !orig.isEmpty()) {
                InputStream origIs = new ByteArrayInputStream(orig.getBytes());
                String origFile = tmpDir + separator + "_" + fileName + "_orig";

                FileUtil.save(origIs, origFile);

                return ResponseEntity.status(HttpStatus.CREATED).body(new ImageDTO(fileName));
            } else {
                Pair<String, String> fullDir = FileUtil.uuidToPath(fileName, true);
                String fullDirName = fullDir.getSecond();
                String origFileName = fullDirName + separator + "_" + fileName + "_orig";
                String tmpOrigFileName = tmpDir + separator + "_" + fileName + "_orig";

                File permOrigFile = new File(origFileName);
                File tmpOrigFile = new File(tmpOrigFileName);

                if (permOrigFile.exists() || tmpOrigFile.exists()) {
                    return ResponseEntity.status(HttpStatus.OK).body(new ImageDTO(fileName));
                } else {
                    return ResponseEntity.badRequest().build();
                }
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
