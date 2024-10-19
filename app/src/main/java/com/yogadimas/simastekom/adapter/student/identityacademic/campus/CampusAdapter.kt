package com.yogadimas.simastekom.adapter.student.identityacademic.campus

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yogadimas.simastekom.databinding.ItemCampusBinding
import com.yogadimas.simastekom.common.interfaces.OnItemClickManipulationCallback
import com.yogadimas.simastekom.model.responses.CampusData

class CampusAdapter(private val itemClickCallback: OnItemClickManipulationCallback<CampusData>) :
    ListAdapter<CampusData, CampusAdapter.MyViewHolder>(DIFF_CALLBACK) {


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
        private val itemClickCallback: OnItemClickManipulationCallback<CampusData>,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: CampusData) {
            binding.apply {
                tvCampusCode.text = data.code
                tvCampusName.text = data.name
                tvCampusAddress.text = data.address
                itemView.setOnClickListener { itemClickCallback.onItemClicked(data) }
                btnDelete.visibility = View.GONE
                btnDelete.isEnabled = false
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