package com.example.smartspenda.ui.history

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartspenda.R
import com.example.smartspenda.adapters.ExpenseAdapter
import com.example.smartspenda.data.database.AppDatabase
import com.example.smartspenda.databinding.FragmentHistoryBinding
import com.example.smartspenda.ui.add_expense.AddExpenseActivity
import com.example.smartspenda.utils.DateHelper
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.launch
import java.util.*

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: AppDatabase
    private lateinit var adapter: ExpenseAdapter
    private var userId: Long = 0
    private var currentStartDate = 0L
    private var currentEndDate = 0L

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = AppDatabase.getInstance(requireContext())
        userId = requireActivity().getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
            .getLong("userId", 0)

        setupRecyclerView()
        setupPeriodSpinner()
        setupSearch()
        setupTabs()
        setupFAB()

        // Load default: This Month
        val (start, end) = DateHelper.getMonthRange()
        currentStartDate = start
        currentEndDate = end
        loadExpenses()
    }

    private fun setupRecyclerView() {
        adapter = ExpenseAdapter { expense ->
            if (!expense.receiptPath.isNullOrEmpty()) {
                showReceiptDialog(expense.receiptPath)
            } else {
                Toast.makeText(requireContext(), "No receipt attached", Toast.LENGTH_SHORT).show()
            }
        }
        binding.rvExpenses.layoutManager = LinearLayoutManager(requireContext())
        binding.rvExpenses.adapter = adapter
    }

    private fun setupPeriodSpinner() {
        val spinner = binding.spinnerPeriod
        val adapterArray = ArrayAdapter.createFromResource(requireContext(), R.array.period_options, android.R.layout.simple_spinner_item)
        adapterArray.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapterArray

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val (start, end) = when (position) {
                    0 -> DateHelper.getTodayRange()
                    1 -> DateHelper.getWeekRange()
                    else -> DateHelper.getMonthRange()
                }
                currentStartDate = start
                currentEndDate = end
                loadExpenses()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.btnCustomRange.setOnClickListener {
            showCustomDateRangePicker()
        }
    }

    private fun showCustomDateRangePicker() {
        val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Select Date Range")
            .build()

        dateRangePicker.addOnPositiveButtonClickListener { selection ->
            currentStartDate = selection.first ?: System.currentTimeMillis()
            currentEndDate = selection.second ?: System.currentTimeMillis()
            loadExpenses()
        }

        dateRangePicker.show(parentFragmentManager, "date_range_picker")
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                filterExpenses(newText)
                return true
            }
        })
    }

    private fun setupTabs() {
        val tabs = listOf("All", "Spending", "Income", "Subscription")
        val tabLayout = binding.tabLayout
        tabs.forEach { tabLayout.addTab(tabLayout.newTab().setText(it)) }
        tabLayout.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                loadExpenses()
            }
            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
        })
    }

    private fun setupFAB() {
        binding.fabAddExpense.setOnClickListener {
            startActivity(Intent(requireContext(), AddExpenseActivity::class.java))
        }
    }

    private fun loadExpenses() {
        lifecycleScope.launch {
            var expenses = db.expenseDao().getExpensesBetween(userId, currentStartDate, currentEndDate)
            val selectedTab = binding.tabLayout.selectedTabPosition
            when (selectedTab) {
                1 -> expenses = expenses.filter { it.amount > 0 }
                2 -> expenses = emptyList()
                3 -> expenses = emptyList()
            }
            adapter.submitList(expenses)
        }
    }

    private fun filterExpenses(query: String?) {
        lifecycleScope.launch {
            val expenses = db.expenseDao().getExpensesBetween(userId, currentStartDate, currentEndDate)
            val filtered = if (!query.isNullOrEmpty()) {
                expenses.filter {
                    it.description.contains(query, ignoreCase = true)
                }
            } else {
                expenses
            }
            adapter.submitList(filtered)
        }
    }

    private fun showReceiptDialog(receiptPath: String) {
        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setTitle("Receipt")
            .setView(android.widget.ImageView(requireContext()).apply {
                setImageURI(android.net.Uri.fromFile(java.io.File(receiptPath)))
                scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
            })
            .setPositiveButton("Close", null)
            .create()
        dialog.show()
    }

    override fun onResume() {
        super.onResume()
        loadExpenses()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}