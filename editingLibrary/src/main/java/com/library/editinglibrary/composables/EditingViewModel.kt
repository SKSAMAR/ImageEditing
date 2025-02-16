package com.library.editinglibrary.composables

import android.graphics.Bitmap
import android.view.ScaleGestureDetector
import android.widget.FrameLayout
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import ja.burhanrashid52.photoeditor.PhotoEditor
import ja.burhanrashid52.photoeditor.shape.ShapeBuilder

class EditingViewModel : ViewModel() {

    var editingModels = mutableStateMapOf<Int, EditingModel>()
    var currentEditingModel by mutableStateOf<EditingModel?>(null)


}

class EditingModel {
    lateinit var initialBitmap: Bitmap
    var unDoAvailable by mutableStateOf(false)
    lateinit var mPhotoEditor: PhotoEditor
    lateinit var mShapeBuilder: ShapeBuilder
    lateinit var scaleGestureDetector: ScaleGestureDetector
    var scaleFactor = 1.0f
    lateinit var frameLayout: FrameLayout
    var lastTouchX: Float = 0f
    var lastTouchY: Float = 0f
    var currentTranslateX: Float = 0f
    var currentTranslateY: Float = 0f
}