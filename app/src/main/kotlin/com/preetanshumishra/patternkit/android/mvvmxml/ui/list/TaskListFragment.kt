package com.preetanshumishra.patternkit.android.mvvmxml.ui.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.preetanshumishra.patternkit.android.mvvmxml.PatternKitApp
import com.preetanshumishra.patternkit.android.mvvmxml.R
import com.preetanshumishra.patternkit.android.mvvmxml.databinding.FragmentListBinding
import com.preetanshumishra.patternkit.android.mvvmxml.model.TaskFilter
import com.preetanshumishra.patternkit.android.mvvmxml.model.TaskSort
import com.preetanshumishra.patternkit.android.mvvmxml.ui.ARG_TASK_ID
import com.preetanshumishra.patternkit.android.mvvmxml.ui.ViewModelFactory

class TaskListFragment : Fragment() {

    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TaskListViewModel by activityViewModels {
        ViewModelFactory.forList((requireActivity().application as PatternKitApp).component)
    }

    private lateinit var adapter: TaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar()
        setUpList()
        setUpFilters()
        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_list_to_form)
        }
        observeState()
        viewModel.load()
    }

    private fun setUpToolbar() {
        binding.toolbar.inflateMenu(R.menu.menu_list)
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_sort_due -> { viewModel.setSort(TaskSort.DUE_DATE); true }
                R.id.action_sort_priority -> { viewModel.setSort(TaskSort.PRIORITY); true }
                else -> false
            }
        }
    }

    private fun setUpList() {
        adapter = TaskAdapter(
            onClick = { task ->
                findNavController().navigate(
                    R.id.action_list_to_detail,
                    bundleOf(ARG_TASK_ID to task.id.toString())
                )
            },
            onToggle = { task -> viewModel.toggleCompletion(task) }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        val swipeToDelete = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder) = false
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val task = adapter.currentList.getOrNull(viewHolder.bindingAdapterPosition) ?: return
                viewModel.delete(task)
            }
        }
        ItemTouchHelper(swipeToDelete).attachToRecyclerView(binding.recyclerView)
    }

    private fun setUpFilters() {
        binding.filterChips.setOnCheckedStateChangeListener { _, checkedIds ->
            val filter = when (checkedIds.firstOrNull()) {
                R.id.chipActive -> TaskFilter.ACTIVE
                R.id.chipCompleted -> TaskFilter.COMPLETED
                else -> TaskFilter.ALL
            }
            viewModel.setFilter(filter)
        }
    }

    private fun observeState() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            adapter.submitList(state.displayedTasks)

            val showLoading = state.isLoading && state.displayedTasks.isEmpty()
            binding.progressBar.visibility = if (showLoading) View.VISIBLE else View.GONE
            binding.emptyView.visibility =
                if (!state.isLoading && state.displayedTasks.isEmpty()) View.VISIBLE else View.GONE

            state.errorMessage?.let { showError(it) }
        }
    }

    private fun showError(message: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Something went wrong")
            .setMessage(message)
            .setPositiveButton("OK") { _, _ -> viewModel.clearError() }
            .setOnDismissListener { viewModel.clearError() }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerView.adapter = null
        _binding = null
    }
}
