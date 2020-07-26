package com.carson.myinsta

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.carson.myinsta.fragament.ProfileFragment
import com.carson.myinsta.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_account_settings.*
import kotlinx.android.synthetic.main.fragment_profile.view.*

class AccountSettingsActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth;
    private lateinit var currentUser: FirebaseUser

    private var checker: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_settings)
        mAuth = FirebaseAuth.getInstance()
        currentUser = mAuth.currentUser!!

        logout_acct_settings.setOnClickListener {
            mAuth.signOut()
            val intent = Intent(this@AccountSettingsActivity, SignInActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        save_info_profile_btn.setOnClickListener{
            if (checker == "changeImageClicked") {

            } else {
                //only change texture information
                updateUserInfoOnly()
            }
        }

        userInfo()
    }

    private fun updateUserInfoOnly() {
        when {
            TextUtils.isEmpty(full_name_profile_frag_acct_setting.text.toString()) ->
                Toast.makeText(this, "Please write full name first.", Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(username_acct_settings.text.toString()) ->
                Toast.makeText(this, "Please write user name first.", Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(bio_profile_acct_settings.text.toString()) ->
                Toast.makeText(this, "Please write bio first.", Toast.LENGTH_LONG).show()
            else -> {
                val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(currentUser.uid)
                val userMap = HashMap<String, Any>()
                userMap["uid"] = currentUser.uid
                userMap["fullName"] = full_name_profile_frag_acct_setting.text.toString().toLowerCase()
                userMap["username"] = username_acct_settings.text.toString().toLowerCase()
                userMap["bio"] = bio_profile_acct_settings.text.toString()
                userRef.updateChildren(userMap).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(this, "Account Information has been updated successfully", Toast.LENGTH_LONG).show()
                        val intent = Intent(this@AccountSettingsActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }
    }

    private fun userInfo() {
        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(currentUser.uid)
        userRef.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()){
                    val user = dataSnapshot.getValue<User>(User::class.java)
                    Picasso.get().load(user!!.getUserImageUrl()).placeholder(R.drawable.profile).fit().into(profile_image_view_acct_settings)
                    username_acct_settings.setText(user.getUsername())
                    full_name_profile_frag_acct_setting.setText(user.getFullName())
                    bio_profile_acct_settings.setText(user.getBio())
                }
            }

        })
    }
}