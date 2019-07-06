package com.soumik.ridesharingapp

import android.content.Intent
import android.opengl.Visibility
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_driver_login.*

class DriverLoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_login)

        tv_no_account_driver.setOnClickListener {
            btn_driver_register.visibility = View.VISIBLE
            btn_driver_login.visibility = View.GONE
            tv_signed_in_driver.visibility = View.VISIBLE
            tv_no_account_driver.visibility= View.GONE
        }

        tv_signed_in_driver.setOnClickListener {
            btn_driver_register.visibility = View.GONE
            btn_driver_login.visibility = View.VISIBLE
            tv_signed_in_driver.visibility = View.GONE
            tv_no_account_driver.visibility= View.VISIBLE
        }

        tv_not_rider.setOnClickListener {
            startActivity(Intent(this,UserLoginActivity::class.java))
            finish()
        }
    }
}
