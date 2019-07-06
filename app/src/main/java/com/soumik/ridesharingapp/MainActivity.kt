package com.soumik.ridesharingapp

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_driver.setOnClickListener {
            startActivity(Intent(this,DriverLoginActivity::class.java))
            finish()
        }

        btn_user.setOnClickListener {
            startActivity(Intent(this,UserLoginActivity::class.java))
            finish()
        }
    }
}
