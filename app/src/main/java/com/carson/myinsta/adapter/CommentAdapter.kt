package com.carson.myinsta.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.carson.myinsta.R
import com.carson.myinsta.model.Comment
import com.carson.myinsta.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView


class CommentAdapter (private var mContext: Context, private var mComments: List<Comment>)
    : RecyclerView.Adapter<CommentAdapter.ViewHolder>() {

    private var currentUser: FirebaseUser? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentAdapter.ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.comment_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mComments.size
    }

    override fun onBindViewHolder(holder: CommentAdapter.ViewHolder, position: Int) {
        val comment = mComments[position]
        currentUser = FirebaseAuth.getInstance().currentUser

        Log.w("comment", "publisher ${comment.getPublisher()}")


        holder.commentTv!!.text = comment.getComment()
        getUserInfo(holder.imageProfile, holder.userNameTv, comment.getPublisher())
    }

    class ViewHolder(@NonNull itemView: View): RecyclerView.ViewHolder(itemView) {
        var imageProfile: CircleImageView? = null
        var userNameTv: TextView? = null
        var commentTv: TextView? = null
        init {
            imageProfile = itemView.findViewById(R.id.user_profile_image_comment)
            userNameTv = itemView.findViewById(R.id.username_comment)
            commentTv = itemView.findViewById(R.id.description_comment)
        }
    }

    private fun getUserInfo(imageProfile: CircleImageView?, userNameTv: TextView?, publisherId: String) {
        val usersRef = FirebaseDatabase.getInstance().reference
            .child("Users")
            .child(publisherId)
        usersRef.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val user = dataSnapshot.getValue(User::class.java)
                    Picasso.get().load(user!!.getUserImageUrl()).fit().into(imageProfile)
                    userNameTv?.text = user.getUsername()
                }
            }

        })
    }



}