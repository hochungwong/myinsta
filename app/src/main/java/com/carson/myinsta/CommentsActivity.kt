package com.carson.myinsta

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.TokenWatcher
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.carson.myinsta.adapter.CommentAdapter
import com.carson.myinsta.model.Comment
import com.carson.myinsta.model.Post
import com.carson.myinsta.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_account_settings.*
import kotlinx.android.synthetic.main.activity_comments.*

class CommentsActivity : AppCompatActivity() {
    private final var TAG: String = CommentsActivity::class.java.simpleName

    private var postId: String? = null
    private var publisherId: String? = null

    private var currentUser: FirebaseUser? = null

    private var commentsList: MutableList<Comment>? = null

    private var commentAdapter: CommentAdapter? = null
    private var recyclerView: RecyclerView? = null
    private var linearLayoutManager: LinearLayoutManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)
        val intent = intent
        postId = intent.getStringExtra("postId")
        publisherId = intent.getStringExtra("publisherId")

        currentUser = FirebaseAuth.getInstance().currentUser

        commentsList = ArrayList<Comment>()
        recyclerView = findViewById(R.id.recycler_view_comments)
        linearLayoutManager = LinearLayoutManager(this)
        commentAdapter = CommentAdapter(this, commentsList as ArrayList<Comment>)

        linearLayoutManager!!.reverseLayout = true
        recyclerView!!.layoutManager = linearLayoutManager

        recyclerView!!.adapter = commentAdapter

        publish_comment_tv_btn.setOnClickListener {
            when {
                TextUtils.isEmpty(add_comment_edit_text!!.text.toString()) ->
                    Toast.makeText(this@CommentsActivity, "Please write some comments before publish", Toast.LENGTH_LONG).show()
                else -> {
                    addComment()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        retrieveUserInfo()
        readComments()
        getPostImage()
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
                addCommentNotification()
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

    private fun getPostImage() {
        val postsImageRef = FirebaseDatabase.getInstance().reference
            .child("Posts")
            .child(postId!!)
            .child("postImageUrl")
        postsImageRef.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()){
                    val postImageUrl = dataSnapshot.value.toString()
                    Picasso.get().load(postImageUrl).placeholder(R.drawable.profile).fit().into(post_image_comments)
                }
            }
        })
    }

    private fun readComments() {
        val commentsRef = FirebaseDatabase.getInstance().reference
            .child("Comments")
            .child(postId!!)
        commentsRef.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    commentsList!!.clear()
                    for (snapshot in dataSnapshot.children) {
                        val comment = snapshot.getValue(Comment::class.java)
                        commentsList!!.add(comment!!)
                    }
                    commentAdapter!!.notifyDataSetChanged()
                }
            }
        })
    }

    private fun addCommentNotification() {
        val notificationsRef = Firebase.database.reference
            .child("Notifications")
            .child(publisherId!!)
        val notificationMap = HashMap<String, Any>()
        notificationMap["userId"] = currentUser!!.uid
        notificationMap["description"] = "commented: ${add_comment_edit_text.text.toString()}"
        notificationMap["postId"] = postId!!
        notificationMap["isPost"] = true
        notificationsRef.push().setValue(notificationMap).addOnCompleteListener {
            if (it.isSuccessful) {

            }
        }
    }
}