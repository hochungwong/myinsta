package com.carson.myinsta.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.carson.myinsta.AddStoryActivity
import com.carson.myinsta.MainActivity
import com.carson.myinsta.R
import com.carson.myinsta.model.Story
import com.carson.myinsta.model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.fragment_profile.view.*

class StoryAdapter(private var mContext: Context, private var mStories: List<Story>): RecyclerView.Adapter<StoryAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (viewType == 0) {
            val view  = LayoutInflater.from(mContext).inflate(R.layout.add_story_item, parent, false);
            ViewHolder(view);
        } else {
            val view  = LayoutInflater.from(mContext).inflate(R.layout.story_item, parent, false);
            ViewHolder(view);
        }
    }

    override fun getItemCount(): Int {
        return mStories.size;
    }

    override fun onBindViewHolder(holder: ViewHolder, i: Int) {
        val story = mStories[i]

        userInfo(holder, story.getUserId(), i)

        holder.itemView.setOnClickListener {
            val intent = Intent(mContext, AddStoryActivity::class.java)
            intent.putExtra("userId", story.getUserId())
            mContext.startActivity(intent)
        }

    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0) return 0 // add story
        return 1
    }

    class ViewHolder(@NonNull itemView: View): RecyclerView.ViewHolder(itemView) {
        //StoryItem
        var storyImageSeen: CircleImageView? = null
        var storyImageUnseen: CircleImageView? = null
        var storyUserName: TextView? = null

        //AddStoryItem
        var addStoryText: TextView? = null
        var storyImage: CircleImageView? = null
        var addStoryBtn: CircleImageView? = null
        init {
            //StoryItem
            storyImageSeen = itemView.findViewById(R.id.story_profile_image_seen)
            storyImageUnseen = itemView.findViewById(R.id.story_profile_image_unseen)
            storyUserName = itemView.findViewById(R.id.story_user_name)
            //AddStoryItem
            addStoryBtn = itemView.findViewById(R.id.story_add)
            storyImage = itemView.findViewById(R.id.story_profile_image)
            addStoryText = itemView.findViewById(R.id.add_story_text)
        }
    }

    private fun userInfo(viewHolder: ViewHolder, userId: String, position: Int) {
        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(userId)
        userRef.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()){
                    val user = dataSnapshot.getValue<User>(User::class.java)!!
                    Picasso.get().load(user.getUserImageUrl()).placeholder(R.drawable.profile).fit().into(viewHolder.storyImage)
                    if (position !=0 ) {
                        Picasso.get().load(user.getUserImageUrl()).placeholder(R.drawable.profile).fit().into(viewHolder.storyImageSeen)
                        viewHolder.storyUserName!!.text = user.getUsername()
                    }
                }
            }

        })
    }
}