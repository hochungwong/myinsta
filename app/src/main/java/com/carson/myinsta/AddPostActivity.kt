package com.carson.myinsta

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_account_settings.*
import kotlinx.android.synthetic.main.activity_add_post.*

class AddPostActivity : AppCompatActivity() {

    private var mUrl = ""
    private var imageUri: Uri? = null

    private var currentUser: FirebaseUser? = null

    private var storagePostPicRef: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_post)

        currentUser = FirebaseAuth.getInstance().currentUser
        storagePostPicRef = FirebaseStorage.getInstance().reference.child("post_images")

        save_new_post_btn.setOnClickListener {
            uploadPostImage()
        }

        CropImage.activity()
            .setAspectRatio(2, 1)
            .start(this@AddPostActivity)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val result = CropImage.getActivityResult(data)
            imageUri = result.uri
            image_new_post.setImageURI(imageUri)
        }
    }

    private fun uploadPostImage() {
        when {
            imageUri == null -> Toast.makeText(this, "Please select a post image.", Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(description_new_post.text.toString()) -> Toast.makeText(this, "Please write some description for your post.", Toast.LENGTH_LONG).show()
            else -> {
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Account Settings")
                progressDialog.setMessage("Please wait, we are uploading your post...")
                progressDialog.show()

                val fileRef = storagePostPicRef!!.child(System.currentTimeMillis().toString() + ".jpg")
                val uploadTask: StorageTask<*>
                uploadTask = fileRef.putFile(imageUri!!)
                uploadTask.continueWithTask(Continuation <UploadTask.TaskSnapshot, Task<Uri>> {
                    if (!it.isSuccessful) {
                        it.exception?.let { it1 ->
                            progressDialog.dismiss()
                            throw it1
                        }
                    }
                    return@Continuation fileRef.downloadUrl
                }).addOnCompleteListener {
                    if (it.isSuccessful) {
                        //get the uri successfully
                        val downloadUrl = it.result
                        mUrl = downloadUrl.toString()

                        val postsRef = FirebaseDatabase.getInstance().reference.child("Posts").child(currentUser!!.uid)
                        val postId = postsRef.push().key// generate an id automatically
                        val postsMap = HashMap<String, Any>()
                        postsMap["postId"] = postId!!
                        postsMap["description"] = description_new_post.text.toString()
                        postsMap["publisher"] = currentUser!!.uid
                        postsMap["postImageUrl"] = mUrl

                        postsRef.child(postId).updateChildren(postsMap).addOnCompleteListener { it1 ->
                            if (it1.isSuccessful) {
                                progressDialog.dismiss()
                                Toast.makeText(this, "Post has been uploaded successfully", Toast.LENGTH_LONG).show()
                                val intent = Intent(this@AddPostActivity, MainActivity::class.java)
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
}