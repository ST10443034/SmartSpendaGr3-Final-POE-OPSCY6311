package com.example.smartspenda.ui.budget

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartspenda.R
import com.example.smartspenda.adapters.CategoryBudgetAdapter
import com.example.smartspenda.data.database.AppDatabase
import com.example.smartspenda.data.entities.Budget
import com.example.smartspenda.databinding.FragmentBudgetBinding
import com.example.smartspenda.utils.DateHelper
import kotlinx.coroutines.launch
import java.util.*

class BudgetFragment : Fragment() {

    private var _binding: FragmentBudgetBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: AppDatabase
    private var userId: Long = 0
    private var currentMonth = 0
    private var currentYear = 0
    private lateinit var categoryBudgetAdapter: CategoryBudgetAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBudgetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = AppDatabase.getInstance(requireContext())
        userId = requireActivity().getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
            .getLong("userId", 0)

        val (month, year) = DateHelper.getCurrentMonthYear()
        currentMonth = month
        currentYear = year

        setupTotalBudget()
        setupCategoryBudgets()
        loadData()
    }

    private fun setupTotalBudget() {
        binding.btnSaveTotalBudget.setOnClickListener {
            val maxStr = binding.etMaxBudget.text.toString().trim()
            val minStr = binding.etMinGoal.text.toString().trim()
            val max = if (maxStr.isNotEmpty()) maxStr.toDoubleOrNull() ?: 0.0 else 0.0
            val min = if (minStr.isNotEmpty()) minStr.toDoubleOrNull() ?: 0.0 else 0.0

            lifecycleScope.launch {
                val existing = db.budgetDao().getTotalMonthlyBudget(userId, currentMonth, currentYear)
                if (existing != null) {
                    val updated = existing.copy(maxAmount = max, minAmount = min)
                    db.budgetDao().updateBudget(updated)
                } else {
                    val newBudget = Budget(
                        userId = userId,
                        month = currentMonth,
                        year = currentYear,
                        categoryId = null,
                        maxAmount = max,
                        minAmount = min
                    )
                    db.budgetDao().insertBudget(newBudget)
                }
                loadTotalBudgetAndProgress()
                Toast.makeText(requireContext(), "Total budget saved", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnAddCategoryBudget.setOnClickListener {
            showAddCategoryBudgetDialog()
        }
    }

    private fun loadTotalBudgetAndProgress() {
        lifecycleScope.launch {
            val totalBudget = db.budgetDao().getTotalMonthlyBudget(userId, currentMonth, currentYear)
            val max = totalBudget?.maxAmount ?: 0.0
            val minGoal = totalBudget?.minAmount ?: 0.0
            binding.etMaxBudget.setText(if (max > 0) max.toInt().toString() else "")
            binding.etMinGoal.setText(if (minGoal > 0) minGoal.toInt().toString() else "")

            val start = DateHelper.getStartOfMonth(currentMonth, currentYear)
            val end = DateHelper.getEndOfMonth(currentMonth, currentYear)
            val expenses = db.expenseDao().getExpensesBetween(userId, start, end)
            val totalSpent = expenses.sumOf { it.amount }

            binding.tvTotalSpent.text = String.format(Locale.getDefault(), "Spent: R%.2f", totalSpent)
            if (max > 0) {
                val percentage = ((totalSpent / max) * 100).toInt().coerceIn(0, 100)
                binding.progressTotalBudget.progress = percentage
            } else {
                binding.progressTotalBudget.progress = 0
            }

            // Show min goal status
            if (minGoal > 0) {
                if (totalSpent >= minGoal) {
                    binding.tvMinGoalStatus.text = String.format(Locale.getDefault(), "✓ Minimum goal reached! (R%.2f)", minGoal)
                    binding.tvMinGoalStatus.setTextColor(requireContext().getColor(R.color.positive_green))
                } else {
                    val remaining = minGoal - totalSpent
                    binding.tvMinGoalStatus.text = String.format(Locale.getDefault(), "Need R%.2f more to reach minimum goal", remaining)
                    binding.tvMinGoalStatus.setTextColor(requireContext().getColor(R.color.warning_orange))
                }
            } else {
                binding.tvMinGoalStatus.text = "No minimum goal set"
                binding.tvMinGoalStatus.setTextColor(requireContext().getColor(R.color.text_secondary))
            }
        }
    }

    private fun setupCategoryBudgets() {
        categoryBudgetAdapter = CategoryBudgetAdapter(
            categories = emptyList(),
            budgets = emptyMap(),
            spentMap = emptyMap()
        ) { category, existingBudget ->
            showEditCategoryBudgetDialog(category, existingBudget)
        }
        binding.rvCategoryBudgets.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCategoryBudgets.adapter = categoryBudgetAdapter
    }

    private fun loadData() {
        lifecycleScope.launch {
            val categories = db.categoryDao().getCategoriesForUser(userId)
            val allBudgets = db.budgetDao().getAllBudgetsForMonth(userId, currentMonth, currentYear)
            val budgetsMap = allBudgets.filter { it.categoryId != null }.associateBy { it.categoryId!! }

            val start = DateHelper.getStartOfMonth(currentMonth, currentYear)
            val end = DateHelper.getEndOfMonth(currentMonth, currentYear)
            val expenses = db.expenseDao().getExpensesBetween(userId, start, end)
            val spentMap = expenses.groupBy { it.categoryId }.mapValues { it.value.sumOf { exp -> exp.amount } }

            categoryBudgetAdapter = CategoryBudgetAdapter(categories, budgetsMap, spentMap) { category, budget ->
                showEditCategoryBudgetDialog(category, budget)
            }
            binding.rvCategoryBudgets.adapter = categoryBudgetAdapter

            loadTotalBudgetAndProgress()
        }
    }

    private fun showAddCategoryBudgetDialog() {
        lifecycleScope.launch {
            val categories = db.categoryDao().getCategoriesForUser(userId)
            val categoryNames = categories.map { it.name }.toTypedArray()
            AlertDialog.Builder(requireContext())
                .setTitle("Select Category")
                .setItems(categoryNames) { _, which ->
                    val selectedCategory = categories[which]
                    showEditCategoryBudgetDialog(selectedCategory, null)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun showEditCategoryBudgetDialog(category: com.example.smartspenda.data.entities.Category, existingBudget: Budget?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_category_budget, null)
        val etMax = dialogView.findViewById<EditText>(R.id.etMaxBudget)
        val etMin = dialogView.findViewById<EditText>(R.id.etMinGoal)

        if (existingBudget != null) {
            etMax.setText(if (existingBudget.maxAmount > 0) existingBudget.maxAmount.toInt().toString() else "")
            etMin.setText(if (existingBudget.minAmount > 0) existingBudget.minAmount.toInt().toString() else "")
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Budget for ${category.name}")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val maxStr = etMax.text.toString().trim()
                val minStr = etMin.text.toString().trim()
                val max = if (maxStr.isNotEmpty()) maxStr.toDoubleOrNull() ?: 0.0 else 0.0
                val min = if (minStr.isNotEmpty()) minStr.toDoubleOrNull() ?: 0.0 else 0.0

                lifecycleScope.launch {
                    if (existingBudget != null) {
                        val updated = existingBudget.copy(maxAmount = max, minAmount = min)
                        db.budgetDao().updateBudget(updated)
                    } else {
                        val newBudget = Budget(
                            userId = userId,
                            month = currentMonth,
                            year = currentYear,
                            categoryId = category.categoryId,
                            maxAmount = max,
                            minAmount = min
                        )
                        db.budgetDao().insertBudget(newBudget)
                    }
                    loadData()
                    Toast.makeText(requireContext(), "Budget saved for ${category.name}", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}