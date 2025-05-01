package com.yogadimas.simastekom.adapter.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.yogadimas.simastekom.common.interfaces.OnItemClickManipulationCallback
import com.yogadimas.simastekom.databinding.ItemAdminBinding
import com.yogadimas.simastekom.model.responses.AdminData

class AdminManipulationAdapter(
    private val itemClickCallback: OnItemClickManipulationCallback<AdminData>,
) : PagingDataAdapter<AdminData, AdminManipulationAdapter.ViewHolder>(DIFF_CALLBACK) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemAdminBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = getItem(position)
        if (data != null) {
            holder.bind(data, itemClickCallback)
        }
    }


    class ViewHolder(private val binding: ItemAdminBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            data: AdminData,
            itemClickCallback: OnItemClickManipulationCallback<AdminData>,
        ) {
            binding.apply {
                tvAdminIdUsername.text = data.username
                tvFullName.text = data.name
                itemView.setOnClickListener { itemClickCallback.onItemClicked(data) }
                btnDelete.setOnClickListener { itemClickCallback.onDeleteClicked(data) }
            }

        }
    }


    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<AdminData>() {
            override fun areItemsTheSame(oldItem: AdminData, newItem: AdminData): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: AdminData, newItem: AdminData): Boolean {
                return oldItem.userId == newItem.userId
            }

        }
    }


}