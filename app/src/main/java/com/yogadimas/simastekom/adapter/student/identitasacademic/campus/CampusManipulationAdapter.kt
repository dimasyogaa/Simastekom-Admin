package com.yogadimas.simastekom.adapter.student.identitasacademic.campus

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yogadimas.simastekom.databinding.ItemCampusBinding
import com.yogadimas.simastekom.interfaces.OnItemClickCallback
import com.yogadimas.simastekom.model.responses.CampusData

class CampusManipulationAdapter(private val itemClickCallback: OnItemClickCallback<CampusData>) :
    ListAdapter<CampusData, CampusManipulationAdapter.MyViewHolder>(DIFF_CALLBACK) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding =
            ItemCampusBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding, itemClickCallback)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val campus = getItem(position)
        holder.bind(campus)
    }

    class MyViewHolder(
        private val binding: ItemCampusBinding,
        private val itemClickCallback: OnItemClickCallback<CampusData>,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: CampusData) {
            binding.apply {
                tvCampusCode.text = data.code
                tvCampusName.text = data.name
                tvCampusAddress.text = data.address
                itemView.setOnClickListener { itemClickCallback.onItemClicked(data) }
                btnDelete.setOnClickListener { itemClickCallback.onDeleteClicked(data) }
            }

        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<CampusData>() {
            override fun areItemsTheSame(
                oldItem: CampusData,
                newItem: CampusData,
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: CampusData,
                newItem: CampusData,
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}