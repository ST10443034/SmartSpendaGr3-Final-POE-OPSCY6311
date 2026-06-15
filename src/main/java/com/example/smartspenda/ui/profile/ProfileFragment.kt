package com.example.smartspenda.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.smartspenda.R
import com.example.smartspenda.data.database.AppDatabase
import com.example.smartspenda.databinding.FragmentProfileBinding
import com.example.smartspenda.ui.auth.LoginActivity
import com.example.smartspenda.ui.categories.ManageCategoriesActivity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: AppDatabase
    private var userId: Long = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = AppDatabase.getInstance(requireContext())
        val prefs = requireActivity().getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
        userId = prefs.getLong("userId", 0)

        loadUserInfo()
        setupClickListeners()
        setupSwitches()
    }

    private fun loadUserInfo() {
        lifecycleScope.launch {
            val email = requireActivity().getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
                .getString("userEmail", "") ?: ""
            val user = db.userDao().getUserByEmail(email)
            if (user != null) {
                binding.tvUserName.text = user.fullName
                binding.tvUserEmail.text = user.email
                val joinDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(user.joinDate))
                binding.tvJoinDate.text = "Joined: $joinDate"

                // Simple gamification: points based on number of expenses (mock)
                val expenses = db.expenseDao().getExpensesBetween(userId, 0, System.currentTimeMillis())
                val count = expenses.size
                val tier = when {
                    count >= 50 -> "Platinum Spender"
                    count >= 20 -> "Gold Saver"
                    count >= 5 -> "Silver Budgeter"
                    else -> "Bronze Starter"
                }
                binding.tvTier.text = "Tier: $tier"

                updateBadges(userId)
            }
        }
    }

    private suspend fun updateBadges(userId: Long) {
        val (month, year) = com.example.smartspenda.utils.DateHelper.getCurrentMonthYear()
        val expenses = db.expenseDao().getExpensesBetween(userId, 0, System.currentTimeMillis())
        val budget = db.budgetDao().getTotalMonthlyBudget(userId, month, year)

        // 1. Consistent Logger (e.g., at least 5 expenses)
        if (expenses.size >= 5) {
            binding.badgeConsistentLogger.alpha = 1.0f
        }

        // 2. Budget Master (met min goal and stayed under max)
        if (budget != null && budget.maxAmount > 0) {
            val start = com.example.smartspenda.utils.DateHelper.getStartOfMonth(month, year)
            val end = com.example.smartspenda.utils.DateHelper.getEndOfMonth(month, year)
            val monthlyExpenses = db.expenseDao().getExpensesBetween(userId, start, end)
            val totalSpent = monthlyExpenses.sumOf { it.amount }
            
            if (totalSpent <= budget.maxAmount && (budget.minAmount == 0.0 || totalSpent >= budget.minAmount)) {
                binding.badgeBudgetMaster.alpha = 1.0f
            }
        }

        // 3. Smart Spender (Spent less than 70% of budget if budget set)
        if (budget != null && budget.maxAmount > 0) {
            val start = com.example.smartspenda.utils.DateHelper.getStartOfMonth(month, year)
            val end = com.example.smartspenda.utils.DateHelper.getEndOfMonth(month, year)
            val monthlyExpenses = db.expenseDao().getExpensesBetween(userId, start, end)
            val totalSpent = monthlyExpenses.sumOf { it.amount }
            
            if (totalSpent < budget.maxAmount * 0.7) {
                binding.badgeSmartSpender.alpha = 1.0f
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnManageCategories.setOnClickListener {
            startActivity(Intent(requireContext(), ManageCategoriesActivity::class.java))
        }

        binding.btnCategorySummary.setOnClickListener {
            findNavController().navigate(R.id.categorySummaryFragment)
        }

        binding.btnHelpCenter.setOnClickListener {
            Toast.makeText(requireContext(), "Help: Contact support@smartspenda.com", Toast.LENGTH_LONG).show()
        }

        binding.btnLogout.setOnClickListener {
            requireActivity().getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE).edit().clear().apply()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }
        
        // Ensure Category Summary also reachable if needed, 
        // but the prompt says to use the new professional layout which might have replaced it.
        // Actually, the user previously asked for Category Summary in Profile.
        // I'll add a button for it if it's missing in the new layout.
        // The new layout has "ACCOUNT & SECURITY" and "PREFERENCES". 
        // I'll add "Category Summary" as a button under ACCOUNT & SECURITY.
    }

    private fun setupSwitches() {
        binding.switchFaceId.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(requireContext(), if (isChecked) "Face ID enabled (demo)" else "Face ID disabled", Toast.LENGTH_SHORT).show()
        }

        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(requireContext(), if (isChecked) "Notifications enabled" else "Notifications disabled", Toast.LENGTH_SHORT).show()
        }

        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadUserInfo()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}