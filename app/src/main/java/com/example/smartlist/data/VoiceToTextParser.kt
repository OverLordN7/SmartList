package com.example.smartlist.data

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.example.smartlist.model.VoiceCommand
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class VoiceToTextParserState(
    val spokenText: String = "",
    val isSpeaking: Boolean = false,
    val error: String? = null,
    val command: VoiceCommand? = null
)

class VoiceToTextParser(private val app: Application): RecognitionListener {

    private val _state = MutableStateFlow(VoiceToTextParserState())
    val state: StateFlow<VoiceToTextParserState>
        get() = _state.asStateFlow()

    var commandCallBack: ((VoiceCommand)-> Unit)? = null

    val recognizer = SpeechRecognizer.createSpeechRecognizer(app)

    val TAG = "VoiceParser is Alive!"

    fun startListening(languageCode: String = "en"){
        _state.update { VoiceToTextParserState()}

        if (!SpeechRecognizer.isRecognitionAvailable(app)){
            _state.update {
                it.copy(
                    error = "Recognition is not available."
                )
            }
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE,languageCode)
        }

        recognizer.setRecognitionListener(this)
        recognizer.startListening(intent)

        _state.update {
            it.copy(
                isSpeaking = true
            )
        }
    }

    fun stopListening(){
        _state.update {
            it.copy(
                isSpeaking = false
            )
        }
        recognizer.stopListening()
    }

    override fun onReadyForSpeech(params: Bundle?){
        _state.update {
            it.copy(
                error = null
            )
        }
    }

    override fun onEndOfSpeech() {
        _state.update {
            it.copy(
                isSpeaking = false
            )
        }
    }

    override fun onError(error: Int) {
        if (error == SpeechRecognizer.ERROR_CLIENT){
            return
        }
        _state.update {
            it.copy(
                error = "Error: $error"
            )
        }
    }

    override fun onResults(results: Bundle?) {
        results
            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            ?.getOrNull(0)
            ?.let { result->
                val command = VoiceCommand(result)
                _state.update {
                    it.copy(
                        spokenText = result,
                        command = command
                    )
                }
                commandCallBack?.invoke(command)
            }
    }





    override fun onBeginningOfSpeech() = Unit

    override fun onRmsChanged(rmsdB: Float) = Unit

    override fun onBufferReceived(buffer: ByteArray?) = Unit


    override fun onPartialResults(partialResults: Bundle?) = Unit

    override fun onEvent(eventType: Int, params: Bundle?) = Unit
}