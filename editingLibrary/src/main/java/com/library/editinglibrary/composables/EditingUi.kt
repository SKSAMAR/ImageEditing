package com.library.editinglibrary.composables

import android.annotation.SuppressLint
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.FrameLayout
import androidx.activity.compose.LocalActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.library.editinglibrary.ComposableActivity
import com.library.editinglibrary.ComposableActivity.Companion.TAG
import com.library.editinglibrary.R
import com.library.editinglibrary.components.TextEditorDialogFragment
import com.library.editinglibrary.composables.components.EditToolBar
import ja.burhanrashid52.photoeditor.OnPhotoEditorListener
import ja.burhanrashid52.photoeditor.PhotoEditor
import ja.burhanrashid52.photoeditor.PhotoEditorView
import ja.burhanrashid52.photoeditor.TextStyleBuilder
import ja.burhanrashid52.photoeditor.ViewType
import ja.burhanrashid52.photoeditor.shape.ShapeBuilder

@Composable
fun EditingUi(viewModel: EditingViewModel) {
    val activity = LocalActivity.current as ComposableActivity
    LaunchedEffect(viewModel.currentEditingModel) {
        viewModel.currentEditingModel?.let {
            viewModel.currentEditingModel?.scaleGestureDetector =
                ScaleGestureDetector(activity, ScaleListener(editingModel = it))
        }
    }

    viewModel.currentEditingModel?.let {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            AndroidView(
                modifier = Modifier.weight(1f),
                factory = { context ->
                    FrameLayout(
                        context, null
                    ).apply {
                        setBackgroundColor(android.graphics.Color.WHITE) // Or Color.White if you have imported androidx.compose.ui.graphics.Color
                        viewModel.currentEditingModel?.frameLayout = this // Initialize frameLayout here!
                        addView(
                            PhotoEditorView(
                                context = context
                            ).apply {
                                source.setImageBitmap(viewModel.currentEditingModel?.initialBitmap)
                                makePhotoPinchAble(
                                    editingModel = viewModel.currentEditingModel!!,
                                    activity = activity
                                )
                                setFrameLayoutOnTouchListener(editingModel = viewModel.currentEditingModel!!)
                                addFrameLayoutOnLayoutChangeListener()
                            }
                        )
                    }
                }
            )
            EditToolBar(
                editingModel = viewModel.currentEditingModel!!,
                onEditClick = {
                    activity.editSomething(viewModel = viewModel)
                },
                onSaveClick = {
                    viewModel.currentEditingModel?.mPhotoEditor?.setBrushDrawingMode(false)
                },
                onWrite = {
                    activity.writeSomething(editingModel = viewModel.currentEditingModel!!)
                },
                onUndo = {
                    viewModel.currentEditingModel?.mPhotoEditor?.undo()
                },
                onEraser = {
                    viewModel.currentEditingModel?.mPhotoEditor?.brushEraser()
                }
            )
        }
    }
}

fun PhotoEditorView.makePhotoPinchAble(editingModel: EditingModel, activity: ComposableActivity) {
    val pinchTextScalable =
        activity.intent.getBooleanExtra(ComposableActivity.PINCH_TEXT_SCALABLE_INTENT_KEY, true)
    editingModel.mPhotoEditor = PhotoEditor.Builder(activity, this)
        .setPinchTextScalable(pinchTextScalable)
        .build()
    editingModel.mPhotoEditor.setOnPhotoEditorListener(object : OnPhotoEditorListener {
        override fun onEditTextChangeListener(rootView: View, text: String, colorCode: Int) {
            val textEditorDialogFragment = TextEditorDialogFragment.show(activity, text, colorCode)
            textEditorDialogFragment.setOnTextEditorListener(object :
                TextEditorDialogFragment.TextEditorListener {
                override fun onDone(inputText: String, colorCode: Int) {
                    val styleBuilder = TextStyleBuilder()
                    styleBuilder.withTextColor(colorCode)
                    editingModel.mPhotoEditor.editText(rootView, inputText, styleBuilder)
                }
            })
        }

        override fun onAddViewListener(viewType: ViewType, numberOfAddedViews: Int) {
            Log.d(
                TAG,
                "onAddViewListener() called with: viewType = [$viewType], numberOfAddedViews = [$numberOfAddedViews]"
            )
        }

        override fun onRemoveViewListener(viewType: ViewType, numberOfAddedViews: Int) {
            Log.d(
                TAG,
                "onRemoveViewListener() called with: viewType = [$viewType], numberOfAddedViews = [$numberOfAddedViews]"
            )

            editingModel.unDoAvailable = editingModel.mPhotoEditor.isUndoAvailable
        }

        override fun onStartViewChangeListener(viewType: ViewType) {
            Log.d(TAG, "onStartViewChangeListener() called with: viewType = [$viewType]")
        }

        override fun onStopViewChangeListener(viewType: ViewType) {
            Log.d(TAG, "onStopViewChangeListener() called with: viewType = [$viewType]")
        }

        override fun onTouchSourceImage(event: MotionEvent) {
            Log.d(TAG, "onTouchView() called with: event = [$event]")
        }


    })
}

