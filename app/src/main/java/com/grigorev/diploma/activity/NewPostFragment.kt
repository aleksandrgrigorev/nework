package com.grigorev.diploma.activity

import android.app.Activity
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
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
import com.grigorev.diploma.viewmodels.PostsViewModel
import com.grigorev.diploma.viewmodels.emptyPost
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
@AndroidEntryPoint
class NewPostFragment : Fragment() {

    private val viewModel: PostsViewModel by activityViewModels()
    var type: AttachmentType? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentNewPostBinding.inflate(inflater, container, false)

        binding.apply {

            editContent.requestFocus()

            if (viewModel.edited.value != emptyPost) {
                editContent.setText(viewModel.edited.value?.content)
                editLink.setText(viewModel.edited.value?.link)

                val url = viewModel.edited.value?.attachment?.url
                val type = viewModel.edited.value?.attachment?.type

                if (!url.isNullOrBlank()) {
                    viewModel.changeMedia(url.toUri(), null, type)
                }
            }

            viewModel.media.observe(viewLifecycleOwner) {
                media.setImageURI(it.uri)
                mediaContainer.isVisible = it.uri != null
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

            activity?.addMenuProvider(object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.menu_new_post_event_job, menu)
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

                        R.id.cancel -> {
                            viewModel.clearEditedPost()
                            findNavController().navigateUp()
                            true
                        }

                        else -> {
                            viewModel.clearEditedPost()
                            false
                        }
                    }
                }
            }, viewLifecycleOwner)
        }

        viewModel.postCreated.observe(viewLifecycleOwner) {
            findNavController().navigateUp()
        }

        viewModel.error.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show()
        }

        return binding.root
    }


}