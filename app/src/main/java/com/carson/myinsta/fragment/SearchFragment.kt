package com.carson.myinsta.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.carson.myinsta.R
import com.carson.myinsta.adapter.UserAdapter
import com.carson.myinsta.model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_search.view.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SearchFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SearchFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var recyclerView: RecyclerView? = null
    private var userAdapter: UserAdapter? = null
    private var mUsers: MutableList<User>? = null

    private var database = Firebase.database
    private var usersRef = database.getReference("Users")

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
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        recyclerView = view.findViewById(R.id.recycler_view_search)
        recyclerView?.setHasFixedSize(true)
        recyclerView?.layoutManager = LinearLayoutManager(context)

        mUsers = ArrayList<User>()
        userAdapter = context?.let {
            UserAdapter(it, mUsers as ArrayList<User>, true)
        }
        recyclerView?.adapter = userAdapter

        view.edit_text_search.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                if (view.edit_text_search.text.toString().isEmpty()) {

                } else {
                    recyclerView?.visibility = View.VISIBLE
                    //retrieve users
                    retrieveUsers()
                    searchUser(s.toString().toLowerCase()) //non case-sensitive search
                }
            }
        })
        return view
    }

    private fun retrieveUsers() {
        usersRef.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (view?.edit_text_search?.text.toString() == "") {
                    mUsers?.clear()
                    for (snapshot in dataSnapshot.children ) {
                        //save as an user object
                        val user = snapshot.getValue(User::class.java)
                        if (user != null) {
                            //add to list
                            mUsers?.add(user as User)
                        }
                    }
                    userAdapter?.notifyDataSetChanged()
                }
            }
        })
    }

    private fun searchUser(input: String) {
        val query = usersRef
            .orderByChild("fullName")
            .startAt(input)
            .endAt(input + "\uf8ff")
        query.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                mUsers?.clear()
                for (snapshot in dataSnapshot.children ) {
                    //save as an user object
                    val user = snapshot.getValue(User::class.java)
                    if (user != null) {
                        //add to list
                        mUsers?.add(user as User)
                    }
                }
                userAdapter?.notifyDataSetChanged()
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
         * @return A new instance of fragment SearchFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SearchFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}