package com.carson.myinsta.model

class Notification {
    private var userId: String = ""
    private var description: String = ""
    private var postId: String = ""
    private var isPost: Boolean = false

    constructor()
    constructor(userId: String, description: String, postId: String, isPost: Boolean) {
        this.userId = userId
        this.description = description
        this.postId = postId
        this.isPost = isPost
    }

    fun setUserId(userId: String) {
        this.userId = userId
    }

    fun setDescription(desc: String) {
        this.description = desc
    }

    fun setPostId(postId: String) {
        this.postId = postId
    }

    fun setIsPost(isPost: Boolean) {
        this.isPost = isPost
    }

    fun getUserId(): String {
        return this.userId
    }

    fun getDescription(): String {
        return this.description
    }

    fun getPostId(): String {
        return this.postId
    }

    fun getIsPost(): Boolean {
        return this.isPost
    }
}