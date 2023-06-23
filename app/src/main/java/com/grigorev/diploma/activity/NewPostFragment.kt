package com.grigorev.diploma.activity

import android.app.Activity
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.snackbar.Snackbar
import com.grigorev.diploma.R
import com.grigorev.diploma.databinding.FragmentNewPostBinding
import com.grigorev.diploma.dto.AttachmentType
import com.grigorev.diploma.util.AndroidUtils
import com.grigorev.diploma.viewmodels.AuthViewModel
import com.grigorev.diploma.viewmodels.PostsViewModel
import com.grigorev.diploma.viewmodels.emptyPost
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
@AndroidEntryPoint
class NewPostFragment : Fragment() {

    private val viewModel: PostsViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by activityViewModels()

    var type: AttachmentType? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentNewPostBinding.inflate(inflater, container, false)

        val content = arguments?.getString("content")
        val link = arguments?.getString("link")

        binding.apply {

            editContent.requestFocus()

            if (viewModel.edited.value != emptyPost) {
                editContent.setText(content)
                editLink.setText(link)
            }

            val photoContract =
                registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                    when (it.resultCode) {
                        ImagePicker.RESULT_ERROR -> Snackbar.make(
                            root, ImagePicker.getError(it.data), Snackbar.LENGTH_LONG
                        ).show()

                        Activity.RESULT_OK -> {
                            it.data?.data.let { uri ->
                                val stream = uri?.let {
                                    context?.contentResolver?.openInputStream(it)
                                }
                                viewModel.changeMedia(uri, stream, type)
                            }
                        }
                    }
                }

            val mediaContract =
                registerForActivityResult(ActivityResultContracts.GetContent()) {
                    it?.let {
                        val stream = context?.contentResolver?.openInputStream(it)
                        viewModel.changeMedia(it, stream, type)
                    }
                }

            viewModel.media.observe(viewLifecycleOwner) {
                media.setImageURI(it.uri)
                mediaContainer.isVisible = it.uri != null
            }

            takePhoto.setOnClickListener {
                ImagePicker.with(this@NewPostFragment)
                    .cameraOnly()
                    .crop()
                    .compress(2048)
                    .createIntent(photoContract::launch)
                type = AttachmentType.IMAGE
            }

            pickPhoto.setOnClickListener {
                ImagePicker.Builder(this@NewPostFragment)
                    .galleryOnly()
                    .galleryMimeTypes(
                        arrayOf(
                            "image/png",
                            "image/jpeg",
                            "image/jpg"
                        )
                    )
                    .maxResultSize(2048, 2048)
                    .createIntent(photoContract::launch)
                type = AttachmentType.IMAGE
            }

            pickAudio.setOnClickListener {
                mediaContract.launch("audio/*")
                type = AttachmentType.AUDIO
            }

            pickVideo.setOnClickListener {
                mediaContract.launch("video/*")
                type = AttachmentType.VIDEO
            }

            removeMedia.setOnClickListener {
                viewModel.removeMedia()
            }

            viewModel.media.observe(viewLifecycleOwner) {
                if (it?.uri == null) {
                    mediaContainer.visibility = View.GONE
                    return@observe
                }
                mediaContainer.visibility = View.VISIBLE
                media.setImageURI(it.uri)
            }

            activity?.addMenuProvider(object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.menu_new_post, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    return when (menuItem.itemId) {
                        R.id.save -> {
                            if (editContent.text.isEmpty()) {
                                Toast.makeText(context, R.string.empty_post, Toast.LENGTH_SHORT)
                                    .show()
                            } else {
                                viewModel.changeContent(editContent.text.toString(), editLink.text.toString())
                                viewModel.save()
                                AndroidUtils.hideKeyboard(requireView())
                            }
                            true
                        }

                        R.id.logout -> {
                            SignOutFragment().show(childFragmentManager, "logoutDialog")
                            if (!authViewModel.authorized) findNavController().navigateUp()
                            true
                        }

                        else -> false
                    }
                }
            }, viewLifecycleOwner)
        }

        viewModel.postCreated.observe(viewLifecycleOwner) {
            findNavController().navigate(R.id.action_newPostFragment_to_navigation_posts)
        }

        viewModel.error.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show()
        }

        return binding.root
    }


}