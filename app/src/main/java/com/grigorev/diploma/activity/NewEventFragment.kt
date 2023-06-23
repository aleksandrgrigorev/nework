package com.grigorev.diploma.activity

import android.app.Activity
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.snackbar.Snackbar
import com.grigorev.diploma.R
import com.grigorev.diploma.databinding.FragmentNewEventBinding
import com.grigorev.diploma.dto.AttachmentType
import com.grigorev.diploma.util.AndroidUtils
import com.grigorev.diploma.util.StringArg
import com.grigorev.diploma.util.pickDate
import com.grigorev.diploma.util.pickTime
import com.grigorev.diploma.viewmodels.EventsViewModel
import com.grigorev.diploma.viewmodels.emptyEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
@AndroidEntryPoint
class NewEventFragment : Fragment() {

    private val eventViewModel: EventsViewModel by viewModels()

    var type: AttachmentType? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentNewEventBinding.inflate(inflater, container, false)

        binding.edit.requestFocus()

        val datetime = arguments?.getString("datetime")
        val date = datetime?.substring(0, 10)
        val time = datetime?.substring(11, 16)

        val content = arguments?.getString("content")

        if (eventViewModel.edited.value != emptyEvent) {
            binding.edit.setText(content)
            binding.editDate.setText(date)
            binding.editTime.setText(time)
        }

        binding.editDate.setOnClickListener {
            context?.let { item ->
                pickDate(binding.editDate, item)
            }
        }

        binding.editTime.setOnClickListener {
            context?.let { item ->
                pickTime(binding.editTime, item)
            }
        }

        val photoContract =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                when (it.resultCode) {
                    ImagePicker.RESULT_ERROR -> Snackbar.make(
                        binding.root,
                        ImagePicker.getError(it.data),
                        Snackbar.LENGTH_LONG
                    ).show()

                    Activity.RESULT_OK -> {
                        it.data?.data.let { uri ->
                            val stream = uri?.let {
                                context?.contentResolver?.openInputStream(it)
                            }
                            eventViewModel.changeMedia(uri, stream, type)
                        }
                    }
                }
            }

        eventViewModel.media.observe(viewLifecycleOwner) {
            binding.media.setImageURI(it.uri)
            binding.mediaContainer.isVisible = it.uri != null
        }

        binding.takePhoto.setOnClickListener {
            ImagePicker.with(this)
                .cameraOnly()
                .crop()
                .compress(2048)
                .createIntent(photoContract::launch)
            type = AttachmentType.IMAGE
        }

        binding.pickPhoto.setOnClickListener {
            ImagePicker.Builder(this)
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

        binding.removeMedia.setOnClickListener {
            eventViewModel.removeMedia()
        }

        eventViewModel.media.observe(viewLifecycleOwner) {
            if (it?.uri == null) {
                binding.mediaContainer.visibility = View.GONE
                return@observe
            }
            binding.mediaContainer.visibility = View.VISIBLE
            binding.media.setImageURI(it.uri)
        }

        activity?.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_new_post, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.save -> {
                        if (binding.edit.text.isEmpty()) {
                            Toast.makeText(context, R.string.empty_post, Toast.LENGTH_SHORT).show()
                        } else {
                            eventViewModel.changeContent(
                                binding.edit.text.toString(),
                                "${binding.editDate.text} " + "${binding.editTime.text}"
                            )
                            eventViewModel.save()
                            AndroidUtils.hideKeyboard(requireView())
                        }
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner)

        eventViewModel.eventCreated.observe(viewLifecycleOwner) {
            findNavController().navigateUp()
        }

        eventViewModel.error.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show()
        }

        return binding.root
    }


}