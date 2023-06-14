package com.grigorev.diploma.dto

import android.net.Uri
import java.io.File

data class Post(
    val id: Int = 0,
    val author: String = "",
    val authorAvatar: String? = null,
    val authorId: Int = 0,
    val authorJob: String? = "",
    val content: String = "",
    val likedByMe: Boolean = false,
    val link: String? = null,
    val mentionIds: Set<Long> = emptySet(),
    val mentionedMe: Boolean,
    val likeOwnerIds: Set<Long> = emptySet(),
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

data class PhotoModel(val uri: Uri? = null, val file: File? = null)