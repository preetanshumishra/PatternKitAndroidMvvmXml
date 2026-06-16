package com.preetanshumishra.patternkit.android.mvvmxml.ui.detail

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.ColorUtils
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.preetanshumishra.patternkit.android.mvvmxml.PatternKitApp
import com.preetanshumishra.patternkit.android.mvvmxml.R
import com.preetanshumishra.patternkit.android.mvvmxml.databinding.FragmentDetailBinding
import com.preetanshumishra.patternkit.android.mvvmxml.model.TaskItem
import com.preetanshumishra.patternkit.android.mvvmxml.ui.ARG_TASK_ID
import com.preetanshumishra.patternkit.android.mvvmxml.ui.ViewModelFactory
import com.preetanshumishra.patternkit.android.mvvmxml.ui.list.TaskAdapter
import com.preetanshumishra.patternkit.android.mvvmxml.ui.list.TaskListViewModel
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID

/**
 * Reads its task live from the shared (activity-scoped) list ViewModel by id —
 * the same approach as every other PatternKit module. Observing `uiState`
 * re-renders this screen after a toggle/edit; the lookup uses the unfiltered
 * `task(id)` so a filtered-out task is still shown.
 */
class TaskDetailFragment : Fragment() {

    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!

    private val listViewModel: TaskListViewModel by activityViewModels {
        ViewModelFactory.forList((requireActivity().application as PatternKitApp).component)
    }

    private val taskId: UUID by lazy { UUID.fromString(requireArguments().getString(ARG_TASK_ID)) }
    private val dueFormatter = DateTimeFormatter.ofPattern("EEE, MMM d, yyyy").withZone(ZoneId.systemDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        binding.toolbar.inflateMenu(R.menu.menu_detail)
        binding.toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_edit) {
                findNavController().navigate(
                    R.id.action_detail_to_form,
                    bundleOf(ARG_TASK_ID to taskId.toString())
                )
                true
            } else false
        }

        listViewModel.uiState.observe(viewLifecycleOwner) { render() }
    }

    private fun render() {
        val task = listViewModel.task(taskId)
        if (task == null) {
            binding.contentGroup.visibility = View.GONE
            binding.unavailableView.visibility = View.VISIBLE
            binding.toolbar.menu.clear()
            return
        }
        bind(task)
    }

    private fun bind(task: TaskItem) {
        binding.contentGroup.visibility = View.VISIBLE
        binding.unavailableView.visibility = View.GONE

        binding.titleText.text = task.title
        binding.titleText.paintFlags = if (task.isCompleted) {
            binding.titleText.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            binding.titleText.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }

        val color = TaskAdapter.colorFor(task.priority)
        binding.priorityText.text = task.priority.displayName
        binding.priorityText.setTextColor(color)
        binding.priorityText.setBackgroundColor(ColorUtils.setAlphaComponent(color, 40))

        val notes = task.notes?.takeIf { it.isNotBlank() }
        binding.notesLabel.visibility = if (notes != null) View.VISIBLE else View.GONE
        binding.notesText.visibility = if (notes != null) View.VISIBLE else View.GONE
        binding.notesText.text = notes.orEmpty()

        binding.dueRow.visibility = if (task.dueDate != null) View.VISIBLE else View.GONE
        task.dueDate?.let { binding.dueValue.text = dueFormatter.format(it) }

        binding.statusValue.text = if (task.isCompleted) "Completed" else "Active"

        binding.toggleButton.text =
            if (task.isCompleted) "Mark as active" else "Mark as completed"
        binding.toggleButton.setOnClickListener { listViewModel.toggleCompletion(task) }

        binding.deleteButton.setOnClickListener {
            listViewModel.delete(task)
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
