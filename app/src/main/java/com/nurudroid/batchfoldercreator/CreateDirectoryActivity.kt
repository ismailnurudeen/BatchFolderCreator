package com.nurudroid.batchfoldercreator

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.nurudroid.batchfoldercreator.adapters.PathsAdapter
import com.nurudroid.batchfoldercreator.utils.AppUtils
import com.nurudroid.batchfoldercreator.utils.Constants
import com.nurudroid.batchfoldercreator.utils.CsvParser1
import com.nurudroid.batchfoldercreator.utils.PathUtil.getPath
import com.nurudroid.batchfoldercreator.utils.PrefsManager
import com.nurudroid.batchfoldercreator.utils.events.Events
import kotlinx.android.synthetic.main.activity_create_directory.*
import kotlinx.android.synthetic.main.dialog_add_path.view.*
import kotlinx.android.synthetic.main.dialog_complete.view.*
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.net.URISyntaxException


class CreateDirectoryActivity : AppCompatActivity() {

    private val WRITE_EXTERNAL_REQUEST_CODE = 101
    private var pathsData: ArrayList<String> = ArrayList()
    private lateinit var adapter: PathsAdapter
    private lateinit var appUtils: AppUtils
    private lateinit var prefsManager: PrefsManager
    private var ROOT_FOLDER_REQUEST_CODE = 103
    private var SELECT_PATH_REQUEST_CODE = 104
    private var dialogPathInput: EditText? = null
    private var primaryRootDir: String = ""
    private var internalStoragePath = ""

    companion object {
        var isPathsSaved = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_directory)
        setSupportActionBar(cdaToolbar)

        appUtils = AppUtils(this)
        prefsManager = PrefsManager(this)
        if (prefsManager.isCdaFirstTimeLaunch) {
            appUtils.showCdaTapTarget(this)
            prefsManager.isCdaFirstTimeLaunch = false
        }

        internalStoragePath = Environment.getExternalStorageDirectory().absolutePath
        primaryRootDir = "$internalStoragePath/"
        Log.i("BFC", primaryRootDir)
        root_dir_tv.text = primaryRootDir

        val csvPath = intent.getStringExtra(Constants.CSV_PATH)
        if (!csvPath.isNullOrEmpty()) pathsData.addAll(CsvParser1.readCsv(csvPath))

        val savedPathIndex = intent.getIntExtra(Constants.SAVED_PATH_INDEX, -1)
        if (savedPathIndex != -1) {
            val savedPaths = prefsManager.dirPaths?.get(savedPathIndex) ?: return
            val paths = prefsManager.dirPaths?.get(savedPathIndex)?.path
            if (!paths.isNullOrEmpty()) {
                pathsData.addAll(paths)
                root_dir_tv.text =
                    if (savedPaths.rootDirectory.isNotEmpty()) savedPaths.rootDirectory else primaryRootDir
            }
            isPathsSaved = true
        }