@SuppressLint("ClickableViewAccessibility")
fun FrameLayout.setFrameLayoutOnTouchListener(editingModel: EditingModel) {
    this.setOnTouchListener { _, motionEvent ->
        editingModel.scaleGestureDetector.onTouchEvent(motionEvent)

        if (!editingModel.scaleGestureDetector.isInProgress) {
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    editingModel.lastTouchX = motionEvent.x
                    editingModel.lastTouchY = motionEvent.y
                }

                MotionEvent.ACTION_MOVE -> {
                    val deltaX = motionEvent.x - editingModel.lastTouchX
                    val deltaY = motionEvent.y - editingModel.lastTouchY

                    val newX = (editingModel.currentTranslateX + deltaX).coerceIn(
                        -width.toFloat(),
                        width.toFloat()
                    )
                    val newY = (editingModel.currentTranslateY + deltaY).coerceIn(
                        -height.toFloat(),
                        height.toFloat()
                    )

                    translationX = newX
                    translationY = newY
                }

                MotionEvent.ACTION_UP -> {
                    editingModel.currentTranslateX = translationX
                    editingModel.currentTranslateY = translationY
                }
            }
        }
        true
    }
}

fun FrameLayout.addFrameLayoutOnLayoutChangeListener() {
    addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
        if (scaleX < 0.5f || scaleX > 3.0f) {
            scaleX = 1.0f
            scaleY = 1.0f
            translationX = 0f
            translationY = 0f
        }
    }
}

fun AppCompatActivity.writeSomething(editingModel: EditingModel) {
    val textEditorDialogFragment = TextEditorDialogFragment.show(this)
    textEditorDialogFragment.setOnTextEditorListener(object :
        TextEditorDialogFragment.TextEditorListener {
        override fun onDone(inputText: String, colorCode: Int) {
            val styleBuilder = TextStyleBuilder()
            styleBuilder.withTextColor(colorCode)
            editingModel.mPhotoEditor.addText(inputText, styleBuilder)
        }
    })
}

fun ComposableActivity.editSomething(viewModel: EditingViewModel) {
    viewModel.currentEditingModel?.mPhotoEditor?.setBrushDrawingMode(true)
    viewModel.currentEditingModel?.mShapeBuilder = ShapeBuilder()
    viewModel.currentEditingModel?.mPhotoEditor?.setShape(viewModel.currentEditingModel?.mShapeBuilder!!)
    if (mShapeBSFragment.isAdded) {
        return
    }
    mShapeBSFragment.show(supportFragmentManager, mShapeBSFragment.tag)
}

class ScaleListener(private val editingModel: EditingModel) :
    ScaleGestureDetector.SimpleOnScaleGestureListener() {
    override fun onScale(detector: ScaleGestureDetector): Boolean {
        editingModel.scaleFactor *= detector.scaleFactor
        editingModel.scaleFactor = editingModel.scaleFactor.coerceIn(1.0f, 3.0f)

        // Ensure pivot remains within bounds
        val focusX = detector.focusX.coerceIn(0f, editingModel.frameLayout.width.toFloat())
        val focusY = detector.focusY.coerceIn(0f, editingModel.frameLayout.height.toFloat())

        editingModel.frameLayout.pivotX = focusX
        editingModel.frameLayout.pivotY = focusY

        editingModel.frameLayout.scaleX = editingModel.scaleFactor
        editingModel.frameLayout.scaleY = editingModel.scaleFactor

        // Prevent pivot issues when zooming out
        if (editingModel.scaleFactor <= 1.0f) {
            editingModel.frameLayout.pivotX = editingModel.frameLayout.width / 2f
            editingModel.frameLayout.pivotY = editingModel.frameLayout.height / 2f
        }

        return true
    }

}
