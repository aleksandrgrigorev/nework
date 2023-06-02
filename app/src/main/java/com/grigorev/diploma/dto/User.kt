package com.grigorev.diploma.dto

data class User(
    val id: Int,
    val login: String?,
    val name: String,
    val avatar: String? = null
)