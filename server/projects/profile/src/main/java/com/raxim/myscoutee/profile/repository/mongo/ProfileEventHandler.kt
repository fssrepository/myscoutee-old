package com.raxim.myscoutee.profile.repository.mongo

//import com.raxim.myscoutee.profile.data.document.QProfile
import com.raxim.myscoutee.common.FileUtil
import com.raxim.myscoutee.profile.data.document.mongo.Profile
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.data.rest.core.annotation.*
import org.springframework.stereotype.Component
import java.io.File

// save by user uuid folder - location included

fun delete(fileName: String, sub: String? = "") {
    val (sep1, tmpDir) = FileUtil.tempToPath(fileName)
    val tmpFile = File("$tmpDir${sep1}_$fileName$sub")

    if (tmpFile.exists()) {
        val (sep2, fullDir) = FileUtil.uuidToPath(fileName, true)
        tmpFile.let { sourceFile ->
            sourceFile.copyTo(File("$fullDir${sep2}_$fileName$sub"), true)
            sourceFile.delete()
        }
    }
}

@Component
@RepositoryEventHandler(Profile::class)
class ProfileEventHandler {
    val logger: Log = LogFactory.getLog(javaClass)

    @HandleBeforeSave
    @HandleBeforeCreate
    fun handleBeforeCreate(profile: Profile) {
        //val qProfile = QProfile("profile")
        logger.info("Handle Profile Before Create....");
    }

    @HandleAfterSave
    @HandleAfterCreate
    fun handleAfterCreate(profile: Profile) {
        profile.images?.forEach { img ->
            run {
                val fileName = img.name!!
                delete(fileName)
                delete(fileName, "_orig")
            }
        }

        logger.info("Handle Profile After Create ....");
    }

    @HandleBeforeDelete
    fun handleBeforeDelete(profile: Profile) {
        logger.info("Handle Profile Before Delete ....");
    }

    @HandleAfterDelete
    fun handleAfterDelete(profile: Profile) {
        logger.info("Handle Profile After Delete ....");
    }
}