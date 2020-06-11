package com.nurudroid.batchfoldercreator.utils

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.nurudroid.batchfoldercreator.R
import com.nurudroid.batchfoldercreator.models.SavedPath
import kotlinx.android.synthetic.main.activity_create_directory.*
import kotlinx.android.synthetic.main.activity_main.*
import org.apache.commons.io.FileUtils
import java.io.File
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

// Created by Ismail Nurudeen on 04-Jun-20.
// Copyright (c) 2020 Nurudroid. All rights reserved.

class AppUtils(private val context: Context) {
    private val prefsManager = PrefsManager(context)
    private val externalStorage = Environment.getExternalStorageDirectory()
    private val appDirectoryPath = "$externalStorage/Batch Folder Creator"
    private val export_path = "${appDirectoryPath}/exported/dirPaths_${formatDate(
        Date(),
        "dd_MM_yyyy_hh_mm"
    )}.csv"

    private fun formatDate(date: Date?, format: String = "dd/MM/yyyy hh:MM a"): String {
        return if (date != null) SimpleDateFormat(format, Locale.getDefault()).format(date)
        else ""
    }

    fun exportPaths(paths: ArrayList<String>) {
        var pathsCsv = ""
        for (pathIndex in 0 until paths.size) {

            pathsCsv += if (pathIndex < paths.size - 1) "${paths[pathIndex]}\n"
            else paths[pathIndex]
        }
        val path = File(export_path)
        FileUtils.write(path, pathsCsv, Charset.defaultCharset())
        Toast.makeText(context, "Successfully Exported to $export_path", Toast.LENGTH_LONG).show()
    }

    fun addSavedPaths(paths: ArrayList<String>,rootDir:String) {
        val savedPaths = prefsManager.dirPaths ?: ArrayList()
        savedPaths.add(
            SavedPath(
                paths, rootDir,formatDate(
                    Date(), "dd/MM/yyyy hh:mm a"
                )
            )
        )
        prefsManager.dirPaths = savedPaths
    }

    fun deletePaths(pos: Int) {
        val savedPaths = prefsManager.dirPaths ?: ArrayList()
        savedPaths.removeAt(pos)
        prefsManager.dirPaths = savedPaths
        Toast.makeText(context, "Paths Deleted!", Toast.LENGTH_SHORT).show()
    }

    private fun getRvFirstItem(rv: RecyclerView): View? {
        var view: View? = null
        val lm = rv.layoutManager
        if (lm is LinearLayoutManager) {
            val index = lm.findFirstCompletelyVisibleItemPosition()
            view = lm.findViewByPosition(index)
        } else if (lm is GridLayoutManager) {
            val index = lm.findFirstCompletelyVisibleItemPosition()
            view = lm.findViewByPosition(index)
        }
        return view
    }

    fun showEmptyHomeTapTarget(activity: Activity) {
        val helpSequence = TapTargetSequence(activity)
        helpSequence.target(
            TapTarget.forView(
                activity.create_paths_btn, "Create New Paths",
                "Click here to start creating new directory paths"
            ).descriptionTextColorInt(Color.WHITE)
                .targetRadius(80)
                .id(1)
        )
        helpSequence.target(
            TapTarget.forView(
                activity.import_csv_btn, "Import Paths",
                "Click here to import predefined paths from a single column .csv file"
            ).tintTarget(false)
                .descriptionTextColorInt(Color.WHITE)
                .targetRadius(80)
                .id(10)
        )
        helpSequence.target(
            TapTarget.forView(
                activity.create_paths_btn_1, "Create New Paths 2",
                "This is also for creating new directory paths"
            ).tintTarget(false)
                .descriptionTextColorInt(Color.WHITE)
                .id(100)
        ).listener(object : TapTargetSequence.Listener {
            override fun onSequenceCanceled(lastTarget: TapTarget?) {}
            override fun onSequenceFinish() {
                Toast.makeText(context, "I hope that was helpful :-)", Toast.LENGTH_SHORT).show()
            }

            override fun onSequenceStep(lastTarget: TapTarget?, targetClicked: Boolean) {}
        }).continueOnCancel(true).start()
    }

    fun showHomeTapTarget(activity: Activity) {
        val helpSequence = TapTargetSequence(activity)
        helpSequence.target(
            TapTarget.forView(
                activity.create_paths_btn_1, "Create New Paths",
                "Click here to start creating new directory paths"
            ).descriptionTextColorInt(Color.WHITE)
                .id(1)
        )
        helpSequence.target(
            TapTarget.forView(
                activity.importCsvFab, "Import Paths",
                "Click here to import predefined paths from a single column .csv file"
            ).tintTarget(false)
                .descriptionTextColorInt(Color.WHITE)
                .id(10)
        ).listener(object : TapTargetSequence.Listener {
            override fun onSequenceCanceled(lastTarget: TapTarget?) {}
            override fun onSequenceFinish() {
                Toast.makeText(context, "I hope that was helpful :-)", Toast.LENGTH_SHORT).show()
            }

            override fun onSequenceStep(lastTarget: TapTarget?, targetClicked: Boolean) {}
        }).continueOnCancel(true).start()
    }

    fun showCdaTapTarget(activity: Activity) {
        val toolbar = activity.findViewById<Toolbar>(R.id.cdaToolbar)
        val helpSequence = TapTargetSequence(activity)
        helpSequence.target(
            TapTarget.forView(
                activity.addPathBtn, "Add Path",
                "Click here to add a new directory path"
            ).transparentTarget(true)
                .descriptionTextColorInt(Color.WHITE)
                .id(1)
        )
        helpSequence.target(
            TapTarget.forView(
                activity.change_root_dir_btn, "Change Root Directory",
                "Click here to change root directory (This is where the folder creation will start from)"
            ).tintTarget(true)
                .descriptionTextColorInt(Color.WHITE)
                .id(2)
        )
        try {
            helpSequence.target(
                TapTarget.forToolbarMenuItem(
                    toolbar, R.id.menu_save_paths, "Save Paths",
                    "Click here to save all the directory paths you've created"
                ).tintTarget(true)
                    .descriptionTextColorInt(Color.WHITE)
                    .id(3)
            )

            helpSequence.target(
                TapTarget.forToolbarMenuItem(
                    toolbar, R.id.menu_export_paths, "Export Paths",
                    "Click here to export all created paths as .csv file that you can import on any device."
                ).tintTarget(true)
                    .descriptionTextColorInt(Color.WHITE)
                    .id(4)
            )
        } catch (e: Exception) {
            //Do nothing...
        }
        helpSequence.start()
    }
}