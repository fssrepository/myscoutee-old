package com.raxim.myscoutee.profile.service

import com.fasterxml.jackson.databind.ObjectMapper
import java.io.IOException

class JsonConverterService(private val objectMapper: ObjectMapper) {
    fun <T> jsonToObject(jsonString: String?, targetClass: Class<T>?): T? {
        return try {
            objectMapper.readValue<T>(jsonString, targetClass)
        } catch (e: IOException) {
            // Handle the exception appropriately
            e.printStackTrace()
            null
        }
    }
}