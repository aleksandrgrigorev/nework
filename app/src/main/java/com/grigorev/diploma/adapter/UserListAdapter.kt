package com.grigorev.diploma.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.grigorev.diploma.R
import com.grigorev.diploma.databinding.ItemUserBinding
import com.grigorev.diploma.dto.User

interface OnUserListInteractionListener {
    fun onOpenProfile(user: User)
}

class UserListAdapter(
    private val onUserListInteractionListener: OnUserListInteractionListener
) : ListAdapter<User, UserViewHolder>(UserDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding, onUserListInteractionListener)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val item = getItem(position) ?: return
        holder.bind(item)
    }
}

class UserViewHolder(
    private val binding: ItemUserBinding,
    private val onUserListInteractionListener: OnUserListInteractionListener
    ) : RecyclerView.ViewHolder(binding.root) {

    fun bind(user: User) {
        binding.apply {
            name.text = user.name

            Glide.with(avatar)
                .load(user.avatar)
                .error(R.drawable.ic_person_24)
                .timeout(10_000)
                .circleCrop()
                .into(avatar)

            userItem.setOnClickListener {
                onUserListInteractionListener.onOpenProfile(user)
            }
        }
    }
}

class UserDiffCallback : DiffUtil.ItemCallback<User>() {

    override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem == newItem
    }
}