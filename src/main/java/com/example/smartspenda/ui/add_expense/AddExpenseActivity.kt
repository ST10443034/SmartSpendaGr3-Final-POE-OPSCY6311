package com.example.smartspenda.ui.add_expense

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.example.smartspenda.R
import com.example.smartspenda.data.database.AppDatabase
import com.example.smartspenda.data.entities.Category
import com.example.smartspenda.data.entities.Expense
import com.example.smartspenda.databinding.ActivityAddExpenseBinding
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class AddExpenseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddExpenseBinding
    private lateinit var db: AppDatabase
    private var userId: Long = 0
    private var categories: List<Category> = emptyList()
    private var selectedCategoryId: Long = -1
    private var currentPhotoPath: String? = null
    private var photoUri: Uri? = null

    // Permission launcher
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) openCamera() else showPermissionDeniedDialog()
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            photoUri?.let { savePhotoToInternalStorage(it) }
        }
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri = result.data?.data
            imageUri?.let { savePhotoToInternalStorage(it) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getInstance(this)
        userId = getSharedPreferences("app_prefs", MODE_PRIVATE).getLong("userId", 0)

        setupDatePicker()
        setupTimePickers()
        loadCategories()
        setupPhotoButtons()

        binding.btnSaveExpense.setOnClickListener { saveExpense() }
    }

    private fun setupDatePicker() {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        binding.etDate.setText(dateFormat.format(calendar.time))

        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            binding.etDate.setText(dateFormat.format(calendar.time))
        }

        binding.etDate.setOnClickListener {
            DatePickerDialog(
                this,
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setupTimePickers() {
        binding.etStartTime.setOnClickListener {
            val cal = Calendar.getInstance()
            TimePickerDialog(this, { _, hour, minute ->
                binding.etStartTime.setText(String.format("%02d:%02d", hour, minute))
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }
        binding.etEndTime.setOnClickListener {
            val cal = Calendar.getInstance()
            TimePickerDialog(this, { _, hour, minute ->
                binding.etEndTime.setText(String.format("%02d:%02d", hour, minute))
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }
    }

    private fun loadCategories() {
        lifecycleScope.launch {
            categories = db.categoryDao().getCategoriesForUser(userId)
            if (categories.isEmpty()) {
                val defaultNames = listOf("Groceries", "Transport", "Entertainment", "Bills", "Dining Out", "Shopping")
                defaultNames.forEach { name ->
                    db.categoryDao().insertCategory(Category(userId = userId, name = name))
                }
                categories = db.categoryDao().getCategoriesForUser(userId)
            }
            val adapter = ArrayAdapter(
                this@AddExpenseActivity,
                android.R.layout.simple_spinner_item,
                categories.map { it.name }
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerCategory.adapter = adapter
            binding.spinnerCategory.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                    selectedCategoryId = categories[position].categoryId
                }
                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
            })
        }
    }

    private fun setupPhotoButtons() {
        binding.btnTakePhoto.setOnClickListener { checkCameraPermissionAndOpen() }
        binding.btnChoosePhoto.setOnClickListener { openGallery() }
    }

    private fun checkCameraPermissionAndOpen() {
        when {
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }
            shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA) -> {
                showPermissionRationale()
            }
            else -> {
                requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
            }
        }
    }

    private fun showPermissionRationale() {
        AlertDialog.Builder(this)
            .setTitle("Camera Permission Needed")
            .setMessage("This app needs camera permission to take receipt photos.")
            .setPositiveButton("OK") { _, _ -> requestPermissionLauncher.launch(android.Manifest.permission.CAMERA) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission Denied")
            .setMessage("Camera permission is required. Please enable it in settings.")
            .setPositiveButton("Go to Settings") { _, _ ->
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:$packageName")
                    startActivity(this)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val photoFile = createImageFile()
        photoFile?.let {
            photoUri = FileProvider.getUriForFile(this, "$packageName.fileprovider", it)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            cameraLauncher.launch(intent)
        }
    }

    private fun createImageFile(): File? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(imageFileName, ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    private fun savePhotoToInternalStorage(sourceUri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(sourceUri) ?: return
            val fileName = "receipt_${System.currentTimeMillis()}.jpg"
            val destFile = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName)
            FileOutputStream(destFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            currentPhotoPath = destFile.absolutePath
            binding.tvPhotoPath.text = "Photo saved: $fileName"
            binding.tvPhotoPath.setTextColor(ContextCompat.getColor(this, R.color.positive_green))
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to save photo", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveExpense() {
        val amountStr = binding.etAmount.text.toString().trim()
        if (amountStr.isEmpty()) {
            binding.etAmount.error = "Enter amount"
            return
        }
        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            binding.etAmount.error = "Valid positive amount required"
            return
        }

        val description = binding.etDescription.text.toString().trim()
        val dateStr = binding.etDate.text.toString().trim()
        val dateTimestamp = try {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(dateStr)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) { System.currentTimeMillis() }

        // Parse start time
        val startTime = if (binding.etStartTime.text.toString().isNotEmpty()) {
            val parts = binding.etStartTime.text.toString().split(":")
            if (parts.size == 2) {
                val cal = Calendar.getInstance()
                cal.timeInMillis = dateTimestamp
                cal.set(Calendar.HOUR_OF_DAY, parts[0].toInt())
                cal.set(Calendar.MINUTE, parts[1].toInt())
                cal.timeInMillis
            } else null
        } else null

        // Parse end time
        val endTime = if (binding.etEndTime.text.toString().isNotEmpty()) {
            val parts = binding.etEndTime.text.toString().split(":")
            if (parts.size == 2) {
                val cal = Calendar.getInstance()
                cal.timeInMillis = dateTimestamp
                cal.set(Calendar.HOUR_OF_DAY, parts[0].toInt())
                cal.set(Calendar.MINUTE, parts[1].toInt())
                cal.timeInMillis
            } else null
        } else null

        if (selectedCategoryId == -1L) {
            Toast.makeText(this, "Select a category", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val expense = Expense(
                userId = userId,
                categoryId = selectedCategoryId,
                amount = amount,
                date = dateTimestamp,
                startTime = startTime,
                endTime = endTime,
                description = description,
                receiptPath = currentPhotoPath
            )
            db.expenseDao().insertExpense(expense)
            Toast.makeText(this@AddExpenseActivity, "Expense saved", Toast.LENGTH_SHORT).show()
            setResult(Activity.RESULT_OK)
            finish()
        }
    }
}