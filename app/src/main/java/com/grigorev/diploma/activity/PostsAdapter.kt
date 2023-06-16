package com.grigorev.diploma.activity

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.grigorev.diploma.R
import com.grigorev.diploma.databinding.ItemPostBinding
import com.grigorev.diploma.dto.Post
import java.text.SimpleDateFormat
import java.util.Locale

interface OnInteractionListener {
    fun onEdit(post: Post) {}
    fun onRemove(post: Post) {}

    fun onLike(post: Post) {}

}

class PostsAdapter(
    private val onInteractionListener: OnInteractionListener,
) : PagingDataAdapter<Post, PostViewHolder>(PostDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, onInteractionListener)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position) ?: return
        holder.bind(post)
    }
}

class PostViewHolder(
    private val binding: ItemPostBinding,
    private val onInteractionListener: OnInteractionListener
) : RecyclerView.ViewHolder(binding.root) {

    val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSS'Z'", Locale.ENGLISH)
    val formatter = SimpleDateFormat("dd MMMM yyyy HH:mm", Locale.ENGLISH)

    fun bind(post: Post) {

        val publishedTimeFormatted = parser.parse(post.published)?.let { formatter.format(it) }

        Glide.with(binding.authorAvatar)
            .load(post.authorAvatar ?: R.mipmap.ic_launcher)
            .circleCrop()
            .into(binding.authorAvatar)

        binding.apply {

            author.text = itemView.context.getString(
                R.string.author_job,
                post.author,
                post.authorJob ?: itemView.context.resources.getString(R.string.null_job)
            )
            published.text = publishedTimeFormatted
            content.text = post.content

            like.isChecked = post.likedByMe
            like.text = "${post.likeOwnerIds.size}"

            menu.isVisible = post.ownedByMe
            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onInteractionListener.onRemove(post)
                                true
                            }

                            R.id.edit -> {
                                onInteractionListener.onEdit(post)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }

            like.setOnClickListener {
                onInteractionListener.onLike(post)
            }
        }
    }

}

class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem == newItem
    }
}