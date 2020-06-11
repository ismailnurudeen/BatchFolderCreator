package com.nurudroid.batchfoldercreator.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nurudroid.batchfoldercreator.models.SavedPath

@SuppressLint("CommitPrefEdits")
class PrefsManager(private val context: Context) {
    var pref: SharedPreferences
    var defaultPref: SharedPreferences
    var defaultPrefEditor: SharedPreferences.Editor
    var editor: SharedPreferences.Editor
    private var gson: Gson

    // shared pref mode
    private val PRIVATE_MODE = 0

    var isFirstTimeLaunch: Boolean
        get() = pref.getBoolean(IS_FIRST_TIME_LAUNCH, true)
        set(isFirstTime) {
            editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime)
            editor.commit()
        }
    var isCdaFirstTimeLaunch: Boolean
        get() = pref.getBoolean(IS_CDA_FIRST_TIME_LAUNCH, true)
        set(isFirstTime) {
            editor.putBoolean(IS_CDA_FIRST_TIME_LAUNCH, isFirstTime)
            editor.commit()
        }
    var isDarkMode: Boolean
        get() = pref.getBoolean(IS_DARK_MODE, false)
        set(isDarkMode) {
            editor.putBoolean(IS_DARK_MODE, isDarkMode)
            editor.commit()
        }
    var dirPaths: ArrayList<SavedPath>?
        get() {
            val type = object : TypeToken<ArrayList<SavedPath>>() {}.type
            val paths: ArrayList<SavedPath>? =
                gson.fromJson(pref.getString(PREF_SAVED_PATHS, ""), type)
            paths?.reverse()
            return paths
        }
        set(path) {
            val pathsString = gson.toJson(path)
            editor.putString(PREF_SAVED_PATHS, pathsString)
            editor.commit()
        }

    init {
        pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE)
        editor = pref.edit()
        gson = Gson()
        defaultPref = PreferenceManager.getDefaultSharedPreferences(context)
        defaultPrefEditor = defaultPref.edit()
    }

    companion object {
        // Shared preferences keys
        private const val PREF_NAME = "nurudroid.batch_folder_creator_pref"
        private const val IS_FIRST_TIME_LAUNCH = "nurudroid.pref_is_first_launch"
        private const val IS_CDA_FIRST_TIME_LAUNCH = "nurudroid.pref_is_cda_first_launch"
        private const val IS_DARK_MODE = "nurudroid.pref_is_dark_mode"
        private const val PREF_SAVED_PATHS = "nurudroid.pref_saved_paths"
    }
}