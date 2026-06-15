package com.example.smartspenda.ui.summary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartspenda.R
import com.example.smartspenda.adapters.CategoryTotalAdapter
import com.example.smartspenda.data.database.AppDatabase
import com.example.smartspenda.databinding.FragmentCategorySummaryBinding
import com.example.smartspenda.utils.DateHelper
import com.google.android.material.datepicker.MaterialDatePicker
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.launch
import java.util.*

class CategorySummaryFragment : Fragment() {

    private var _binding: FragmentCategorySummaryBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: AppDatabase
    private var userId: Long = 0
    private var currentStartDate = 0L
    private var currentEndDate = 0L

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCategorySummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = AppDatabase.getInstance(requireContext())
        userId = requireActivity().getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
            .getLong("userId", 0)

        setupPeriodSpinner()
        setupRecyclerView()

        val (start, end) = DateHelper.getMonthRange()
        currentStartDate = start
        currentEndDate = end
        loadSummary()
    }

    private fun setupPeriodSpinner() {
        val spinner = binding.spinnerPeriodSummary
        val adapter = ArrayAdapter.createFromResource(requireContext(), R.array.period_options, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val (start, end) = when (position) {
                    0 -> DateHelper.getTodayRange()
                    1 -> DateHelper.getWeekRange()
                    else -> DateHelper.getMonthRange()
                }
                currentStartDate = start
                currentEndDate = end
                loadSummary()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.btnCustomRangeSummary.setOnClickListener {
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
            loadSummary()
        }
        dateRangePicker.show(parentFragmentManager, "summary_date_picker")
    }

    private fun setupRecyclerView() {
        binding.rvCategoryTotals.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun loadSummary() {
        lifecycleScope.launch {
            val categories = db.categoryDao().getCategoriesForUser(userId)
            val expenses = db.expenseDao().getExpensesBetween(userId, currentStartDate, currentEndDate)
            val totalSpentAll = expenses.sumOf { it.amount }
            binding.tvTotalSpent.text = String.format(Locale.getDefault(), "Total spent: R%.2f", totalSpentAll)

            val categoryTotals = mutableMapOf<com.example.smartspenda.data.entities.Category, Double>()
            categories.forEach { category ->
                val spent = expenses.filter { it.categoryId == category.categoryId }.sumOf { it.amount }
                if (spent > 0) {
                    categoryTotals[category] = spent
                }
            }
            val adapter = CategoryTotalAdapter(categoryTotals)
            binding.rvCategoryTotals.adapter = adapter

            updateChart(categoryTotals)
        }
    }

    private fun updateChart(categoryTotals: Map<com.example.smartspenda.data.entities.Category, Double>) {
        val entries = mutableListOf<BarEntry>()
        val labels = mutableListOf<String>()

        categoryTotals.entries.forEachIndexed { index, entry ->
            entries.add(BarEntry(index.toFloat(), entry.value.toFloat()))
            labels.add(entry.key.name)
        }

        val dataSet = BarDataSet(entries, "Spending per Category")
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
        dataSet.valueTextSize = 12f

        val barData = BarData(dataSet)
        binding.categoryBarChart.data = barData

        // Customize Chart
        binding.categoryBarChart.description.isEnabled = false
        binding.categoryBarChart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(labels)
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            setDrawGridLines(false)
        }

        // Add Min/Max Goal Lines (if total budget exists for current month)
        lifecycleScope.launch {
            val (month, year) = DateHelper.getCurrentMonthYear()
            val totalBudget = db.budgetDao().getTotalMonthlyBudget(userId, month, year)
            
            binding.categoryBarChart.axisLeft.removeAllLimitLines()
            if (totalBudget != null) {
                if (totalBudget.maxAmount > 0) {
                    val maxLine = LimitLine(totalBudget.maxAmount.toFloat(), "Max Goal")
                    maxLine.lineWidth = 2f
                    maxLine.lineColor = android.graphics.Color.RED
                    maxLine.textColor = android.graphics.Color.RED
                    maxLine.textSize = 10f
                    binding.categoryBarChart.axisLeft.addLimitLine(maxLine)
                }
                if (totalBudget.minAmount > 0) {
                    val minLine = LimitLine(totalBudget.minAmount.toFloat(), "Min Goal")
                    minLine.lineWidth = 2f
                    minLine.lineColor = android.graphics.Color.GREEN
                    minLine.textColor = android.graphics.Color.GREEN
                    minLine.textSize = 10f
                    binding.categoryBarChart.axisLeft.addLimitLine(minLine)
                }
            }
            binding.categoryBarChart.invalidate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}