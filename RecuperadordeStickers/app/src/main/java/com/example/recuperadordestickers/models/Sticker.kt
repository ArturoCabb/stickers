package com.example.recuperadordestickers.models

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Sticker(
    val uri: Uri,
    val imageFileName: String? = null,
    val emojis: List<String?>? = null,
    val accessibilityText: String? = null,
    var size: Long = 0
) : Parcelable
