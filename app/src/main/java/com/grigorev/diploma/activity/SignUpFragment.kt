package com.grigorev.diploma.activity

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.constant.ImageProvider
import com.google.android.material.snackbar.Snackbar
import com.grigorev.diploma.R
import com.grigorev.diploma.databinding.FragmentSignUpBinding
import com.grigorev.diploma.util.AndroidUtils.hideKeyboard
import com.grigorev.diploma.viewmodels.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignUpFragment : Fragment() {
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentSignUpBinding.inflate(inflater, container, false)

        val selectAvatarContract =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                when (it.resultCode) {
                    ImagePicker.RESULT_ERROR -> Snackbar.make(
                        binding.root,
                        ImagePicker.getError(it.data),
                        Snackbar.LENGTH_LONG
                    ).show()

                    Activity.RESULT_OK -> {
                        val uri: Uri? = it.data?.data
                        authViewModel.changePhoto(uri, uri?.toFile())
                    }
                }
            }

        binding.avatar.setOnClickListener {
            ImagePicker.with(this)
                .galleryOnly()
                .crop()
                .compress(2048)
                .provider(ImageProvider.GALLERY)
                .galleryMimeTypes(
                    arrayOf(
                        "image/png",
                        "image/jpeg",
                    )
                )
                .createIntent(selectAvatarContract::launch)
        }

        authViewModel.photo.observe(viewLifecycleOwner) {
            binding.avatar.setImageURI(it.uri)
        }

        binding.registerButton.setOnClickListener {
            if (binding.username.text.isBlank() || binding.login.text.isBlank() ||
                binding.password.text.isBlank() || binding.repeatPassword.text.isBlank()
            ) {
                Toast.makeText(context, R.string.error_blank_fields, Toast.LENGTH_SHORT).show()
            } else if (binding.password.text.toString() != binding.repeatPassword.text.toString()) {
                Toast.makeText(context, R.string.error_passwords, Toast.LENGTH_SHORT).show()
                hideKeyboard(requireView())
            } else if (authViewModel.photo.value == authViewModel.noPhoto) {
                Toast.makeText(context, R.string.pick_avatar, Toast.LENGTH_LONG).show()
            } else {
                authViewModel.photo.value?.file?.let { file ->
                    authViewModel.registerUser(
                        binding.login.text.toString(),
                        binding.password.text.toString(),
                        binding.username.text.toString(),
                        file
                    )
                }
                Toast.makeText(context, R.string.reg_successful, Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_signUpFragment_to_navigation_posts)
            }
        }

        binding.signInButton.setOnClickListener {
            findNavController().navigate(R.id.action_signUpFragment_to_signInFragment)
        }

        return binding.root
    }
}