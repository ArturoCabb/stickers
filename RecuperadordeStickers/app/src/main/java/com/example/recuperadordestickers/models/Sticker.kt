package com.example.recuperadordestickers.models

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Sticker(
    val uri: Uri,
    val imageFileName: String? = null,
    val emojis: List<String?>? = null, // Solo puede contener 3 emogies
    val accessibilityText: String? = null,
    var size: Long = 0
) : Parcelable
