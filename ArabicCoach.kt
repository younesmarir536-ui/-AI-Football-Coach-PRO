package com.aifootballcoach.pro

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class ArabicCoach(context: Context) : TextToSpeech.OnInitListener {
    private var engine: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    @Volatile private var ready = false

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = engine?.setLanguage(Locale("ar"))
            ready = result != TextToSpeech.LANG_MISSING_DATA &&
                    result != TextToSpeech.LANG_NOT_SUPPORTED
        }
    }

    fun speak(text: String) {
        if (ready) engine?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "coach_tip")
    }

    fun shutdown() {
        engine?.stop()
        engine?.shutdown()
        engine = null
        ready = false
    }
}
