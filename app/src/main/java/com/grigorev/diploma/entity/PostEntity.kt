package com.grigorev.diploma.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.grigorev.diploma.dto.Attachment
import com.grigorev.diploma.dto.AttachmentType
import com.grigorev.diploma.dto.Post

@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val author: String,
    val authorAvatar: String?,
    val authorId: Int,
    val authorJob: String?,
    val content: String,
    val likedByMe: Boolean,
    val link: String? = null,
    val mentionIds: Set<Int> = emptySet(),
    val mentionedMe: Boolean,
    val likeOwnerIds: Set<Int> = emptySet(),
    val ownedByMe: Boolean,
    val published: String,
    @Embedded
    val attachment: AttachmentEntity?
) {
    fun toDto() = Post(
        id,
        author,
        authorAvatar,
        authorId,
        authorJob,
        content,
        likedByMe,
        link,
        mentionIds,
        mentionedMe,
        likeOwnerIds,
        ownedByMe,
        published,
        attachment?.toDto()
    )

    companion object {
        fun fromDto(dto: Post) = PostEntity(
            dto.id,
            dto.author,
            dto.authorAvatar,
            dto.authorId,
            dto.authorJob,
            dto.content,
            dto.likedByMe,
            dto.link,
            dto.mentionIds,
            dto.mentionedMe,
            dto.likeOwnerIds,
            dto.ownedByMe,
            dto.published,
            AttachmentEntity.fromDto(dto.attachment)
        )
    }
}

@Entity
data class AttachmentEntity(
    val url: String,
    val type: AttachmentType,
) {
    fun toDto() = Attachment(url, type)

    companion object {
        fun fromDto(dto: Attachment?): AttachmentEntity? {
            return if (dto != null) AttachmentEntity(dto.url, dto.type) else null
        }
    }
}

fun List<PostEntity>.toDto() = map { it.toDto() }
fun List<Post>.toEntity() = map { PostEntity.fromDto(it) }