package com.yogadimas.simastekom.core.ui.adapter.paging

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yogadimas.simastekom.core.common.interfaces.RetryClickCallback
import com.yogadimas.simastekom.core.databinding.CommonLayoutLoadPaginationBinding

class LoadingStateAdapter(
    private val listener: RetryClickCallback,
) : LoadStateAdapter<LoadingStateAdapter.LoadingStateViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState,
    ): LoadingStateViewHolder {
        val binding =
            CommonLayoutLoadPaginationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LoadingStateViewHolder(binding, listener)
    }

    override fun onBindViewHolder(holder: LoadingStateViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }


    class LoadingStateViewHolder(
        private val binding: CommonLayoutLoadPaginationBinding,
        private val listener: RetryClickCallback,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(loadState: LoadState) {
            val isLoading = loadState is LoadState.Loading
            val isError = loadState is LoadState.Error

            binding.apply {
                paginationProgressBar.isVisible = isLoading
                paginationTvErrorMessage.isVisible = isError
                paginationBtnRetry.apply {
                    isVisible = isError
                    setOnClickListener { listener.onClick() }
                }
            }
        }

    }
}
