package com.carson.myinsta.model

class Story {
    private var imageUrl: String = ""
    private var timeStart: Long = 0
    private var timeEnd: Long = 0
    private var storyId: String = ""
    private var userId: String = ""

    constructor()

    constructor(imageUrl: String, timeStart: Long, timeEnd: Long, storyId: String, userId: String) {
        this.imageUrl = imageUrl
        this.timeStart = timeStart
        this.timeEnd = timeEnd
        this.storyId = storyId
        this.userId = userId
    }

    fun getImageUrl(): String {
        return this.imageUrl
    }

    fun getTimeStart(): Long {
        return this.timeStart
    }

    fun getTimeEnd(): Long {
        return this.timeEnd
    }

    fun getStoryId(): String {
        return this.storyId
    }

    fun getUserId(): String {
        return this.userId
    }

    fun setImageUrl(url: String) {
        this.imageUrl = url
    }

    fun setTimeStart(ts: Long) {
        this.timeStart = ts
    }

    fun setTimeEnd(te: Long) {
        this.timeEnd = te
    }

    fun setStoryId(storyId: String) {
        this.storyId = storyId
    }

    fun setUserId(userId: String) {
        this.userId = userId
    }
}