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
import com.grigorev.diploma.viewmodels.AuthViewModel
import com.grigorev.diploma.viewmodels.PostsViewModel
import com.grigorev.diploma.viewmodels.ProfileViewModel
import com.grigorev.diploma.viewmodels.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private val postsViewModel: PostsViewModel by activityViewModels()
    private val profileViewModel: ProfileViewModel by activityViewModels()
    private val userViewModel: UserViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by activityViewModels()

    @OptIn(ExperimentalPagingApi::class)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val binding = FragmentProfileBinding.inflate(inflater, container, false)

        val authorAvatar = userViewModel.user.value?.avatar
        val author = userViewModel.user.value?.name
        val authorId = userViewModel.user.value?.id ?: 0

        val ownedByMe = authorId == authViewModel.state.value?.id

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
                findNavController()
                    .navigate(R.id.action_userProfileFragment_to_newJobFragment)
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
            binding.userProfileToolbar.addJob.visibility = View.VISIBLE
            binding.userProfileToolbar.addJob.setOnClickListener {
                findNavController().navigate(R.id.action_userProfileFragment_to_newJobFragment)
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
                lifecycleScope.launch {
                    when (post.likedByMe) {
                        true -> postsViewModel.unlikeById(post.id)
                        false -> postsViewModel.likeById(post.id)
                    }.join()
                    profileViewModel.getLatestWallPosts(authorId)
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

            override fun onOpenLikers(post: Post) {
                userViewModel.getUsersIds(post.likeOwnerIds)
                if (post.likeOwnerIds.isEmpty()) {
                    Toast.makeText(context, R.string.empty_post_likers, Toast.LENGTH_SHORT)
                        .show()
                } else {
                    UserListFragment().show(childFragmentManager, UserListFragment.TAG)
                }
            }
        })

        binding.postsList.adapter = postsAdapter

        lifecycleScope.launch {
            profileViewModel.getWallPosts(authorId).collectLatest {
                postsAdapter.submitData(it)
            }
        }

        return binding.root
    }

}