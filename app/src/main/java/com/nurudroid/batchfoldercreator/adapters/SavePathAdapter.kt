package com.nurudroid.batchfoldercreator.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nurudroid.batchfoldercreator.CreateDirectoryActivity
import com.nurudroid.batchfoldercreator.R
import com.nurudroid.batchfoldercreator.models.SavedPath
import com.nurudroid.batchfoldercreator.utils.AppUtils
import com.nurudroid.batchfoldercreator.utils.Constants
import kotlinx.android.synthetic.main.item_saved_path.view.*

/*
 ******************************************************
 * Created by Ismail Nurudeen on 04-Jun-20.           *
 * Copyright (c) 2020 Nurudroid. All rights reserved. *
 ******************************************************
 * */

class SavePathAdapter(val context: Context, val savedPaths: ArrayList<SavedPath>) :
    RecyclerView.Adapter<SavePathAdapter.SavePathViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavePathViewHolder =
        SavePathViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_saved_path, parent, false
            )
        )

    override fun getItemCount(): Int = savedPaths.size

    override fun onBindViewHolder(holder: SavePathViewHolder, position: Int) {
        holder.bind(savedPaths[position], position)
    }

    inner class SavePathViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val appUtils = AppUtils(context)
        fun bind(path: SavedPath, pos: Int) {
            itemView.saved_path_label.text = "${path.path[0]}..."
            itemView.saved_path_date.text = "Saved: ${path.savedDate}"
            itemView.setOnClickListener {
                val createDirIntent = Intent(context, CreateDirectoryActivity::class.java)
                createDirIntent.putExtra(Constants.SAVED_PATH_INDEX, pos)
                context.startActivity(createDirIntent)
            }
            itemView.setOnLongClickListener {
                showDeleteConfirmation(pos)
                true
            }
        }

        private fun showDeleteConfirmation(pos: Int) {
            AlertDialog.Builder(context).setTitle("Delete Path")
                .setMessage("Do you want to delete this path?")
                .setPositiveButton("Yes") { dialog, _ ->
                    appUtils.deletePaths(pos)
                    savedPaths.removeAt(pos)
                    notifyDataSetChanged()
                    dialog.dismiss()
                }.setNegativeButton("Cancel") { dialog, _ ->
                    dialog.cancel()
                }.show()
        }
    }
}