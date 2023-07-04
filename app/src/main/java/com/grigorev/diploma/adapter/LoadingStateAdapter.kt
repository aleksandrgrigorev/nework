package com.grigorev.diploma.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.grigorev.diploma.databinding.ItemLoadingBinding

class LoadingStateAdapter(
    private val retryListener: () -> Unit,
) : LoadStateAdapter<LoadingViewHolder>() {

    override fun onBindViewHolder(holder: LoadingViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): LoadingViewHolder =
        LoadingViewHolder(
            ItemLoadingBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            retryListener,
        )
}

class LoadingViewHolder(
    private val itemLoadingBinding: ItemLoadingBinding,
    private val retryListener: () -> Unit,
) : RecyclerView.ViewHolder(itemLoadingBinding.root) {
    fun bind(loadState: LoadState) {
        itemLoadingBinding.apply {
            progressBar.isVisible = loadState is LoadState.Loading
            retryButton.isVisible = loadState is LoadState.Error
            retryButton.setOnClickListener {
                retryListener()
            }
        }
    }
}