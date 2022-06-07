package com.example.androidallfunctions

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainActivityVM @Inject constructor(): ViewModel() {
    /** Flashlight **/
    var isTorchON = false
    /** AutoRotate **/
    var isAutoRotateON = 0
}