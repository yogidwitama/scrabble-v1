package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

object ScrabbleSoundEngine {

    private const val SAMPLE_RATE = 22050
    private var isMuted = false

    private val liftBuffer: ShortArray by lazy { generateWoodLiftSound() }
    private val placeBuffer: ShortArray by lazy { generateWoodPlaceSound() }
    private val dragBuffer: ShortArray by lazy { generateWoodDragSound() }
    private val bingoBuffer: ShortArray by lazy { generateBingoFanfareSound() }

    private val audioScope = CoroutineScope(Dispatchers.Default)

    fun setMuted(muted: Boolean) {
        isMuted = muted
    }

    fun isMuted(): Boolean = isMuted

    /**
     * Plays sound when a tile is lifted / picked up from rack or board
     */
    fun playLiftSound() {
        if (isMuted) return
        audioScope.launch {
            playBuffer(liftBuffer)
        }
    }

    /**
     * Plays sound when a tile is placed / dropped onto a board cell or rack slot
     */
    fun playPlaceSound() {
        if (isMuted) return
        audioScope.launch {
            playBuffer(placeBuffer)
        }
    }

    /**
     * Plays subtle sound while dragging
     */
    private var lastDragPlayTime = 0L
    fun playDragSound() {
        if (isMuted) return
        val now = System.currentTimeMillis()
        if (now - lastDragPlayTime > 120) { // Throttle drag sound so it doesn't flood
            lastDragPlayTime = now
            audioScope.launch {
                playBuffer(dragBuffer)
            }
        }
    }

    /**
     * Plays celebratory triumphant fanfare when a Bingo (+50 bonus) is scored
     */
    fun playBingoSound() {
        if (isMuted) return
        audioScope.launch {
            playBuffer(bingoBuffer)
        }
    }

    private fun playBuffer(buffer: ShortArray) {
        try {
            val audioTrack = AudioTrack.Builder()
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
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()

            // Release after completion
            val durationMs = (buffer.size.toDouble() / SAMPLE_RATE * 1000).toLong() + 100
            Thread.sleep(durationMs)
            audioTrack.stop()
            audioTrack.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Generates a realistic wooden tile pickup / lift click (45ms)
     */
    private fun generateWoodLiftSound(): ShortArray {
        val durationMs = 45
        val numSamples = (SAMPLE_RATE * durationMs / 1000)
        val buffer = ShortArray(numSamples)
        val freq1 = 820.0
        val freq2 = 1240.0

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val envelope = exp(-t * 90.0) // Fast exponential decay
            val wave = 0.65 * sin(2.0 * PI * freq1 * t) + 0.35 * sin(2.0 * PI * freq2 * t)
            buffer[i] = (wave * envelope * Short.MAX_VALUE * 0.75).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    /**
     * Generates a solid wooden tile place / drop clack with double impact (65ms)
     */
    private fun generateWoodPlaceSound(): ShortArray {
        val durationMs = 70
        val numSamples = (SAMPLE_RATE * durationMs / 1000)
        val buffer = ShortArray(numSamples)

        val freqImpact1 = 480.0
        val freqImpact2 = 720.0
        val freqResonance = 310.0

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val env1 = exp(-t * 65.0)
            // Second micro-bounce at 12ms
            val env2 = if (t > 0.012) exp(-(t - 0.012) * 85.0) * 0.4 else 0.0

            val wave1 = 0.5 * sin(2.0 * PI * freqImpact1 * t) + 0.35 * sin(2.0 * PI * freqImpact2 * t)
            val wave2 = 0.15 * sin(2.0 * PI * freqResonance * t)

            val total = (wave1 + wave2) * env1 + (wave1 * env2)
            buffer[i] = (total * Short.MAX_VALUE * 0.85).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    /**
     * Generates subtle wooden slide / drag sound (35ms)
     */
    private fun generateWoodDragSound(): ShortArray {
        val durationMs = 35
        val numSamples = (SAMPLE_RATE * durationMs / 1000)
        val buffer = ShortArray(numSamples)
        val freq = 560.0

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val envelope = sin(PI * (i.toDouble() / numSamples)) * 0.5
            val wave = sin(2.0 * PI * freq * t) + ((Math.random() - 0.5) * 0.25)
            buffer[i] = (wave * envelope * Short.MAX_VALUE * 0.4).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    /**
     * Generates celebratory triumphant Bingo fanfare chord arpeggio (C5 -> E5 -> G5 -> C6 -> E6)
     */
    private fun generateBingoFanfareSound(): ShortArray {
        val durationMs = 1200
        val numSamples = (SAMPLE_RATE * durationMs / 1000)
        val buffer = ShortArray(numSamples)

        // Notes in Hz: C5, E5, G5, C6, E6
        val notes = listOf(523.25, 659.25, 783.99, 1046.50, 1318.51)
        val noteSpacing = (0.13 * SAMPLE_RATE).toInt()

        for (i in 0 until numSamples) {
            var sample = 0.0

            notes.forEachIndexed { index, freq ->
                val noteStart = index * noteSpacing
                if (i >= noteStart) {
                    val tNote = (i - noteStart).toDouble() / SAMPLE_RATE
                    val noteEnv = exp(-tNote * 3.8) // Lingering chime
                    // Fundamental + subtle octave shimmer
                    val noteWave = 0.7 * sin(2.0 * PI * freq * tNote) + 0.3 * sin(2.0 * PI * (freq * 2.0) * tNote)
                    sample += noteWave * noteEnv
                }
            }

            buffer[i] = (sample * Short.MAX_VALUE * 0.55).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }
}
