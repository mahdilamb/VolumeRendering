package com.github.mahdilamb.vrii;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL2;
import org.joml.Matrix4f;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;

import static com.jogamp.opengl.GL.*;
import static com.jogamp.opengl.GL2ES2.GL_TEXTURE_3D;
import static com.jogamp.opengl.GL2ES2.GL_TEXTURE_WRAP_R;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_BASE_LEVEL;

public class Volume extends Texture {
    static Buffer buffer = null;
    final int width;
    final int height;
    final int depth;
    final byte[] data;
    final float[] scale;
    Byte min = null;
    Byte max = null;

    boolean hasChanges = true;

    //from raw 8 bit image
    public Volume(File file, int width, int height, int depth) throws IOException {
        this.width = width;
        this.height = height;
        this.depth = depth;

        final InputStream is = new FileInputStream(Utils.getFilePath(file));
        data = new byte[width * height * depth];
        is.read(data);
        for (final byte b : data) {
            if (min == null || b < min) {
                min = b;
            }
            if (max == null || b > max) {
                max = b;
            }
        }

        buffer = Buffers.newDirectByteBuffer(data);
        scale = new float[]{1, 1, 1};
    }

    public Volume(MosaicVolumeSource source) throws IOException {

        final BufferedImage bufferedImage = ImageIO.read(Utils.getFilePath(source.getFile()));
        width = bufferedImage.getWidth() / source.getNColumns();
        height = bufferedImage.getHeight() / source.getNRows();
        depth = source.getNSlices();
        data = new byte[width * height * depth];
        for (int w = 0, b = 0; w < source.getNColumns(); w++) {
            for (int h = 0; h < source.getNRows(); h++) {
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        data[b++] = (byte) ((bufferedImage.getRGB(w * width + x, h * height + y) >> 16) & 0xff);
                        if (min == null || data[b - 1] < min) {
                            min = data[b - 1];
                        }
                        if (max == null || data[b - 1] > max) {
                            max = data[b - 1];
                        }
                    }
                }
            }
        }
        scale = new float[]{1, 1, source.getZScale()};
        buffer = Buffers.newDirectByteBuffer(data);

    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getDepth() {
        return depth;
    }

    public Buffer getBuffer() {
        return buffer;
    }


    public int getMin() {
        return Byte.toUnsignedInt(min);
    }

    public int getMax() {
        return Byte.toUnsignedInt(max);
    }

    public int getRange() {
        return getMax() - getMin();
    }

    public Matrix4f getModelMatrix() {
        return new Matrix4f();
    }

    @Override
    public void init(GL2 gl) {

        super.init(gl);


        gl.glActiveTexture(GL_TEXTURE0);
        gl.glBindTexture(GL_TEXTURE_3D, getTextureID());
        gl.glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_BASE_LEVEL, 0);
        gl.glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        gl.glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        gl.glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
        gl.glTexImage3D(
                GL_TEXTURE_3D,  // target
                0,              // level
                GL_LUMINANCE,        // internalformat
                getWidth(),           // width
                getHeight(),           // height
                getDepth(),           // depth
                0,              // border
                GL_LUMINANCE,         // format
                GL_UNSIGNED_BYTE,       // type
                buffer.rewind()           // pixel
        );
    }


    public void render(GL2 gl) {
        update(gl);

    }

    private void update(GL2 gl) {
        if (gl == null || hasChanges == false) {
            return;
        }
        if (getTextureID() == 0) {
            init(gl);
        }
        gl.glActiveTexture(GL_TEXTURE0);
        gl.glBindTexture(GL_TEXTURE_3D, getTextureID());

        gl.glTexImage3D(
                GL_TEXTURE_3D,  // target
                0,              // level
                GL_LUMINANCE,        // internalformat
                width,           // width
                height,           // height
                depth,           // depth
                0,              // border
                GL_LUMINANCE,         // format
                GL_UNSIGNED_BYTE,       // type
                buffer.rewind()            // pixel
        );
        hasChanges = false;

    }


    public float[] getScale() {
        return scale;
    }

    public float getScale(int dim) {

        return scale[dim];
    }
}
