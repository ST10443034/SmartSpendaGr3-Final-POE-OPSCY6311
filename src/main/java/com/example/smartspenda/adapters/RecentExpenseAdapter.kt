package com.example.smartspenda.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.smartspenda.data.entities.Expense
import com.example.smartspenda.databinding.ItemRecentExpenseBinding
import com.example.smartspenda.utils.DateHelper
import java.util.*

class RecentExpenseAdapter(
    private val onItemClick: (Expense) -> Unit
) : RecyclerView.Adapter<RecentExpenseAdapter.ViewHolder>() {

    private var expenses = listOf<Expense>()

    fun submitList(list: List<Expense>) {
        expenses = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecentExpenseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val expense = expenses[position]
        holder.bind(expense)
        holder.itemView.setOnClickListener { onItemClick(expense) }
    }

    override fun getItemCount() = expenses.size

    inner class ViewHolder(private val binding: ItemRecentExpenseBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(expense: Expense) {
            binding.tvDescription.text = expense.description.ifEmpty { "Expense" }
            binding.tvAmount.text = String.format(Locale.getDefault(), "R%.2f", expense.amount)
            binding.tvDate.text = DateHelper.timestampToString(expense.date)
        }
    }
}