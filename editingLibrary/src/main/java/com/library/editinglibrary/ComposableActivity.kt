package com.library.editinglibrary

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.burhanrashid52.photoediting.PropertiesBSFragment
import com.burhanrashid52.photoediting.ShapeBSFragment
import com.library.editinglibrary.composables.EditingUi
import com.library.editinglibrary.composables.EditingViewModel
import com.library.editinglibrary.ui.theme.MyContentEditorTheme
import ja.burhanrashid52.photoeditor.shape.ShapeType

class ComposableActivity : AppCompatActivity(),
    PropertiesBSFragment.Properties, ShapeBSFragment.Properties{

    val viewModel by viewModels<EditingViewModel>()

    lateinit var mPropertiesBSFragment: PropertiesBSFragment
    lateinit var mShapeBSFragment: ShapeBSFragment


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        initFragments()
        setContent {
            MyContentEditorTheme {
                Surface(
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        Column(
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            EditingUi(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }


    private fun initFragments(){
        mPropertiesBSFragment = PropertiesBSFragment()
        mShapeBSFragment = ShapeBSFragment()
        mPropertiesBSFragment.setPropertiesChangeListener(this)
        mShapeBSFragment.setPropertiesChangeListener(this)
    }


    override fun onColorChanged(colorCode: Int) {
        viewModel.mPhotoEditor.setShape(viewModel.mShapeBuilder.withShapeColor(colorCode))
    }

    override fun onOpacityChanged(opacity: Int) {
        viewModel.mPhotoEditor.setShape(viewModel.mShapeBuilder.withShapeOpacity(opacity))
    }

    override fun onShapeSizeChanged(shapeSize: Int) {
        viewModel.mPhotoEditor.setShape(viewModel.mShapeBuilder.withShapeSize(shapeSize.toFloat()))
    }

    override fun onShapePicked(shapeType: ShapeType) {
        viewModel.mPhotoEditor.setShape(viewModel.mShapeBuilder.withShapeType(shapeType))
    }

    companion object {
        const val TAG = "EditImageActivity"
        const val PINCH_TEXT_SCALABLE_INTENT_KEY = "PINCH_TEXT_SCALABLE"
    }
}