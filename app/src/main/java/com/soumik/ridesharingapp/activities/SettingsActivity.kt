package com.soumik.ridesharingapp.activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.soumik.ridesharingapp.R
import com.soumik.ridesharingapp.activities.driversActivity.DriverMapActivity
import com.soumik.ridesharingapp.activities.usersActivity.UserMapActivity
import com.soumik.ridesharingapp.appUtils.hideProgressDialog
import com.soumik.ridesharingapp.appUtils.showProgressDialog
import com.soumik.ridesharingapp.appUtils.showToast
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_settings.*
import org.w3c.dom.Text

class SettingsActivity : AppCompatActivity() {

    companion object {

        const val TYPE = "type"
    }

    lateinit var getType: String
    var checker = ""
    var imageURI: Uri? = null
    var imageURL: String = ""
    lateinit var uploadTask: UploadTask
    lateinit var storegeProfilePicRef: StorageReference
    lateinit var databaseReference: DatabaseReference
    lateinit var mAuth: FirebaseAuth
    lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        progressDialog = ProgressDialog(this)

        getType = intent.getStringExtra(TYPE)

        if (getType == "Drivers") {
            et_set_car.visibility = View.VISIBLE
        }
        showToast(applicationContext, getType)

        mAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference.child("Users").child(getType)
        storegeProfilePicRef = FirebaseStorage.getInstance().reference.child("Profile Pictures")

        tv_close.setOnClickListener {
            if (getType == "Drivers") {
                startActivity(Intent(this, DriverMapActivity::class.java))
                finish()
            } else {
                startActivity(Intent(this, UserMapActivity::class.java))
                finish()
            }
        }

        tv_update.setOnClickListener {

            if (checker == "clicked") {

                validateControllers()

            } else {

                validateAndSaveOnlyInformation()
            }
        }

        tv_setting_change_icon.setOnClickListener {

            checker = "clicked"

            CropImage.activity().setAspectRatio(1, 1).start(this)
        }

        getUserInformation()
    }

    private fun validateAndSaveOnlyInformation() {

        if (TextUtils.isEmpty(et_set_name.text.toString())){
            showToast(applicationContext, "Please Provide Your Name")
        } else if (TextUtils.isEmpty(et_set_phone.text.toString())){
            showToast(applicationContext,"Please Provide Your Phone Number")
        } else if (TextUtils.isEmpty(et_set_car.text.toString()) && getType=="Drivers"){
            showToast(applicationContext,"Please Provide Your Car Name")
        } else {

            var userMap: HashMap<String, Any> = HashMap()

            userMap["uid"] = mAuth.currentUser?.uid!!
            userMap["name"] = et_set_name.text.toString()
            userMap["phone"] = et_set_phone.text.toString()

            if (getType == "Drivers") {
                userMap["car"] = et_set_car.text.toString()
            }

            databaseReference.child(mAuth.currentUser?.uid!!).updateChildren(userMap)

            if (getType == "Drivers") {
                startActivity(Intent(this, DriverMapActivity::class.java))
                finish()
            } else {
                startActivity(Intent(this, UserMapActivity::class.java))
                finish()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {

            var result: CropImage.ActivityResult = CropImage.getActivityResult(data)

            imageURI = result.uri

            iv_setting_profile_icon.setImageURI(imageURI)
        } else {

            if (getType == "Drivers") {
                startActivity(Intent(this, DriverMapActivity::class.java))
                finish()
            } else {
                startActivity(Intent(this, UserMapActivity::class.java))
                finish()
            }

            showToast(applicationContext, "Error! Try Again")
        }
    }

    private fun validateControllers() {

        when {
            TextUtils.isEmpty(et_set_name.text.toString()) -> showToast(applicationContext, "Please Provide Your Name")
            TextUtils.isEmpty(et_set_phone.text.toString()) -> showToast(applicationContext,"Please Provide Your Phone Number")
            TextUtils.isEmpty(et_set_car.text.toString()) && getType == "Drivers" -> showToast(applicationContext,"Please Provide Your Car Name")
            checker == "clicked" -> updateProfilePicture()
        }
    }

    private fun updateProfilePicture() {

        showProgressDialog(progressDialog, "Updating Information", "Please wait,while we are updating your information's")

        if (imageURI != null) {

            val fileRef: StorageReference = storegeProfilePicRef.child(mAuth.currentUser?.uid!! + " .jpg")

            uploadTask = fileRef.putFile(imageURI!!)


            uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> {

                if (!it.isSuccessful){
                    throw it.exception!!
                }

                return@Continuation fileRef.downloadUrl
            }).addOnCompleteListener {
                if (it.isSuccessful){

                    var downloadURI : Uri = it?.result!!
                    imageURL = downloadURI.toString()

                    var userMap: HashMap<String, Any> = HashMap()

                    userMap["uid"] = mAuth.currentUser?.uid!!
                    userMap["name"] = et_set_name.text.toString()
                    userMap["phone"] = et_set_phone.text.toString()
                    userMap["image"] = imageURL

                    if (getType == "Drivers") {
                        userMap["car"] = et_set_car.text.toString()
                    }

                    databaseReference.child(mAuth.currentUser?.uid!!).updateChildren(userMap)

                    hideProgressDialog(progressDialog)

                    if (getType == "Drivers") {
                        startActivity(Intent(this, DriverMapActivity::class.java))
                        finish()
                    } else {
                        startActivity(Intent(this, UserMapActivity::class.java))
                        finish()
                    }
                }
            }

        } else {

            showToast(applicationContext, "Image is not selected")
        }

    }

    private fun getUserInformation() {

        databaseReference.child(mAuth.currentUser?.uid!!).addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {

                if (p0.exists() && p0.childrenCount > 0) {

                    var name = p0.child("name").value.toString()
                    var phone = p0.child("phone").value.toString()

                    et_set_name.setText(name)
                    et_set_phone.setText(phone)

                    if (getType == "Drivers") {
                        var car = p0.child("car").value.toString()
                        et_set_car.setText(car)
                    }

                    if (p0.hasChild("image")) {
                        var image = p0.child("image").value.toString()
                        Picasso.get().load(image).into(iv_setting_profile_icon)
                    }
                }
            }
        })
    }
}
