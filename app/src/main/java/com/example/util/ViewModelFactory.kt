package com.example.util

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.MyApplication

inline fun <reified T : ViewModel> viewModelFactory(
    crossinline creator: (MyApplication) -> T
): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
    override fun <V : ViewModel> create(modelClass: Class<V>, extras: CreationExtras): V {
        val application = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MyApplication
        @Suppress("UNCHECKED_CAST")
        return creator(application) as V
    }
}
