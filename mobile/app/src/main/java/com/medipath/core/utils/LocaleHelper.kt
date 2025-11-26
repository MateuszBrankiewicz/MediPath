package com.medipath.core.utils

import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import java.util.Locale
import androidx.core.content.edit

object LocaleHelper {
    private const val SELECTED_LANGUAGE = "selected_language"
    private const val PREFS_NAME = "locale_prefs"

    fun setLocale(context: Context, languageCode: String): Context {
        saveLanguagePreference(context, languageCode)
        return updateResources(context, languageCode)
    }

    fun loadLocale(context: Context): Context {
        val languageCode = getSavedLanguage(context)
        return if (languageCode != null) {
            updateResources(context, languageCode)
        } else {
            context
        }
    }

    fun getSavedLanguage(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(SELECTED_LANGUAGE, null)
    }

    fun getLocale(context: Context): Locale {
        val languageCode = getSavedLanguage(context)
        return when (languageCode?.uppercase()) {
            "EN" -> Locale.ENGLISH
            "PL" -> Locale("pl", "PL")
            else -> Locale.getDefault()
        }
    }

    private fun saveLanguagePreference(context: Context, languageCode: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putString(SELECTED_LANGUAGE, languageCode) }
    }

    private fun updateResources(context: Context, languageCode: String): Context {
        val locale = when (languageCode.uppercase()) {
            "EN" -> Locale.ENGLISH
            "PL" -> Locale("pl", "PL")
            else -> Locale.getDefault()
        }

        Locale.setDefault(locale)

        val configuration = Configuration(context.resources.configuration)

        configuration.setLocale(locale)
        val localeList = LocaleList(locale)
        LocaleList.setDefault(localeList)
        configuration.setLocales(localeList)

        context.resources.updateConfiguration(configuration, context.resources.displayMetrics)

        return context.createConfigurationContext(configuration)
    }
}
