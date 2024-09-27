package com.sdidsa.bondcheck.abs.components.controls.audio;

import java.io.File;

public record AudioFile(File file, long duration, float loudness) {
    @Override
    public float loudness() {
        return loudness;
    }
}
