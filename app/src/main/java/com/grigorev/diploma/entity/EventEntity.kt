package com.grigorev.diploma.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.grigorev.diploma.dto.Event
import com.grigorev.diploma.dto.EventType

@Entity
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val authorId: Int,
    val author: String,
    val authorAvatar: String?,
    val content: String,
    val datetime: String,
    val published: String,
    @Embedded
    val type: EventTypeEntity,
    val likeOwnerIds: Set<Int> = emptySet(),
    val likedByMe: Boolean = false,
    val speakerIds: Set<Int> = emptySet(),
    val participantsIds: Set<Int> = emptySet(),
    val participatedByMe: Boolean = false,
    @Embedded
    val attachment: AttachmentEntity?,
    val link: String? = null,
    val ownedByMe: Boolean = false,
) {
    fun toDto() = Event(
        id = id,
        authorId = authorId,
        author = author,
        authorAvatar = authorAvatar,
        content = content,
        datetime = datetime,
        published = published,
        type = type.toDto(),
        likeOwnerIds = likeOwnerIds,
        likedByMe = likedByMe,
        speakerIds = speakerIds,
        participantsIds = participantsIds,
        participatedByMe = participatedByMe,
        attachment = attachment?.toDto(),
        link = link,
        ownedByMe = ownedByMe,
    )

    companion object {
        fun fromDto(dto: Event) =
            EventEntity(
                dto.id,
                dto.authorId,
                dto.author,
                dto.authorAvatar,
                dto.content,
                dto.datetime,
                dto.published,
                EventTypeEntity.fromDto(dto.type),
                dto.likeOwnerIds,
                dto.likedByMe,
                dto.speakerIds,
                dto.participantsIds,
                dto.participatedByMe,
                AttachmentEntity.fromDto(dto.attachment),
                dto.link,
                dto.ownedByMe,
            )
    }
}

fun List<EventEntity>.toDto() = map(EventEntity::toDto)
fun List<Event>.toEventEntity() = map(EventEntity.Companion::fromDto)

data class EventTypeEntity(
    val eventType: String,
) {
    fun toDto() = EventType.valueOf(eventType)

    companion object {
        fun fromDto(dto: EventType) = EventTypeEntity(dto.name)
    }
}