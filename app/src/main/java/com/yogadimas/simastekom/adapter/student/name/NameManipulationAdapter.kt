package com.yogadimas.simastekom.adapter.student.name

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yogadimas.simastekom.adapter.student.identitasacademic.campus.CampusManipulationAdapter
import com.yogadimas.simastekom.databinding.ItemCampusBinding
import com.yogadimas.simastekom.databinding.ItemNameBinding
import com.yogadimas.simastekom.interfaces.OnItemClickCallback
import com.yogadimas.simastekom.model.responses.CampusData
import com.yogadimas.simastekom.model.responses.NameData

class NameManipulationAdapter(private val itemClickCallback: OnItemClickCallback<NameData>) :
    ListAdapter<NameData, NameManipulationAdapter.MyViewHolder>(DIFF_CALLBACK) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding =
            ItemNameBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding, itemClickCallback)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val name = getItem(position)
        holder.bind(name)
    }

    class MyViewHolder(
        private val binding: ItemNameBinding,
        private val itemClickCallback: OnItemClickCallback<NameData>,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: NameData) {
            binding.apply {
                tvName.text = data.name
                itemView.setOnClickListener { itemClickCallback.onItemClicked(data) }
                btnDelete.setOnClickListener { itemClickCallback.onDeleteClicked(data) }
            }

        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<NameData>() {
            override fun areItemsTheSame(
                oldItem: NameData,
                newItem: NameData,
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: NameData,
                newItem: NameData,
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}