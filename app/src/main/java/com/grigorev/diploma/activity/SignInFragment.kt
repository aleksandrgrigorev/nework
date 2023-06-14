package com.grigorev.diploma.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.grigorev.diploma.R
import com.grigorev.diploma.databinding.FragmentSignInBinding
import com.grigorev.diploma.error.ApiException
import com.grigorev.diploma.viewmodels.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignInFragment : Fragment() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentSignInBinding.inflate(inflater, container, false)

        authViewModel.error.observe(viewLifecycleOwner) {
            when (it) {
                is ApiException -> Toast.makeText(context, R.string.incorrect_credentials, Toast.LENGTH_LONG).show()
                else -> Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show()
            }
        }

        binding.signUpButton.setOnClickListener {
            findNavController().navigate(R.id.action_signInFragment_to_signUpFragment)
        }

        binding.authorizeButton.setOnClickListener {
            if (binding.login.text.isBlank() && binding.password.text.isBlank()) {
                Toast.makeText(context, R.string.error_blank_auth, Toast.LENGTH_LONG).show()
            } else if (binding.login.text.isBlank()) {
                Toast.makeText(context, R.string.error_blank_username, Toast.LENGTH_LONG).show()
            } else if (binding.password.text.isBlank()) {
                Toast.makeText(context, R.string.error_blank_password, Toast.LENGTH_LONG).show()
            } else {
                authViewModel.updateUser(
                    binding.login.text.toString(),
                    binding.password.text.toString()
                )
            }
        }

        return binding.root
    }
}