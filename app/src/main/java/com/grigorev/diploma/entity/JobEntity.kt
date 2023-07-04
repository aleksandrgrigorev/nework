package com.grigorev.diploma.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.grigorev.diploma.dto.Job

@Entity
data class JobEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String = "",
    val position: String = "",
    val start: String = "",
    val finish: String? = null,
    val link: String? = null,
) {

    fun toDto(): Job = Job(
        id = id,
        name = name,
        position = position,
        start = start,
        finish = finish,
        link = link
    )

    companion object {
        fun fromDto(jobDto: Job): JobEntity {
            return JobEntity(
                jobDto.id, jobDto.name, jobDto.position, jobDto.start, jobDto.finish, jobDto.link,
            )
        }
    }
}

fun List<JobEntity>.toDto(): List<Job> = map(JobEntity::toDto)
fun List<Job>.fromDto(): List<JobEntity> = map(JobEntity::fromDto)