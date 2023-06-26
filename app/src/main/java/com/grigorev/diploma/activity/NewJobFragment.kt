package com.grigorev.diploma.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.grigorev.diploma.R
import com.grigorev.diploma.databinding.FragmentNewJobBinding
import com.grigorev.diploma.dto.Job
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

        val name = arguments?.getString("name")
        val positionArg = arguments?.getString("link")
        val startArg = arguments?.getString("start")
        val finishArg = arguments?.getString("finish")
        val linkArg = arguments?.getString("link")

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

            cancelEditJob.setOnClickListener {
                findNavController().navigateUp()
            }

            saveJob.setOnClickListener {
                val company = jobTitle.text.toString().trim()
                val position = jobPosition.text.toString().trim()
                val dateStart = editStart.text.toString().trim()
                val dateEnd = editEnd.text.toString().trim()
                val link = editLink.text.toString().trim()

                if (company.isEmpty() || position.isEmpty() || dateStart.isEmpty() || link.isEmpty()) {
                    Toast.makeText(it.context, R.string.error_blank_fields, Toast.LENGTH_SHORT)
                        .show()
                } else {
                    viewModel.changeJobContent(company, position, dateStart, dateEnd, link)
                    viewModel.saveJob(
                        Job(
                            name = company,
                            position = position,
                            start = dateStart,
                            finish = dateEnd,
                            link = link
                        )
                    )
                }
                findNavController().navigate(R.id.action_newJobFragment_to_userProfileFragment)
            }
        }

        return binding.root
    }
}