package com.soumik.ridesharingapp.activities.usersActivity

import android.app.ProgressDialog
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.soumik.ridesharingapp.R
import com.soumik.ridesharingapp.appUtils.hideProgressDialog
import com.soumik.ridesharingapp.appUtils.showProgressDialog
import com.soumik.ridesharingapp.appUtils.showToast
import kotlinx.android.synthetic.main.activity_user_login.*

class UserLoginActivity : AppCompatActivity() {

    lateinit var progressDialog:ProgressDialog
    lateinit var mAuth:FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_login)

        progressDialog = ProgressDialog(this)
        mAuth = FirebaseAuth.getInstance()

        tv_no_account.setOnClickListener {
            tv_no_account.visibility = View.GONE
            btn_user_login.visibility = View.GONE
            btn_user_register.visibility = View.VISIBLE
            tv_signed_in.visibility = View.VISIBLE
            tv_asuser.text = "Register as a User"
        }

        tv_signed_in.setOnClickListener {
            tv_no_account.visibility = View.VISIBLE
            btn_user_login.visibility = View.VISIBLE
            btn_user_register.visibility = View.GONE
            tv_signed_in.visibility = View.GONE
            tv_asuser.text = "Login as a User"
        }

        btn_user_register.setOnClickListener {
            var userEmail = et_user_email.text.toString()
            var userPassword = et_user_password.text.toString()

            registerUser(userEmail,userPassword)
        }

        btn_user_login.setOnClickListener {
            var userEmail = et_user_email.text.toString()
            var userPassword = et_user_password.text.toString()

            loginDriver(userEmail,userPassword)
        }
    }

    private fun loginDriver(userEmail: String, userPassword: String) {
        when{
            TextUtils.isEmpty(userEmail) -> showToast(applicationContext,"Email field can't be empty")
            TextUtils.isEmpty(userPassword) -> showToast(applicationContext,"Password field can't be empty")

            else->{
                showProgressDialog(progressDialog,"User Logging In","Please wait, while we are matching your credentials")

                mAuth.signInWithEmailAndPassword(userEmail,userPassword)
                    .addOnCompleteListener {
                        if (it.isSuccessful){
                            showToast(applicationContext,"Congrats! You are successfully Logged In")
                            hideProgressDialog(progressDialog)
                        } else{
                            showToast(applicationContext,"Oops! Unsuccessful Log In, Try again later")
                            hideProgressDialog(progressDialog)
                        }
                    }
            }
        }
    }

    private fun registerUser(userEmail: String, userPassword: String) {
        when{
            TextUtils.isEmpty(userEmail) -> showToast(applicationContext,"Email field can't be empty")
            TextUtils.isEmpty(userPassword) -> showToast(applicationContext,"Password field can't be empty")

            else->{
                showProgressDialog(progressDialog,"User Registration","Please wait, while we are registering you as a user")

                mAuth.createUserWithEmailAndPassword(userEmail,userPassword)
                    .addOnCompleteListener {
                        if (it.isSuccessful){
                            showToast(applicationContext,"Congrats! You are successfully registered as a user")
                            hideProgressDialog(progressDialog)
                        } else{
                            showToast(applicationContext,"Oops! Registration failed, Try again later")
                            hideProgressDialog(progressDialog)
                        }
                    }
            }
        }
    }
}
