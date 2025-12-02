package com.example.recuperadordestickers.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// Solo puede contener una lista de 30 stickers

@Parcelize
data class StickerPack(
    val identifier: String? = "0",
    val name: String? = "Default name",
    val publisher: String? = "Potro",
    val trayImageFile: String? = "replace with image converted to .png",
    val publisherEmail: String? = "arturcabbb@gmail.com",
    val publisherWebsite: String? = "",
    val privacyPolicyWebsite: String? = "",
    val licenseAgreementWebsite: String? = "",
    val imageDataVersion: String? = "1",
    val avoidCache: Boolean = false,
    val animatedStickerPack: Boolean = false,
    var iosAppStoreLink: String? = null,
    var androidPlayStoreLink: String? = null,
    var isWhitelisted: Boolean = false,
    var stickers: List<Sticker> = emptyList()
) : Parcelable {
    val totalSize: Long
        get() = stickers.sumOf { it.size }
}