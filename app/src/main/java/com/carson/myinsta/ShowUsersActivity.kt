package com.carson.myinsta

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.carson.myinsta.adapter.UserAdapter
import com.carson.myinsta.model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_show_users.*
import kotlinx.android.synthetic.main.fragment_profile.view.*
import kotlinx.android.synthetic.main.fragment_search.view.*

class ShowUsersActivity : AppCompatActivity() {

    private var id: String = ""
    private var title: String ="" // title passed from click

    private var userAdapter: UserAdapter? = null
    private var usersList: MutableList<User>? = null
    private var idsList: MutableList<String>? = null

    private var recyclerView: RecyclerView? = null
    private var linearLayoutManager: LinearLayoutManager? = null

    private var showUsersToolBar: Toolbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_users)

        id = intent.getStringExtra("id")!! //id depends
        title = intent.getStringExtra("title")!!

        showUsersToolBar = findViewById(R.id.toolbar)

        setSupportActionBar(showUsersToolBar)
        supportActionBar!!.title = title
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        showUsersToolBar?.setNavigationOnClickListener {
            finish()
        }

        recyclerView = show_users_recycler_view
        recyclerView?.setHasFixedSize(true)
        linearLayoutManager = LinearLayoutManager(this)
        recyclerView?.layoutManager = linearLayoutManager

        usersList = ArrayList<User>()
        idsList = ArrayList<String>() // users ids list
        userAdapter = UserAdapter(this, usersList as ArrayList<User>, false)
        recyclerView?.adapter = userAdapter
    }

    override fun onStart() {
        super.onStart()
        when(title) {
            "likes" -> getLikes()
            "following" -> getFollowings()
            "followers" -> getFollowers()
            "stories" -> getStories()
        }
    }

    private fun getStories() {
        //show users stories
    }

    private fun getFollowers() {
        val followersRef = FirebaseDatabase.getInstance().reference.child("Follow")
            .child(id) // user id
            .child("Followers")
        followersRef.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    (idsList as ArrayList<String>).clear()
                    for (snapshot in dataSnapshot.children) {
                        (idsList as ArrayList<String>).add(snapshot.key!!)
                    }
                    showUsers()
                }
            }
        })
    }

    private fun getFollowings() {
        val followingsRef = FirebaseDatabase.getInstance().reference.child("Follow")
            .child(id) // user id
            .child("Following")
        followingsRef.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    (idsList as ArrayList<String>).clear()
                    for (snapshot in dataSnapshot.children) {
                        (idsList as ArrayList<String>).add(snapshot.key!!)
                    }
                    showUsers()
                }
            }
        })
    }

    private fun getLikes() {
        val likesRef =  FirebaseDatabase.getInstance().reference.child("Likes")
            .child(id) //post id
        likesRef.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    (idsList as ArrayList<String>).clear()
                    for (sp in snapshot.children) {
                        (idsList as ArrayList<String>).add(sp.key!!)
                    }
                    showUsers()
                }
            }

        })
    }

    private fun showUsers() {
        val usersRef = Firebase.database.reference.child("Users")
        usersRef.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    (usersList as ArrayList<User>).clear()
                    for (snapshot in dataSnapshot.children) {
                        val user = snapshot.getValue(User::class.java)!!
                        for (id in idsList!!) {
                            if (user.getUID() == id) {
                                (usersList as ArrayList<User>).add(user)
                            }
                        }

                    }
                    userAdapter?.notifyDataSetChanged()
                }
            }
        })
    }
}