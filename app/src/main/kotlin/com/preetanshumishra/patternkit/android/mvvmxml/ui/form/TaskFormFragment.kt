package com.preetanshumishra.patternkit.android.mvvmxml.ui.form

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.preetanshumishra.patternkit.android.mvvmxml.PatternKitApp
import com.preetanshumishra.patternkit.android.mvvmxml.R
import com.preetanshumishra.patternkit.android.mvvmxml.databinding.FragmentFormBinding
import com.preetanshumishra.patternkit.android.mvvmxml.model.Priority
import com.preetanshumishra.patternkit.android.mvvmxml.ui.ARG_TASK_ID
import com.preetanshumishra.patternkit.android.mvvmxml.ui.ViewModelFactory
import com.preetanshumishra.patternkit.android.mvvmxml.ui.list.TaskListViewModel
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID

class TaskFormFragment : Fragment() {

    private var _binding: FragmentFormBinding? = null
    private val binding get() = _binding!!

    private val listViewModel: TaskListViewModel by activityViewModels {
        ViewModelFactory.forList((requireActivity().application as PatternKitApp).component)
    }

    private lateinit var viewModel: TaskFormViewModel
    private val dueFormatter = DateTimeFormatter.ofPattern("EEE, MMM d, yyyy").withZone(ZoneId.systemDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Resolve create-vs-edit from the nav arg, looking the task up in the
        // shared list ViewModel for edit mode.
        val component = (requireActivity().application as PatternKitApp).component
        val editTask = arguments?.getString(ARG_TASK_ID)
            ?.let { runCatching { UUID.fromString(it) }.getOrNull() }
            ?.let { listViewModel.task(it) }
        val mode = editTask?.let { TaskFormMode.Edit(it) } ?: TaskFormMode.Create

        viewModel = ViewModelProvider(this, ViewModelFactory.forForm(component, mode))[TaskFormViewModel::class.java]

        binding.toolbar.title = viewModel.screenTitle
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        seedFields()
        wireInputs()
        observe()
    }

    /** Seed the editable fields once, so the uiState observer doesn't fight the
     *  text watchers. */
    private fun seedFields() {
        val state = viewModel.uiState.value ?: TaskFormUiState()
        binding.titleInput.setText(state.title)
        binding.notesInput.setText(state.notes)
        binding.dueDateSwitch.isChecked = state.hasDueDate
        when (state.priority) {
            Priority.LOW    -> binding.priorityGroup.check(R.id.btnLow)
            Priority.MEDIUM -> binding.priorityGroup.check(R.id.btnMedium)
            Priority.HIGH   -> binding.priorityGroup.check(R.id.btnHigh)
        }
    }

    private fun wireInputs() {
        binding.titleInput.doAfterTextChanged { viewModel.setTitle(it?.toString().orEmpty()) }
        binding.notesInput.doAfterTextChanged { viewModel.setNotes(it?.toString().orEmpty()) }
        binding.dueDateSwitch.setOnCheckedChangeListener { _, isChecked -> viewModel.setHasDueDate(isChecked) }
        binding.priorityGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            viewModel.setPriority(
                when (checkedId) {
                    R.id.btnLow -> Priority.LOW
                    R.id.btnHigh -> Priority.HIGH
                    else -> Priority.MEDIUM
                }
            )
        }
        binding.cancelButton.setOnClickListener { findNavController().navigateUp() }
        binding.saveButton.setOnClickListener { viewModel.save() }
    }

    private fun observe() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            binding.saveButton.isEnabled = state.isValid && !state.isSaving
            binding.saveButton.text = if (state.isSaving) "Saving…" else "Save"

            binding.dueValue.visibility = if (state.hasDueDate) View.VISIBLE else View.GONE
            if (state.hasDueDate) binding.dueValue.text = "Due: " + dueFormatter.format(state.dueDate)

            state.errorMessage?.let { showError(it) }
        }

        viewModel.saved.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { saved ->
                listViewModel.apply(saved)
                findNavController().navigateUp()
            }
        }
    }

    private fun showError(message: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Couldn't save")
            .setMessage(message)
            .setPositiveButton("OK") { _, _ -> viewModel.clearError() }
            .setOnDismissListener { viewModel.clearError() }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
