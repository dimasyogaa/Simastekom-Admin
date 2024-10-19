package com.yogadimas.simastekom.common.paging

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yogadimas.simastekom.databinding.LayoutPaginationBinding

class LoadingStateAdapter(private val retry: () -> Unit) :
    LoadStateAdapter<LoadingStateAdapter.LoadingStateViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState,
    ): LoadingStateViewHolder {
        val binding = LayoutPaginationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LoadingStateViewHolder(binding, retry)
    }

    override fun onBindViewHolder(holder: LoadingStateViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    class LoadingStateViewHolder(private val binding: LayoutPaginationBinding, retry: () -> Unit) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.paginationRetryButton.setOnClickListener { retry.invoke() }
        }

        fun bind(loadState: LoadState) {

            if (loadState is LoadState.Error) {
                binding.paginationErrorMessage.text = loadState.error.localizedMessage
            }
            val progressBarIsVisible =  loadState is LoadState.Loading
            val errorIsVisible =  loadState is LoadState.Error
            binding.paginationProgressBar.isInvisible = !progressBarIsVisible
            binding.paginationRetryButton.isInvisible = !errorIsVisible
            binding.paginationErrorMessage.isInvisible = !errorIsVisible
        }
    }

}