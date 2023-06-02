package com.grigorev.diploma.dto

data class Job(
    val id: Int = 0,
    val name: String = "",
    val position: String = "",
    val start: String = "",
    val finish: String? = null,
    val link: String? = null,
)