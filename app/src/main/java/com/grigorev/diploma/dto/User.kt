package com.grigorev.diploma.dto

import android.net.Uri
import java.io.File

data class User(
    val id: Int,
    val login: String?,
    val name: String,
    val avatar: String? = null
)

data class PhotoModel(val uri: Uri? = null, val file: File? = null)