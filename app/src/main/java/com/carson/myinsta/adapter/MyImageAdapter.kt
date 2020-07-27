package com.carson.myinsta.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.carson.myinsta.R
import com.carson.myinsta.model.Post
import com.squareup.picasso.Picasso

class MyImageAdapter (private var mContext: Context, private var mMyPosts: List<Post>) : RecyclerView.Adapter<MyImageAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.my_image_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mMyPosts.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var myPost = mMyPosts[position]
        Picasso.get().load(myPost.getPostImageUrl()).placeholder(R.drawable.camera).fit().into(holder.myPostImage)
    }

    class ViewHolder(@NonNull itemView: View): RecyclerView.ViewHolder(itemView) {
        var myPostImage: ImageView? = null
        init {
            myPostImage = itemView.findViewById(R.id.my_post_image)
        }
    }


}