
package com.carson.myinsta.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.carson.myinsta.R
import com.carson.myinsta.adapter.NotificationAdapter
import com.carson.myinsta.adapter.UserAdapter
import com.carson.myinsta.model.Notification
import com.carson.myinsta.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [NotificationsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class NotificationsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var currentUser: FirebaseUser? = null

    private var recyclerView: RecyclerView? = null
    private var notificationAdapter: NotificationAdapter? = null
    private var notificationsList: MutableList<Notification>? = null
    private var linearLayoutManager: LinearLayoutManager? = null

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
        val view =  inflater.inflate(R.layout.fragment_notifications, container, false)

        currentUser = FirebaseAuth.getInstance().currentUser

        recyclerView = view.findViewById(R.id.recycler_view_notifications_frag)
        recyclerView?.setHasFixedSize(true)

        linearLayoutManager = LinearLayoutManager(context)
        recyclerView?.layoutManager = linearLayoutManager

        notificationsList = ArrayList<Notification>()
        notificationAdapter = context?.let {
            NotificationAdapter(it, notificationsList as ArrayList<Notification>)
        }
        recyclerView?.adapter = notificationAdapter

        return view
    }

    override fun onStart() {
        super.onStart()
        readNotifications()
    }

    private fun readNotifications() {
        val notificationsRef = Firebase.database.reference
            .child("Notifications")
            .child(currentUser!!.uid)
        notificationsRef.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()){
                    (notificationsList as ArrayList<Notification>).clear()
                    for (snapshot in dataSnapshot.children) {
                        val notification = snapshot.getValue(Notification::class.java)
                        (notificationsList as ArrayList<Notification>).add(notification!!)
                    }
                    notificationsList?.reverse()
                    notificationAdapter?.notifyDataSetChanged()
                }
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
         * @return A new instance of fragment NotificationsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            NotificationsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}