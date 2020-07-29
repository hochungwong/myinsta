package com.carson.myinsta.fragment

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.carson.myinsta.AccountSettingsActivity
import com.carson.myinsta.R
import com.carson.myinsta.ShowUsersActivity
import com.carson.myinsta.adapter.MyImageAdapter
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
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile.view.*
import java.util.*
import kotlin.collections.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment() {
    private final val TAG: String? = ProfileFragment::class.simpleName

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var profileId: String
    private lateinit var currentUser: FirebaseUser

    private var postsList: MutableList<Post>? = null
    //for saved pics
    private var savedPostsList: MutableList<Post>? = null
    private var savedPostIds: MutableList<String>? = null//use for retrieve saved post id

    private var recyclerViewUploadPics: RecyclerView? = null
    private var recyclerViewSavePics: RecyclerView? = null
    private var gridLayoutManagerForUploadPics: LinearLayoutManager? = null
    private var gridLayoutManagerForSavedPics: LinearLayoutManager? = null

    private var myImageAdapter: MyImageAdapter? = null
    private var savedImageAdapter: MyImageAdapter? = null

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
        val view =  inflater.inflate(R.layout.fragment_profile, container, false)
        currentUser = FirebaseAuth.getInstance().currentUser!!

        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        if (pref != null) {
            profileId = (pref as SharedPreferences).getString("profileId", "none")!!
        }

        if (profileId == currentUser.uid) {
            //user on profile, myself
            view.edit_account_settings_btn.text = "Edit Profile"
        } else if (profileId != currentUser.uid) {
            checkFollowAndFollowingButtonStatus()
        }

        //init grid view recyclerView for uploads pics
        recyclerViewUploadPics = view.findViewById(R.id.recycler_view_user_pic_grid_view)
        recyclerViewUploadPics?.setHasFixedSize(true)
        //layout manager
        gridLayoutManagerForUploadPics = GridLayoutManager(context, 3) // 3 items each row
        //set layout manager for upload pics view
        recyclerViewUploadPics?.layoutManager = gridLayoutManagerForUploadPics
        //set upload pics adapter
        postsList = ArrayList<Post>()
        myImageAdapter = context?.let {
            MyImageAdapter(it, postsList as ArrayList<Post>)
        }
        recyclerViewUploadPics?.adapter = myImageAdapter

        //init grid view for save pics
        recyclerViewSavePics = view.findViewById(R.id.recycler_view_save_pic_grid_view)
        recyclerViewSavePics?.setHasFixedSize(true)
        //layout manager
        gridLayoutManagerForSavedPics = GridLayoutManager(context, 3) // 3 items each row
        //set layout manager for upload pics view
        recyclerViewSavePics?.layoutManager = gridLayoutManagerForSavedPics
        //set upload pics adapter
        savedPostsList = ArrayList<Post>()
        savedImageAdapter = context?.let {
            MyImageAdapter(it, savedPostsList as ArrayList<Post>)
        }
        recyclerViewSavePics?.adapter = savedImageAdapter

        //default showing my posted image
        if (currentUser!!.uid != profileId) {
            recyclerViewSavePics?.visibility = View.GONE
            view.image_save_view_btn.visibility = View.GONE
        }
        recyclerViewSavePics?.visibility = View.GONE
        recyclerViewUploadPics?.visibility = View.VISIBLE
        //display my posted images
        view.image_my_post_view_btn.setOnClickListener{
            recyclerViewSavePics?.visibility = View.GONE
            recyclerViewUploadPics?.visibility = View.VISIBLE
        }
        //display my saved images
        view.image_save_view_btn.setOnClickListener{
            recyclerViewSavePics?.visibility = View.VISIBLE
            recyclerViewUploadPics?.visibility = View.GONE
        }

        //set edit account
        view.edit_account_settings_btn.setOnClickListener {
            when (view.edit_account_settings_btn.text.toString()) {
                "Edit Profile" -> startActivity(Intent(context, AccountSettingsActivity::class.java))
                //follow in profile page
                "Follow" -> {
                    //add to Followings list
                    currentUser.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("Follow")
                            .child(it1.toString()) // currentUser
                            .child("Following")
                            .child(profileId) //follow the person clicked from list
                            .setValue(true)
                    }
                    //add to Followers list
                    currentUser.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("Follow")
                            .child(profileId) // currentUser
                            .child("Followers")
                            .child(it1.toString()) //follow the person clicked from list
                            .setValue(true)
                    }
                    //create notification
                    addFollowNotification()
                }
                //un-follow in profile page
                "Following" -> {
                    //remove form Followings list
                    currentUser.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("Follow")
                            .child(it1.toString()) // currentUser
                            .child("Following")
                            .child(profileId) //follow the person clicked from list
                            .removeValue()
                    }
                    //remove from Followers list
                    currentUser.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("Follow")
                            .child(profileId) // currentUser
                            .child("Followers")
                            .child(it1.toString()) //follow the person clicked from list
                            .removeValue()
                    }
                }
            }
        }
        view.total_followers.setOnClickListener {
            val intent = Intent(context, ShowUsersActivity::class.java)
            intent.putExtra("id", profileId)
            intent.putExtra("title", "followers")
            startActivity(intent)
        }
        view.total_following.setOnClickListener {
            val intent = Intent(context, ShowUsersActivity::class.java)
            intent.putExtra("id", profileId)
            intent.putExtra("title", "following")
            startActivity(intent)
        }
        getFollowers()
        getFollowings()
        userInfo()
        retrieveUserPhotos()
        getTotalNumberOfPosts()
        retrieveMySavedPostsIds()
        return view
    }

    override fun onStart() {
        super.onStart()

    }

    private fun checkFollowAndFollowingButtonStatus() {
        val followingsRef = currentUser.uid.let {
            FirebaseDatabase.getInstance().reference.child("Follow")
                .child(it.toString()) // currentUser
                .child("Following")
        }
        followingsRef.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.child(profileId).exists()) {
                    //find the following user
                    view?.edit_account_settings_btn?.text = "Following"
                } else {
                    view?.edit_account_settings_btn?.text = "Follow"
                }
            }

        })
    }

    private fun getFollowers() {
        val followersRef = FirebaseDatabase.getInstance().reference.child("Follow")
            .child(profileId.toString()) // currentUser
            .child("Followers")
        followersRef.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    view?.total_followers?.text = dataSnapshot.childrenCount.toString()
                } else {
                    view?.total_followers?.text = "0"
                }
            }
        })
    }

    private fun getFollowings() {
        val followingsRef = FirebaseDatabase.getInstance().reference.child("Follow")
                .child(profileId.toString()) // currentUser
                .child("Following")
        followingsRef.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    view?.total_following?.text = dataSnapshot.childrenCount.toString()
                } else {
                    view?.total_following?.text = "0"
                }
            }

        })
    }

    private fun userInfo() {
        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(profileId.toString())
        userRef.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()){
                    val user = dataSnapshot.getValue<User>(User::class.java)!!
                    Log.w(TAG, "imageUrl " + user.getUserImageUrl())
                    Picasso.get().load(user.getUserImageUrl()).placeholder(R.drawable.profile).fit().into(view?.pro_image_profile_frg)
                    view?.profile_fragment_username?.text = user.getUsername()
                    view?.full_name_profile_frag?.text = user.getFullName()
                    view?.bio_profile_frag?.text = user.getBio()
                }
            }

        })
    }

    private fun retrieveUserPhotos() {
        val postsRef = FirebaseDatabase.getInstance().reference.child("Posts")
        postsRef.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.exists()) {
                    (postsList as ArrayList<Post>).clear()
                    for (snapshot in dataSnapshot.children) {
                        val post = snapshot.getValue(Post::class.java)
                        if (post?.getPublisherId().equals(profileId)) {
                            (postsList as ArrayList<Post>).add(post!!)
                        }
                        (postsList as ArrayList<Post>).reverse()
                        myImageAdapter!!.notifyDataSetChanged()
                    }
                }
            }

        })
    }

    private fun retrieveMySavedPostsIds() {
        savedPostIds = ArrayList()
        val savedRef = FirebaseDatabase.getInstance().reference
            .child("Saves")
            .child(currentUser!!.uid)
        savedRef.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    //user have saved images
                    for (snapshot in dataSnapshot.children) {
                        //retrieve saved posts id and put them in an array list ready for use
                        (savedPostIds as ArrayList<String>).add(snapshot.key!!)
                    }
                    readSavedPosts()
                }
            }
        })
    }


    //read saved post base on savedPostIds array list
    private fun readSavedPosts() {
        val postsRef = FirebaseDatabase.getInstance().reference.child("Posts")
        postsRef.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    (savedPostsList as ArrayList<Post>).clear()
                    for (snapshot in dataSnapshot.children) {
                        val post = snapshot.getValue(Post::class.java)
                        for (id in savedPostIds as ArrayList<String>) {
                            if (post!!.getPostId() == id.toString()) {
                                (savedPostsList as ArrayList<Post>).add(post as Post)
                            }
                        }
                    }
                    savedImageAdapter!!.notifyDataSetChanged()
                }
            }

        })
    }

    private fun getTotalNumberOfPosts() {
        val postsRef = FirebaseDatabase.getInstance().reference.child("Posts")
        postsRef.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    var postCounter = 0;
                    for(snapshot in dataSnapshot.children) {
                        //check post belongs to current user
                        val post = snapshot.getValue(Post::class.java)!!
                        if (post.getPublisherId() == profileId) {
                            postCounter++
                        }
                    }
                    total_posts.text = " ${postCounter.toString()}"
                }
            }
        })
    }

    private fun addFollowNotification() {
        val notificationsRef = Firebase.database.reference
            .child("Notifications")
            .child(profileId)//to whom is followed
        val notificationMap = HashMap<String, Any>()
        notificationMap["userId"] = currentUser.uid
        notificationMap["description"] = "start following you"
        notificationMap["postId"] = ""
        notificationMap["isPost"] = false
        notificationsRef.push().setValue(notificationMap).addOnCompleteListener {
            if (it.isSuccessful) {

            }
        }
    }

    override fun onStop() {
        super.onStop()
        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", currentUser.uid)
        pref?.apply()
    }

    override fun onPause() {
        super.onPause()
        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", currentUser.uid)
        pref?.apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", currentUser.uid)
        pref?.apply()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ProfileFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}