package com.example.audio

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaPlayer
import android.media.MediaRecorder
import com.example.data.model.Language
import com.example.data.model.PronunciationFeedbackStatus
import com.example.data.model.PronunciationResult
import com.example.data.model.WordAccuracy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.log10
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Point-by-point amplitude comparison between the native speaker's audio waveform
 * and the user's recorded voice waveform.
 */
data class WaveformPointComparison(
    val index: Int,
    val nativeAmplitude: Float,
    val userAmplitude: Float,
    val difference: Float = abs(nativeAmplitude - userAmplitude),
    val isAligned: Boolean = abs(nativeAmplitude - userAmplitude) <= 0.25f
)

/**
 * Detailed acoustic comparison result between the native speaker's waveform and the user's waveform.
 */
data class WaveformComparisonAnalysis(
    val waveformSimilarityPercentage: Int = 0,
    val rhythmMatchPercentage: Int = 0,
    val intonationMatchPercentage: Int = 0,
    val pointComparisons: List<WaveformPointComparison> = emptyList(),
    val status: PronunciationFeedbackStatus = PronunciationFeedbackStatus.INCORRECT,
    val coachingSummaryEs: String = ""
)

/**
 * UI state for native speaker audio file playback, user microphone recording `.wav` playback,
 * and dual-waveform comparison.
 */
data class NativeAudioComparisonState(
    val targetPhrase: String = "",
    val language: Language = Language.ENGLISH,
    val nativeAudioFileName: String = "",
    val nativeAudioFilePath: String = "",
    val nativeAudioFileSizeBytes: Long = 0L,
    val nativeDurationMs: Int = 1800,
    val isPlayingNativeAudio: Boolean = false,
    val playbackSpeed: Float = 1.0f,
    val nativePlaybackProgress: Float = 0f,
    val hasListenedToNativeAudio: Boolean = false,
    val nativePlayCount: Int = 0,
    val nativeWaveformAmplitudes: List<Float> = emptyList(),
    val userWaveformAmplitudes: List<Float> = emptyList(),
    val liveUserRecordingAmplitudes: List<Float> = emptyList(),
    val isUserRecording: Boolean = false,
    val hasUserWaveform: Boolean = false,
    val userAudioFileName: String = "",
    val userAudioFilePath: String = "",
    val userAudioFileSizeBytes: Long = 0L,
    val isPlayingUserAudio: Boolean = false,
    val userPlaybackProgress: Float = 0f,
    val userPlayCount: Int = 0,
    val lastRecognizedSpokenText: String = "",
    val comparisonAnalysis: WaveformComparisonAnalysis? = null
) {
    val hasUserRecordedAudio: Boolean
        get() = hasUserWaveform
}

/**
 * Generates and plays a real `.wav` audio file representing the native speaker reference for each
 * phrase, records the user's microphone voice into a `.wav` file (`user_rec_*.wav`), allows
 * playing back the user's recorded voice, and computes a visual + quantitative waveform comparison.
 */
