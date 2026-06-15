package com.example.smartspenda.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.smartspenda.data.entities.Category
import com.example.smartspenda.databinding.ItemCategoryTotalBinding
import java.util.*

class CategoryTotalAdapter(
    private val categoryTotals: Map<Category, Double>
) : RecyclerView.Adapter<CategoryTotalAdapter.ViewHolder>() {

    private val entries = categoryTotals.entries.toList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCategoryTotalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (category, total) = entries[position]
        holder.binding.tvCategoryName.text = category.name
        holder.binding.tvCategoryTotal.text = String.format(Locale.getDefault(), "R%.2f", total)
    }

    override fun getItemCount() = entries.size

    inner class ViewHolder(val binding: ItemCategoryTotalBinding) : RecyclerView.ViewHolder(binding.root)
}