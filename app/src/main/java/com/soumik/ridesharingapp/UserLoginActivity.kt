package com.soumik.ridesharingapp

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_user_login.*

class UserLoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_login)

        tv_no_account.setOnClickListener {
            tv_no_account.visibility = View.GONE
            btn_user_login.visibility = View.GONE
            btn_user_register.visibility = View.VISIBLE
            tv_signed_in.visibility = View.VISIBLE
        }

        tv_signed_in.setOnClickListener {
            tv_no_account.visibility = View.VISIBLE
            btn_user_login.visibility = View.VISIBLE
            btn_user_register.visibility = View.GONE
            tv_signed_in.visibility = View.GONE
        }
    }
}
