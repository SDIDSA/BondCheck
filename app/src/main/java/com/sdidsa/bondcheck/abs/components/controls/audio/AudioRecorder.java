package com.sdidsa.bondcheck.abs.components.controls.audio;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Build;

import com.sdidsa.bondcheck.abs.utils.ContextUtils;
import com.sdidsa.bondcheck.abs.utils.ErrorHandler;
import com.sdidsa.bondcheck.app.app_content.session.permission.PermissionCheck;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

public class AudioRecorder {

    private MediaRecorder recorder = null;
    private File file;

    public boolean startRecording(Context context) {
        if(PermissionCheck.hasMicrophonePermission(context)) {
            ContextUtils.requirePermissions(context, () -> start(context),
                    android.Manifest.permission.RECORD_AUDIO);
            return true;
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    private void start(Context context) {
        try {
            file = File.createTempFile("record", ".mp4");
            file.deleteOnExit();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                recorder = new MediaRecorder(context);
            }else {
                recorder = new MediaRecorder();
            }
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setOutputFile(file);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            recorder.setAudioEncodingBitRate(16*44100);
            recorder.setAudioSamplingRate(44100);
            recorder.prepare();
        } catch (IOException e) {
            ErrorHandler.handle(e, "recording audio from mic");
        }

        recorder.start();
    }

    public void stopRecording(Consumer<File> onSuccess) {
        recorder.stop();
        recorder.release();
        onSuccess.accept(file);
        recorder = null;
    }

}
