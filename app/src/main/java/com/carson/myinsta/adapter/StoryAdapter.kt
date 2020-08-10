package com.carson.myinsta.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.carson.myinsta.R
import com.carson.myinsta.model.Story
import de.hdodenhof.circleimageview.CircleImageView

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
}