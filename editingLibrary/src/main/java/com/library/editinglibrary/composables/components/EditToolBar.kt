package com.library.editinglibrary.composables.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.library.editinglibrary.composables.EditingModel

@Composable
fun EditToolBar(
    editingModel: EditingModel,
    onEditClick: () -> Unit,
    onSaveClick: () -> Unit,
    onWrite: () -> Unit,
    onUndo: () -> Unit,
    onEraser: () -> Unit,
    onDownload: () -> Unit
) {
    Box(
        modifier = Modifier.background(Color.Black)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(
                    onClick = {
                        editingModel.unDoAvailable = editingModel.mPhotoEditor.isUndoAvailable
                        onEditClick()
                    }
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "edit")
                }
                Text("Edit")
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(
                    onClick = {
                        editingModel.unDoAvailable = editingModel.mPhotoEditor.isUndoAvailable
                        onSaveClick()
                    }
                ) {
                    Icon(imageVector = Icons.Default.Done, contentDescription = "save")
                }
                Text("Save")
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(
                    onClick = {
                        editingModel.unDoAvailable = editingModel.mPhotoEditor.isUndoAvailable
                        onWrite()
                    }
                ) {
                    Icon(imageVector = Icons.Default.Create, contentDescription = "Write")
                }
                Text("Write")
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(
                    enabled = editingModel.unDoAvailable,
                    onClick = {
                        editingModel.unDoAvailable = editingModel.mPhotoEditor.isUndoAvailable
                        if (editingModel.unDoAvailable){
                            onUndo()
                        }

                    }
                ) {
                    Icon(imageVector = Icons.Default.KeyboardArrowLeft, contentDescription = "Undo")
                }
                Text("Undo")
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(
                    onClick = {
                        editingModel.unDoAvailable = editingModel.mPhotoEditor.isUndoAvailable
                        onEraser()
                    }
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Eraser")
                }
                Text("Eraser")
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(
                    onClick = {
                        onDownload()
                    }
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Download")
                }
                Text("Download")
            }
        }
    }
}