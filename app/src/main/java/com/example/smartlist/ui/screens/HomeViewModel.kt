package com.example.smartlist.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import com.example.smartlist.R
import com.example.smartlist.SmartListApplication
import com.example.smartlist.data.DishRepository
import com.example.smartlist.data.PurchaseRepository
import com.example.smartlist.data.VoiceToTextParser
import com.example.smartlist.extend_functions.capitalizeFirstChar
import com.example.smartlist.model.MenuItem
import com.example.smartlist.model.VoiceCommand
import com.example.smartlist.navigation.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

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


    //Attributes of sharedPreferences
    private val sharedPrefFileName = "my_pref_file_name"

    private val themeKey = "theme_pref"
    private val sharedPreferences = application.getSharedPreferences(sharedPrefFileName,Context.MODE_PRIVATE)

    private val langKey = "lang_pref"
    private val sharedLanguagePreferences = application.getSharedPreferences(sharedPrefFileName,Context.MODE_PRIVATE)


    private val _isDarkThemeEnabled = MutableStateFlow(isDarkThemeEnabled())
    val isDarkThemeEnabled: StateFlow<Boolean> = _isDarkThemeEnabled

    private val _currentLanguage = MutableStateFlow(getCurrentLanguage())
    val currentLanguage: StateFlow<String> = _currentLanguage

    init {
        voiceToTextParser.commandCallBack = { command ->
            _voiceCommand.value = command
        }
    }

    fun setCurrentLanguage(language: String){
        _currentLanguage.value = language
        sharedLanguagePreferences.edit().putString(langKey,language).apply()
    }

    private fun getCurrentLanguage(): String {
        return sharedLanguagePreferences.getString(langKey,"en").toString()
    }

    private fun isDarkThemeEnabled(): Boolean{
        return sharedPreferences.getBoolean(themeKey,false)
    }

    fun setDarkThemeEnabled(isEnabled: Boolean){
        _isDarkThemeEnabled.value = isEnabled
        sharedPreferences.edit().putBoolean(themeKey,isEnabled).apply()
    }

    fun clearVoiceCommand(){
        _voiceCommand.value = null
    }

    fun startListening(languageCode: String = currentLanguage.value) = voiceToTextParser.startListening(languageCode)
    fun stopListening() = voiceToTextParser.stopListening()

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
        if (command.text.capitalizeFirstChar() == currentScreen){
            Toast.makeText(context, sameScreenMessage, Toast.LENGTH_SHORT).show()
        }
        else{
            for (screen in mapOfScreen){
                if (command.text.capitalizeFirstChar() == screen.key){
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

    fun processDrawerBodyCommand(
        item: MenuItem,
        currentScreen: String,
        context: Context,
        navController: NavController,
    ){
        var isCommandMatchSuccessful = false

        val sameScreenMessage = context.getString(R.string.notification_message)
        val unknownVoiceCommandMessage = context.getString(R.string.unknown_command)

        val mapOfScreen = mapOf(
            "home" to Screen.HomeScreen.route,
            "purchaseList" to Screen.PurchasesScreen.route,
            "dishList" to Screen.DishesScreen.route,
            "graphs" to Screen.GraphScreen.route,
            "settings" to Screen.SettingScreen.route
        )

        if (item.id == currentScreen){
            Toast.makeText(context, sameScreenMessage, Toast.LENGTH_SHORT).show()
        }
        else{
            for(screen in mapOfScreen){
                if (item.id == screen.key){
                    isCommandMatchSuccessful = true
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