        if (pathsData.isNotEmpty()) {
            paths_rv.visibility = View.VISIBLE
            cda_instruction_tv.visibility = View.GONE
        } else {
            paths_rv.visibility = View.GONE
            cda_instruction_tv.visibility = View.VISIBLE
        }
        adapter = PathsAdapter(this, pathsData)
        paths_rv.adapter = adapter
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                Log.i("BFC CDA", "Data changed: ${pathsData.size}")
                if (pathsData.isNotEmpty()) {
                    paths_rv.visibility = View.VISIBLE
                    cda_instruction_tv.visibility = View.GONE
                } else {
                    paths_rv.visibility = View.GONE
                    cda_instruction_tv.visibility = View.VISIBLE
                }
                super.onChanged()
            }

            override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
                Log.i(
                    "BFC CDA",
                    "Range Changed: Start:$positionStart Count:$itemCount Size:${pathsData.size}"
                )
                super.onItemRangeChanged(positionStart, itemCount)
            }
        })
        root_dir_tv.setOnClickListener {
            Toast.makeText(this, "${root_dir_tv.text}", Toast.LENGTH_LONG).show()
        }
        createDirsBtn.setOnClickListener {
            if (checkPermission(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    WRITE_EXTERNAL_REQUEST_CODE
                )
            ) {
                if (pathsData.isNotEmpty()) {
                    AlertDialog.Builder(this).setTitle("Create Directories")
                        .setMessage(
                            "You are about to create ${pathsData.size} folders inside '$primaryRootDir'" +
                                    "\n\nDo you want to continue?"
                        )
                        .setPositiveButton("Yes") { _, _ ->
                            createDirectories(pathsData)
                        }.setNegativeButton("No") { dialog, _ ->
                            dialog.dismiss()
                        }.show()
                }
            }
        }

        addPathBtn.setOnClickListener {
            showAddPathDialog()
        }
        change_root_dir_btn.setOnClickListener {
            if (checkPermission(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    WRITE_EXTERNAL_REQUEST_CODE
                )
            ) {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                intent.addCategory(Intent.CATEGORY_DEFAULT)
                startActivityForResult(
                    intent,
                    ROOT_FOLDER_REQUEST_CODE
                )
            }
        }
        directories_undo_btn.setOnClickListener {
            if (checkPermission(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    WRITE_EXTERNAL_REQUEST_CODE
                )
            ) {
                if (pathsData.isNotEmpty()) {
                    AlertDialog.Builder(this).setTitle("Reverse Folder Creation")
                        .setMessage(
                            "You are about to delete all the folders whose names are on this list.\n\n" +
                                    "Note: Only the inner folders in the directory will be deleted\n\n" +
                                    "Warning: You might lose important files inside this directories."

                        )
                        .setPositiveButton("Proceed") { _, _ ->
                            undoDirectories(pathsData)
                        }.setNegativeButton("Cancel") { dialog, _ ->
                            dialog.dismiss()
                        }.show()
                }
            }
        }
    }

    private fun createDirectories(paths: ArrayList<String>) {
        var successCount = 0
        var failureCount = 0
        for (path in paths) {
            val file = File(if (path.startsWith(primaryRootDir)) path else primaryRootDir + path)
            if (file.mkdirs()) successCount++
            else failureCount++
        }
        showCompletionDialog(successCount, failureCount)
        Toast.makeText(
            this,
            "Folders created",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun undoDirectories(paths: ArrayList<String>) {
        var successCount = 0
        var failureCount = 0

        for (path in paths) {
            val file = File(if (path.startsWith(primaryRootDir)) path else primaryRootDir + path)
            if (file.deleteRecursively()) successCount++
            else failureCount++
        }
        showCompletionDialog(successCount, failureCount, "Folders Creation Reversed!")
        Toast.makeText(
            this,
            "Undone",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.cda_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_save_paths -> {
                if (pathsData.isNotEmpty()) {
                    if (isPathsSaved) {
                        Toast.makeText(this, "Already Saved", Toast.LENGTH_SHORT).show()

                    } else {
                        appUtils.addSavedPaths(pathsData, primaryRootDir)
                        Toast.makeText(this, "Paths Saved", Toast.LENGTH_SHORT).show()
                        EventBus.getDefault().postSticky(Events())
                        isPathsSaved = true
                    }
                }
            }
            R.id.menu_export_paths -> {
                if (pathsData.isNotEmpty()) {
                    appUtils.exportPaths(pathsData)
                }
            }
            R.id.menu_cda_help -> {
                appUtils.showCdaTapTarget(this)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showAddPathDialog() {
        val builder = AlertDialog.Builder(this)
        val dialogLayout = LayoutInflater.from(this).inflate(R.layout.dialog_add_path, null)
        builder.setView(dialogLayout)
        val dialog = builder.create()
        dialogLayout.dialog_root_dir_tv.text = primaryRootDir
        dialogLayout.dialog_cancel.setOnClickListener {
            dialog.cancel()
        }
        dialogPathInput = dialogLayout.dialog_path_input
        dialogLayout.dialog_add_path.setOnClickListener {
            val pathTxt = dialogLayout.dialog_path_input.text.toString()
            if (pathTxt.isNotEmpty()) {
                pathsData.add(pathTxt)
                adapter.notifyDataSetChanged()
                isPathsSaved = false
                dialog.cancel()
            }
        }
        dialogLayout.dialog_select_path_btn.setOnClickListener {
            if (checkPermission(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    WRITE_EXTERNAL_REQUEST_CODE
                )
            ) {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                intent.addCategory(Intent.CATEGORY_DEFAULT)
                startActivityForResult(
                    intent,
                    SELECT_PATH_REQUEST_CODE
                )
            }

        }
        dialog.show()
    }

    private fun showCompletionDialog(
        success: Int,
        failure: Int,
        title: String = "Folders Creation Complete!"
    ) {
        val builder = AlertDialog.Builder(this)
        val dialogLayout = LayoutInflater.from(this).inflate(R.layout.dialog_complete, null)
        builder.setView(dialogLayout)
        val dialog = builder.create()
        dialogLayout.completion_title.text = title
        dialogLayout.complete_dialog_root_dir.text = primaryRootDir
        dialogLayout.successCountTv.text = "$success Successful"
        dialogLayout.failureCountTv.text = "$failure Failed"

        dialogLayout.close_complete_dialog.setOnClickListener {
            dialog.cancel()
        }
        dialog.show()
    }

    private fun checkPermission(permission: String, requestCode: Int): Boolean {
        return if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                Log.v(Constants.PERMISSION_TAG, "Permission is granted")
                true
            } else {
                Log.v(Constants.PERMISSION_TAG, "Permission is revoked")
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(permission),
                    requestCode
                )
                false
            }
        } else {
            //permission is automatically granted on sdk<23 upon installation
            Log.v(Constants.PERMISSION_TAG, "Permission is granted")
            true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.v(
                Constants.PERMISSION_TAG,
                "Permission: " + permissions[0] + "was " + grantResults[0]
            )
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(
            requestCode,
            resultCode, data
        )
        when (requestCode) {
            ROOT_FOLDER_REQUEST_CODE -> if (resultCode == Activity.RESULT_OK) {

                try {
                    val uri = data?.data
                    val docUri: Uri = DocumentsContract.buildDocumentUriUsingTree(
                        uri,
                        DocumentsContract.getTreeDocumentId(uri)
                    )
                    val selectedPath: String = getPath(this, docUri)
                    root_dir_tv.text = "$selectedPath/"
                    primaryRootDir = "$selectedPath/"
                    Toast.makeText(this, primaryRootDir, Toast.LENGTH_LONG).show()

                } catch (e: URISyntaxException) {
                    e.printStackTrace()
                }
            }
            SELECT_PATH_REQUEST_CODE -> if (resultCode == Activity.RESULT_OK) {

                try {
                    val uri = data?.data
                    val docUri: Uri = DocumentsContract.buildDocumentUriUsingTree(
                        uri,
                        DocumentsContract.getTreeDocumentId(uri)
                    )
                    val selectedPath: String = getPath(this, docUri) + "/"
                    if (dialogPathInput != null) dialogPathInput?.setText(selectedPath)
                    Toast.makeText(this, primaryRootDir, Toast.LENGTH_SHORT).show()

                } catch (e: URISyntaxException) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onBackPressed() {
        if (!pathsData.isNullOrEmpty() && !isPathsSaved) {
            AlertDialog.Builder(this).setTitle("Exit Without Saving")
                .setMessage("Do you want to exit without saving paths?")
                .setPositiveButton("Exit") { _, _ ->
                    super.onBackPressed()
                }.setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }.show()

        } else {
            super.onBackPressed()
        }
    }
}
