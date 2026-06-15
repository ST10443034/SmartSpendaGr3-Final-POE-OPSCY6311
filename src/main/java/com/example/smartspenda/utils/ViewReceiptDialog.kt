package com.example.smartspenda.utils

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.smartspenda.R

class ViewReceiptDialog(context: Context, private val imagePath: String) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_view_receipt)

        val imageView = findViewById<ImageView>(R.id.ivFullReceipt)
        val btnClose = findViewById<Button>(R.id.btnCloseDialog)

        Glide.with(context)
            .load(imagePath)
            .into(imageView)

        btnClose.setOnClickListener {
            dismiss()
        }
    }
}