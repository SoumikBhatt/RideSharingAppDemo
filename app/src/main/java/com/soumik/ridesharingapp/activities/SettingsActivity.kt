package com.soumik.ridesharingapp.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.soumik.ridesharingapp.R
import com.soumik.ridesharingapp.activities.driversActivity.DriverMapActivity
import com.soumik.ridesharingapp.activities.usersActivity.UserMapActivity
import com.soumik.ridesharingapp.appUtils.showToast
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity() {

    companion object{

        const val TYPE = "type"
    }

    lateinit var getType : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        getType = intent.getStringExtra(TYPE)

        if (getType == "Drivers"){
            et_set_car.visibility = View.VISIBLE
        }

        showToast(applicationContext,getType)

        tv_close.setOnClickListener {
            if (getType == "Drivers"){
                startActivity(Intent(this,DriverMapActivity::class.java))
                finish()
            } else {
                startActivity(Intent(this,UserMapActivity::class.java))
                finish()
            }
        }
    }
}
