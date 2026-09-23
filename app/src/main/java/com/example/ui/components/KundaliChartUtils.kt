package com.example.ui.components

import com.example.core.model.Graha

fun Graha.shortCode(): String = when (this) {
    Graha.SURYA -> "Su"
    Graha.CHANDRA -> "Mo"
    Graha.MANGALA -> "Ma"
    Graha.BUDHA -> "Me"
    Graha.GURU -> "Ju"
    Graha.SHUKRA -> "Ve"
    Graha.SHANI -> "Sa"
    Graha.RAHU -> "Ra"
    Graha.KETU -> "Ke"
}

fun Graha.devanagariShort(): String = when (this) {
    Graha.SURYA -> "सू"
    Graha.CHANDRA -> "चं"
    Graha.MANGALA -> "मं"
    Graha.BUDHA -> "बु"
    Graha.GURU -> "गु"
    Graha.SHUKRA -> "शु"
    Graha.SHANI -> "श"
    Graha.RAHU -> "रा"
    Graha.KETU -> "के"
}
