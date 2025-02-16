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
    LaunchedEffect(Unit) {
        viewModel.scaleGestureDetector = ScaleGestureDetector(activity, ScaleListener(viewModel = viewModel))
    }
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        AndroidView(
            modifier = Modifier.weight(1f),
            factory = { context ->
                FrameLayout(
                    context, null
                ).apply {
                    viewModel.frameLayout = this // Initialize frameLayout here!
                    addView(
                        PhotoEditorView(
                            context = context
                        ).apply {
                            source.setImageResource(R.drawable.paris_tower)
                            makePhotoPinchAble(viewModel = viewModel, activity = activity)
                            setFrameLayoutOnTouchListener(viewModel = viewModel)
                            addFrameLayoutOnLayoutChangeListener()
                        }
                    )
                }
            }
        )
        EditToolBar(
            viewModel = viewModel,
            onEditClick = {
                activity.editSomething(viewModel = viewModel)
            },
            onSaveClick = {
                viewModel.mPhotoEditor.setBrushDrawingMode(false)
            },
            onWrite = {
                activity.writeSomething(viewModel = viewModel)
            },
            onUndo = {
                viewModel.mPhotoEditor.undo()
            },
            onEraser = {
                viewModel.mPhotoEditor.brushEraser()
            }
        )
    }
}

fun PhotoEditorView.makePhotoPinchAble(viewModel: EditingViewModel, activity: ComposableActivity) {
    val pinchTextScalable =
        activity.intent.getBooleanExtra(ComposableActivity.PINCH_TEXT_SCALABLE_INTENT_KEY, true)
    viewModel.mPhotoEditor = PhotoEditor.Builder(activity, this)
        .setPinchTextScalable(pinchTextScalable)
        .build()
    viewModel.mPhotoEditor.setOnPhotoEditorListener(object : OnPhotoEditorListener {
        override fun onEditTextChangeListener(rootView: View, text: String, colorCode: Int) {
            val textEditorDialogFragment = TextEditorDialogFragment.show(activity, text, colorCode)
            textEditorDialogFragment.setOnTextEditorListener(object :
                TextEditorDialogFragment.TextEditorListener {
                override fun onDone(inputText: String, colorCode: Int) {
                    val styleBuilder = TextStyleBuilder()
                    styleBuilder.withTextColor(colorCode)
                    viewModel.mPhotoEditor.editText(rootView, inputText, styleBuilder)
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

            viewModel.unDoAvailable = viewModel.mPhotoEditor.isUndoAvailable
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
fun FrameLayout.setFrameLayoutOnTouchListener(viewModel: EditingViewModel) {
    this.setOnTouchListener { _, motionEvent ->
        viewModel.scaleGestureDetector.onTouchEvent(motionEvent)

        if (!viewModel.scaleGestureDetector.isInProgress) {
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    viewModel.lastTouchX = motionEvent.x
                    viewModel.lastTouchY = motionEvent.y
                }

                MotionEvent.ACTION_MOVE -> {
                    val deltaX = motionEvent.x - viewModel.lastTouchX
                    val deltaY = motionEvent.y - viewModel.lastTouchY

                    val newX = (viewModel.currentTranslateX + deltaX).coerceIn(
                        -width.toFloat(),
                        width.toFloat()
                    )
                    val newY = (viewModel.currentTranslateY + deltaY).coerceIn(
                        -height.toFloat(),
                        height.toFloat()
                    )

                    translationX = newX
                    translationY = newY
                }

                MotionEvent.ACTION_UP -> {
                    viewModel.currentTranslateX = translationX
                    viewModel.currentTranslateY = translationY
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

fun AppCompatActivity.writeSomething(viewModel: EditingViewModel){
    val textEditorDialogFragment = TextEditorDialogFragment.show(this)
    textEditorDialogFragment.setOnTextEditorListener(object :
        TextEditorDialogFragment.TextEditorListener {
        override fun onDone(inputText: String, colorCode: Int) {
            val styleBuilder = TextStyleBuilder()
            styleBuilder.withTextColor(colorCode)
            viewModel.mPhotoEditor.addText(inputText, styleBuilder)
        }
    })
}

fun ComposableActivity.editSomething(viewModel: EditingViewModel){
    viewModel.mPhotoEditor.setBrushDrawingMode(true)
    viewModel.mShapeBuilder = ShapeBuilder()
    viewModel.mPhotoEditor.setShape(viewModel.mShapeBuilder)
    if (mShapeBSFragment.isAdded) {
        return
    }
    mShapeBSFragment.show(supportFragmentManager, mShapeBSFragment.tag)
}

class ScaleListener(private val viewModel: EditingViewModel) :
    ScaleGestureDetector.SimpleOnScaleGestureListener() {
    override fun onScale(detector: ScaleGestureDetector): Boolean {
        viewModel.scaleFactor *= detector.scaleFactor
        viewModel.scaleFactor = viewModel.scaleFactor.coerceIn(1.0f, 3.0f)

        // Ensure pivot remains within bounds
        val focusX = detector.focusX.coerceIn(0f, viewModel.frameLayout.width.toFloat())
        val focusY = detector.focusY.coerceIn(0f, viewModel.frameLayout.height.toFloat())

        viewModel.frameLayout.pivotX = focusX
        viewModel.frameLayout.pivotY = focusY

        viewModel.frameLayout.scaleX = viewModel.scaleFactor
        viewModel.frameLayout.scaleY = viewModel.scaleFactor

        // Prevent pivot issues when zooming out
        if (viewModel.scaleFactor <= 1.0f) {
            viewModel.frameLayout.pivotX = viewModel.frameLayout.width / 2f
            viewModel.frameLayout.pivotY = viewModel.frameLayout.height / 2f
        }

        return true
    }

}