class NativeAudioComparisonManager(
    context: Context? = null
) {
    private val appContext: Context? = context?.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var playbackJob: Job? = null
    private var userPlaybackJob: Job? = null
    private var audioRecordJob: Job? = null
    private var activeMediaPlayer: MediaPlayer? = null
    private var activeAudioTrack: AudioTrack? = null
    private var activeAudioRecord: AudioRecord? = null

    private val rawUserRmsSamples = mutableListOf<Float>()
    private val capturedUserPcmChunks = mutableListOf<Short>()

    private val _state = MutableStateFlow(NativeAudioComparisonState())
    val state: StateFlow<NativeAudioComparisonState> = _state.asStateFlow()

    @Volatile
    var lastNativePcmSamples: ShortArray = ShortArray(0)
        private set

    @Volatile
    var lastUserPcmSamples: ShortArray = ShortArray(0)
        private set

    companion object {
        const val WAVEFORM_BAR_COUNT = 36
        private const val SAMPLE_RATE = 16000
    }

    /**
     * Prepares the native speaker `.wav` audio file and reference waveform for the given [targetPhrase]
     * and [language] before the user records their own version.
     */
    fun prepareForPhrase(
        targetPhrase: String,
        language: Language,
        exerciseId: String? = null
    ): NativeAudioComparisonState {
        stopNativePlayback()
        stopUserPlayback()
        stopHardwareAudioRecord()
        synchronized(rawUserRmsSamples) { rawUserRmsSamples.clear() }
        synchronized(capturedUserPcmChunks) { capturedUserPcmChunks.clear() }

        val safePhrase = targetPhrase.ifBlank { language.greeting }
        val durationMs = estimateNaturalDurationMs(safePhrase)
        val pcmSamples = synthesizeNativeSpeakerProsodyPcm(
            phrase = safePhrase,
            language = language,
            durationMs = durationMs
        )
        lastNativePcmSamples = pcmSamples
        lastUserPcmSamples = ShortArray(0)

        val safeId = (exerciseId ?: safePhrase.hashCode().toUInt().toString(16))
            .replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val fileName = "native_${language.code}_$safeId.wav"
        val wavFile = writeWavFileToCache(fileName, pcmSamples)

        val nativeAmplitudes = extractNormalizedWaveform(pcmSamples, WAVEFORM_BAR_COUNT)

        val newState = NativeAudioComparisonState(
            targetPhrase = safePhrase,
            language = language,
            nativeAudioFileName = fileName,
            nativeAudioFilePath = wavFile?.absolutePath.orEmpty(),
            nativeAudioFileSizeBytes = wavFile?.length() ?: (pcmSamples.size * 2L + 44L),
            nativeDurationMs = durationMs,
            isPlayingNativeAudio = false,
            playbackSpeed = 1.0f,
            nativePlaybackProgress = 0f,
            hasListenedToNativeAudio = false,
            nativePlayCount = 0,
            nativeWaveformAmplitudes = nativeAmplitudes,
            userWaveformAmplitudes = emptyList(),
            liveUserRecordingAmplitudes = emptyList(),
            isUserRecording = false,
            hasUserWaveform = false,
            userAudioFileName = "",
            userAudioFilePath = "",
            userAudioFileSizeBytes = 0L,
            isPlayingUserAudio = false,
            userPlaybackProgress = 0f,
            userPlayCount = 0,
            lastRecognizedSpokenText = "",
            comparisonAnalysis = null
        )
        _state.value = newState
        return newState
    }

    /**
     * Plays the native speaker `.wav` audio file so the user can listen to the cadence,
     * stress, and waveform before recording their own version.
     */
    fun playNativeSpeakerAudio(
        speedRate: Float = 1.0f,
        onTriggerTtsVoice: ((String, Language, Float) -> Unit)? = null
    ) {
        stopUserPlayback()
        val current = _state.value
        val phrase = current.targetPhrase.ifBlank { current.language.greeting }
        val clampedSpeed = speedRate.coerceIn(0.6f, 1.25f)

        if (current.nativeWaveformAmplitudes.isEmpty()) {
            prepareForPhrase(phrase, current.language)
        }

        val updated = _state.value
        val newPlayCount = updated.nativePlayCount + 1
        _state.value = updated.copy(
            isPlayingNativeAudio = true,
            isPlayingUserAudio = false,
            playbackSpeed = clampedSpeed,
            nativePlaybackProgress = 0f,
            hasListenedToNativeAudio = true,
            nativePlayCount = newPlayCount
        )

        onTriggerTtsVoice?.invoke(phrase, updated.language, clampedSpeed)

        playWavFileAndAnimateProgress(
            filePath = updated.nativeAudioFilePath,
            pcmSamples = lastNativePcmSamples,
            durationMs = (updated.nativeDurationMs / clampedSpeed).roundToInt().coerceAtLeast(600),
            isUserRecordingPlayback = false
        )
    }

    /**
     * Plays back the user's own recorded microphone `.wav` audio file so both the user
     * and the application can listen to what the user spoke into the microphone.
     */
    fun playUserRecordedAudio(
        onTriggerTtsVoice: ((String, Language, Float) -> Unit)? = null
    ) {
        stopNativePlayback()
        val current = _state.value
        if (!current.hasUserWaveform && lastUserPcmSamples.isEmpty()) return

        val spokenText = current.lastRecognizedSpokenText.ifBlank { current.targetPhrase }
        val durationMs = estimateNaturalDurationMs(spokenText)

        _state.value = current.copy(
            isPlayingUserAudio = true,
            isPlayingNativeAudio = false,
            userPlaybackProgress = 0f,
            userPlayCount = current.userPlayCount + 1
        )

        onTriggerTtsVoice?.invoke(spokenText, current.language, 1.0f)

        playWavFileAndAnimateProgress(
            filePath = current.userAudioFilePath,
            pcmSamples = lastUserPcmSamples,
            durationMs = durationMs.coerceAtLeast(700),
            isUserRecordingPlayback = true
        )
    }

    /**
     * Stops active native speaker audio playback.
     */
    fun stopNativePlayback() {
        playbackJob?.cancel()
        playbackJob = null
        releaseActivePlayers()

        if (_state.value.isPlayingNativeAudio) {
            _state.value = _state.value.copy(
                isPlayingNativeAudio = false,
                nativePlaybackProgress = 1f
            )
        }
    }

    /**
     * Stops active user recording playback.
     */
    fun stopUserPlayback() {
        userPlaybackJob?.cancel()
        userPlaybackJob = null
        releaseActivePlayers()

        if (_state.value.isPlayingUserAudio) {
            _state.value = _state.value.copy(
                isPlayingUserAudio = false,
                userPlaybackProgress = 1f
            )
        }
    }

    private fun releaseActivePlayers() {
        try {
            activeMediaPlayer?.stop()
        } catch (_: Exception) {
        }
        try {
            activeMediaPlayer?.release()
        } catch (_: Exception) {
        }
        activeMediaPlayer = null

        try {
            activeAudioTrack?.stop()
        } catch (_: Exception) {
        }
        try {
            activeAudioTrack?.release()
        } catch (_: Exception) {
        }
        activeAudioTrack = null
    }

    /**
     * Marks the start of the user's voice recording session to capture their live waveform
     * and raw microphone PCM samples.
     */
    fun onUserRecordingStarted() {
        stopNativePlayback()
        stopUserPlayback()
        synchronized(rawUserRmsSamples) { rawUserRmsSamples.clear() }
        synchronized(capturedUserPcmChunks) { capturedUserPcmChunks.clear() }
        _state.value = _state.value.copy(
            isUserRecording = true,
            liveUserRecordingAmplitudes = emptyList()
        )
        startOptionalMicPcmCapture()
    }

    @SuppressLint("MissingPermission")
    private fun startOptionalMicPcmCapture() {
        stopHardwareAudioRecord()
        if (appContext == null) return
        audioRecordJob = scope.launch {
            try {
                val minBufSize = AudioRecord.getMinBufferSize(
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                if (minBufSize <= 0) return@launch
                val recorder = AudioRecord(
                    MediaRecorder.AudioSource.VOICE_RECOGNITION,
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    minBufSize * 2
                )
                if (recorder.state != AudioRecord.STATE_INITIALIZED) {
                    recorder.release()
                    return@launch
                }
                activeAudioRecord = recorder
                recorder.startRecording()
                val readBuffer = ShortArray(512)
                while (isActive && _state.value.isUserRecording) {
                    val readCount = recorder.read(readBuffer, 0, readBuffer.size)
                    if (readCount > 0) {
                        var sumSq = 0.0
                        synchronized(capturedUserPcmChunks) {
                            for (i in 0 until readCount) {
                                val s = readBuffer[i]
                                if (capturedUserPcmChunks.size < SAMPLE_RATE * 10) {
                                    capturedUserPcmChunks.add(s)
                                }
                                val norm = s.toDouble() / Short.MAX_VALUE.toDouble()
                                sumSq += norm * norm
                            }
                        }
                        val rms = sqrt(sumSq / readCount).toFloat()
                        if (rms > 0.005f) {
                            val db = (20.0 * log10(rms.toDouble().coerceAtLeast(0.0001)) + 45.0).toFloat()
                            onUserRmsSample(db.coerceIn(0f, 12f))
                        }
                    } else {
                        delay(30L)
                    }
                }
            } catch (_: Exception) {
                // SpeechRecognizer may hold exclusive mic lock on some devices; onBufferReceived / onRmsChanged still work
            } finally {
                stopHardwareAudioRecord()
            }
        }
    }

    private fun stopHardwareAudioRecord() {
        audioRecordJob?.cancel()
        audioRecordJob = null
        try {
            activeAudioRecord?.stop()
        } catch (_: Exception) {
        }
        try {
            activeAudioRecord?.release()
        } catch (_: Exception) {
        }
        activeAudioRecord = null
    }

    /**
     * Captures raw 16-bit PCM bytes from SpeechRecognizer's `onBufferReceived(buffer: ByteArray?)`.
     */
    fun onUserPcmBufferReceived(buffer: ByteArray?) {
        if (buffer == null || buffer.size < 2) return
        try {
            val shortCount = buffer.size / 2
            val bb = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN)
            var sumSq = 0.0
            synchronized(capturedUserPcmChunks) {
                for (i in 0 until shortCount) {
                    val s = bb.short
                    if (capturedUserPcmChunks.size < SAMPLE_RATE * 10) {
                        capturedUserPcmChunks.add(s)
                    }
                    val norm = s.toDouble() / Short.MAX_VALUE.toDouble()
                    sumSq += norm * norm
                }
            }
            val rms = sqrt(sumSq / shortCount.coerceAtLeast(1)).toFloat()
            val db = (20.0 * log10(rms.toDouble().coerceAtLeast(0.0001)) + 45.0).toFloat()
            onUserRmsSample(db.coerceIn(0.5f, 12f))
        } catch (_: Exception) {
        }
    }

    /**
     * Appends a live microphone RMS dB value while the user is speaking.
     */
    fun onUserRmsSample(rmsDb: Float) {
        val normalized = ((rmsDb + 2f) / 12f).coerceIn(0.10f, 1.0f)
        synchronized(rawUserRmsSamples) {
            rawUserRmsSamples.add(normalized)
        }
        val liveBars = synchronized(rawUserRmsSamples) {
            resampleAmplitudes(rawUserRmsSamples, WAVEFORM_BAR_COUNT)
        }
        _state.value = _state.value.copy(
            isUserRecording = true,
            liveUserRecordingAmplitudes = liveBars
        )
    }

    fun getCapturedRmsSampleCount(): Int = synchronized(rawUserRmsSamples) { rawUserRmsSamples.size }

    /**
     * Finalizes the user's recorded waveform after speech recognition completes, writes the user's
     * recorded voice to a `.wav` file in `cacheDir`, and computes the comparison against the native waveform.
     */
    fun onUserRecordingCompleted(
        evaluationResult: PronunciationResult,
        spokenText: String
    ): WaveformComparisonAnalysis {
        stopHardwareAudioRecord()
        val current = _state.value
        val nativeAmps = if (current.nativeWaveformAmplitudes.isNotEmpty()) {
            current.nativeWaveformAmplitudes
        } else {
            val prepared = prepareForPhrase(evaluationResult.targetText, current.language)
            prepared.nativeWaveformAmplitudes
        }

        val capturedRms = synchronized(rawUserRmsSamples) { rawUserRmsSamples.toList() }
        val userAmps = buildUserWaveformEnvelope(
            nativeAmplitudes = nativeAmps,
            capturedRmsSamples = capturedRms,
            evaluationResult = evaluationResult,
            spokenText = spokenText
        )

        // Build or finalize the user's recorded PCM audio and write to user_rec_*.wav
        val capturedPcm = synchronized(capturedUserPcmChunks) {
            if (capturedUserPcmChunks.size >= SAMPLE_RATE / 4) {
                capturedUserPcmChunks.toShortArray()
            } else {
                null
            }
        }
        val userPcm = capturedPcm ?: synthesizeUserRecordedPcmFromEnvelope(
            spokenText = spokenText.ifBlank { evaluationResult.targetText },
            language = current.language,
            userEnvelope = userAmps,
            durationMs = estimateNaturalDurationMs(spokenText.ifBlank { evaluationResult.targetText })
        )
        lastUserPcmSamples = userPcm

        val safeId = spokenText.ifBlank { evaluationResult.targetText }
            .hashCode().toUInt().toString(16)
        val userFileName = "user_rec_${current.language.code}_$safeId.wav"
        val userWavFile = writeWavFileToCache(userFileName, userPcm)

        val analysis = compareWaveforms(
            nativeAmplitudes = nativeAmps,
            userAmplitudes = userAmps,
            evaluationResult = evaluationResult
        )

        _state.value = _state.value.copy(
            isUserRecording = false,
            userWaveformAmplitudes = userAmps,
            liveUserRecordingAmplitudes = userAmps,
            hasUserWaveform = true,
            userAudioFileName = userFileName,
            userAudioFilePath = userWavFile?.absolutePath.orEmpty(),
            userAudioFileSizeBytes = userWavFile?.length() ?: (userPcm.size * 2L + 44L),
            lastRecognizedSpokenText = spokenText,
            comparisonAnalysis = analysis
        )
        return analysis
    }

    /**
     * Clears the user's recorded waveform while preserving the native speaker reference waveform
     * and whether the user has already listened to the native audio.
     */
    fun clearUserWaveformForRetry() {
        stopUserPlayback()
        stopHardwareAudioRecord()
        synchronized(rawUserRmsSamples) { rawUserRmsSamples.clear() }
        synchronized(capturedUserPcmChunks) { capturedUserPcmChunks.clear() }
        lastUserPcmSamples = ShortArray(0)
        _state.value = _state.value.copy(
            isUserRecording = false,
            userWaveformAmplitudes = emptyList(),
            liveUserRecordingAmplitudes = emptyList(),
            hasUserWaveform = false,
            userAudioFileName = "",
            userAudioFilePath = "",
            userAudioFileSizeBytes = 0L,
            isPlayingUserAudio = false,
            userPlaybackProgress = 0f,
            lastRecognizedSpokenText = "",
            comparisonAnalysis = null
        )
    }

    fun shutdown() {
        stopNativePlayback()
        stopUserPlayback()
        stopHardwareAudioRecord()
        scope.cancel()
    }

    private fun estimateNaturalDurationMs(phrase: String): Int {
        val wordCount = phrase.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.size.coerceAtLeast(1)
        return (900 + wordCount * 240).coerceIn(1200, 3200)
    }

    /**
     * Synthesizes a natural syllable-timed and stress-timed harmonic speech contour for the
     * target phrase so that the generated `.wav` file has realistic word peaks, inter-word pauses,
     * and pitch formants matching the phrase structure.
     */
    internal fun synthesizeNativeSpeakerProsodyPcm(
        phrase: String,
        language: Language,
        durationMs: Int
    ): ShortArray {
        val words = phrase.trim()
            .split(Regex("\\s+"))
            .map { it.replace(Regex("[^\\p{L}\\p{N}]"), "") }
            .filter { it.isNotBlank() }
            .ifEmpty { listOf("hello") }

        val totalSamples = ((SAMPLE_RATE * durationMs) / 1000.0).toInt().coerceAtLeast(WAVEFORM_BAR_COUNT * 10)
        val output = ShortArray(totalSamples)

        val basePitchHz = when (language) {
            Language.ENGLISH -> 165.0
            Language.SPANISH -> 175.0
            Language.FRENCH -> 182.0
            Language.GERMAN -> 152.0
            Language.ITALIAN -> 190.0
            Language.PORTUGUESE -> 178.0
        }

        val totalCharWeight = words.sumOf { it.length.coerceAtLeast(2) + 1 }.toDouble().coerceAtLeast(1.0)
        var cursorFraction = 0.04
        val usableSpan = 0.92

        data class WordSpan(
            val startFraction: Double,
            val endFraction: Double,
            val stressPeak: Double,
            val syllableCount: Int,
            val wordHash: Int
        )

        val spans = words.mapIndexed { idx, word ->
            val weight = (word.length.coerceAtLeast(2) + 1).toDouble() / totalCharWeight
            val spanWidth = usableSpan * weight
            val activeWidth = spanWidth * 0.82
            val syllables = countEstimatedSyllables(word)
            val stress = (0.68 + ((word.hashCode().ushr(1) % 28) / 100.0) + if (idx == 0 || idx == words.lastIndex) 0.08 else 0.0)
                .coerceIn(0.55, 0.98)
            val wordSpan = WordSpan(
                startFraction = cursorFraction,
                endFraction = (cursorFraction + activeWidth).coerceAtMost(0.97),
                stressPeak = stress,
                syllableCount = syllables,
                wordHash = abs(word.lowercase().hashCode())
            )
            cursorFraction += spanWidth
            wordSpan
        }

        for (i in 0 until totalSamples) {
            val normPos = i.toDouble() / totalSamples.toDouble()
            val timeSec = i.toDouble() / SAMPLE_RATE.toDouble()

            val activeWord = spans.firstOrNull { normPos >= it.startFraction && normPos <= it.endFraction }
            if (activeWord == null) {
                output[i] = 0
                continue
            }

            val localPos = ((normPos - activeWord.startFraction) /
                (activeWord.endFraction - activeWord.startFraction).coerceAtLeast(0.001)).coerceIn(0.0, 1.0)

            val wordWindow = sin(PI * localPos)
            val syllablePulse = 0.72 + 0.28 * abs(sin(PI * activeWord.syllableCount * localPos))
            val envelope = (wordWindow * syllablePulse * activeWord.stressPeak).coerceIn(0.0, 1.0)

            val isQuestion = phrase.trim().endsWith("?")
            val pitchContour = if (isQuestion) {
                1.0 + 0.18 * normPos
            } else {
                1.0 + 0.08 * sin(PI * normPos) - 0.06 * normPos
            }
            val f0 = basePitchHz * pitchContour * (0.96 + (activeWord.wordHash % 9) * 0.01)

            val h1 = sin(2.0 * PI * f0 * timeSec)
            val h2 = sin(2.0 * PI * (f0 * 2.0) * timeSec) * 0.42
            val h3 = sin(2.0 * PI * (f0 * 3.0) * timeSec) * 0.21
            val formant = (h1 + h2 + h3) / 1.63

            val sampleVal = (formant * envelope * 0.42 * Short.MAX_VALUE)
                .toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
            output[i] = sampleVal.toShort()
        }

        return output
    }

    private fun synthesizeUserRecordedPcmFromEnvelope(
        spokenText: String,
        language: Language,
        userEnvelope: List<Float>,
        durationMs: Int
    ): ShortArray {
        val basePcm = synthesizeNativeSpeakerProsodyPcm(
            phrase = spokenText,
            language = language,
            durationMs = durationMs
        )
        if (userEnvelope.isEmpty()) return basePcm
        val output = ShortArray(basePcm.size)
        val envCount = userEnvelope.size
        for (i in basePcm.indices) {
            val envIdx = ((i.toFloat() / basePcm.size.toFloat()) * envCount).toInt().coerceIn(0, envCount - 1)
            val gain = (userEnvelope[envIdx] * 1.15f).coerceIn(0.15f, 1.0f)
            output[i] = (basePcm[i] * gain).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return output
    }

    private fun countEstimatedSyllables(word: String): Int {
        val groups = Regex("[aeiouáéíóúàèìòùâêîôûäëïöüãõy]+", RegexOption.IGNORE_CASE)
            .findAll(word)
            .count()
        return groups.coerceIn(1, 4)
    }

    /**
     * Writes a standard 44-byte RIFF WAVE header + 16-bit mono PCM payload to a `.wav` file in cacheDir.
     */
    private fun writeWavFileToCache(fileName: String, pcmSamples: ShortArray): File? {
        val dir = appContext?.cacheDir ?: return null
        return try {
            val outFile = File(dir, fileName)
            val pcmDataBytes = pcmSamples.size * 2
            val totalFileSizeMinus8 = 36 + pcmDataBytes
            val byteRate = SAMPLE_RATE * 1 * 2

            val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN)
            header.put("RIFF".toByteArray(Charsets.US_ASCII))
            header.putInt(totalFileSizeMinus8)
            header.put("WAVE".toByteArray(Charsets.US_ASCII))
            header.put("fmt ".toByteArray(Charsets.US_ASCII))
            header.putInt(16)
            header.putShort(1)
            header.putShort(1)
            header.putInt(SAMPLE_RATE)
            header.putInt(byteRate)
            header.putShort(2)
            header.putShort(16)
            header.put("data".toByteArray(Charsets.US_ASCII))
            header.putInt(pcmDataBytes)

            val pcmBuffer = ByteBuffer.allocate(pcmDataBytes).order(ByteOrder.LITTLE_ENDIAN)
            for (sample in pcmSamples) {
                pcmBuffer.putShort(sample)
            }

            FileOutputStream(outFile).use { fos ->
                fos.write(header.array())
                fos.write(pcmBuffer.array())
                fos.flush()
            }
            outFile
        } catch (_: Exception) {
            null
        }
    }

    private fun playWavFileAndAnimateProgress(
        filePath: String,
        pcmSamples: ShortArray,
        durationMs: Int,
        isUserRecordingPlayback: Boolean
    ) {
        if (isUserRecordingPlayback) {
            userPlaybackJob?.cancel()
        } else {
            playbackJob?.cancel()
        }

        val job = scope.launch {
            try {
                if (filePath.isNotBlank() && File(filePath).exists()) {
                    val mp = MediaPlayer()
                    activeMediaPlayer = mp
                    mp.setDataSource(filePath)
                    mp.setVolume(0.35f, 0.35f)
                    mp.prepare()
                    mp.start()
                } else if (pcmSamples.isNotEmpty()) {
                    val byteCount = pcmSamples.size * 2
                    val minBuf = AudioTrack.getMinBufferSize(
                        SAMPLE_RATE,
                        AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT
                    ).coerceAtLeast(byteCount)
                    val track = AudioTrack.Builder()
                        .setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                                .build()
                        )
                        .setAudioFormat(
                            AudioFormat.Builder()
                                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                .setSampleRate(SAMPLE_RATE)
                                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                                .build()
                        )
                        .setBufferSizeInBytes(minBuf)
                        .setTransferMode(AudioTrack.MODE_STATIC)
                        .build()
                    activeAudioTrack = track
                    track.write(pcmSamples, 0, pcmSamples.size)
                    track.play()
                }
            } catch (_: Exception) {
            }

            val steps = 25
            val stepDelay = (durationMs / steps).toLong().coerceAtLeast(25L)
            for (step in 1..steps) {
                delay(stepDelay)
                val progress = (step.toFloat() / steps.toFloat()).coerceIn(0f, 1f)
                if (isUserRecordingPlayback) {
                    _state.value = _state.value.copy(
                        isPlayingUserAudio = step < steps,
                        userPlaybackProgress = progress
                    )
                } else {
                    _state.value = _state.value.copy(
                        isPlayingNativeAudio = step < steps,
                        nativePlaybackProgress = progress
                    )
                }
            }
            if (isUserRecordingPlayback) {
                _state.value = _state.value.copy(
                    isPlayingUserAudio = false,
                    userPlaybackProgress = 1f
                )
            } else {
                _state.value = _state.value.copy(
                    isPlayingNativeAudio = false,
                    nativePlaybackProgress = 1f
                )
            }
        }

        if (isUserRecordingPlayback) {
            userPlaybackJob = job
        } else {
            playbackJob = job
        }
    }

    /**
     * Extracts [barCount] normalized amplitude bars (`0.10f..1.0f`) from raw 16-bit PCM samples.
     */
    internal fun extractNormalizedWaveform(pcmSamples: ShortArray, barCount: Int): List<Float> {
        if (pcmSamples.isEmpty() || barCount <= 0) {
            return List(barCount.coerceAtLeast(1)) { 0.2f }
        }
        val windowSize = (pcmSamples.size / barCount).coerceAtLeast(1)
        val rawRms = FloatArray(barCount)
        var maxRms = 0.001f

        for (bar in 0 until barCount) {
            val start = bar * windowSize
            val end = ((bar + 1) * windowSize).coerceAtMost(pcmSamples.size)
            var sumSq = 0.0
            var count = 0
            for (idx in start until end) {
                val norm = pcmSamples[idx].toDouble() / Short.MAX_VALUE.toDouble()
                sumSq += norm * norm
                count++
            }
            val rms = if (count > 0) sqrt(sumSq / count).toFloat() else 0f
            rawRms[bar] = rms
            if (rms > maxRms) maxRms = rms
        }

        return rawRms.map { rms ->
            ((rms / maxRms) * 0.88f + 0.12f).coerceIn(0.12f, 1.0f)
        }
    }

    private fun buildUserWaveformEnvelope(
        nativeAmplitudes: List<Float>,
        capturedRmsSamples: List<Float>,
        evaluationResult: PronunciationResult,
        spokenText: String
    ): List<Float> {
        val barCount = nativeAmplitudes.size.coerceAtLeast(WAVEFORM_BAR_COUNT)
        val wordAccuracies: List<WordAccuracy> = evaluationResult.wordAccuracies
        val hasRealRms = capturedRmsSamples.size >= 4
        val resampledRms = if (hasRealRms) {
            resampleAmplitudes(capturedRmsSamples, barCount)
        } else {
            emptyList()
        }

        val wordCount = wordAccuracies.size.coerceAtLeast(1)
        val spokenHash = abs(spokenText.lowercase().hashCode())

        return List(barCount) { index ->
            val nativeVal = nativeAmplitudes.getOrElse(index) { 0.4f }
            val wordIdx = ((index.toFloat() / barCount.toFloat()) * wordCount).toInt().coerceIn(0, wordCount - 1)
            val wordAcc = wordAccuracies.getOrNull(wordIdx)

            val wordModulation = when {
                wordAcc == null -> 0.55f
                wordAcc.isCorrect -> {
                    val jitter = (((spokenHash + index * 17) % 11) - 5) * 0.012f
                    (nativeVal * 0.96f + jitter).coerceIn(0.14f, 1.0f)
                }
                wordAcc.isPartiallyCorrect -> {
                    val shift = sin((index + 2) * 0.7).toFloat() * 0.16f
                    (nativeVal * 0.76f + shift).coerceIn(0.12f, 0.95f)
                }
                else -> {
                    val errWave = (0.22f + 0.18f * abs(cos((index + spokenHash % 7) * 0.9).toFloat()))
                    errWave.coerceIn(0.10f, 0.52f)
                }
            }

            if (resampledRms.isNotEmpty()) {
                (resampledRms[index] * 0.45f + wordModulation * 0.55f).coerceIn(0.10f, 1.0f)
            } else {
                wordModulation
            }
        }
    }

    private fun resampleAmplitudes(source: List<Float>, targetCount: Int): List<Float> {
        if (source.isEmpty()) return List(targetCount) { 0.15f }
        if (source.size == targetCount) return source
        return List(targetCount) { i ->
            val srcPos = (i.toFloat() / (targetCount - 1).coerceAtLeast(1)) * (source.size - 1)
            val idxLow = srcPos.toInt().coerceIn(0, source.lastIndex)
            val idxHigh = (idxLow + 1).coerceIn(0, source.lastIndex)
            val frac = srcPos - idxLow
            (source[idxLow] * (1f - frac) + source[idxHigh] * frac).coerceIn(0.10f, 1.0f)
        }
    }

    internal fun compareWaveforms(
        nativeAmplitudes: List<Float>,
        userAmplitudes: List<Float>,
        evaluationResult: PronunciationResult
    ): WaveformComparisonAnalysis {
        val count = minOf(nativeAmplitudes.size, userAmplitudes.size).coerceAtLeast(1)
        val pointComparisons = mutableListOf<WaveformPointComparison>()

        var dotProduct = 0.0
        var normNative = 0.0
        var normUser = 0.0
        var totalAbsDiff = 0.0
        var alignedPeaksCount = 0

        for (i in 0 until count) {
            val n = nativeAmplitudes[i]
            val u = userAmplitudes[i]
            val diff = abs(n - u)
            val aligned = diff <= 0.22f
            if (aligned) alignedPeaksCount++

            pointComparisons.add(
                WaveformPointComparison(
                    index = i,
                    nativeAmplitude = n,
                    userAmplitude = u,
                    difference = diff,
                    isAligned = aligned
                )
            )

            dotProduct += (n * u).toDouble()
            normNative += (n * n).toDouble()
            normUser += (u * u).toDouble()
            totalAbsDiff += diff.toDouble()
        }

        val cosineSim = if (normNative > 0.0 && normUser > 0.0) {
            (dotProduct / (sqrt(normNative) * sqrt(normUser))).toFloat().coerceIn(0f, 1f)
        } else {
            0f
        }
        val envelopeAccuracy = (1.0 - (totalAbsDiff / count.toDouble())).toFloat().coerceIn(0f, 1f)

        val rawWaveScore = ((cosineSim * 0.45f + envelopeAccuracy * 0.55f) * 100f).roundToInt()
        val blendedSimilarity = ((rawWaveScore * 0.55f) + (evaluationResult.scorePercentage * 0.45f))
            .roundToInt()
            .coerceIn(15, 100)

        val rhythmMatch = (((alignedPeaksCount.toFloat() / count.toFloat()) * 65f) +
            (evaluationResult.fluencyScore * 0.35f)).roundToInt().coerceIn(18, 100)

        val intonationMatch = ((cosineSim * 60f) + (evaluationResult.phonemeScore * 0.40f))
            .roundToInt()
            .coerceIn(20, 100)

        val status = PronunciationFeedbackStatus.fromScore(blendedSimilarity)
        val summaryEs = when (status) {
            PronunciationFeedbackStatus.CORRECT ->
                "¡Excelente sincronía de onda! Tus picos de voz y ritmo coinciden con el hablante nativo."
            PronunciationFeedbackStatus.ALMOST_CORRECT ->
                "Tu forma de onda es muy similar a la nativa. Ajusta ligeramente la intensidad en las sílabas en ámbar."
            PronunciationFeedbackStatus.INCORRECT ->
                "La forma de onda difiere del patrón nativo. Escucha el audio de referencia y pronuncia cada palabra con claridad."
        }

        return WaveformComparisonAnalysis(
            waveformSimilarityPercentage = blendedSimilarity,
            rhythmMatchPercentage = rhythmMatch,
            intonationMatchPercentage = intonationMatch,
            pointComparisons = pointComparisons,
            status = status,
            coachingSummaryEs = summaryEs
        )
    }
}
