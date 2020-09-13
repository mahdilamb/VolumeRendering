package com.github.mahdilamb.vrii;

import java.io.File;

public class MosaicVolumeSource {
    private final String name;
    private final File source;
    private final int columns;
    private final int rows;
    private final int slices;
    private final float zScale;

    public MosaicVolumeSource(String name, File source, int columns, int slices, float zScale) {
        this.name = name;
        this.source = source;
        this.columns = columns;
        this.slices = slices;
        this.rows = slices/columns;
        this.zScale = zScale;
    }

    @Override
    public String toString() {
        return name;
    }

    public File getFile() {
        return source;
    }

    public int getNColumns() {
        return columns;
    }

    public int getNRows() {
        return rows;
    }

    public int getNSlices() {
        return slices;
    }

    public float getZScale() {
        return zScale;
    }
}
