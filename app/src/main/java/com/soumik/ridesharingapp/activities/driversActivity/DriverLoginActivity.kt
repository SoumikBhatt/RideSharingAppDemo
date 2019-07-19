package com.soumik.ridesharingapp.activities.driversActivity

import android.app.ProgressDialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.soumik.ridesharingapp.activities.usersActivity.UserLoginActivity
import com.soumik.ridesharingapp.appUtils.hideProgressDialog
import com.soumik.ridesharingapp.appUtils.showProgressDialog
import com.soumik.ridesharingapp.appUtils.showToast
import com.soumik.ridesharingapp.R
import kotlinx.android.synthetic.main.activity_driver_login.*

class DriverLoginActivity : AppCompatActivity() {

    lateinit var mAuth:FirebaseAuth
    lateinit var progressDialog: ProgressDialog
    lateinit var driverDatabaseRef : DatabaseReference
    lateinit var onlineDriverID : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_login)

        mAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)


        tv_no_account_driver.setOnClickListener {
            btn_driver_register.visibility = View.VISIBLE
            btn_driver_login.visibility = View.GONE
            tv_signed_in_driver.visibility = View.VISIBLE
            tv_no_account_driver.visibility= View.GONE
            tv_as_driver.text = "Register as a Rider"
        }

        tv_signed_in_driver.setOnClickListener {
            btn_driver_register.visibility = View.GONE
            btn_driver_login.visibility = View.VISIBLE
            tv_signed_in_driver.visibility = View.GONE
            tv_no_account_driver.visibility= View.VISIBLE
            tv_as_driver.text = "Login as a Rider"
        }

        tv_not_rider.setOnClickListener {
            startActivity(Intent(this, UserLoginActivity::class.java))
            finish()
        }

        btn_driver_register.setOnClickListener {

            var driverEmail = et_driver_email.text.toString()
            var driverPassword = et_driver_password.text.toString()

            registerDriver(driverEmail,driverPassword)
        }

        btn_driver_login.setOnClickListener {
            var driverEmail = et_driver_email.text.toString()
            var driverPassword = et_driver_password.text.toString()

            loginDriver(driverEmail,driverPassword)
        }
    }

    private fun loginDriver(driverEmail: String, driverPassword: String) {

        when {
            TextUtils.isEmpty(driverEmail) -> showToast(applicationContext,"Email field can't be empty")
            TextUtils.isEmpty(driverPassword) -> showToast(applicationContext,"Password field can't be empty")

            else -> {

                showProgressDialog(progressDialog,"Rider Logging In","Please wait, while we are matching your credentials")

                mAuth.signInWithEmailAndPassword(driverEmail,driverPassword)
                    .addOnCompleteListener {
                        if (it.isSuccessful){
                            startActivity(Intent(this,DriverMapActivity::class.java))
                            showToast(applicationContext,"Logged in Successfully")
                            hideProgressDialog(progressDialog)
                        } else{
                            showToast(applicationContext,"Oops! Logging in Failed, Try again later")
                            hideProgressDialog(progressDialog)
                        }
                    }
            }
        }

    }

    private fun registerDriver(driverEmail:String,driverPassword:String) {

        when {
            TextUtils.isEmpty(driverEmail) -> showToast(applicationContext,"Email field can't be empty")
            TextUtils.isEmpty(driverPassword) -> showToast(applicationContext,"Password field can't be empty")

            else -> {

                showProgressDialog(progressDialog,"Rider Registration","Please wait, while we are registering you as a rider")

                mAuth.createUserWithEmailAndPassword(driverEmail,driverPassword)
                    .addOnCompleteListener {
                        if (it.isSuccessful){

                            onlineDriverID = mAuth.currentUser?.uid!!
                            driverDatabaseRef = FirebaseDatabase.getInstance().reference.child("Users").child("Drivers").child(onlineDriverID)

                            driverDatabaseRef.setValue(true)

                            startActivity(Intent(this,DriverMapActivity::class.java))

                            showToast(applicationContext,"Congrats! You are registered as a Rider")
                            hideProgressDialog(progressDialog)
                        } else{
                            showToast(applicationContext,"Oops! Registration Failed, Try again later")
                            hideProgressDialog(progressDialog)
                        }
                    }
            }
        }
    }
}
