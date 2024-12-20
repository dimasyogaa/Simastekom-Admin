package com.yogadimas.simastekom.adapter.student.name

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yogadimas.simastekom.databinding.ItemNameBinding
import com.yogadimas.simastekom.common.interfaces.OnItemClickManipulationCallback
import com.yogadimas.simastekom.model.responses.NameData

class NameAdapter(private val itemClickCallback: OnItemClickManipulationCallback<NameData>) :
    ListAdapter<NameData, NameAdapter.MyViewHolder>(DIFF_CALLBACK) {


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
        private val itemClickCallback: OnItemClickManipulationCallback<NameData>,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: NameData) {
            binding.apply {
                tvName.text = data.name
                itemView.setOnClickListener { itemClickCallback.onItemClicked(data) }
                btnDelete.visibility = View.GONE
                btnDelete.isEnabled = false
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