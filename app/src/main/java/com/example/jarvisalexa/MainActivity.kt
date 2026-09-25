package com.example.jarvisalexa

import android.Manifest
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity(), TextToSpeech.OnInitListener {

    private lateinit var response: TextView
    private lateinit var input: EditText
    private lateinit var tts: TextToSpeech
    private lateinit var avatarPulse: View
    private lateinit var speakingState: TextView

    private var recognizer: SpeechRecognizer? = null
    private val handler = Handler(Looper.getMainLooper())
    private var pulseAnim: AnimatorSet? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        response = findViewById(R.id.boxResponse)
        input = findViewById(R.id.input)
        avatarPulse = findViewById(R.id.avatarPulse)
        speakingState = findViewById(R.id.tvSpeakingState)

        tts = TextToSpeech(this, this)
        updateDateTime()

        val sendAction = {
            val text = input.text.toString().trim()
            if (text.isNotEmpty()) {
                command(text)
                input.text.clear()
            }
        }

        findViewById<Button>(R.id.send).setOnClickListener { sendAction() }
        findViewById<View>(R.id.btnSendIcon).setOnClickListener { sendAction() }
        findViewById<Button>(R.id.mic).setOnClickListener { listen() }
        findViewById<Button>(R.id.mainMic).setOnClickListener { listen() }

        findViewById<Button>(R.id.youtube).setOnClickListener {
            say("Opening YouTube.")
            openUrl("https://www.youtube.com")
        }

        findViewById<Button>(R.id.whatsapp).setOnClickListener {
            openWhatsApp()
        }

        findViewById<Button>(R.id.weather).setOnClickListener {
            say("Opening live weather.")
            openUrl("https://www.google.com/search?q=weather+today")
        }

        findViewById<Button>(R.id.termux).setOnClickListener {
            openTermux()
        }

        findViewById<Button>(R.id.quickHi).setOnClickListener {
            say("Hello! I'm Aisha. How can I help you?")
        }

        findViewById<Button>(R.id.quickTime).setOnClickListener {
            sayTime()
        }

        findViewById<Button>(R.id.quickCount).setOnClickListener {
            say((1..100).joinToString(", "))
        }

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 100)
        }
    }

    private fun updateDateTime() {
        findViewById<TextView>(R.id.date).text =
            SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(Date())
        findViewById<TextView>(R.id.clock).text =
            SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        handler.postDelayed({ updateDateTime() }, 1000)
    }

    private fun listen() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 100)
            return
        }

        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            say("Voice recognition is not available on this phone.")
            return
        }

        recognizer?.destroy()
        recognizer = SpeechRecognizer.createSpeechRecognizer(this)

        recognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                speakingState.text = "◉  Listening..."
            }

            override fun onBeginningOfSpeech() {
                speakingState.text = "◉  Listening..."
            }

            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                speakingState.text = "◉  Processing..."
            }

            override fun onError(error: Int) {
                speakingState.text = "◉  Listening..."
                response.text = "I couldn't hear that. Please try again."
            }

            override fun onResults(results: Bundle?) {
                val text = results?.getStringArrayList(
                    SpeechRecognizer.RESULTS_RECOGNITION
                )?.firstOrNull().orEmpty()

                if (text.isNotBlank()) {
                    command(text)
                } else {
                    response.text = "I didn't catch that."
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }

        recognizer?.startListening(intent)
    }

    private fun command(rawText: String) {
        val text = rawText.trim()
        val query = text.lowercase(Locale.getDefault())

        when {
            query == "hi" || query == "hello" ||
            query.contains("hi aisha") || query.contains("hello aisha") -> {
                say("Hello! I'm Aisha. How can I help you?")
            }

            query.contains("open youtube") ||
            query.contains("youtube kholo") ||
            query.contains("youtube open") -> {
                say("Opening YouTube.")
                openUrl("https://www.youtube.com")
            }

            query.contains("open whatsapp") ||
            query.contains("whatsapp kholo") ||
            query.contains("whatsapp open") -> {
                openWhatsApp()
            }

            query.contains("open termux") ||
            query.contains("termux kholo") ||
            query.contains("termux open") -> {
                openTermux()
            }

            query.contains("open chrome") ||
            query.contains("chrome kholo") ||
            query.contains("chrome open") -> {
                openChrome()
            }

            query.contains("weather") ||
            query.contains("mausam") -> {
                say("Opening live weather.")
                openUrl("https://www.google.com/search?q=weather+today")
            }

            query.contains("what time") ||
            query == "time" ||
            query.contains("kitne baje") -> {
                sayTime()
            }

            query.contains("date") ||
            query.contains("tarikh") -> {
                val date = SimpleDateFormat(
                    "EEEE, dd MMMM yyyy",
                    Locale.getDefault()
                ).format(Date())
                say("Today is $date")
            }

            query.contains("count 1 to 100") ||
            query.contains("count from 1 to 100") ||
            query.contains("1 se 100") -> {
                say((1..100).joinToString(", "))
            }

            query.startsWith("search ") -> {
                val searchText = text.substringAfter("search ", "").trim()
                if (searchText.isNotEmpty()) {
                    say("Searching for $searchText")
                    openUrl("https://www.google.com/search?q=" + Uri.encode(searchText))
                }
            }

            else -> {
                say("I heard you. Try saying open YouTube, open Termux, open Chrome, weather, time, or hi Aisha.")
            }
        }
    }

    private fun sayTime() {
        val time = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        say("The time is $time")
    }

    private fun openUrl(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (_: Exception) {
            say("I couldn't open that.")
        }
    }

    private fun openWhatsApp() {
        try {
            val intent = packageManager.getLaunchIntentForPackage("com.whatsapp")
            if (intent != null) {
                say("Opening WhatsApp.")
                startActivity(intent)
            } else {
                say("WhatsApp is not installed.")
            }
        } catch (_: Exception) {
            say("I couldn't open WhatsApp.")
        }
    }

    private fun openChrome() {
        try {
            val intent = packageManager.getLaunchIntentForPackage("com.android.chrome")
            if (intent != null) {
                say("Opening Chrome.")
                startActivity(intent)
            } else {
                openUrl("https://www.google.com")
            }
        } catch (_: Exception) {
            openUrl("https://www.google.com")
        }
    }

    private fun openTermux() {
        try {
            val intent = packageManager.getLaunchIntentForPackage("com.termux")
            if (intent != null) {
                say("Opening Termux.")
                startActivity(intent)
            } else {
                say("Termux is not installed on this phone.")
            }
        } catch (_: Exception) {
            say("I couldn't open Termux.")
        }
    }

    private fun say(text: String) {
        response.text = text
        if (::tts.isInitialized) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "aisha_voice")
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale.getDefault())
            if (result == TextToSpeech.LANG_MISSING_DATA ||
                result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts.language = Locale.US
            }

            tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    runOnUiThread { startPulse() }
                }

                override fun onDone(utteranceId: String?) {
                    runOnUiThread { stopPulse() }
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    runOnUiThread { stopPulse() }
                }
            })
        }
    }

    private fun startPulse() {
        speakingState.text = "◉  Speaking..."
        pulseAnim?.cancel()

        val scaleX = ObjectAnimator.ofFloat(
            avatarPulse, "scaleX", 1f, 1.10f, 1f
        ).apply {
            duration = 850
            repeatCount = ObjectAnimator.INFINITE
        }

        val scaleY = ObjectAnimator.ofFloat(
            avatarPulse, "scaleY", 1f, 1.10f, 1f
        ).apply {
            duration = 850
            repeatCount = ObjectAnimator.INFINITE
        }

        val alpha = ObjectAnimator.ofFloat(
            avatarPulse, "alpha", 1f, 0.65f, 1f
        ).apply {
            duration = 850
            repeatCount = ObjectAnimator.INFINITE
        }

        pulseAnim = AnimatorSet().apply {
            playTogether(scaleX, scaleY, alpha)
            interpolator = LinearInterpolator()
            start()
        }
    }

    private fun stopPulse() {
        pulseAnim?.cancel()
        pulseAnim = null
        avatarPulse.scaleX = 1f
        avatarPulse.scaleY = 1f
        avatarPulse.alpha = 1f
        speakingState.text = "◉  Listening..."
    }

    override fun onDestroy() {
        recognizer?.destroy()
        recognizer = null
        pulseAnim?.cancel()

        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }

        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}
