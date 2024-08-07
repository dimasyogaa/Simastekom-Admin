package com.yogadimas.simastekom.adapter.student.identitasacademic.studiprogram.facultylevelmajordegree

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yogadimas.simastekom.R
import com.yogadimas.simastekom.databinding.ItemCodeNameBinding
import com.yogadimas.simastekom.interfaces.OnItemClickCallback
import com.yogadimas.simastekom.model.responses.IdentityAcademicData

class CodeNameManipulationAdapter(private val itemClickCallback: OnItemClickCallback<IdentityAcademicData>) :
    ListAdapter<IdentityAcademicData, CodeNameManipulationAdapter.MyViewHolder>(DIFF_CALLBACK) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding =
            ItemCodeNameBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding, itemClickCallback)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val level = getItem(position)
        holder.bind(level)
    }

    class MyViewHolder(
        private val binding: ItemCodeNameBinding,
        private val itemClickCallback: OnItemClickCallback<IdentityAcademicData>,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: IdentityAcademicData) {
            binding.apply {
                tvCodeName.text =
                    itemView.context.getString(R.string.format_code_name, data.code, data.name)
                itemView.setOnClickListener { itemClickCallback.onItemClicked(data) }
                btnDelete.setOnClickListener { itemClickCallback.onDeleteClicked(data) }
            }

        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<IdentityAcademicData>() {
            override fun areItemsTheSame(
                oldItem: IdentityAcademicData,
                newItem: IdentityAcademicData,
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: IdentityAcademicData,
                newItem: IdentityAcademicData,
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}