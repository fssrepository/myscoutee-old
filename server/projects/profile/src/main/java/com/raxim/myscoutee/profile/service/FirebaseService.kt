package com.raxim.myscoutee.profile.service

import com.raxim.myscoutee.common.config.FirebaseProperties
import com.raxim.myscoutee.profile.message.Notification
//import com.raxim.myscoutee.profile.message.Notification
import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ByteArrayEntity
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient
import org.apache.http.impl.nio.client.HttpAsyncClients
import org.apache.http.message.BasicHeader
import java.util.*
import java.util.concurrent.Future

class FirebaseService(private val firebaseProperties: FirebaseProperties) {

    @Throws(Exception::class)
    fun sendAsync(notification: Notification): Future<HttpResponse?>? {
        val httpPost = HttpPost(firebaseProperties.url)
        val headers: MutableMap<String, String> = HashMap()

        headers["Authorization"] = "key=${firebaseProperties.apiKey}"

        headers["Content-Type"] = "application/json"
        httpPost.entity = ByteArrayEntity(null)

        for ((key, value) in headers) {
            httpPost.addHeader(BasicHeader(key, value))
        }
        val closeableHttpAsyncClient: CloseableHttpAsyncClient = HttpAsyncClients.createDefault()
        closeableHttpAsyncClient.start()
        return closeableHttpAsyncClient.execute(httpPost, ClosableCallback(closeableHttpAsyncClient))
    }
}