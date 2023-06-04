package com.raxim.myscoutee.common

import org.springframework.util.FileCopyUtils
import java.io.*
import java.net.URLConnection
import javax.servlet.http.HttpServletResponse

fun InputStream.save(fileName: String) {
    val serverFile = File(fileName)
    val buffStream = BufferedOutputStream(FileOutputStream(serverFile))
    var bytesRead: Int
    val buffer = ByteArray(this.available())
    if ((this.read(buffer).also { bytesRead = it }) != -1) {
        buffStream.write(buffer, 0, bytesRead)
    }
    buffStream.close()
}

class FileUtil {
    companion object {
        private val separator = File.separator.toString()
        private val rootPath = "${System.getProperty("java.io.tmpdir")}${separator}img"

        @Throws(
            IOException::class
        )
        fun copyFileToHttp(response: HttpServletResponse, file: File) {
            if (!file.exists()) {
                return;
            }

            val mimeType = URLConnection.guessContentTypeFromName(file.name) ?: "application/octet-stream";
            response.contentType = mimeType;
            response.setHeader("Content-Disposition", String.format("inline; filename=\"" + file.name + "\""));
            response.setContentLength(file.length().toInt());

            val inputStream = BufferedInputStream(FileInputStream(file));

            FileCopyUtils.copy(inputStream, response.outputStream);
        }

        fun uuidToPath(fileName: String, isWrite: Boolean = false): Pair<String, String> {

            val dirs = mutableListOf(rootPath, "permanent") + fileName.split("-") + mutableListOf("");
            val fullDir = dirs.reduce { acc, curr ->
                val dir = File(acc)
                if (isWrite && !dir.exists()) {
                    dir.mkdirs()
                }
                acc + separator + curr
            };
            return Pair(separator, fullDir)
        }

        fun tempToPath(fileName: String, isWrite: Boolean = false): Pair<String, String> {
            val dirs = mutableListOf(rootPath, "uploaded")/* + fileName.split("-")*/ + mutableListOf("");
            val fullDir = dirs.reduce { acc, curr ->
                val dir = File(acc)
                if (isWrite && !dir.exists()) {
                    dir.mkdirs()
                }
                acc + separator + curr
            };
            return Pair(separator, fullDir)
        }
    }

}