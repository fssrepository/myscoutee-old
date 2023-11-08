package com.raxim.myscoutee.common;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

public class FileSystemTest {

    @Test
    public void shouldGetFiles() throws IOException {

//        "jar:file:/app/spring-boot-application.jar!/BOOT-INF/classes!/mongo/profile"

        /*String userDir = new File(System.getProperty("user.dir")).getAbsolutePath() + "/build/libs";

        URLClassLoader jarClassLoader = new URLClassLoader(
                new URL[] { new URL("file://" + userDir + "/profile-0.0.1-SNAPSHOT.jar") },
                this.getClass().getClassLoader());

        ClassPathResource classPathResource = new ClassPathResource("/mongo/profile", jarClassLoader);

        FileSystem fileSystem = FileSystems.newFileSystem(classPathResource.getURI(),
                Collections.<String, Object>emptyMap());
        Path path = fileSystem.getPath("/");

        Files.list(path).toList();

        System.out.println();*/
    }
}
