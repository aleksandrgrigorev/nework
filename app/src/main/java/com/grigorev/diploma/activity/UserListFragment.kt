package com.grigorev.diploma.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.grigorev.diploma.R
import com.grigorev.diploma.adapter.OnUserListInteractionListener
import com.grigorev.diploma.adapter.UserListAdapter
import com.grigorev.diploma.databinding.FragmentUserListBinding
import com.grigorev.diploma.dto.User
import com.grigorev.diploma.viewmodels.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UserListFragment : BottomSheetDialogFragment() {

    private val userViewModel: UserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentUserListBinding.inflate(inflater, container, false)

        val adapter = UserListAdapter(object : OnUserListInteractionListener {
            override fun onOpenProfile(user: User) {
                lifecycleScope.launch {
                    userViewModel.getUserById(user.id).join()
                    findNavController().navigate(R.id.userProfileFragment)
                }
            }
        })

        binding.userList.adapter = adapter

        userViewModel.data.observe(viewLifecycleOwner) {
            adapter.submitList(it.filter { user ->
                userViewModel.userIds.value!!.contains(user.id)
            })
        }

        return binding.root
    }

    companion object {
        const val TAG = "UserListFragment"
    }
}