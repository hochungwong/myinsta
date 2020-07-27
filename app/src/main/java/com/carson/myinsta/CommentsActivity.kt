package com.carson.myinsta

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.TokenWatcher
import android.text.TextUtils
import android.widget.Toast
import com.carson.myinsta.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_account_settings.*
import kotlinx.android.synthetic.main.activity_comments.*

class CommentsActivity : AppCompatActivity() {
    private var postId: String? = null
    private var publisherId: String? = null

    private var currentUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)
        val intent = intent
        postId = intent.getStringExtra("postId")
        publisherId = intent.getStringExtra("publisherId")

        currentUser = FirebaseAuth.getInstance().currentUser

        publish_comment_tv_btn.setOnClickListener {
            when {
                TextUtils.isEmpty(add_comment_edit_text!!.text.toString()) ->
                    Toast.makeText(this@CommentsActivity, "Please write some comments before publish", Toast.LENGTH_LONG)
                else -> {
                    addComment()
                }
            }
        }

        retrieveUserInfo()
    }

    private fun addComment() {
        val commentsRef = FirebaseDatabase.getInstance().reference
            .child("Comments")
            .child(postId!!) //comments for every post
        val commentMap = HashMap<String, Any>()
        commentMap["comment"] = add_comment_edit_text!!.text.toString()
        commentMap["publisher"] = currentUser!!.uid
        //push() generate comment random key
        commentsRef.push().setValue(commentMap).addOnCompleteListener {
            if (it.isSuccessful) {
                add_comment_edit_text!!.text.clear()
            } else {
                it.exception?.let {it1 ->
                    throw it1
                }
            }
        }
    }

    private fun retrieveUserInfo() {
        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(currentUser!!.uid)
        userRef.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()){
                    val user = dataSnapshot.getValue<User>(User::class.java)
                    Picasso.get().load(user!!.getUserImageUrl()).placeholder(R.drawable.profile).fit().into(profile_image_comment)
                }
            }

        })
    }
}