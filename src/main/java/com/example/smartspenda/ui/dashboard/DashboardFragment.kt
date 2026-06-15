package com.example.smartspenda.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.navigation.fragment.findNavController
import com.example.smartspenda.R
import com.example.smartspenda.adapters.RecentExpenseAdapter
import com.example.smartspenda.data.database.AppDatabase
import com.example.smartspenda.databinding.FragmentDashboardBinding
import com.example.smartspenda.utils.DateHelper
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import kotlinx.coroutines.launch
import java.util.*

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: AppDatabase
    private lateinit var adapter: RecentExpenseAdapter
    private var userId: Long = 0
    private var userName: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = AppDatabase.getInstance(requireContext())
        val prefs = requireActivity().getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
        userId = prefs.getLong("userId", 0)
        userName = prefs.getString("userName", "User") ?: "User"

        setupRecyclerView()
        loadDashboardData()
    }

    private fun setupRecyclerView() {
        adapter = RecentExpenseAdapter { expense ->
            Toast.makeText(requireContext(), "Tap to view details (coming soon)", Toast.LENGTH_SHORT).show()
        }
        binding.rvRecentTransactions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRecentTransactions.adapter = adapter

        binding.tvViewAll.setOnClickListener {
            findNavController().navigate(R.id.historyFragment)
        }
    }

    private fun loadDashboardData() {
        lifecycleScope.launch {
            val (month, year) = DateHelper.getCurrentMonthYear()
            val start = DateHelper.getStartOfMonth(month, year)
            val end = DateHelper.getEndOfMonth(month, year)

            // Get total monthly budget (max budget)
            val totalBudget = db.budgetDao().getTotalMonthlyBudget(userId, month, year)
            val maxBudget = totalBudget?.maxAmount ?: 0.0
            val minGoal = totalBudget?.minAmount ?: 0.0

            // Get all expenses for this month
            val expenses = db.expenseDao().getExpensesBetween(userId, start, end)
            val totalSpent = expenses.sumOf { it.amount }
            val remaining = if (maxBudget > totalSpent) maxBudget - totalSpent else 0.0
            val percentage = if (maxBudget > 0) ((totalSpent / maxBudget) * 100).toInt().coerceIn(0, 100) else 0

            // Update UI
            binding.tvGreeting.text = String.format(Locale.getDefault(), "Good %s, %s", getTimeOfDay(), userName.split(" ").first())
            binding.tvTotalBalance.text = String.format(Locale.getDefault(), "R%.2f", totalSpent)
            binding.tvBudgetUsage.text = String.format(Locale.getDefault(), "%d%%", percentage)
            binding.progressBudget.progress = percentage
            binding.tvRemaining.text = String.format(Locale.getDefault(), "R%.2f remaining", remaining)

            updateGoalChart(totalSpent, maxBudget, minGoal)

            // Generate tip
            val tip = generateTip(totalSpent, maxBudget, percentage, minGoal)
            binding.tvTip.text = tip

            // Load recent transactions (last 5)
            val recent = expenses.take(5)
            adapter.submitList(recent)
        }
    }

    private fun updateGoalChart(totalSpent: Double, maxBudget: Double, minGoal: Double) {
        val entries = listOf(BarEntry(0f, totalSpent.toFloat()))
        val dataSet = BarDataSet(entries, "Current Spending")
        
        val color = when {
            maxBudget > 0 && totalSpent > maxBudget -> android.graphics.Color.RED
            minGoal > 0 && totalSpent < minGoal -> android.graphics.Color.YELLOW
            else -> android.graphics.Color.GREEN
        }
        dataSet.color = color
        dataSet.setDrawValues(false)

        val barData = BarData(dataSet)
        binding.goalPerformanceChart.data = barData

        // Configure axes
        binding.goalPerformanceChart.xAxis.isEnabled = false
        binding.goalPerformanceChart.axisRight.isEnabled = false
        binding.goalPerformanceChart.axisLeft.apply {
            axisMinimum = 0f
            axisMaximum = (maxOf(maxBudget, totalSpent) * 1.2).toFloat()
            setDrawGridLines(false)
            
            removeAllLimitLines()
            if (maxBudget > 0) {
                val maxLine = LimitLine(maxBudget.toFloat(), "Max")
                maxLine.lineColor = android.graphics.Color.RED
                maxLine.lineWidth = 2f
                addLimitLine(maxLine)
            }
            if (minGoal > 0) {
                val minLine = LimitLine(minGoal.toFloat(), "Min")
                minLine.lineColor = android.graphics.Color.BLUE
                minLine.lineWidth = 2f
                addLimitLine(minLine)
            }
        }

        binding.goalPerformanceChart.description.isEnabled = false
        binding.goalPerformanceChart.legend.isEnabled = false
        binding.goalPerformanceChart.setTouchEnabled(false)
        binding.goalPerformanceChart.invalidate()

        val status = when {
            maxBudget == 0.0 -> "Set goals to track performance"
            totalSpent > maxBudget -> "⚠️ Over maximum goal!"
            totalSpent >= minGoal -> "✅ Stayed between goals!"
            else -> "Keep spending to reach minimum goal"
        }
        binding.tvGoalStatus.text = status
    }

    private fun getTimeOfDay(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 0..11 -> "morning"
            in 12..16 -> "afternoon"
            else -> "evening"
        }
    }

    private fun generateTip(totalSpent: Double, maxBudget: Double, percentage: Int, minGoal: Double): String {
        return when {
            maxBudget == 0.0 -> "Set a monthly budget to start tracking your spending goals."
            percentage >= 100 -> "⚠️ You've exceeded your monthly budget! Review your expenses."
            percentage >= 85 -> "Warning: You've used $percentage% of your budget. Consider reducing discretionary spending."
            percentage >= 70 -> "You've spent $percentage% of your budget. You're on track, but stay mindful."
            minGoal > 0 && totalSpent < minGoal -> String.format(Locale.getDefault(), "You haven't reached your minimum spending goal of R%.2f. You need R%.2f more.", minGoal, minGoal - totalSpent)
            minGoal > 0 && totalSpent >= minGoal -> "✓ Minimum spending goal reached! Great job."
            else -> String.format(Locale.getDefault(), "You've spent %d%% of your budget. Keep it up!", percentage)
        }
    }

    override fun onResume() {
        super.onResume()
        loadDashboardData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}