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
import androidx.navigation.fragment.findNavController
import com.grigorev.diploma.R
import com.grigorev.diploma.databinding.FragmentNewJobBinding
import com.grigorev.diploma.util.AndroidUtils
import com.grigorev.diploma.util.formatJobDate
import com.grigorev.diploma.util.pickDate
import com.grigorev.diploma.viewmodels.ProfileViewModel
import com.grigorev.diploma.viewmodels.emptyJob
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NewJobFragment : Fragment() {

    private val viewModel: ProfileViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentNewJobBinding.inflate(inflater, container, false)

        val name = viewModel.editedJob.value?.name ?: ""
        val positionArg = viewModel.editedJob.value?.position ?: ""

        val startDateUnprocessed = viewModel.editedJob.value?.start
        val startArg = if (startDateUnprocessed.isNullOrBlank()) "" else {
            formatJobDate(startDateUnprocessed)
        }

        val finishDateUnprocessed = viewModel.editedJob.value?.finish
        val finishArg = if (finishDateUnprocessed.isNullOrBlank()) "" else {
            formatJobDate(finishDateUnprocessed)
        }

        val linkArg = viewModel.editedJob.value?.link ?: ""

        binding.apply {

            if (viewModel.editedJob.value != emptyJob) {
                jobTitle.setText(name)
                jobPosition.setText(positionArg)
                editStart.setText(startArg)
                editEnd.setText(finishArg)
                editLink.setText(linkArg)
            }

            editStart.setOnClickListener {
                context?.let { item ->
                    pickDate(editStart, item)
                }
            }

            editEnd.setOnClickListener {
                context?.let { item ->
                    pickDate(editEnd, item)
                }
            }

            activity?.addMenuProvider(object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.menu_new_post_event_job, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    return when (menuItem.itemId) {
                        R.id.save -> {
                            val company = jobTitle.text.toString().trim()
                            val position = jobPosition.text.toString().trim()
                            val dateStart = editStart.text.toString().trim()
                            val dateEndUnprocessed = editEnd.text.toString().trim()
                            val dateEnd = dateEndUnprocessed.ifBlank { "" }
                            val link = editLink.text.toString().trim()

                            if (company.isEmpty() || position.isEmpty() || dateStart.isEmpty()) {
                                Toast.makeText(context, R.string.error_blank_fields, Toast.LENGTH_SHORT)
                                    .show()
                                return false
                            } else {
                                viewModel.changeJobContent(
                                    company,
                                    position,
                                    dateStart,
                                    dateEnd,
                                    link
                                )
                                viewModel.saveJob()
                                AndroidUtils.hideKeyboard(requireView())
                            }
                            findNavController().navigate(R.id.action_newJobFragment_to_userProfileFragment)
                            true
                        }

                        R.id.cancel -> {
                            viewModel.clearEditedJob()
                            findNavController().navigateUp()
                            true
                        }

                        else -> {
                            viewModel.clearEditedJob()
                            false
                        }
                    }
                }
            }, viewLifecycleOwner)
        }

        return binding.root
    }
}