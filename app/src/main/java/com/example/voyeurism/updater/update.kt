package com.example.voyeurism.updater

import android.content.Context
import android.content.DialogInterface
import com.example.voyeurism.activitys.MainActivity

class update {

    private val context:Context = MainActivity().applicationContext

    fun showAlert(title: String?, message: String?) {
        val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(context)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setCancelable(true)
        builder.setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })
        builder.show()
    }

}