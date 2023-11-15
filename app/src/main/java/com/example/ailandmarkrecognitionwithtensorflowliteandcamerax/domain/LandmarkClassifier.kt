package com.example.ailandmarkrecognitionwithtensorflowliteandcamerax.domain

import android.graphics.Bitmap

interface LandmarkClassifier {

    fun classify(bitmap: Bitmap, rotation: Int): List<Classification>
}