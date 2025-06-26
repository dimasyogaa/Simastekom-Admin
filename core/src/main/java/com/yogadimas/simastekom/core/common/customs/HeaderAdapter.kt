package com.yogadimas.simastekom.core.common.customs

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class HeaderAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        object : RecyclerView.ViewHolder(View(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 1
            )
        }) {}

    override fun getItemCount() = 1
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {}
}