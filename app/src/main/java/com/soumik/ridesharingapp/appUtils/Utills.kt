package com.soumik.ridesharingapp.appUtils

import android.app.ProgressDialog
import android.content.Context
import android.widget.Toast


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