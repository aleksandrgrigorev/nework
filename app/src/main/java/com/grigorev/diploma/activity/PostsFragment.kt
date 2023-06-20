package com.grigorev.diploma.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.google.android.material.snackbar.Snackbar
import com.grigorev.diploma.R
import com.grigorev.diploma.activity.NewPostFragment.Companion.textArg
import com.grigorev.diploma.adapter.OnPostInteractionListener
import com.grigorev.diploma.adapter.PostsAdapter
import com.grigorev.diploma.auth.AppAuth
import com.grigorev.diploma.databinding.FragmentPostsBinding
import com.grigorev.diploma.dto.Post
import com.grigorev.diploma.util.DateTimeFormatter
import com.grigorev.diploma.viewmodels.AuthViewModel
import com.grigorev.diploma.viewmodels.PostsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class PostsFragment : Fragment() {

    private val postsViewModel: PostsViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

    @Inject
    lateinit var appAuth: AppAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentPostsBinding.inflate(inflater, container, false)

        val adapter = PostsAdapter(DateTimeFormatter(), object : OnPostInteractionListener {

            override fun onEdit(post: Post) {
                postsViewModel.edit(post)
                findNavController().navigate(
                    R.id.action_navigation_posts_to_newPostFragment,
                    Bundle().apply {
                        textArg = post.content
                    }
                )
            }

            override fun onRemove(post: Post) {
                postsViewModel.removeById(post.id)
            }

            override fun onLike(post: Post) {
                when (authViewModel.authorized) {
                    true -> {
                        when (post.likedByMe) {
                            true -> postsViewModel.unlikeById(post.id)
                            false -> postsViewModel.likeById(post.id)
                        }
                    }
                    false -> unauthorizedAccessAttempt()
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
        })

        lifecycleScope.launch {
            postsViewModel.data.collect {
                binding.postsList.adapter = adapter
                adapter.submitData(it)
            }
        }

        lifecycleScope.launch {
            adapter.loadStateFlow.collectLatest {
                binding.swipeRefreshLayout.isRefreshing = it.refresh is LoadState.Loading
                        || it.append is LoadState.Loading
                        || it.prepend is LoadState.Loading
            }
        }

        authViewModel.state.observe(viewLifecycleOwner) {
            binding.fab.setOnClickListener {
                when (authViewModel.authorized) {
                    true -> findNavController().navigate(R.id.action_navigation_posts_to_newPostFragment)
                    false -> unauthorizedAccessAttempt()
                }
            }
        }

        binding.swipeRefreshLayout.setOnRefreshListener { adapter.refresh() }

        postsViewModel.error.observe(viewLifecycleOwner) {
            Snackbar.make(requireView(), it.message as CharSequence, Snackbar.LENGTH_LONG).show()
        }

        var menuProvider: MenuProvider? = null

        authViewModel.state.observe(viewLifecycleOwner) {
            menuProvider?.let { requireActivity()::removeMenuProvider }
        }

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_auth, menu)

                menu.setGroupVisible(R.id.authorized, authViewModel.authorized)
                menu.setGroupVisible(R.id.unauthorized, !authViewModel.authorized)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                when (menuItem.itemId) {
                    R.id.logout -> {
                        SignOutFragment().show(childFragmentManager, "logoutDialog")
                        true
                    }

                    R.id.signIn -> {
                        findNavController().navigate(R.id.action_navigation_posts_to_signInFragment)
                        true
                    }

                    R.id.signUp -> {
                        findNavController().navigate(R.id.action_navigation_posts_to_signUpFragment)
                        true
                    }

                    else -> false
                }
        }.apply {
            menuProvider = this
        }, viewLifecycleOwner)

        authViewModel.state.observe(viewLifecycleOwner) {
            requireActivity().invalidateOptionsMenu()
        }

        return binding.root
    }

    private fun unauthorizedAccessAttempt() {
        Toast.makeText(context, R.string.sign_in_to_continue, Toast.LENGTH_LONG).show()
        findNavController().navigate(R.id.action_navigation_posts_to_signInFragment)
    }

}