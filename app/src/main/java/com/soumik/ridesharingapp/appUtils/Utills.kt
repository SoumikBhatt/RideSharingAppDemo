package com.soumik.ridesharingapp.appUtils

import android.app.ProgressDialog
import android.content.Context
import android.support.v7.app.AlertDialog
import android.widget.Toast
import com.soumik.ridesharingapp.R


fun showToast(context: Context, text:String){

    Toast.makeText(context,text, Toast.LENGTH_SHORT).show()
}

fun showProgressDialog(progressDialog: ProgressDialog, title:String, message:String){
    progressDialog.setTitle(title)
    progressDialog.setMessage(message)
    progressDialog.setCanceledOnTouchOutside(false)
    progressDialog.show()
}

fun hideProgressDialog(progressDialog: ProgressDialog){
    progressDialog.dismiss()
}

fun showAlertDialog(context: Context){
    AlertDialog.Builder(context)
        .setIcon(R.drawable.ic_alert)
        .setTitle("Closing The App")
        .setMessage("Do you really want to exit?")
        .setPositiveButton("Yes") { _, _ ->  }
        .setNegativeButton("No",null)
        .show()
}