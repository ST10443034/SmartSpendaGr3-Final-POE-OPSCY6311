package com.example.smartspenda.ui.categories

import android.app.AlertDialog
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartspenda.adapters.CategoryAdapter
import com.example.smartspenda.data.database.AppDatabase
import com.example.smartspenda.data.entities.Category
import com.example.smartspenda.databinding.ActivityManageCategoriesBinding
import kotlinx.coroutines.launch

class ManageCategoriesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManageCategoriesBinding
    private lateinit var db: AppDatabase
    private lateinit var adapter: CategoryAdapter
    private var userId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageCategoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getInstance(this)
        userId = getSharedPreferences("app_prefs", MODE_PRIVATE).getLong("userId", 0)

        setupRecyclerView()
        loadCategories()

        binding.btnAddCategory.setOnClickListener {
            val name = binding.etNewCategory.text.toString().trim()
            if (name.isNotEmpty()) {
                lifecycleScope.launch {
                    db.categoryDao().insertCategory(Category(userId = userId, name = name))
                    loadCategories()
                    binding.etNewCategory.text?.clear()
                }
            } else {
                Toast.makeText(this, "Enter category name", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = CategoryAdapter(
            onEdit = { category -> showEditDialog(category) },
            onDelete = { category ->
                AlertDialog.Builder(this)
                    .setTitle("Delete Category")
                    .setMessage("Delete '${category.name}'? This will not delete existing expenses, but they will have no category.")
                    .setPositiveButton("Delete") { _, _ ->
                        lifecycleScope.launch {
                            db.categoryDao().deleteCategory(category)
                            loadCategories()
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )
        binding.rvCategories.layoutManager = LinearLayoutManager(this)
        binding.rvCategories.adapter = adapter
    }

    private fun loadCategories() {
        lifecycleScope.launch {
            val categories = db.categoryDao().getCategoriesForUser(userId)
            adapter.submitList(categories)
        }
    }

    private fun showEditDialog(category: Category) {
        val input = EditText(this)
        input.setText(category.name)
        AlertDialog.Builder(this)
            .setTitle("Edit Category")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val newName = input.text.toString().trim()
                if (newName.isNotEmpty()) {
                    lifecycleScope.launch {
                        val updated = category.copy(name = newName)
                        db.categoryDao().updateCategory(updated)
                        loadCategories()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}