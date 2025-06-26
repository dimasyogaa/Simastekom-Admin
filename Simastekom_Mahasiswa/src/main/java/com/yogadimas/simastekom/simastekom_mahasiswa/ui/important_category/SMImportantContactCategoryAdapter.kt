package com.yogadimas.simastekom.simastekom_mahasiswa.ui.important_category

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.yogadimas.simastekom.core.common.interfaces.ItemClickCallback
import com.yogadimas.simastekom.core.data.source.remote.response.ImportantContactCategoryData
import com.yogadimas.simastekom.simastekom_mahasiswa.databinding.ItemSmimportantContactCategoryBinding


class SMImportantContactCategoryAdapter(
    private val itemClickCallback: ItemClickCallback<ImportantContactCategoryData>
) : PagingDataAdapter<ImportantContactCategoryData, SMImportantContactCategoryAdapter.ViewHolder>(
    DIFF_CALLBACK
) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemSmimportantContactCategoryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ViewHolder(binding)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = getItem(position)
        if (data != null) {
            holder.bind(data, itemClickCallback)
        }

    }


    class ViewHolder(private val binding: ItemSmimportantContactCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            data: ImportantContactCategoryData,
            itemClickCallback: ItemClickCallback<ImportantContactCategoryData>
        ) {
            binding.apply {
                tvName.text = data.name
                itemView.setOnClickListener { itemClickCallback.onClick(data) }
            }

        }
    }


    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ImportantContactCategoryData>() {
            override fun areItemsTheSame(
                oldItem: ImportantContactCategoryData,
                newItem: ImportantContactCategoryData
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: ImportantContactCategoryData,
                newItem: ImportantContactCategoryData
            ): Boolean {
                return oldItem.id == newItem.id
            }

        }
    }


}