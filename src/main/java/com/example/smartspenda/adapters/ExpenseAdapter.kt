package com.example.smartspenda.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.smartspenda.R
import com.example.smartspenda.data.entities.Expense
import com.example.smartspenda.databinding.ItemExpenseBinding
import com.example.smartspenda.utils.DateHelper
import java.util.*

class ExpenseAdapter(
    private val onItemClick: (Expense) -> Unit
) : ListAdapter<Expense, ExpenseAdapter.ViewHolder>(ExpenseDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemExpenseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemExpenseBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(expense: Expense) {
            binding.tvDescription.text = expense.description.ifEmpty { "Expense" }
            binding.tvAmount.text = String.format(Locale.getDefault(), "R%.2f", expense.amount)
            
            val dateStr = DateHelper.timestampToString(expense.date)
            val timeStr = if (expense.startTime != null && expense.endTime != null) {
                " | ${DateHelper.timeToString(expense.startTime)} - ${DateHelper.timeToString(expense.endTime)}"
            } else ""
            binding.tvDate.text = "$dateStr$timeStr"

            if (!expense.receiptPath.isNullOrEmpty()) {
                Glide.with(binding.ivReceiptThumb.context)
                    .load(expense.receiptPath)
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(binding.ivReceiptThumb)
            } else {
                binding.ivReceiptThumb.setImageResource(R.drawable.ic_launcher_background)
            }

            binding.root.setOnClickListener { onItemClick(expense) }
        }
    }

    class ExpenseDiffCallback : DiffUtil.ItemCallback<Expense>() {
        override fun areItemsTheSame(oldItem: Expense, newItem: Expense): Boolean = oldItem.expenseId == newItem.expenseId
        override fun areContentsTheSame(oldItem: Expense, newItem: Expense): Boolean = oldItem == newItem
    }
}