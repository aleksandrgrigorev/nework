package com.grigorev.diploma.dto

data class Event(
    val id: Int = 0,
    val author: String = "",
    val authorAvatar: String? = null,
    val authorId: Int = 0,
    val authorJob: String? = null,
    val content: String = "",
    val datetime: String = "",
    val likeOwnerIds: List<Int> = emptyList(),
    val likedByMe: Boolean = false,
    val link: String? = null,
    val ownedByMe: Boolean = false,
    val participantsIds: List<Int> = emptyList(),
    val participatedByMe: Boolean = false,
    val published: String = "",
    val speakerIds: List<Int> = emptyList(),
    val type: EventType = EventType.ONLINE,
    val users: Map<Int, User> = emptyMap(),
    val attachment: Attachment? = null
)

enum class EventType {
    ONLINE, OFFLINE
}