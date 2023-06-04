package com.raxim.myscoutee.common;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class InputStreamUtil {
    public static void save(InputStream inputStream, String fileName) throws IOException {
        File serverFile = new File(fileName);
        try (BufferedOutputStream buffStream = new BufferedOutputStream(new FileOutputStream(serverFile))) {
            int bytesRead;
            byte[] buffer = new byte[inputStream.available()];
            if ((bytesRead = inputStream.read(buffer)) != -1) {
                buffStream.write(buffer, 0, bytesRead);
            }
        }
    }
}