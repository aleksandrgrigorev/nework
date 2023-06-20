package com.grigorev.diploma.activity

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.grigorev.diploma.R
import com.grigorev.diploma.adapter.EventAdapter
import com.grigorev.diploma.adapter.OnEventInteractionListener
import com.grigorev.diploma.databinding.FragmentEventsBinding
import com.grigorev.diploma.dto.Event
import com.grigorev.diploma.util.DateTimeFormatter
import com.grigorev.diploma.viewmodels.AuthViewModel
import com.grigorev.diploma.viewmodels.EventViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class EventsFragment : Fragment() {

    private val eventViewModel by activityViewModels<EventViewModel>()
    private val authViewModel by activityViewModels<AuthViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = FragmentEventsBinding.inflate(inflater, container, false)

        val adapter = EventAdapter(DateTimeFormatter(), object : OnEventInteractionListener {

            override fun onEditEvent(event: Event) {
                TODO()
            }

            override fun onRemoveEvent(event: Event) {
                eventViewModel.removeById(event.id)
            }

            override fun onLikeEvent(event: Event) {
                when (authViewModel.authorized) {
                    true -> when(event.likedByMe) {
                        true -> eventViewModel.unlikeById(event.id)
                        false -> eventViewModel.likeById(event.id)
                    }
                    false -> unauthorizedAccessAttempt()
                }
            }

            override fun onParticipate(event: Event) {
                if (authViewModel.authorized) {
                    if (!event.participatedByMe)
                        eventViewModel.participate(event.id)
                    else eventViewModel.doNotParticipate(event.id)
                } else {
                    unauthorizedAccessAttempt()
                }
            }
        })

            lifecycleScope.launch {
                eventViewModel.data.collectLatest(adapter::submitData)
            }

            eventViewModel.dataState.observe(viewLifecycleOwner) {
                when {
                    it.error -> {
                        Toast.makeText(context, R.string.error_loading, Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }

        lifecycleScope.launch {
            eventViewModel.data.collect {
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

            return binding.root
        }
    fun unauthorizedAccessAttempt() {
        Toast.makeText(context, R.string.sign_in_to_continue, Toast.LENGTH_LONG).show()
        findNavController().navigate(R.id.action_navigation_posts_to_signInFragment)
    }
}