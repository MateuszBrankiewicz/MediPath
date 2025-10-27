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
        val sessionId = runBlocking { sessionManager.getSessionId() }
        return if (sessionId != null) {
            val cookie = Cookie.Builder()
                .name("SESSION")
                .value(sessionId)
                .domain(url.host)
                .path("/")
                .httpOnly()
                .build()
            listOf(cookie)
        } else {
            emptyList()
        }
    }
}