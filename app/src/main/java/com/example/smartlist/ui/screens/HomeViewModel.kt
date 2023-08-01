package com.example.smartlist.ui.screens

import android.content.Context
import android.preference.PreferenceManager
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import com.example.smartlist.R
import com.example.smartlist.SmartListApplication
import com.example.smartlist.data.DefaultPurchaseRepository
import com.example.smartlist.data.DishRepository
import com.example.smartlist.data.PurchaseRepository
import com.example.smartlist.data.VoiceToTextParser
import com.example.smartlist.model.PurchaseList
import com.example.smartlist.model.VoiceCommand
import com.example.smartlist.navigation.Screen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.util.prefs.Preferences

private const val TAG = "HomeViewModel"

class HomeViewModel(
    val voiceToTextParser: VoiceToTextParser,
    val purchaseRepository: PurchaseRepository,
    val dishRepository: DishRepository,
    application: SmartListApplication,
):ViewModel() {

    private val _voiceCommand  = MutableStateFlow<VoiceCommand?>(null)
    val voiceCommand: StateFlow<VoiceCommand?>
        get() = _voiceCommand


    //Create a sharedPreferences
    private val themeKey = "theme_pref"
    private val sharedPreferences = application.getSharedPreferences("my_pref_file_name",Context.MODE_PRIVATE)

    //Attributes for changing language of app
    private val langKey = "lang_pref"
    private val sharedLanguagePreferences = application.getSharedPreferences("my_pref_file_name",Context.MODE_PRIVATE)


    private val _currentLanguage = MutableStateFlow("en")
    val currentLanguage: StateFlow<String> = _currentLanguage

    fun setCurrentLanguage(language: String){
        _currentLanguage.value = language
        sharedLanguagePreferences.edit().putString(langKey,language).apply()
    }

    fun getCurrentLanguage(): String {
        return sharedLanguagePreferences.getString(langKey,"en")!!
    }


    private val _isDarkThemeEnabled = MutableStateFlow(isDarkThemeEnabled())
    val isDarkThemeEnabled: StateFlow<Boolean> = _isDarkThemeEnabled


    fun isDarkThemeEnabled(): Boolean{
        return sharedPreferences.getBoolean(themeKey,false)
    }

    fun setDarkThemeEnabled(isEnabled: Boolean){
        _isDarkThemeEnabled.value = isEnabled
        sharedPreferences.edit().putBoolean(themeKey,isEnabled).apply()
    }


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

    fun processNavigationCommand(
        command: VoiceCommand,
        currentScreen: String,
        navController: NavController,
        context: Context,
    ){
        val navigationTransition = context.getString(R.string.navigation_transition)
        val sameScreenMessage = context.getString(R.string.notification_message)
        val unknownVoiceCommandMessage = context.getString(R.string.unknown_command)

        var isCommandMatchSuccessful = false

        val mapOfScreen = mapOf(
            context.getString(R.string.home_screen) to Screen.HomeScreen.route,
            context.getString(R.string.purchase_screen) to Screen.PurchasesScreen.route,
            context.getString(R.string.dish_screen) to Screen.DishesScreen.route,
            context.getString(R.string.graph_screen) to Screen.GraphScreen.route,
            context.getString(R.string.settings_screen) to Screen.SettingScreen.route
        )

        // Show message if user already on currentScreen or switch otherwise
        if (command.text == currentScreen){
            Toast.makeText(context, sameScreenMessage, Toast.LENGTH_SHORT).show()
        }
        else{
            for (screen in mapOfScreen){
                if (command.text == screen.key){
                    isCommandMatchSuccessful = true
                    Toast.makeText(context, navigationTransition, Toast.LENGTH_SHORT).show()
                    clearVoiceCommand()
                    navController.navigate(screen.value)
                }
            }

            if (!isCommandMatchSuccessful){
                Toast.makeText(context, unknownVoiceCommandMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object{
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as SmartListApplication)
                val voiceToTextParser = VoiceToTextParser(application)
                val purchaseRepository = application.container.purchaseRepository
                val dishRepository = application.container.dishRepository
                HomeViewModel(
                    voiceToTextParser = voiceToTextParser,
                    purchaseRepository = purchaseRepository,
                    dishRepository = dishRepository,
                    application = application,
                )
            }
        }
    }
}