package com.grigorev.diploma.dto

data class Post(
    val id: Int = 0,
    val author: String = "",
    val authorAvatar: String? = null,
    val authorId: Int = 0,
    val authorJob: String? = "",
    val content: String = "",
    val likedByMe: Boolean = false,
    val link: String? = null,
    val mentionedMe: Boolean = false,
    val ownedByMe: Boolean = false,
    val published: String = "",
    val attachment: Attachment? = null
)

data class Attachment(
    val url: String,
    val type: AttachmentType,
)

enum class AttachmentType {
    IMAGE, AUDIO, VIDEO
}