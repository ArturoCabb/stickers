package com.example.recuperadordestickers.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class StickerPack(
    val identifier: String?,
    val name: String?,
    val publisher: String?,
    val trayImageFile: String?,
    val publisherEmail: String?,
    val publisherWebsite: String?,
    val privacyPolicyWebsite: String?,
    val licenseAgreementWebsite: String?,
    val imageDataVersion: String?,
    val avoidCache: Boolean,
    val animatedStickerPack: Boolean,
    var iosAppStoreLink: String? = null,
    var androidPlayStoreLink: String? = null,
    var isWhitelisted: Boolean = false,
    var stickers: List<Sticker> = emptyList()
) : Parcelable {
    val totalSize: Long
        get() = stickers.sumOf { it.size }
}