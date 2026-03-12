package com.example.juegoaerolinea.util

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool

/**
 * Manejo simple de sonidos del juego.
 *
 * TODO: Agregar archivos .ogg/.wav en res/raw/ para cada efecto:
 *   - res/raw/sound_buy.ogg       → al comprar/mejorar estación
 *   - res/raw/sound_collect.ogg   → al recolectar bono
 *   - res/raw/sound_prestige.ogg  → al prestigiar
 *
 * Ejemplo de uso:
 *   SoundManager.init(context)
 *   SoundManager.playBuy()
 */
object SoundManager {
    private var soundPool: SoundPool? = null
    private var buyId = 0
    private var collectId = 0
    private var prestigeId = 0
    private var isInitialized = false

    fun init(context: Context) {
        if (isInitialized) return
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(attributes)
            .build()

        // TODO: Descomentar cuando se agreguen los archivos de sonido en res/raw/
        // buyId = soundPool!!.load(context, R.raw.sound_buy, 1)
        // collectId = soundPool!!.load(context, R.raw.sound_collect, 1)
        // prestigeId = soundPool!!.load(context, R.raw.sound_prestige, 1)

        isInitialized = true
    }

    fun playBuy() {
        // TODO: Descomentar cuando los sonidos estén cargados
        // soundPool?.play(buyId, 1f, 1f, 1, 0, 1f)
    }

    fun playCollect() {
        // TODO: soundPool?.play(collectId, 1f, 1f, 1, 0, 1f)
    }

    fun playPrestige() {
        // TODO: soundPool?.play(prestigeId, 1f, 1f, 1, 0, 1f)
    }

    fun release() {
        soundPool?.release()
        soundPool = null
        isInitialized = false
    }
}
