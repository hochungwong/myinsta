package com.carson.myinsta.fragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.carson.myinsta.R
import com.carson.myinsta.adapter.PostAdapter
import com.carson.myinsta.model.Post
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PostDetailsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PostDetailsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var postAdapter: PostAdapter? = null
    private var postList: MutableList<Post>? = null

    private var postId: String? = ""

    private var recyclerView: RecyclerView? = null
    var linearLayoutManager: LinearLayoutManager? = null

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
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_post_details, container, false)

        val prefs = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        //get post id from prefs
        postId = prefs?.getString("postId", "none")

        recyclerView = view.findViewById(R.id.recycler_view_post_detail_frag)
        recyclerView?.setHasFixedSize(true)
        linearLayoutManager = LinearLayoutManager(context)

        recyclerView?.layoutManager = linearLayoutManager

        postList = ArrayList<Post>()
        postAdapter = context?.let {
            PostAdapter(it, postList as ArrayList<Post>)
        }
        recyclerView?.adapter = postAdapter

        return view
    }

    override fun onStart() {
        super.onStart()
        retrievePost()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PostDetailsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PostDetailsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun retrievePost() {
        val postsRef = FirebaseDatabase.getInstance().reference
            .child("Posts")
            .child(postId!!)
        postsRef.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                postList?.clear()
                val post = dataSnapshot.getValue(Post::class.java)
                postList!!.add(post as Post)
                postAdapter!!.notifyDataSetChanged()
            }
        })
    }
}