package com.medipath.core.utils

import com.medipath.core.network.SharedPreferencesSessionManager
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

class SessionCookieJar(
    private val sessionManager: SharedPreferencesSessionManager
) : CookieJar {

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val sessionCookie = cookies.find { it.name == "SESSION" }
        if (sessionCookie != null) {
            sessionManager.saveSessionId(sessionCookie.value)
        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val sessionId = sessionManager.getSessionId()

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