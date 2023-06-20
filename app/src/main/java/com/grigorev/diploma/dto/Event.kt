package com.grigorev.diploma.dto

data class Event(
    val id: Int = 0,
    val author: String = "",
    val authorAvatar: String? = null,
    val authorId: Int = 0,
    val authorJob: String? = null,
    val content: String = "",
    val datetime: String = "",
    val likeOwnerIds: Set<Int> = emptySet(),
    val likedByMe: Boolean = false,
    val link: String? = null,
    val ownedByMe: Boolean = false,
    val participantsIds: Set<Int> = emptySet(),
    val participatedByMe: Boolean = false,
    val published: String = "",
    val speakerIds: Set<Int> = emptySet(),
    val type: EventType = EventType.ONLINE,
    val users: Map<Int, User> = emptyMap(),
    val attachment: Attachment? = null
)

enum class EventType {
    ONLINE, OFFLINE
}