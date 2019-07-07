package com.soumik.ridesharingapp.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.soumik.ridesharingapp.activities.driversActivity.DriverLoginActivity
import com.soumik.ridesharingapp.activities.usersActivity.UserLoginActivity
import com.soumik.ridesharingapp.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_driver.setOnClickListener {
            startActivity(Intent(this, DriverLoginActivity::class.java))
        }

        btn_user.setOnClickListener {
            startActivity(Intent(this, UserLoginActivity::class.java))
        }
    }
}
