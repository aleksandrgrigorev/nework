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
import com.grigorev.diploma.databinding.FragmentNewEventBinding
import com.grigorev.diploma.dto.AttachmentType
import com.grigorev.diploma.util.AndroidUtils
import com.grigorev.diploma.util.pickDate
import com.grigorev.diploma.util.pickTime
import com.grigorev.diploma.viewmodels.EventsViewModel
import com.grigorev.diploma.viewmodels.emptyEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
@AndroidEntryPoint
class NewEventFragment : Fragment() {

    private val eventViewModel: EventsViewModel by activityViewModels()

    var type: AttachmentType? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentNewEventBinding.inflate(inflater, container, false)

        binding.apply {

            editContent.requestFocus()

            if (eventViewModel.edited.value != emptyEvent) {
                val datetime = eventViewModel.edited.value?.datetime
                val date = datetime?.substring(0, 10)
                val time = datetime?.substring(11, 16)

                editDate.setText(date)
                editTime.setText(time)

                editContent.setText(eventViewModel.edited.value?.content)
                editLink.setText(eventViewModel.edited.value?.link)

                when (eventViewModel.edited.value?.type?.name) {
                    "ONLINE" -> eventTypeCheckBox.isChecked = true
                    "OFFLINE" -> eventTypeCheckBox.isChecked = false
                }

                val url = eventViewModel.edited.value?.attachment?.url
                val type = eventViewModel.edited.value?.attachment?.type

                if (!url.isNullOrBlank()) {
                    eventViewModel.changeMedia(url.toUri(), null, type)
                }
            }

            editDate.setOnClickListener {
                context?.let { item ->
                    pickDate(editDate, item)
                }
            }

            editTime.setOnClickListener {
                context?.let { item ->
                    pickTime(editTime, item)
                }
            }

            eventViewModel.media.observe(viewLifecycleOwner) {
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
                                eventViewModel.changeMedia(uri, stream, type)
                            }
                        }
                    }
                }

            takePhoto.setOnClickListener {
                ImagePicker.with(this@NewEventFragment)
                    .cameraOnly()
                    .crop()
                    .compress(2048)
                    .createIntent(photoContract::launch)
                type = AttachmentType.IMAGE
            }

            pickPhoto.setOnClickListener {
                ImagePicker.Builder(this@NewEventFragment)
                    .galleryOnly()
                    .galleryMimeTypes(
                        arrayOf("image/png", "image/jpeg", "image/jpg")
                    )
                    .maxResultSize(2048, 2048)
                    .createIntent(photoContract::launch)
                type = AttachmentType.IMAGE
            }

            removeMedia.setOnClickListener {
                eventViewModel.removeMedia()
            }

            activity?.addMenuProvider(object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.menu_new_post_event_job, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    return when (menuItem.itemId) {
                        R.id.save -> {
                            if (editContent.text.isEmpty()) {
                                Toast.makeText(context, R.string.empty_event, Toast.LENGTH_SHORT)
                                    .show()
                            } else {
                                eventViewModel.changeContent(
                                    editContent.text.toString(), editLink.text.toString(),
                                    "${editDate.text} " + "${editTime.text}"
                                )
                                eventViewModel.changeEventType(eventTypeCheckBox.isChecked)
                                eventViewModel.save()
                                AndroidUtils.hideKeyboard(requireView())
                            }
                            true
                        }

                        R.id.cancel -> {
                            eventViewModel.clearEditedEvent()
                            findNavController().navigateUp()
                            true
                        }

                        else -> {
                            eventViewModel.clearEditedEvent()
                            false
                        }
                    }
                }
            }, viewLifecycleOwner)
        }

        eventViewModel.eventCreated.observe(viewLifecycleOwner) {
            findNavController().navigateUp()
        }

        eventViewModel.error.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show()
        }

        return binding.root
    }
}