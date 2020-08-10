package com.carson.myinsta.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.carson.myinsta.R
import com.carson.myinsta.adapter.PostAdapter
import com.carson.myinsta.adapter.StoryAdapter
import com.carson.myinsta.model.Post
import com.carson.myinsta.model.Story
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {
    // TODO: Rename and change types of parameters

    private final var TAG: String = HomeFragment::class.java.simpleName

    private var currentUser: FirebaseUser? = null

    private var postAdapter: PostAdapter? = null
    private var postsList: MutableList<Post>? = null

    private var storyAdapter: StoryAdapter? = null
    private var storyList: MutableList<Story>? = null

    private var followingList: MutableList<String>? = null

    private var param1: String? = null
    private var param2: String? = null

    private var recyclerViewPosts: RecyclerView? = null
    private var recyclerViewStories: RecyclerView? = null
    private var postsLinearLayoutManager: LinearLayoutManager? = null
    private var storiesLinearLayoutManager: LinearLayoutManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        currentUser = FirebaseAuth.getInstance().currentUser
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_home, container, false)
        //recycler view for posts
        recyclerViewPosts = view.findViewById(R.id.recycler_view_home)
        postsLinearLayoutManager = LinearLayoutManager(context)
        postsLinearLayoutManager?.reverseLayout = true //newest at the top
        postsLinearLayoutManager?.stackFromEnd = true
        recyclerViewPosts?.layoutManager = postsLinearLayoutManager
        postsList = ArrayList()
        postAdapter = context?.let {
            PostAdapter(it, postsList as ArrayList<Post>)
        }
        recyclerViewPosts?.adapter = postAdapter
        //recycler view for stories
        recyclerViewStories = view.findViewById(R.id.recycler_view_story)
        storiesLinearLayoutManager = LinearLayoutManager(context)
        storiesLinearLayoutManager?.reverseLayout = true //newest at the top
        storiesLinearLayoutManager?.stackFromEnd = true
        recyclerViewStories?.layoutManager = storiesLinearLayoutManager
        storyList = ArrayList()
        storyAdapter = context?.let {
            StoryAdapter(it, storyList as ArrayList<Story>)
        }
        recyclerViewStories?.adapter = storyAdapter

        return view
    }

    override fun onStart() {
        super.onStart()
        checkFollowings()
    }

    private fun checkFollowings() {
        followingList = ArrayList()
        val followingRef = FirebaseDatabase.getInstance().reference.child("Follow")
                .child(FirebaseAuth.getInstance().currentUser!!.uid) // currentUser
                .child("Following")
        followingRef.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    (followingList as ArrayList<String>).clear()
                    for (snapshot in dataSnapshot.children) {
                        snapshot.key?.let {
                            //find all the followings ids
                            //use them to retrieve posts
                            (followingList as ArrayList<String>).add(it)
                        }
                        retrievePosts()
                        retrieveStories()
                    }
                }
            }
        })
    }

    private fun retrievePosts() {
        val postsRef = FirebaseDatabase.getInstance().reference.child("Posts")
        postsRef.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                postsList?.clear()
                for (snapshot in dataSnapshot.children) {
                    val post = snapshot.getValue(Post::class.java)
                    //check publisher
                    for (followingId in (followingList as ArrayList<String>)) {
                        if (post!!.getPublisherId() == followingId) {
                            postsList!!.add(post as Post)
                        }
                        postAdapter!!.notifyDataSetChanged()
                    }
                }
            }
        })
    }

    private fun retrieveStories() {
        val storiesRef = Firebase.database.reference.child("Story")
        storiesRef.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(snapshot: DataSnapshot) {
                val timeCurrent = System.currentTimeMillis()
                (storyList as ArrayList<Story>).clear()
                (storyList as ArrayList<Story>).add(Story("", 0, 0, "", currentUser!!.uid))
                for (id in followingList!!) {
                    var countStory = 0
                    var story: Story? = null
                    for (s in snapshot.child(id.toString()).children) { //retrieve all the stories based on followingList
                        story = s.getValue(Story::class.java)
                        if (timeCurrent > story!!.getTimeStart() && timeCurrent < story!!.getTimeEnd()) {
                            // story not expired
                            countStory++
                        }
                    }
                    if (countStory > 0) {
                        (storyList as ArrayList<Story>).add(story!!)
                    }
                }
                storyAdapter!!.notifyDataSetChanged()
            }
        })
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}