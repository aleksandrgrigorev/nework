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

        val datetime = arguments?.getString("datetime")
        val date = datetime?.substring(0, 10)
        val time = datetime?.substring(11, 16)

        val content = arguments?.getString("content")
        val eventType = arguments?.getString("eventType")

        binding.apply {

            edit.requestFocus()

            if (eventViewModel.edited.value != emptyEvent) {
                edit.setText(content)
                editDate.setText(date)
                editTime.setText(time)
                when (eventType) {
                    "ONLINE" -> eventTypeCheckBox.isChecked = true
                    "OFFLINE" -> eventTypeCheckBox.isChecked = false
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

            eventViewModel.media.observe(viewLifecycleOwner) {
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
                            if (edit.text.isEmpty()) {
                                Toast.makeText(context, R.string.empty_event, Toast.LENGTH_SHORT)
                                    .show()
                            } else {
                                eventViewModel.changeContent(
                                    edit.text.toString(),
                                    "${editDate.text} " + "${editTime.text}"
                                )
                                eventViewModel.changeEventType(eventTypeCheckBox.isChecked)
                                eventViewModel.save()
                                AndroidUtils.hideKeyboard(requireView())
                            }
                            true
                        }

                        else -> false
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