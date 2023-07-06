package com.example.smartlist.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.smartlist.SmartListApplication
import com.example.smartlist.data.VoiceToTextParser
import com.example.smartlist.model.VoiceCommand
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

private const val TAG = "HomeViewModel"

class HomeViewModel( val voiceToTextParser: VoiceToTextParser):ViewModel() {

    private val _voiceCommand  = MutableStateFlow<VoiceCommand?>(null)
    val voiceCommand: StateFlow<VoiceCommand?>
        get() = _voiceCommand


    init {
        voiceToTextParser.commandCallBack = { command ->
            _voiceCommand.value = command
        }
    }

    fun clearVoiceCommand(){
        _voiceCommand.value = null
    }

    fun startListening(languageCode: String = "ru") {
        voiceToTextParser.startListening(languageCode)
    }

    fun stopListening() {
        voiceToTextParser.stopListening()
    }

    companion object{
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as SmartListApplication)
                val voiceToTextParser = VoiceToTextParser(application)
                HomeViewModel(voiceToTextParser)
            }
        }
    }
}