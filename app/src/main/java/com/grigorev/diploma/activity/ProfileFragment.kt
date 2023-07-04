package com.grigorev.diploma.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.ExperimentalPagingApi
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.grigorev.diploma.R
import com.grigorev.diploma.adapter.JobAdapter
import com.grigorev.diploma.adapter.OnJobInteractionListener
import com.grigorev.diploma.adapter.OnPostInteractionListener
import com.grigorev.diploma.adapter.PostsAdapter
import com.grigorev.diploma.databinding.FragmentProfileBinding
import com.grigorev.diploma.dto.Job
import com.grigorev.diploma.dto.Post
import com.grigorev.diploma.viewmodels.PostsViewModel
import com.grigorev.diploma.viewmodels.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private val postsViewModel: PostsViewModel by activityViewModels()
    private val profileViewModel: ProfileViewModel by activityViewModels()

    @OptIn(ExperimentalPagingApi::class)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val binding = FragmentProfileBinding.inflate(inflater, container, false)

        val authorAvatar = arguments?.getString("authorAvatar")
        val author = arguments?.getString("author")!!
        val authorId = arguments?.getInt("authorId")!!
        val ownedByMe = arguments?.getBoolean("ownedByMe")!!

        Glide.with(binding.userProfileToolbar.userAvatar)
            .load(authorAvatar)
            .error(R.drawable.ic_person_24)
            .timeout(10_000)
            .circleCrop()
            .into(binding.userProfileToolbar.userAvatar)

        binding.userProfileToolbar.username.text = author

        binding.appBar.addOnOffsetChangedListener { _, verticalOffset ->
            binding.swipeRefreshLayout.isEnabled = verticalOffset == 0
        }

        profileViewModel.loadJobs(authorId)
        profileViewModel.getLatestWallPosts(authorId)

        val jobAdapter = JobAdapter(object : OnJobInteractionListener {

            override fun onEditJob(job: Job) {
                profileViewModel.edit(job)
                val bundle = Bundle().apply {
                    putString("name", job.name)
                    putString("position", job.position)
                    putString("start", job.start)
                    putString("finish", job.finish)
                    putString("link", job.link)
                    putInt("id", job.id)

                    putString("authorAvatar", authorAvatar)
                    putString("author", author)
                    putInt("authorId", authorId)
                    putBoolean("ownedByMe", ownedByMe)
                }
                findNavController()
                    .navigate(R.id.action_userProfileFragment_to_newJobFragment, bundle)
            }

            override fun onDeleteJob(job: Job) {
                profileViewModel.deleteJobById(job.id)
            }

        }, ownedByMe)

        val itemAnimator: DefaultItemAnimator = object : DefaultItemAnimator() {
            override fun canReuseUpdatedViewHolder(viewHolder: RecyclerView.ViewHolder): Boolean {
                return true
            }
        }

        if (ownedByMe) {
            val bundle = Bundle().apply {
                putString("authorAvatar", authorAvatar)
                putString("author", author)
                putInt("authorId", authorId)
                putBoolean("ownedByMe", ownedByMe)
            }
            binding.userProfileToolbar.addJob.visibility = View.VISIBLE
            binding.userProfileToolbar.addJob.setOnClickListener {
                findNavController().navigate(
                    R.id.action_userProfileFragment_to_newJobFragment,
                    bundle
                )
            }
        } else {
            binding.userProfileToolbar.addJob.visibility = View.GONE
        }

        binding.userProfileToolbar.jobsList.itemAnimator = itemAnimator
        binding.userProfileToolbar.jobsList.adapter = jobAdapter

        profileViewModel.getAllJobs().observe(viewLifecycleOwner) {
            jobAdapter.submitList(it.toList())
        }

        val postsAdapter = PostsAdapter(object : OnPostInteractionListener {

            override fun onEdit(post: Post) {
                postsViewModel.edit(post)
                findNavController().navigate(R.id.action_userProfileFragment_to_newPostFragment)
            }

            override fun onRemove(post: Post) {
                postsViewModel.removeById(post.id)
            }

            override fun onLike(post: Post) {
                when (post.likedByMe) {
                    true -> postsViewModel.unlikeById(post.id)
                    false -> postsViewModel.likeById(post.id)
                }
            }

            override fun onWatchVideo(post: Post) {
                try {
                    val uri = Uri.parse(post.attachment?.url)
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.setDataAndType(uri, "video/*")
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(context, R.string.error_loading, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onOpenUserProfile(post: Post) {
                Toast.makeText(
                    requireContext(),
                    "You are already on this profile",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        binding.postsList.adapter = postsAdapter

        lifecycleScope.launch {
            profileViewModel.getWallPosts(authorId).collectLatest {
                postsAdapter.submitData(it)
            }
        }

        postsAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                if (positionStart == 0) {
                    binding.postsList.smoothScrollToPosition(0)
                }
            }
        })

        return binding.root
    }

}