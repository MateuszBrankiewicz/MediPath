package com.medipath.utils

import com.medipath.core.network.DataStoreSessionManager
import kotlinx.coroutines.runBlocking
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import java.util.concurrent.ConcurrentHashMap

class MyCookieJar(private val sessionManager: DataStoreSessionManager, private val backendHost: String) : CookieJar {

    private val cookieStore = ConcurrentHashMap<HttpUrl, List<Cookie>>()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val sessionCookie = cookies.find { it.name == "SESSION" }
        if (sessionCookie != null) {
            runBlocking {
                sessionManager.saveSessionId(sessionCookie.value)
            }
            cookieStore[url] = listOf(sessionCookie)
        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        if (url.host != backendHost) {
            return emptyList()
        }
        val sessionId = runBlocking { sessionManager.getSessionId() }
        return if (sessionId != null) {
            Cookie.Builder()
                .name("SESSION")
                .value(sessionId)
                .domain(backendHost)
                .path("/")
                .httpOnly()
                .build()
                .let { listOf(it) }
        } else {
            emptyList()
        }
    }
}