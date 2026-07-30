package com.example.data.network

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Logging interceptor to monitor API request latency, with specific tracking
 * for Gemini API and cloud sync endpoints.
 */
class LatencyLoggingInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url.toString()
        val isGeminiApi = url.contains("generativelanguage.googleapis.com") || url.contains("firebase.ai")

        val startNs = System.nanoTime()
        Log.d(TAG, "--> [Network Request Start] ${request.method} $url")

        return try {
            val response = chain.proceed(request)
            val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)

            if (isGeminiApi) {
                Log.i(TAG, "⚡ [Gemini API Latency] ${request.method} $url - Status Code: ${response.code} - Latency: ${tookMs}ms")
            } else {
                Log.d(TAG, "<-- [Network Request End] ${request.method} $url - Status: ${response.code} (${tookMs}ms)")
            }
            response
        } catch (e: Exception) {
            val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)
            Log.e(TAG, "❌ [Network Request Failed] ${request.method} $url after ${tookMs}ms - Error: ${e.message}")
            throw e
        }
    }

    companion object {
        private const val TAG = "LatencyLoggingInterceptor"
    }
}
