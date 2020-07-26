package com.carson.myinsta.model

class User {
    private var uid: String? = ""
    private var username: String? = ""
    private var email: String? = ""
    private var fullName: String? = ""
    private var userImageUrl: String? = ""
    private var bio: String? = ""

    constructor()

    constructor(
        uid: String?,
        username: String?,
        email: String?,
        fullName: String?,
        userImage: String?,
        bio: String?
    ) {
        this.uid = uid
        this.username = username
        this.email = email
        this.fullName = fullName
        this.userImageUrl = userImage
        this.bio = bio
    }

    fun getUsername(): String? {
        return username
    }

    fun getEmail(): String? {
        return email
    }
    fun getFullName(): String? {
        return fullName
    }

    fun getUserImageUrl(): String? {
        return userImageUrl
    }

    fun getBio(): String? {
        return bio
    }

    fun setUsername(username: String) {
        this.username = username
    }

    fun setEmail(email: String) {
        this.email = email
    }

    fun setFullName(fullName: String) {
        this.fullName = fullName
    }

    fun setUserImageUrl(userImage: String) {
        this.userImageUrl = userImage
    }

    fun setBio(bio: String) {
        this.bio = bio
    }

    fun getUID(): String? {
        return uid
    }

    fun setUID(uid: String) {
        this.uid = uid
    }

}
