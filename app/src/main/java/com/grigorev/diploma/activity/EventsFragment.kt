package com.grigorev.diploma.activity

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
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.grigorev.diploma.R
import com.grigorev.diploma.adapter.EventAdapter
import com.grigorev.diploma.adapter.LoadingStateAdapter
import com.grigorev.diploma.adapter.OnEventInteractionListener
import com.grigorev.diploma.databinding.FragmentEventsBinding
import com.grigorev.diploma.dto.Event
import com.grigorev.diploma.viewmodels.AuthViewModel
import com.grigorev.diploma.viewmodels.EventsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class EventsFragment : Fragment() {

    private val eventsViewModel: EventsViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = FragmentEventsBinding.inflate(inflater, container, false)

        val adapter = EventAdapter(object : OnEventInteractionListener {

            override fun onEditEvent(event: Event) {
                eventsViewModel.edit(event)
                val bundle = Bundle().apply {
                    putString("content", event.content)
                    putString("datetime", event.datetime)
                    putString("eventType", event.type.toString())
                    putString("link", event.link ?: "")
                }
                findNavController()
                    .navigate(R.id.action_events_to_newEventFragment, bundle)
            }

            override fun onRemoveEvent(event: Event) {
                eventsViewModel.removeById(event.id)
            }

            override fun onLikeEvent(event: Event) {
                when (authViewModel.authorized) {
                    true -> when (event.likedByMe) {
                        true -> eventsViewModel.unlikeById(event.id)
                        false -> eventsViewModel.likeById(event.id)
                    }

                    false -> unauthorizedAccessAttempt()
                }
            }

            override fun onParticipate(event: Event) {
                when (authViewModel.authorized) {
                    true -> when (event.participatedByMe) {
                        true -> eventsViewModel.doNotParticipate(event.id)
                        false -> eventsViewModel.participate(event.id)
                    }

                    false -> unauthorizedAccessAttempt()
                }
            }

            override fun onOpenUserProfile(event: Event) {
                if (authViewModel.authorized) {
                    val bundle = Bundle().apply {
                        putString("authorAvatar", event.authorAvatar)
                        putString("author", event.author)
                        putInt("authorId", event.authorId)
                        putBoolean("ownedByMe", event.ownedByMe)
                    }
                    findNavController()
                        .navigate(R.id.action_events_to_userProfileFragment, bundle)
                } else {
                    unauthorizedAccessAttempt()
                }
            }
        })

        val itemAnimator: DefaultItemAnimator = object : DefaultItemAnimator() {
            override fun canReuseUpdatedViewHolder(viewHolder: RecyclerView.ViewHolder): Boolean {
                return true
            }
        }

        binding.eventsList.itemAnimator = itemAnimator

        binding.eventsList.adapter = adapter.withLoadStateHeaderAndFooter(
            header = LoadingStateAdapter { adapter.retry() },
            footer = LoadingStateAdapter { adapter.retry() },
        )

        lifecycleScope.launch {
            eventsViewModel.data.collectLatest(adapter::submitData)
        }

        lifecycleScope.launch {
            eventsViewModel.data.collect {
                binding.eventsList.adapter = adapter
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
                    true -> findNavController().navigate(R.id.action_events_to_newEventFragment)
                    false -> unauthorizedAccessAttempt()
                }
            }
        }

        binding.swipeRefreshLayout.setOnRefreshListener { adapter.refresh() }

        eventsViewModel.error.observe(viewLifecycleOwner) {
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
                        findNavController().navigate(R.id.action_events_to_signInFragment)
                        true
                    }

                    R.id.signUp -> {
                        findNavController().navigate(R.id.action_events_to_signUpFragment)
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

    fun unauthorizedAccessAttempt() {
        Toast.makeText(context, R.string.sign_in_to_continue, Toast.LENGTH_LONG).show()
        findNavController().navigate(R.id.action_events_to_signInFragment)
    }
}