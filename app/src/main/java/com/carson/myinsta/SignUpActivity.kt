package com.carson.myinsta

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_sign_in.*
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUpActivity : AppCompatActivity() {

    lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        mAuth = FirebaseAuth.getInstance()

        login_link_btn.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }

        register_btn.setOnClickListener {
            createAccount()
        }
    }

    private fun createAccount() {
        val fullName = fullName_register.text.toString()
        val userName = username_register.text.toString()
        val email = email_register.text.toString()
        val password = pwd_register.text.toString()

        when {
            TextUtils.isEmpty(fullName) -> Toast.makeText(this, "Full name is required.", Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(userName) -> Toast.makeText(this, "User name is required.", Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(email) -> Toast.makeText(this, "Email is required.", Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(password) -> Toast.makeText(this, "Password is required.", Toast.LENGTH_LONG).show()

            else -> {
                val progressDialog = ProgressDialog(this@SignUpActivity)
                progressDialog.setTitle("Sign Up")
                progressDialog.setMessage("Please watt, this may take a while")
                progressDialog.setCancelable(false)
                progressDialog.show()

                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                    if(it.isSuccessful) {
                        saveUserInfo(fullName, userName, email, progressDialog)
                    } else {
                        val errorMessage = it.exception!!.toString()
                        Toast.makeText(this, "Error: $errorMessage", Toast.LENGTH_LONG).show()
                        mAuth.signOut()
                        progressDialog.dismiss()
                    }
                }
            }
        }
    }

    private fun saveUserInfo(fullName: String, userName: String, email: String, progressDialog: ProgressDialog) {
        val currentUserID = FirebaseAuth.getInstance().currentUser!!.uid
        val database = Firebase.database
        val userRef = database.getReference("Users")

        val userMap = HashMap<String, Any>()
        userMap["uid"] = currentUserID
        userMap["fullname"] = fullName
        userMap["username"] = userName
        userMap["email"] = email
        userMap["bio"] = "Hey I am using the MyInsta"
        userMap["userImage"] = "https://firebasestorage.googleapis.com/v0/b/myinsta-7515c.appspot.com/o/default_images%2Fdefault_profile.png?alt=media&token=c60150b4-6f67-477f-8945-23554f0b38d2"

        userRef.child(currentUserID).setValue(userMap).addOnCompleteListener {
            if (it.isSuccessful) {
                progressDialog.dismiss()
                Toast.makeText(this, "Account has been created successfully", Toast.LENGTH_LONG).show()
                val intent = Intent(this@SignUpActivity, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            } else {
                val errorMessage = it.exception!!.toString()
                Toast.makeText(this, "Error: $errorMessage", Toast.LENGTH_LONG).show()
                mAuth.signOut()
                progressDialog.dismiss()
            }
        }
    }
}