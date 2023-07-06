package com.grigorev.diploma.adapter

import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.grigorev.diploma.R
import com.grigorev.diploma.databinding.ItemEventBinding
import com.grigorev.diploma.dto.AttachmentType
import com.grigorev.diploma.dto.Event
import com.grigorev.diploma.util.formatDateTime

interface OnEventInteractionListener {
    fun onEditEvent(event: Event)
    fun onRemoveEvent(event: Event)
    fun onLikeEvent(event: Event)
    fun onParticipate(event: Event)

    fun onOpenUserProfile(event: Event)

    fun onOpenParticipants(event: Event)

    fun onOpenLikers(event: Event)

    fun onOpenSpeakers(event: Event)
}

class EventAdapter(
    private val onEventInteractionListener: OnEventInteractionListener,
) : PagingDataAdapter<Event, EventViewHolder>(EventDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = ItemEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventViewHolder(binding, onEventInteractionListener)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = getItem(position) ?: return
        holder.bind(event)
    }
}

class EventViewHolder(
    private val binding: ItemEventBinding,
    private val onEventInteractionListener: OnEventInteractionListener,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(event: Event) {

        binding.apply {
            if (event.authorJob != null) {
                author.text = itemView.context.getString(
                    R.string.author_job,
                    event.author,
                    event.authorJob
                )
            } else author.text = event.author
            published.text = formatDateTime(event.published)
            content.text = event.content
            dateTime.text = itemView.context.getString(
                R.string.event_type_datetime,
                event.type,
                formatDateTime(event.datetime)
            )
            speakers.text =
                itemView.context.getString(R.string.speakers, event.speakerIds.size.toString())
            participants.text = itemView.context.getString(
                R.string.participants,
                event.participantsIds.size.toString()
            )


            authorAvatar.setOnClickListener { onEventInteractionListener.onOpenUserProfile(event) }
            author.setOnClickListener { onEventInteractionListener.onOpenUserProfile(event) }

            imageAttachment.visibility =
                if (event.attachment != null && event.attachment.type == AttachmentType.IMAGE) VISIBLE else GONE

            Glide.with(authorAvatar)
                .load(event.authorAvatar ?: R.drawable.ic_person_24)
                .placeholder(R.drawable.ic_person_24)
                .circleCrop()
                .into(authorAvatar)

            event.attachment?.apply {
                Glide.with(imageAttachment)
                    .load(this.url)
                    .error(R.drawable.ic_error_24)
                    .timeout(10_000)
                    .into(imageAttachment)
            }

            if (event.link != null) {
                link.visibility = VISIBLE
                link.text = itemView.context.getString(R.string.get_link, event.link)
            } else link.visibility = GONE

            speakers.setOnClickListener {
                onEventInteractionListener.onOpenSpeakers(event)
            }

            like.isChecked = event.likedByMe
            like.text = "${event.likeOwnerIds.size}"

            like.setOnClickListener {
                onEventInteractionListener.onLikeEvent(event)
            }

            like.setOnLongClickListener {
                onEventInteractionListener.onOpenLikers(event)
                true
            }

            participate.isChecked = event.participatedByMe
            participate.setOnClickListener {
                onEventInteractionListener.onParticipate(event)
            }

            participants.setOnClickListener {
                onEventInteractionListener.onOpenParticipants(event)
            }

            menu.isVisible = event.ownedByMe
            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onEventInteractionListener.onRemoveEvent(event)
                                true
                            }

                            R.id.editContent -> {
                                onEventInteractionListener.onEditEvent(event)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }
        }
    }
}

class EventDiffCallback : DiffUtil.ItemCallback<Event>() {
    override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
        return oldItem == newItem
    }
}