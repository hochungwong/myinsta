package com.carson.myinsta.adapter

import android.content.Context
import android.content.Intent
import android.media.Image
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.carson.myinsta.CommentsActivity
import com.carson.myinsta.MainActivity
import com.carson.myinsta.R
import com.carson.myinsta.model.Post
import com.carson.myinsta.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class PostAdapter
    (private var mContext: Context, private var mPosts: List<Post>) : RecyclerView.Adapter<PostAdapter.ViewHolder>() {
    private final var TAG: String = PostAdapter::class.java.simpleName

    private var currentUser: FirebaseUser? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.post_item_layout, parent, false)
        return PostAdapter.ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mPosts.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = mPosts[position]
        currentUser = FirebaseAuth.getInstance().currentUser

        Picasso.get().load(post.getPostImageUrl()).placeholder(R.drawable.camera).fit().into(holder.postImage)
        if (TextUtils.isEmpty(post.getDescription())){
            holder.description.visibility = View.GONE
        } else {
            holder.description.visibility = View.VISIBLE
            holder.description.text = post.getDescription()
        }

        publisherInfo(holder.publisherProfileImage, holder.username, holder.publisher, post.getPublisherId())
        checkLikeStatus(post.getPostId(), holder.likeBtn)
        retrieveNumberOfLikes(holder.likes, post.getPostId())
        retrieveNumberOfComments(holder.comments, post.getPostId())
        checkSavedStatus(post.getPostId(), holder.savePostBtn)
        //like btn onClickListener
        holder.likeBtn.setOnClickListener {
            //save likes and unlike
            if (holder.likeBtn.tag == "Like") {
                Log.w(TAG, "postId: ${post.getPostId()}")
                FirebaseDatabase.getInstance().reference
                    .child("Likes")
                    .child(post.getPostId())
                    .child(currentUser!!.uid)
                    .setValue(true)
            } else {
                FirebaseDatabase.getInstance().reference
                    .child("Likes")
                    .child(post.getPostId())
                    .child(currentUser!!.uid)
                    .removeValue()

//                val intent = Intent(mContext, MainActivity::class.java)
//                mContext.startActivity(intent)
            }
        }
        //save post btn
        holder.savePostBtn.setOnClickListener {
            if (holder.savePostBtn.tag == "Save") {
                //not saved yet
                FirebaseDatabase.getInstance().reference
                    .child("Saves")
                    .child(currentUser!!.uid)
                    .child(post.getPostId())
                    .setValue(true)
            } else {
                //remove saved post
                FirebaseDatabase.getInstance().reference
                    .child("Saves")
                    .child(currentUser!!.uid)
                    .child(post.getPostId())
                    .removeValue()
            }
        }
        //comment btn
        holder.commentBtn.setOnClickListener {
            val intent = Intent(mContext, CommentsActivity::class.java)
            intent.putExtra("postId", post.getPostId())
            intent.putExtra("publisherId", post.getPublisherId())
            mContext.startActivity(intent)
        }
        holder.comments.setOnClickListener {
            val intent = Intent(mContext, CommentsActivity::class.java)
            intent.putExtra("postId", post.getPostId())
            intent.putExtra("publisherId", post.getPublisherId())
            mContext.startActivity(intent)
        }

    }

    private fun retrieveNumberOfLikes(likes: TextView, postId: String) {
        val likesRef =  FirebaseDatabase.getInstance().reference.child("Likes")
            .child(postId)
        likesRef.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    likes.text = "${snapshot.childrenCount.toString()} likes"
                }
            }

        })
    }

    private fun retrieveNumberOfComments(comments: TextView, postId: String) {
        val commentsRef =  FirebaseDatabase.getInstance().reference.child("Comments")
            .child(postId)
        commentsRef.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    comments.text = "View all ${snapshot.childrenCount.toString()} Comments"
                }
            }

        })
    }

    private fun checkLikeStatus(postId: String, likeBtn: ImageView) {
        val likesRef =  FirebaseDatabase.getInstance().reference.child("Likes")
            .child(postId)

        likesRef.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.child(currentUser!!.uid).exists()) {
                    //the user likes of posts exists
                    likeBtn.setImageResource(R.drawable.heart_clicked)
                    likeBtn.tag = "Liked"
                } else {
                    likeBtn.setImageResource(R.drawable.heart_not_clicked)
                    likeBtn.tag = "Like"
                }
            }

        })
    }

    private fun checkSavedStatus(postId: String, saveBtnImageView: ImageView) {
        val savesRef = FirebaseDatabase.getInstance().reference
            .child("Saves")
            .child(currentUser!!.uid)
        savesRef.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.child(postId).exists()) {
                    saveBtnImageView.setImageResource(R.drawable.save_large_icon)
                    saveBtnImageView.tag = "Saved"
                } else {
                    saveBtnImageView.setImageResource(R.drawable.save_unfilled_large_icon)
                    saveBtnImageView.tag = "Save"
                }
            }
        })
    }

    class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
        var username: TextView = itemView.findViewById(R.id.user_name_post)
        var publisherProfileImage: CircleImageView = itemView.findViewById(R.id.user_profile_image_post)
        var postImage: ImageView = itemView.findViewById(R.id.post_image_home)
        var likeBtn: ImageView = itemView.findViewById(R.id.post_image_like_btn)
        var commentBtn: ImageView = itemView.findViewById(R.id.post_image_comment_btn)
        var savePostBtn: ImageView = itemView.findViewById(R.id.post_image_save_btn)
        var likes: TextView = itemView.findViewById(R.id.likes)
        var comments: TextView = itemView.findViewById(R.id.comments)
        var description: TextView = itemView.findViewById(R.id.description)
        var publisher: TextView = itemView.findViewById(R.id.publisher)
    }

    private fun publisherInfo(publisherProfileImage: CircleImageView, usernameTextView: TextView, publisherTextView: TextView, publisherId: String) {
        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(publisherId)
        userRef.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.exists()) {
                    val user = dataSnapshot.getValue<User>(User::class.java)
                    Picasso.get().load(user!!.getUserImageUrl()).placeholder(R.drawable.profile).fit().into(publisherProfileImage)
                    usernameTextView.text = user.getUsername()
                    publisherTextView.text = user.getFullName()
                }
            }

        })
    }
}