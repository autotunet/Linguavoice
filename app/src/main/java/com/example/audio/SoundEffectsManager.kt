package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.ToneGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Represents the type of audio feedback played after a pronunciation attempt.
 */
enum class PronunciationFeedbackSoundType {
    SUCCESS_CHIME,
    ERROR_SOUND
}

/**
 * Observable state for pronunciation audio feedback effects.
 */
data class AudioFeedbackState(
    val isSoundEnabled: Boolean = true,
    val lastPlayedSound: PronunciationFeedbackSoundType? = null,
    val successChimeCount: Int = 0,
    val errorSoundCount: Int = 0,
    val lastScorePercentage: Int? = null,
    val isLevelRestartError: Boolean = false,
    val lastTriggeredTimestampMs: Long = 0L
)

/**
 * Synthesizes and plays engaging, low-latency game audio feedback for pronunciation results:
 * - A bright, ascending harmonic chime for correct pronunciation (`playSuccessChime`)
 * - A subtle, soft low descending tone for pronunciation errors (`playErrorSound`)
 */
class SoundEffectsManager(context: Context? = null) {

    private val appContext: Context? = context?.applicationContext
    private val audioScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _state = MutableStateFlow(AudioFeedbackState())
    val state: StateFlow<AudioFeedbackState> = _state.asStateFlow()

    @Volatile
    var lastGeneratedPcmSamples: ShortArray = ShortArray(0)
        private set

    private var activeAudioTrack: AudioTrack? = null

    /**
     * Enables or disables game sound effects.
     */
    fun setSoundEnabled(enabled: Boolean) {
        _state.value = _state.value.copy(isSoundEnabled = enabled)
        if (!enabled) {
            stopActiveSound()
        }
    }

    fun toggleSoundEnabled(): Boolean {
        val next = !_state.value.isSoundEnabled
        setSoundEnabled(next)
        return next
    }

    /**
     * Plays a short, uplifting ascending chime for a correct pronunciation result.
     */
    fun playSuccessChime(scorePercentage: Int = 90) {
        val samples = synthesizeSuccessChimePcm(scorePercentage)
        lastGeneratedPcmSamples = samples

        val current = _state.value
        _state.value = current.copy(
            lastPlayedSound = PronunciationFeedbackSoundType.SUCCESS_CHIME,
            successChimeCount = current.successChimeCount + 1,
            lastScorePercentage = scorePercentage.coerceIn(0, 100),
            isLevelRestartError = false,
            lastTriggeredTimestampMs = System.currentTimeMillis()
        )

        if (current.isSoundEnabled) {
            playPcmBufferAsync(samples, fallbackTone = ToneGenerator.TONE_PROP_ACK, fallbackDurationMs = 180)
        }
    }

    /**
     * Plays a subtle, non-harsh 'wrong' sound when a pronunciation error occurs.
     */
    fun playErrorSound(scorePercentage: Int = 40, isLevelRestart: Boolean = false) {
        val samples = synthesizeSubtleErrorPcm(isLevelRestart)
        lastGeneratedPcmSamples = samples

        val current = _state.value
        _state.value = current.copy(
            lastPlayedSound = PronunciationFeedbackSoundType.ERROR_SOUND,
            errorSoundCount = current.errorSoundCount + 1,
            lastScorePercentage = scorePercentage.coerceIn(0, 100),
            isLevelRestartError = isLevelRestart,
            lastTriggeredTimestampMs = System.currentTimeMillis()
        )

        if (current.isSoundEnabled) {
            playPcmBufferAsync(samples, fallbackTone = ToneGenerator.TONE_PROP_NACK, fallbackDurationMs = 160)
        }
    }

    /**
     * Replays the most recently triggered pronunciation feedback sound.
     */
    fun replayLastFeedbackSound() {
        val current = _state.value
        val score = current.lastScorePercentage ?: 85
        when (current.lastPlayedSound) {
            PronunciationFeedbackSoundType.SUCCESS_CHIME -> {
                val samples = synthesizeSuccessChimePcm(score)
                lastGeneratedPcmSamples = samples
                if (current.isSoundEnabled) {
                    playPcmBufferAsync(samples, fallbackTone = ToneGenerator.TONE_PROP_ACK, fallbackDurationMs = 180)
                }
            }
            PronunciationFeedbackSoundType.ERROR_SOUND -> {
                val samples = synthesizeSubtleErrorPcm(isLevelRestart = current.isLevelRestartError)
                lastGeneratedPcmSamples = samples
                if (current.isSoundEnabled) {
                    playPcmBufferAsync(samples, fallbackTone = ToneGenerator.TONE_PROP_NACK, fallbackDurationMs = 160)
                }
            }
            null -> Unit
        }
    }

    /**
     * Synthesizes a crisp, pleasant ascending major triad chime (C5 -> E5 -> G5 -> C6)
     * with soft bell overtones and smooth attack/release envelope.
     */
    internal fun synthesizeSuccessChimePcm(scorePercentage: Int = 90): ShortArray {
        val notes = if (scorePercentage >= 92) {
            // Bright 4-note major arpeggio for high accuracy: C5, E5, G5, C6
            listOf(
                NoteSpec(freqHz = 523.25, durationMs = 70, amplitude = 0.48),
                NoteSpec(freqHz = 659.25, durationMs = 75, amplitude = 0.52),
                NoteSpec(freqHz = 783.99, durationMs = 85, amplitude = 0.56),
                NoteSpec(freqHz = 1046.50, durationMs = 150, amplitude = 0.62)
            )
        } else {
            // Pleasant 3-note ascending chime: C5, E5, G5
            listOf(
                NoteSpec(freqHz = 523.25, durationMs = 80, amplitude = 0.46),
                NoteSpec(freqHz = 659.25, durationMs = 85, amplitude = 0.52),
                NoteSpec(freqHz = 783.99, durationMs = 145, amplitude = 0.58)
            )
        }
        return renderNotesToPcm(notes, harmonicRatio = 0.25, decayRate = 5.5)
    }

