package com.carson.myinsta

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_add_post.*

class AddStoryActivity : AppCompatActivity() {
    private var mUrl = ""
    private var imageUri: Uri? = null

    private var currentUser: FirebaseUser? = null

    private var storageStoryPicRef: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_story)

        currentUser = FirebaseAuth.getInstance().currentUser
        storageStoryPicRef = FirebaseStorage.getInstance().reference.child("story_images")

        CropImage.activity()
            .setAspectRatio(9, 16)
            .start(this@AddStoryActivity)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val result = CropImage.getActivityResult(data)
            imageUri = result.uri
            uploadStory()
        }
    }

    private fun uploadStory() {
        when (imageUri) {
            null -> Toast.makeText(this, "Please select a story image.", Toast.LENGTH_LONG).show()
            //TextUtils.isEmpty(description_new_post.text.toString()) -> Toast.makeText(this, "Please write some description for your post.", Toast.LENGTH_LONG).show()
            else -> {
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Adding new story")
                progressDialog.setMessage("Please wait, we are uploading your post...")
                progressDialog.show()

                val fileRef = storageStoryPicRef!!.child(System.currentTimeMillis().toString() + ".jpg")
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

                        val timeEnd = System.currentTimeMillis() + 86400000 // story will expire in a day

                        val storiesRef = Firebase.database.reference.child("Stories")
                        val storyId = storiesRef.push().key// generate an id automatically
                        val storyMap = HashMap<String, Any>()
                        storyMap["userId"] = currentUser!!.uid
                        storyMap["storyId"] = storyId!!.toString()
                        storyMap["timeStart"] = ServerValue.TIMESTAMP
                        storyMap["timeEnd"] = timeEnd
                        storyMap["imageUrl"] = mUrl

                        storiesRef.child(storyId.toString()).updateChildren(storyMap).addOnCompleteListener { it1 ->
                            if (it1.isSuccessful) {
                                progressDialog.dismiss()
                                Toast.makeText(this, "Story has been uploaded successfully", Toast.LENGTH_LONG).show()
                                val intent = Intent(this@AddStoryActivity, MainActivity::class.java)
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