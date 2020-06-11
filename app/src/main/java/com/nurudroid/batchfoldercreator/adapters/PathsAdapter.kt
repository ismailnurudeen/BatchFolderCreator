package com.nurudroid.batchfoldercreator.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nurudroid.batchfoldercreator.CreateDirectoryActivity
import com.nurudroid.batchfoldercreator.R
import kotlinx.android.synthetic.main.item_directory.view.*

// Created by ismailnurudeen on 01-Jun-20.
// Copyright (c) 2020 Nurudroid. All rights reserved.

class PathsAdapter(private val context: Context, private val paths: ArrayList<String>) :
    RecyclerView.Adapter<PathsAdapter.PathsHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PathsHolder =
        PathsHolder(LayoutInflater.from(context).inflate(R.layout.item_directory, parent, false))

    override fun getItemCount(): Int = paths.size

    override fun onBindViewHolder(holder: PathsHolder, position: Int) {
        holder.bind(paths[position], position)
    }

    inner class PathsHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var isEditingPath = false

        fun bind(path: String, pos: Int = adapterPosition) {
            itemView.item_path_tv.text = path
            itemView.item_path_edt.setText(path)

            itemView.edit_save_path_btn.setOnClickListener {
                if (isEditingPath) {
                    paths[pos] = itemView.item_path_edt.text.toString()
                    itemView.item_path_tv.visibility = View.VISIBLE
                    itemView.item_path_edt.visibility = View.GONE
                    itemView.edit_save_path_btn.setImageResource(R.drawable.ic_mode_edit_black_24dp)
                    notifyItemChanged(pos)
                    CreateDirectoryActivity.isPathsSaved = false
                } else {
                    itemView.item_path_tv.visibility = View.GONE
                    itemView.item_path_edt.visibility = View.VISIBLE
                    itemView.edit_save_path_btn.setImageResource(R.drawable.ic_done_black_24dp)
                }
                isEditingPath = !isEditingPath
            }
            itemView.remove_path_btn.setOnClickListener {
                if (isEditingPath) {
                    itemView.item_path_tv.text = path
                    itemView.item_path_tv.visibility = View.VISIBLE
                    itemView.item_path_edt.visibility = View.GONE
                    itemView.edit_save_path_btn.setImageResource(R.drawable.ic_mode_edit_black_24dp)
                } else {
                    notifyDataSetChanged()
                    paths.removeAt(pos)
                    CreateDirectoryActivity.isPathsSaved = false
                }
                isEditingPath = !isEditingPath
            }
        }
    }
}