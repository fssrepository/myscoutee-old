package com.raxim.myscoutee.common.file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

import org.springframework.data.util.Pair;
import org.springframework.util.FileCopyUtils;

import jakarta.servlet.http.HttpServletResponse;

public class FileUtil {

    private static final String separator = File.separator;
    private static final String rootPath = System.getProperty("java.io.tmpdir") + separator + "img";

    public static void save(InputStream inputStream, String fileName) throws IOException {
        File serverFile = new File(fileName);
        BufferedOutputStream buffStream = new BufferedOutputStream(new FileOutputStream(serverFile));
        int bytesRead;
        byte[] buffer = new byte[inputStream.available()];
        if ((bytesRead = inputStream.read(buffer)) != -1) {
            buffStream.write(buffer, 0, bytesRead);
        }
        buffStream.close();
    }

    public static void copyFileToHttp(HttpServletResponse response, File file) throws IOException {
        if (!file.exists()) {
            return;
        }

        String mimeType = URLConnection.guessContentTypeFromName(file.getName());
        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }
        response.setContentType(mimeType);
        response.setHeader("Content-Disposition", String.format("inline; filename=\"" + file.getName() + "\""));
        response.setContentLength((int) file.length());

        BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));

        FileCopyUtils.copy(inputStream, response.getOutputStream());
    }

    public static Pair<String, String> uuidToPath(String fileName, boolean isWrite) {
        String[] dirs = { rootPath, "permanent" };
        for (String part : fileName.split("-")) {
            dirs = appendToArray(dirs, part);
        }
        dirs = appendToArray(dirs, "");

        StringBuilder fullDir = new StringBuilder();
        for (String dir : dirs) {
            File file = new File(fullDir.toString());
            if (isWrite && !file.exists()) {
                file.mkdirs();
            }
            fullDir.append(separator).append(dir);
        }

        return Pair.of(separator, fullDir.toString());
    }

    public static Pair<String, String> tempToPath(String fileName, boolean isWrite) {
        String[] dirs = { rootPath, "uploaded" };
        dirs = appendToArray(dirs, "");

        StringBuilder fullDir = new StringBuilder();
        for (String dir : dirs) {
            File file = new File(fullDir.toString());
            if (isWrite && !file.exists()) {
                file.mkdirs();
            }
            fullDir.append(separator).append(dir);
        }

        return Pair.of(separator, fullDir.toString());
    }

    private static String[] appendToArray(String[] array, String element) {
        String[] newArray = new String[array.length + 1];
        System.arraycopy(array, 0, newArray, 0, array.length);
        newArray[array.length] = element;
        return newArray;
    }
}
