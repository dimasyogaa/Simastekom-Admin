package com.yogadimas.simastekom.simastekom_mahasiswa.ui.important_contact

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.yogadimas.simastekom.core.common.interfaces.ItemClickCallback
import com.yogadimas.simastekom.core.data.source.remote.response.ImportantContactData
import com.yogadimas.simastekom.databinding.ItemLecturerBinding
import com.yogadimas.simastekom.simastekom_mahasiswa.databinding.ItemSmimportantContactBinding


class SMImportantContactAdapter(
    private val itemClickCallback: ItemClickCallback<ImportantContactData>
) : PagingDataAdapter<ImportantContactData, SMImportantContactAdapter.ViewHolder>(DIFF_CALLBACK) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemSmimportantContactBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = getItem(position)
        if (data != null) {
            holder.bind(data, itemClickCallback)
        }

    }


    class ViewHolder(private val binding: ItemSmimportantContactBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            data: ImportantContactData,
            itemClickCallback: ItemClickCallback<ImportantContactData>
        ) {
            binding.apply {
                tvName.text = data.name
                tvPhone.text = data.phone
                tvCategory.text = data.category?.name.orEmpty()
                itemView.setOnClickListener { itemClickCallback.onClick(data) }
            }

        }
    }


    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ImportantContactData>() {
            override fun areItemsTheSame(
                oldItem: ImportantContactData,
                newItem: ImportantContactData
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: ImportantContactData,
                newItem: ImportantContactData
            ): Boolean {
                return oldItem.id == newItem.id
            }

        }
    }


}