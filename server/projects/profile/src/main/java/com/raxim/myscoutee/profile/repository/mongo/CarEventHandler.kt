package com.raxim.myscoutee.profile.repository.mongo

//import com.raxim.myscoutee.profile.data.document.QProfile
import com.raxim.myscoutee.common.FileUtil
import com.raxim.myscoutee.profile.data.document.mongo.Car
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.data.rest.core.annotation.*
import org.springframework.stereotype.Component
import java.io.File

@Component
@RepositoryEventHandler(Car::class)
class CarEventHandler() {
    val logger: Log = LogFactory.getLog(javaClass)

    @HandleBeforeSave
    @HandleBeforeCreate
    fun handleBeforeCreate(car: Car) {
        //val qProfile = QProfile("profile")
        logger.info("Handle Car Before Create....");
    }

    @HandleAfterSave
    @HandleAfterCreate
    fun handleAfterCreate(car: Car) {
        car.images?.forEach { img ->
            run {
                val fileName = img.name!!
                delete(fileName)
                delete(fileName, "_orig")
            }
        }

        logger.info("Handle Car After Create ....");
    }

    @HandleBeforeDelete
    fun handleBeforeDelete(car: Car) {
        logger.info("Handle Car Before Delete ....");
    }

    @HandleAfterDelete
    fun handleAfterDelete(car: Car) {
        logger.info("Handle Car After Delete ....");
    }
}