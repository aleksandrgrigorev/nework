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
    val author: String,
    val authorAvatar: String?,
    val authorId: Int,
    val authorJob: String? = null,
    val content: String,
    val datetime: String,
    val likeOwnerIds: Set<Int> = emptySet(),
    val likedByMe: Boolean = false,
    val link: String? = null,
    val ownedByMe: Boolean = false,
    val participantsIds: Set<Int> = emptySet(),
    val participatedByMe: Boolean = false,
    val published: String,
    val speakerIds: Set<Int> = emptySet(),
    @Embedded
    val type: EventTypeEntity,
    @Embedded
    val attachment: AttachmentEntity?,
) {
    fun toDto() = Event(
        id,
        author,
        authorAvatar,
        authorId,
        authorJob,
        content,
        datetime,
        likeOwnerIds,
        likedByMe,
        link,
        ownedByMe,
        participantsIds,
        participatedByMe,
        published,
        speakerIds,
        type.toDto(),
        attachment?.toDto()
    )

    companion object {
        fun fromDto(dto: Event) =
            EventEntity(
                dto.id,
                dto.author,
                dto.authorAvatar,
                dto.authorId,
                dto.authorJob,
                dto.content,
                dto.datetime,
                dto.likeOwnerIds,
                dto.likedByMe,
                dto.link,
                dto.ownedByMe,
                dto.participantsIds,
                dto.participatedByMe,
                dto.published,
                dto.speakerIds,
                EventTypeEntity.fromDto(dto.type),
                AttachmentEntity.fromDto(dto.attachment),
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