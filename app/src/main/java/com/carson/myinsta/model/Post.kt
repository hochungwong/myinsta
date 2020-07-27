package com.carson.myinsta.model

class Post {
    private var postId: String = ""
    private var postImageUrl: String = ""
    private var publisherId: String = ""
    private var description: String = ""

    constructor()

    constructor(postId: String, postImageUrl: String, publisherId: String, description: String) {
        this.postId = postId
        this.postImageUrl = postImageUrl
        this.publisherId = publisherId
        this.description = description
    }

    fun getPostId(): String {
        return this.postId
    }
    fun getPostImageUrl(): String {
        return this.postImageUrl
    }
    fun getPublisherId(): String {
        return this.publisherId
    }
    fun getDescription(): String {
        return this.description
    }

    fun setPostId(postId: String) {
        this.postId = postId
    }
    fun setPostImageUrl(url: String) {
        this.postImageUrl = url
    }
    fun setPublisherId(publisherId: String) {
        this.publisherId = publisherId
    }
    fun setDescription(desc: String) {
        this.description = desc
    }
}