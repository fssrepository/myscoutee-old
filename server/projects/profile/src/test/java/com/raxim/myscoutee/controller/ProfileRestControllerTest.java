package com.raxim.myscoutee.controller;

import java.io.FileInputStream;
import java.nio.file.Paths;
import java.util.UUID;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.raxim.myscoutee.common.config.WebSecurityConfig;
import com.raxim.myscoutee.profile.controller.ImagesRestController;

@Disabled
@WebMvcTest(excludeAutoConfiguration = { WebSecurityConfig.class }, controllers = { ImagesRestController.class })
public class ProfileRestControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    public void shouldUploadFile() throws Exception {

        MockMultipartFile canvasFile = new MockMultipartFile(
                "file",
                "img.canvas",
                "multipart/form-data",
                new FileInputStream(Paths.get("src/test/resources/file", "img.canvas").toFile()));

        mvc.perform(MockMvcRequestBuilders.multipart("/images")
                .file(canvasFile))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn();

        UUID profileId = UUID.randomUUID();
        /*
         * String regex =
         * "[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}";
         * 
         * String responseContent = result.getResponse().getContentAsString();
         * 
         * Pattern pattern = Pattern.compile(regex);
         * Matcher matcher = pattern.matcher(responseContent);
         * 
         * if (matcher.find()) {
         * String imageId = matcher.group();
         * 
         * String requestJson = "{\"images\":[\"" + imageId + "\"]}";
         * 
         * mvc.perform(post("/profiles").contentType(MediaType.APPLICATION_JSON_VALUE)
         * .content(requestJson))
         * .andExpect(status().isCreated);
         * }
         */
    }
}
