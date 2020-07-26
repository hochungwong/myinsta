package com.carson.myinsta.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.carson.myinsta.R
import com.carson.myinsta.fragament.ProfileFragment
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
import de.hdodenhof.circleimageview.CircleImageView

class UserAdapter(private var mContext: Context, private var mUsers: List<User>, private var isFragment: Boolean = false)
    : RecyclerView.Adapter<UserAdapter.ViewHolder>(){

    private var currentUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.user_item_layout, parent, false)
        return UserAdapter.ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mUsers.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = mUsers[position]
        holder.usernameTextView.text = user.getUsername()
        holder.fullNameTextView.text = user.getFullName()
        Picasso
            .get()
            .load(user.getUserImageUrl())
            .placeholder(R.drawable.profile)
            .fit()
            .into(holder.userProfileImage)

        checkFollowingStatus(user.getUID(), holder.followButton)

        holder.itemView.setOnClickListener {
            val pref = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
            pref.putString("profileId", user.getUID())
            pref.apply()

            (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ProfileFragment()).commit()
        }

        //user can't follow themself
        holder.followButton.setOnClickListener {
            if (holder.followButton.text.toString() == "Follow") {
                //follow
                currentUser?.uid.let { it1 ->
                    FirebaseDatabase.getInstance().reference
                        .child("Follow")
                        .child(it1.toString()) // currentUser
                        .child("Following") //followings list belong to the currentUser
                        .child(user.getUID().toString())//following user id
                        .setValue(true) //follow? boolean
                        .addOnCompleteListener { task1 ->
                            if (task1.isSuccessful) {
                                FirebaseDatabase.getInstance().reference
                                    .child("Follow")
                                    .child(user.getUID().toString()) // following user id
                                    .child("Followers") //followers list belong to the clicked user
                                    .child(it1.toString())//follower user id (current user id)
                                    .setValue(true) //follow? boolean
                                    .addOnCompleteListener { task2->
                                        if (task2.isSuccessful) {

                                        }
                                    }

                            }
                        }
                }
            } else {
                //un-follow
                currentUser?.uid.let { it1 ->
                    FirebaseDatabase.getInstance().reference.child("Follow")
                        .child(it1.toString()) // currentUser
                        .child("Following") //followings list belong to the currentUser
                        .child(user.getUID().toString())//following user id
                        .removeValue() //follow? boolean
                        .addOnCompleteListener { task1 ->
                            if (task1.isSuccessful) {
                                FirebaseDatabase.getInstance().reference.child("Follow")
                                    .child(user.getUID().toString()) // following user id
                                    .child("Followers") //followers list belong to the currentUser
                                    .child(it1.toString())//follower user id
                                    .removeValue() //follow? boolean
                                    .addOnCompleteListener { task2->
                                        if (task2.isSuccessful) {

                                        }
                                    }

                            }
                        }
                }
            }
        }
    }

    class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
        var usernameTextView: TextView = itemView.findViewById(R.id.user_name_search)
        var fullNameTextView: TextView = itemView.findViewById(R.id.user_full_name_search)
        var userProfileImage: CircleImageView = itemView.findViewById(R.id.user_profile_image_search)
        var followButton: Button = itemView.findViewById(R.id.follow_btn_search)
    }

    private fun checkFollowingStatus(uid: String?, followButton: Button) {
        val followingRef = currentUser?.uid.let { it1 ->
            FirebaseDatabase.getInstance().reference.child("Follow")
                .child(it1.toString()) // currentUser
                .child("Following")
        }
        followingRef.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.child(uid.toString()).exists()) {
                    //find user does exit in the following list?
                    followButton.text = "Following"
                } else {
                    followButton.text = "Follow"
                }
            }

        })
    }

}


