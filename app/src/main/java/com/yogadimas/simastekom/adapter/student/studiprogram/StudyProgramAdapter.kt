package com.yogadimas.simastekom.adapter.student.studiprogram

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yogadimas.simastekom.R
import com.yogadimas.simastekom.databinding.ItemStudyProgramBinding
import com.yogadimas.simastekom.interfaces.OnItemClickCallback
import com.yogadimas.simastekom.model.responses.IdentityAcademicData

class StudyProgramAdapter(private val itemClickCallback: OnItemClickCallback<IdentityAcademicData>) :
    ListAdapter<IdentityAcademicData, StudyProgramAdapter.MyViewHolder>(DIFF_CALLBACK) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding =
            ItemStudyProgramBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding, itemClickCallback)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val level = getItem(position)
        holder.bind(level)
    }

    class MyViewHolder(
        private val binding: ItemStudyProgramBinding,
        private val itemClickCallback: OnItemClickCallback<IdentityAcademicData>,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: IdentityAcademicData) {
            binding.apply {
                tvCode.text = data.code
                tvFaculty.text = data.facultyName
                tvLevelMajorDegree.text = itemView.context.getString(
                    R.string.format_level_major_degree,
                    data.levelName,
                    data.majorName,
                    data.degreeName
                )
                itemView.setOnClickListener { itemClickCallback.onItemClicked(data) }
                btnDelete.visibility = View.GONE
                btnDelete.isEnabled = false
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