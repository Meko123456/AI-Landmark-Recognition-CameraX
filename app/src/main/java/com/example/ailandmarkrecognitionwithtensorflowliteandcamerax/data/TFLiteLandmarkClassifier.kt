package com.example.ailandmarkrecognitionwithtensorflowliteandcamerax.data

import android.content.Context
import android.graphics.Bitmap
import android.view.Surface
import com.example.ailandmarkrecognitionwithtensorflowliteandcamerax.domain.Classification
import com.example.ailandmarkrecognitionwithtensorflowliteandcamerax.domain.LandmarkClassifier
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.core.vision.ImageProcessingOptions
import org.tensorflow.lite.task.vision.classifier.ImageClassifier

class TFLiteLandmarkClassifier(
    private val context: Context,
    private val threshold: Float = 0.5f,
    private val maxResults: Int = 1,

    ) : LandmarkClassifier {

    private var classifier: ImageClassifier? = null

    private fun setUpClassifier() {
        val baseOptions = BaseOptions.builder()
            .setNumThreads(2)
            .build()
        val option = ImageClassifier.ImageClassifierOptions.builder()
            .setBaseOptions(baseOptions)
            .setMaxResults(maxResults)
            .setScoreThreshold(threshold)
            .build()

        try {
            classifier = ImageClassifier.createFromFileAndOptions(
                context,
                "lite-model_on_device_vision_classifier_landmarks_classifier_europe_V1_1.tflite",
                option
            )
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }

    }

    override fun classify(bitmap: Bitmap, rotation: Int): List<Classification> {
        if (classifier == null) {
            setUpClassifier()
        }

        val imageProcess = ImageProcessor.Builder().build()
        val tensorImage = imageProcess.process(TensorImage.fromBitmap(bitmap))

        val imageProcessingOption = ImageProcessingOptions.builder()
            .setOrientation(getOrientationFromRotation(rotation)).build()

        val result = classifier?.classify(tensorImage, imageProcessingOption)

        return result?.flatMap { classifications ->
            classifications.categories.map { category ->
                Classification(
                    name = category.displayName,
                    score = category.score
                )
            }
        }?.distinctBy { it.name } ?: emptyList()
    }

    private fun getOrientationFromRotation(rotation: Int): ImageProcessingOptions.Orientation {
        return when (rotation) {
            Surface.ROTATION_270 -> ImageProcessingOptions.Orientation.BOTTOM_RIGHT
            Surface.ROTATION_90 -> ImageProcessingOptions.Orientation.TOP_LEFT
            Surface.ROTATION_180 -> ImageProcessingOptions.Orientation.RIGHT_BOTTOM
            else -> ImageProcessingOptions.Orientation.RIGHT_TOP

        }
    }
}