    /**
     * Synthesizes a subtle, warm low descending two-note sound (G4 -> Eb4, plus C4 on level restart)
     * that gently signals an error without sounding harsh or discouraging.
     */
    internal fun synthesizeSubtleErrorPcm(isLevelRestart: Boolean = false): ShortArray {
        val notes = if (isLevelRestart) {
            listOf(
                NoteSpec(freqHz = 392.00, durationMs = 85, amplitude = 0.34), // G4
                NoteSpec(freqHz = 329.63, durationMs = 90, amplitude = 0.32), // E4
                NoteSpec(freqHz = 261.63, durationMs = 140, amplitude = 0.30) // C4
            )
        } else {
            listOf(
                NoteSpec(freqHz = 369.99, durationMs = 90, amplitude = 0.32), // F#4 / warm mid-low
                NoteSpec(freqHz = 293.66, durationMs = 135, amplitude = 0.28) // D4 soft low resolution
            )
        }
        return renderNotesToPcm(notes, harmonicRatio = 0.12, decayRate = 6.8)
    }

    private fun renderNotesToPcm(
        notes: List<NoteSpec>,
        harmonicRatio: Double,
        decayRate: Double
    ): ShortArray {
        val totalDurationMs = notes.sumOf { it.durationMs }
        val totalSamples = ((SAMPLE_RATE * totalDurationMs) / 1000.0).toInt().coerceAtLeast(1)
        val output = ShortArray(totalSamples)

        var sampleCursor = 0
        for (note in notes) {
            val noteSamples = ((SAMPLE_RATE * note.durationMs) / 1000.0).toInt().coerceAtLeast(1)
            val attackSamples = (noteSamples * 0.08).toInt().coerceAtLeast(1)
            val releaseStartSample = (noteSamples * 0.72).toInt()

            for (i in 0 until noteSamples) {
                val outIdx = sampleCursor + i
                if (outIdx >= totalSamples) break

                val timeSec = i.toDouble() / SAMPLE_RATE.toDouble()
                val progress = i.toDouble() / noteSamples.toDouble()

                // Smooth attack + exponential bell decay + clean tail release to prevent clicks
                val attackEnv = if (i < attackSamples) {
                    i.toDouble() / attackSamples.toDouble()
                } else {
                    1.0
                }
                val expDecay = exp(-decayRate * progress * 0.45)
                val releaseEnv = if (i > releaseStartSample) {
                    val rem = (noteSamples - i).toDouble() / (noteSamples - releaseStartSample).coerceAtLeast(1).toDouble()
                    rem.coerceIn(0.0, 1.0)
                } else {
                    1.0
                }

                val envelope = attackEnv * expDecay * releaseEnv

                // Fundamental sine + soft octave & fifth harmonics for a pleasant musical timbre
                val fundamental = sin(2.0 * PI * note.freqHz * timeSec)
                val secondHarmonic = sin(2.0 * PI * (note.freqHz * 2.0) * timeSec) * harmonicRatio
                val thirdHarmonic = sin(2.0 * PI * (note.freqHz * 1.5) * timeSec) * (harmonicRatio * 0.4)

                val normalizedWave = (fundamental + secondHarmonic + thirdHarmonic) / (1.0 + harmonicRatio * 1.4)
                val sampleValue = (normalizedWave * note.amplitude * envelope * Short.MAX_VALUE)
                    .toInt()
                    .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())

                output[outIdx] = sampleValue.toShort()
            }
            sampleCursor += noteSamples
        }
        return output
    }

    private fun playPcmBufferAsync(
        pcmSamples: ShortArray,
        fallbackTone: Int,
        fallbackDurationMs: Int
    ) {
        if (pcmSamples.isEmpty()) return

        audioScope.launch {
            try {
                stopActiveSound()

                val byteCount = pcmSamples.size * 2
                val minBufferSize = AudioTrack.getMinBufferSize(
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                val bufferSizeInBytes = maxOf(byteCount, minBufferSize.coerceAtLeast(byteCount))

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .setBufferSizeInBytes(bufferSizeInBytes)
                    .build()

                val written = track.write(pcmSamples, 0, pcmSamples.size)
                if (written > 0 && track.state == AudioTrack.STATE_INITIALIZED) {
                    activeAudioTrack = track
                    track.play()
                } else {
                    track.release()
                    playFallbackTone(fallbackTone, fallbackDurationMs)
                }
            } catch (_: Throwable) {
                playFallbackTone(fallbackTone, fallbackDurationMs)
            }
        }
    }

    private fun playFallbackTone(toneType: Int, durationMs: Int) {
        try {
            val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 65)
            toneGen.startTone(toneType, durationMs)
        } catch (_: Throwable) {
            // Safe no-op in headless/JVM test environments
        }
    }

    fun stopActiveSound() {
        try {
            activeAudioTrack?.let { track ->
                if (track.state == AudioTrack.STATE_INITIALIZED) {
                    track.stop()
                }
                track.release()
            }
        } catch (_: Throwable) {
            // Ignore cleanup errors
        } finally {
            activeAudioTrack = null
        }
    }

    fun shutdown() {
        stopActiveSound()
        audioScope.cancel()
    }

    private data class NoteSpec(
        val freqHz: Double,
        val durationMs: Int,
        val amplitude: Double
    )

    companion object {
        const val SAMPLE_RATE = 44100
    }
}
