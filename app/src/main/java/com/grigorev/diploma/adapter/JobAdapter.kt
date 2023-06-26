package com.grigorev.diploma.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.grigorev.diploma.R
import com.grigorev.diploma.databinding.ItemJobBinding
import com.grigorev.diploma.dto.Job

interface OnJobInteractionListener {
    fun onEditJob(job: Job)
    fun onDeleteJob(job: Job)
}

class JobAdapter(private val onJobInteractionListener: OnJobInteractionListener, private val isProfileMine: Boolean) :
    ListAdapter<Job, JobViewHolder>(JobDiffItemCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val binding = ItemJobBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return JobViewHolder(binding, onJobInteractionListener, isProfileMine)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        val job = getItem(position) ?: return
        holder.bind(job)
    }
}

class JobViewHolder(
    private val binding: ItemJobBinding,
    private val onJobInteractionListener: OnJobInteractionListener,
    private val isProfileMine: Boolean
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(job: Job) {
        val startJob = job.start
        val endJob = job.finish ?: ""
        with(binding) {

            jobCompany.text = job.name
            jobPosition.text = job.position
            jobPeriod.text = if (endJob.isNotEmpty()) {
                itemView.context.getString(R.string.job_start_end, startJob, endJob)
            } else {
                itemView.context.getString(R.string.job_start, startJob)
            }
            jobLink.text = job.link

            menu.isVisible = isProfileMine
            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onJobInteractionListener.onDeleteJob(job)
                                true
                            }

                            R.id.editContent -> {
                                onJobInteractionListener.onEditJob(job)
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

object JobDiffItemCallback : DiffUtil.ItemCallback<Job>() {
    override fun areItemsTheSame(oldItem: Job, newItem: Job): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Job, newItem: Job): Boolean {
        return oldItem == newItem
    }
}