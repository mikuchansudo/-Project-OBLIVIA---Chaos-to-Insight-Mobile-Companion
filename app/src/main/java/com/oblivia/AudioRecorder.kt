package com.oblivia

import android.media.MediaRecorder
import java.io.IOException

class AudioRecorder {
    private var recorder: MediaRecorder? = null

    fun startRecording() {
        recorder = MediaRecorder()
        recorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        recorder?.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        recorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        recorder?.setOutputFile("/path/to/output/file.3gp")
        try {
            recorder?.prepare()
            recorder?.start()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun stopRecording() {
        recorder?.stop()
        recorder?.release()
    }
}
