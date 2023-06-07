package com.raxim.myscoutee.profile.service

import org.apache.http.HttpResponse
import org.apache.http.concurrent.FutureCallback
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient
import java.io.IOException
import java.lang.Exception

/**
 * Java 7's try-with-resource closes the client before the future is completed.
 * This callback captures the client and closes it once the request is
 * completed.
 *
 * See also http://stackoverflow.com/a/35962718/368220.
 */
class ClosableCallback(private val closeableHttpAsyncClient: CloseableHttpAsyncClient) :
    FutureCallback<HttpResponse?> {
    override fun completed(httpResponse: HttpResponse?) {
        close()
    }

    override fun failed(e: Exception) {
        close()
    }

    override fun cancelled() {
        close()
    }

    private fun close() {
        try {
            closeableHttpAsyncClient.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
