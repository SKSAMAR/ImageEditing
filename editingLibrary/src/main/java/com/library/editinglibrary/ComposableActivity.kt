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
import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.library.editinglibrary.composables.EditingModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class ComposableActivity : AppCompatActivity(),
    PropertiesBSFragment.Properties, ShapeBSFragment.Properties{

    private val viewModel by viewModels<EditingViewModel>()
    lateinit var mPropertiesBSFragment: PropertiesBSFragment
    lateinit var mShapeBSFragment: ShapeBSFragment


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        initFragments()
        fetchFromAssets()
        setContent {
            MyContentEditorTheme {
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

    private fun fetchFromAssets(){
        lifecycleScope.launch {
            val bitmaps = loadPdfFromAssets(this@ComposableActivity, "sample.pdf")
            if (bitmaps != null) {
                viewModel.editingModels.clear()
                bitmaps.forEachIndexed { index, bitmap ->
                    val editingModel = EditingModel()
                    editingModel.initialBitmap = bitmap
                    viewModel.editingModels[index] = editingModel
                }
                viewModel.currentIndex = 0
                viewModel.currentEditingModel = viewModel.editingModels[0]
            } else {
                Log.e(TAG, "Failed to load PDF or convert to bitmaps")
            }
        }
    }


    private suspend fun loadPdfFromAssets(context: Context, fileName: String): List<Bitmap>? = withContext(Dispatchers.IO) {
        try {
            val assetManager = context.assets
            val inputStream = assetManager.open(fileName)

            // 1. Copy the PDF from assets to a temporary file
            val tempFile = File.createTempFile("pdf_temp", ".pdf", context.cacheDir)
            val outputStream = FileOutputStream(tempFile)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()


            // 2. Now use the temporary file to create the PdfRenderer
            val parcelFileDescriptor = ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY)
            val pdfRenderer = PdfRenderer(parcelFileDescriptor)
            val pageCount = pdfRenderer.pageCount
            val bitmaps = mutableListOf<Bitmap>()

            for (i in 0 until pageCount) {
                val page = pdfRenderer.openPage(i)
                val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                bitmaps.add(bitmap)
                page.close()
            }

            pdfRenderer.close()
            parcelFileDescriptor.close()
            tempFile.delete() // Clean up the temporary file

            return@withContext bitmaps

        } catch (e: IOException) {
            Log.e(TAG, "Error loading PDF from assets: ${e.message}")
            return@withContext null
        } catch (e: Exception) {
            Log.e(TAG, "Other error: ${e.message}")
            return@withContext null
        }
    }

    private fun initFragments(){
        mPropertiesBSFragment = PropertiesBSFragment()
        mShapeBSFragment = ShapeBSFragment()
        mPropertiesBSFragment.setPropertiesChangeListener(this)
        mShapeBSFragment.setPropertiesChangeListener(this)
    }


    override fun onColorChanged(colorCode: Int) {
        viewModel.currentEditingModel?.mPhotoEditor?.setShape(viewModel.currentEditingModel?.mShapeBuilder?.withShapeColor(colorCode)!!)
    }

    override fun onOpacityChanged(opacity: Int) {
        viewModel.currentEditingModel?.mPhotoEditor?.setShape(viewModel.currentEditingModel?.mShapeBuilder?.withShapeOpacity(opacity)!!)
    }

    override fun onShapeSizeChanged(shapeSize: Int) {
        viewModel.currentEditingModel?.mPhotoEditor?.setShape(viewModel.currentEditingModel?.mShapeBuilder?.withShapeSize(shapeSize.toFloat())!!)
    }

    override fun onShapePicked(shapeType: ShapeType) {
        viewModel.currentEditingModel?.mPhotoEditor?.setShape(viewModel.currentEditingModel?.mShapeBuilder?.withShapeType(shapeType)!!)
    }

    companion object {
        const val TAG = "EditImageActivity"
        const val PINCH_TEXT_SCALABLE_INTENT_KEY = "PINCH_TEXT_SCALABLE"
    }
}