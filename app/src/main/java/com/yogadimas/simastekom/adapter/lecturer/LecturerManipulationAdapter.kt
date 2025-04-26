package com.yogadimas.simastekom.adapter.lecturer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.yogadimas.simastekom.common.interfaces.OnItemClickManipulationCallback
import com.yogadimas.simastekom.databinding.ItemLecturerBinding
import com.yogadimas.simastekom.model.responses.LecturerData

class LecturerManipulationAdapter(
    private val itemClickCallback: OnItemClickManipulationCallback<LecturerData>
) : PagingDataAdapter<LecturerData, LecturerManipulationAdapter.ViewHolder>(DIFF_CALLBACK) {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemLecturerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = getItem(position)
        if (data != null) {
            holder.bind(data, itemClickCallback)
        }

    }


    class ViewHolder(private val binding: ItemLecturerBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            data: LecturerData,
            itemClickCallback: OnItemClickManipulationCallback<LecturerData>
        ) {
            binding.apply {
                tvLectureIdCardNumber.text = data.lecturerIdNumber
                tvFullName.text = data.fullName
                tvDegree.text = data.degree
                itemView.setOnClickListener { itemClickCallback.onItemClicked(data) }
                btnDelete.setOnClickListener { itemClickCallback.onDeleteClicked(data) }
            }

        }
    }


    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<LecturerData>() {
            override fun areItemsTheSame(oldItem: LecturerData, newItem: LecturerData): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: LecturerData, newItem: LecturerData): Boolean {
                return oldItem.userId == newItem.userId
            }

        }
    }


}