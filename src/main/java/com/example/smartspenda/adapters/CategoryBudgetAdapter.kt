package com.example.smartspenda.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.smartspenda.data.entities.Budget
import com.example.smartspenda.data.entities.Category
import com.example.smartspenda.databinding.ItemCategoryBudgetBinding
import java.util.*

class CategoryBudgetAdapter(
    private val categories: List<Category>,
    private val budgets: Map<Long, Budget>,  // categoryId -> Budget
    private val spentMap: Map<Long, Double>, // categoryId -> spent amount this month
    private val onEdit: (Category, Budget?) -> Unit
) : RecyclerView.Adapter<CategoryBudgetAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCategoryBudgetBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]
        val budget = budgets[category.categoryId]
        val spent = spentMap[category.categoryId] ?: 0.0

        holder.binding.tvCategoryName.text = category.name
        holder.binding.etMaxBudget.setText(budget?.maxAmount?.toInt()?.toString() ?: "")
        holder.binding.etMinGoal.setText(budget?.minAmount?.toInt()?.toString() ?: "")

        if (budget != null && budget.maxAmount > 0) {
            val percentage = ((spent / budget.maxAmount) * 100).toInt().coerceIn(0, 100)
            holder.binding.progressCategoryBudget.progress = percentage
            holder.binding.tvSpentInfo.text = String.format(Locale.getDefault(), "Spent: R%.2f / R%.2f", spent, budget.maxAmount)
        } else {
            holder.binding.progressCategoryBudget.progress = 0
            holder.binding.tvSpentInfo.text = String.format(Locale.getDefault(), "Spent: R%.2f (no budget set)", spent)
        }

        holder.binding.btnEditCategoryBudget.setOnClickListener {
            onEdit(category, budget)
        }
    }

    override fun getItemCount() = categories.size

    inner class ViewHolder(val binding: ItemCategoryBudgetBinding) : RecyclerView.ViewHolder(binding.root)
}