package com.clean.filecleaner.ui.module.junk.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class JunkSearchViewModel : ViewModel() {

    fun searchJunk() {

        viewModelScope.launch(Dispatchers.IO + SupervisorJob()) {




        }

    }

}