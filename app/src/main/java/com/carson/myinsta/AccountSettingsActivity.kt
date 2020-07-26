package com.carson.myinsta

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.carson.myinsta.fragament.ProfileFragment
import com.carson.myinsta.model.User
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_account_settings.*

class AccountSettingsActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth;
    private lateinit var currentUser: FirebaseUser

    private var mUrl = ""
    private var imageUri: Uri? = null
    private var checker: String = ""

    private var storageProfilePicRef: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_settings)

        //init firebase stuff
        mAuth = FirebaseAuth.getInstance()
        currentUser = mAuth.currentUser!!
        storageProfilePicRef = FirebaseStorage.getInstance().reference.child("profile_images")

        logout_acct_settings.setOnClickListener {
            mAuth.signOut()
            val intent = Intent(this@AccountSettingsActivity, SignInActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        //change avatar
        change_image_text_btn.setOnClickListener {
            checker = "changeImageClicked"
            CropImage.activity()
                .setAspectRatio(1, 1)
                .start(this@AccountSettingsActivity)
        }

        save_info_profile_btn.setOnClickListener{
            if (checker == "changeImageClicked") {
                uploadImageAndUpdateInfo()
            } else {
                //only change texture information
                updateUserInfoOnly()
            }
        }

        userInfo()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val result = CropImage.getActivityResult(data)
            imageUri = result.uri
            profile_image_view_acct_settings.setImageURI(imageUri)
        }
    }

    private fun uploadImageAndUpdateInfo() {

        when {
            imageUri == null -> Toast.makeText(this, "Please select a profile image.", Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(full_name_profile_frag_acct_setting.text.toString()) ->
                Toast.makeText(this, "Please write full name first.", Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(username_acct_settings.text.toString()) ->
                Toast.makeText(this, "Please write user name first.", Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(bio_profile_acct_settings.text.toString()) ->
                Toast.makeText(this, "Please write bio first.", Toast.LENGTH_LONG).show()

            else -> {
                val fileRef = storageProfilePicRef!!.child(currentUser.uid + ".jpg")
                val uploadTask: StorageTask<*>
                uploadTask = fileRef.putFile(imageUri!!)

                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Account Settings")
                progressDialog.setMessage("Please wait, we are updating your profile...")
                progressDialog.show()

                uploadTask.continueWithTask( Continuation <UploadTask.TaskSnapshot, Task<Uri>>{ task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            progressDialog.dismiss()
                            throw it
                        }
                    }
                    return@Continuation fileRef.downloadUrl
                }).addOnCompleteListener {
                    if (it.isSuccessful) {
                        //get the uri successfully
                        val downloadUrl = it.result
                        mUrl = downloadUrl.toString()

                        val ref = FirebaseDatabase.getInstance().reference.child("Users").child(currentUser.uid)
                        val updatedUserMap = HashMap<String, Any>()
                        updatedUserMap["uid"] = currentUser.uid
                        updatedUserMap["fullName"] = full_name_profile_frag_acct_setting.text.toString().toLowerCase()
                        updatedUserMap["username"] = username_acct_settings.text.toString().toLowerCase()
                        updatedUserMap["bio"] = bio_profile_acct_settings.text.toString()
                        updatedUserMap["userImageUrl"] = mUrl

                        ref.updateChildren(updatedUserMap).addOnCompleteListener {
                            if (it.isSuccessful) {
                                progressDialog.dismiss()
                                Toast.makeText(this, "Account Information has been updated successfully", Toast.LENGTH_LONG).show()
                                val intent = Intent(this@AccountSettingsActivity, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            }else {
                                progressDialog.dismiss()
                            }
                        }
                    } else {
                        progressDialog.dismiss()
                    }
                }
            }
        }
    }


    private fun updateUserInfoOnly() {
        when {
            TextUtils.isEmpty(full_name_profile_frag_acct_setting.text.toString()) ->
                Toast.makeText(this, "Please write full name first.", Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(username_acct_settings.text.toString()) ->
                Toast.makeText(this, "Please write user name first.", Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(bio_profile_acct_settings.text.toString()) ->
                Toast.makeText(this, "Please write bio first.", Toast.LENGTH_LONG).show()

            else -> {
                val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(currentUser.uid)
                val updatedUserMap = HashMap<String, Any>()
                updatedUserMap["uid"] = currentUser.uid
                updatedUserMap["fullName"] = full_name_profile_frag_acct_setting.text.toString().toLowerCase()
                updatedUserMap["username"] = username_acct_settings.text.toString().toLowerCase()
                updatedUserMap["bio"] = bio_profile_acct_settings.text.toString()
                userRef.updateChildren(updatedUserMap).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(this, "Account Information has been updated successfully", Toast.LENGTH_LONG).show()
                        val intent = Intent(this@AccountSettingsActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }
    }

    private fun userInfo() {
        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(currentUser.uid)
        userRef.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()){
                    val user = dataSnapshot.getValue<User>(User::class.java)
                    Picasso.get().load(user!!.getUserImageUrl()).placeholder(R.drawable.profile).fit().into(profile_image_view_acct_settings)
                    username_acct_settings.setText(user.getUsername())
                    full_name_profile_frag_acct_setting.setText(user.getFullName())
                    bio_profile_acct_settings.setText(user.getBio())
                }
            }

        })
    }
}