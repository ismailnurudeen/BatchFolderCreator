package com.nurudroid.batchfoldercreator

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.nurudroid.batchfoldercreator.adapters.SavePathAdapter
import com.nurudroid.batchfoldercreator.models.SavedPath
import com.nurudroid.batchfoldercreator.utils.AppUtils
import com.nurudroid.batchfoldercreator.utils.Constants
import com.nurudroid.batchfoldercreator.utils.PathUtil
import com.nurudroid.batchfoldercreator.utils.PrefsManager
import com.nurudroid.batchfoldercreator.utils.events.Events
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.net.URISyntaxException
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {
    private val CSV_UPLOAD_REQUEST_CODE = 100
    private lateinit var prefsManager: PrefsManager
    private lateinit var adapter: SavePathAdapter
    private lateinit var appUtils: AppUtils
    private lateinit var savedPaths: ArrayList<SavedPath>

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(mainToolbar)
        EventBus.getDefault().register(this)
        prefsManager = PrefsManager(this)
        appUtils = AppUtils(this)
        savedPaths = prefsManager.dirPaths ?: ArrayList()
        Log.i("BFC", savedPaths.toString())
        if (prefsManager.isFirstTimeLaunch) {
            appUtils.showEmptyHomeTapTarget(this)
            prefsManager.isFirstTimeLaunch = false
        }
        // Enable Dark or Light Mode
        AppCompatDelegate.setDefaultNightMode(if (prefsManager.isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)

        if (savedPaths.isNotEmpty()) {
            adapter = SavePathAdapter(this, savedPaths)
            recent_paths_rv.adapter = adapter
            recent_paths_rv.addItemDecoration(
                DividerItemDecoration(
                    this,
                    DividerItemDecoration.VERTICAL
                )
            )

            no_prev_paths_layout.visibility = View.GONE
            recent_paths_rv.visibility = View.VISIBLE
            importCsvFab.visibility = View.VISIBLE

            adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                override fun onChanged() {
                    Log.i("BFC MAIN", "Data changed")
                    if (savedPaths.isEmpty()) {
                        no_prev_paths_layout.visibility = View.VISIBLE
                        recent_paths_rv.visibility = View.GONE
                        importCsvFab.visibility = View.GONE
                    } else {
                        no_prev_paths_layout.visibility = View.GONE
                        recent_paths_rv.visibility = View.VISIBLE
                        importCsvFab.visibility = View.VISIBLE
                    }
                    super.onChanged()
                }
            })
        } else {
            no_prev_paths_layout.visibility = View.VISIBLE
            recent_paths_rv.visibility = View.GONE
            importCsvFab.visibility = View.GONE
        }
        import_csv_btn.setOnClickListener {
            if (checkPermission(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    CSV_UPLOAD_REQUEST_CODE
                )
            ) {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "*/*"
                startActivityForResult(intent, CSV_UPLOAD_REQUEST_CODE)
            }
        }
        importCsvFab.setOnClickListener {
            if (checkPermission(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    CSV_UPLOAD_REQUEST_CODE
                )
            ) {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "*/*"
                startActivityForResult(intent, CSV_UPLOAD_REQUEST_CODE)
            }
        }
        create_paths_btn.setOnClickListener {
            startActivity(Intent(this, CreateDirectoryActivity::class.java))
        }
        create_paths_btn_1.setOnClickListener {
            startActivity(Intent(this, CreateDirectoryActivity::class.java))
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onSaveEvent(event: Events) {
        savedPaths.clear()
        savedPaths.addAll(prefsManager.dirPaths ?: ArrayList())
        adapter.notifyDataSetChanged()
        if (savedPaths.isNotEmpty()) {
            no_prev_paths_layout.visibility = View.GONE
            recent_paths_rv.visibility = View.VISIBLE
            importCsvFab.visibility = View.VISIBLE
        }

        Log.i("BFC EVENT", "triggered")
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        val item = menu?.findItem(R.id.menu_switch_display)
        item?.icon =
            getDrawable(if (prefsManager.isDarkMode) R.drawable.ic_brightness_5_black_24dp else R.drawable.ic_brightness_4_black_24dp)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_switch_display -> {
                prefsManager.isDarkMode = !prefsManager.isDarkMode
                // Enable Dark or Light Mode
                AppCompatDelegate.setDefaultNightMode(if (prefsManager.isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)
                item.icon =
                    getDrawable(if (prefsManager.isDarkMode) R.drawable.ic_brightness_5_black_24dp else R.drawable.ic_brightness_4_black_24dp)
            }
            R.id.menu_about -> {
                startActivity(Intent(this, AboutActivity::class.java))
            }
            R.id.menu_help -> {
                if (savedPaths.isEmpty()) appUtils.showEmptyHomeTapTarget(this)
                else appUtils.showHomeTapTarget(this)
            }
        }
        return super.onOptionsItemSelected(item)
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
            if (requestCode == CSV_UPLOAD_REQUEST_CODE) {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "*/*"
                startActivityForResult(intent, CSV_UPLOAD_REQUEST_CODE)
            }
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
            CSV_UPLOAD_REQUEST_CODE -> if (resultCode == Activity.RESULT_OK) {
                val pathUri: Uri = data?.data!!
                if (pathUri.toString().toLowerCase(Locale.getDefault()).contains(".csv")) {
                    var csvPathHolder: String? = null
                    try {
                        csvPathHolder = PathUtil.getPath(this@MainActivity, data.data)
                    } catch (e: URISyntaxException) {
                        e.printStackTrace()
                    }
                    assert(csvPathHolder != null)
                    val csvFileName =
                        csvPathHolder!!.substring(csvPathHolder.lastIndexOf('/') + 1)
                    val createDirIntent = Intent(this, CreateDirectoryActivity::class.java)
                    createDirIntent.putExtra(Constants.CSV_PATH, csvPathHolder)
                    startActivity(createDirIntent)
                } else {
                    Toast.makeText(applicationContext, "File is not a csv", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}
