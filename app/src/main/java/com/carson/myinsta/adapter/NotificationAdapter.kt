package com.carson.myinsta.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.carson.myinsta.R
import com.carson.myinsta.fragment.PostDetailsFragment
import com.carson.myinsta.fragment.ProfileFragment
import com.carson.myinsta.model.Notification
import com.carson.myinsta.model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_comments.*
import kotlinx.android.synthetic.main.fragment_profile.view.*

class NotificationAdapter(private var mContext: Context, private var mNotifications: List<Notification>): RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.notification_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mNotifications.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notification = mNotifications[position]
        when {
            notification.getDescription() == "started following you" -> holder.description?.text = "started following you"
            notification.getDescription() == "liked your post" -> holder.description?.text = "liked your post"
            notification.getDescription().contains("commented") ->
                holder.description?.text = notification.getDescription().replace("commented:", "commented: ")
            else -> {
                holder.description?.text = notification.getDescription()
            }
        }
        //get user info about the notification
        userInfo(holder.profileImage!!, holder.username!!, notification.getUserId())
        if (notification.getIsPost()) {
            //likes and comments notification
            holder.postImage?.visibility = View.VISIBLE
            getPostImage(holder.postImage!!, notification.getPostId())
        } else {
            //follow notifications
            holder.postImage?.visibility = View.GONE
        }
        //whole itemView set onClickListener
        holder.itemView.setOnClickListener {
            if (notification.getIsPost()) {
                //to post
                val editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
                editor.putString("postId", notification.getPostId())
                editor.apply()
                //transition
                (mContext as FragmentActivity).supportFragmentManager.beginTransaction().replace(
                    R.id.fragment_container, PostDetailsFragment()
                ).commit()
            } else {
                //to follower
                val pref = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
                pref.putString("profileId", notification.getUserId())
                pref.apply()

                (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, ProfileFragment()).commit()
            }
        }

    }

    //retrieve notification user info
    private fun userInfo(profileImageView: CircleImageView, username: TextView, publisherId: String) {
        val userRef = Firebase.database.reference
            .child("Users")
            .child(publisherId)//retrieve all the create notification users
        userRef.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()){
                    val user = dataSnapshot.getValue<User>(User::class.java)!!
                    Picasso.get().load(user.getUserImageUrl()).placeholder(R.drawable.profile).fit().into(profileImageView)
                    username.text = user.getUsername()
                }
            }
        })
    }

    private fun getPostImage(postImageView: ImageView, postId: String) {
        val postsImageRef = FirebaseDatabase.getInstance().reference
            .child("Posts")
            .child(postId)
            .child("postImageUrl")
        postsImageRef.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()){
                    val postImageUrl = dataSnapshot.value.toString()
                    Picasso.get().load(postImageUrl).placeholder(R.drawable.camera).fit().into(postImageView)
                }
            }
        })
    }

    class ViewHolder(@NonNull itemView: View): RecyclerView.ViewHolder(itemView) {
        var profileImage: CircleImageView? = null
        var username: TextView? = null
        var description: TextView? = null
        var postImage: ImageView? = null
        init {
            profileImage = itemView.findViewById(R.id.profile_image_notification)
            username = itemView.findViewById(R.id.username_notification)
            description = itemView.findViewById(R.id.description_notification)
            postImage = itemView.findViewById(R.id.post_image_notification)
        }
    }


}