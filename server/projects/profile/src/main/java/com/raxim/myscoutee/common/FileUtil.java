package com.raxim.myscoutee.common;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.springframework.util.FileCopyUtils;

import jakarta.servlet.http.HttpServletResponse;

public class FileUtil {
    private static final String separator = File.separator;
    private static final String rootPath = System.getProperty("java.io.tmpdir") + separator + "img";

    public static void copyFileToHttp(HttpServletResponse response, File file) throws IOException {
        if (!file.exists()) {
            return;
        }

        String mimeType = URLConnection.guessContentTypeFromName(file.getName());
        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }
        response.setContentType(mimeType);
        response.setHeader("Content-Disposition", String.format("inline; filename=\"%s\"", file.getName()));
        response.setContentLength((int) file.length());

        try (InputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
            FileCopyUtils.copy(inputStream, response.getOutputStream());
        }
    }

    public static Pair<String, String> uuidToPath(String fileName, boolean isWrite) {
        String[] dirs = { rootPath, "permanent" };
        String[] fileNameParts = fileName.split("-");
        String[] pathParts = new String[dirs.length + fileNameParts.length + 1];
        System.arraycopy(dirs, 0, pathParts, 0, dirs.length);
        System.arraycopy(fileNameParts, 0, pathParts, dirs.length, fileNameParts.length);
        pathParts[pathParts.length - 1] = "";

        StringBuilder fullDirBuilder = new StringBuilder();
        String separator = File.separator;
        for (String pathPart : pathParts) {
            fullDirBuilder.append(separator);
            fullDirBuilder.append(pathPart);

            File dir = new File(fullDirBuilder.toString());
            if (isWrite && !dir.exists()) {
                dir.mkdirs();
            }
        }
        String fullDir = fullDirBuilder.toString();

        return new Pair<>(separator, fullDir);
    }

    public static Pair<String, String> tempToPath(String fileName, boolean isWrite) {
        String[] dirs = { rootPath, "uploaded" };
        String[] pathParts = new String[dirs.length + 1];
        System.arraycopy(dirs, 0, pathParts, 0, dirs.length);
        pathParts[pathParts.length - 1] = "";

        StringBuilder fullDirBuilder = new StringBuilder();
        String separator = File.separator;
        for (String pathPart : pathParts) {
            fullDirBuilder.append(separator);
            fullDirBuilder.append(pathPart);

            File dir = new File(fullDirBuilder.toString());
            if (isWrite && !dir.exists()) {
                dir.mkdirs();
            }
        }
        String fullDir = fullDirBuilder.toString();

        return new Pair<>(separator, fullDir);
    }

    public static void save(InputStream inputStream, String fileName) throws IOException {
        try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(fileName))) {
            byte[] buffer = new byte[inputStream.available()];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
    }

    public static void delete(String fileName, String sub) {
        Pair<String, String> sep1TmpDir = FileUtil.tempToPath(fileName, false);
        String sep1 = sep1TmpDir.getKey();
        String tmpDir = sep1TmpDir.getValue();
        File tmpFile = new File(tmpDir + sep1 + "_" + fileName + sub);

        if (tmpFile.exists()) {
            Pair<String, String> sep2FullDir = FileUtil.uuidToPath(fileName, true);
            String sep2 = sep2FullDir.getKey();
            String fullDir = sep2FullDir.getValue();
            File sourceFile = tmpFile;
            try {
                Files.copy(sourceFile.toPath(), new File(fullDir + sep2 + "_" + fileName + sub).toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                System.out.println("File not foud to delete!");
            }

            sourceFile.delete();
        }
    }
}