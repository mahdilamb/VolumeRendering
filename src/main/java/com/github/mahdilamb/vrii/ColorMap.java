package com.github.mahdilamb.vrii;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL2;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static com.jogamp.opengl.GL.*;

public class ColorMap extends Texture {
    final byte[] originalColors = new byte[256 * 3];
    static final byte[] colors = new byte[256 * 4];
    public static float[] opacityLevels = new float[]{0, 1};
    public static float[] opacityNodes = new float[]{0f, 1f};
    public static float[] colorRange = new float[]{0f, 1f};
    static boolean hasChanges = true;
    Renderer renderer;

    public ColorMap(Renderer renderer, File source) throws IOException {
        this.renderer = renderer;
        final BufferedImage bufferedImage = ImageIO.read(Utils.getFilePath(source));
        for (int i = 0; i < 256; i++) {
            final int color = bufferedImage.getRGB(i, 0);


            originalColors[i * 3 + 0] = (byte) ((color >> 16) & 0xff);
            originalColors[i * 3 + 1] = (byte) ((color >> 8) & 0xff);
            originalColors[i * 3 + 2] = (byte) ((color) & 0xff);

        }
        update(null);

    }

    public ColorMap(File source) throws IOException {
        this(null, source);
    }

    public void setRenderer(Renderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public void init(GL2 gl) {
        super.init(gl);
        update(gl);
        gl.glActiveTexture(GL_TEXTURE1);
        gl.glBindTexture(GL_TEXTURE_2D, getTextureID());
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, 1, 1, 0, GL_RGBA, GL_UNSIGNED_BYTE, Buffers.newDirectByteBuffer(colors));

    }

    @Override
    public void render(GL2 gl) {
        update(gl);
    }


    public Color getColorAt(int position) {
        return new Color(
                Byte.toUnsignedInt(originalColors[position * 3 + 0]),
                Byte.toUnsignedInt(originalColors[position * 3 + 1]),
                Byte.toUnsignedInt(originalColors[position * 3 + 2])
        );
    }


    void update(GL2 gl) {

        if (renderer != null && (hasChanges && renderer.colorMap != null)) {
            final float colorRange = ColorMap.colorRange[1] - ColorMap.colorRange[0];
            final float min = opacityLevels[0] * opacityLevels[0];
            final float max = opacityLevels[1] * opacityLevels[1];
            final float opacityNodeRange = opacityNodes[1] - opacityNodes[0];
            for (int i = 0; i < 256; i++) {
                float px = ((float) i) / 255;
                float a;
                if (px <= opacityNodes[0]) {
                    a = opacityNodes[0];
                } else if (px > opacityNodes[1]) {
                    a = opacityNodes[1];
                } else {
                    final float ratio = (px - opacityNodes[0]) / opacityNodeRange;
                    a = (min * (1 - ratio) + max * ratio);
                }
                int colorI = 0;
                if (px > ColorMap.colorRange[1] * 255) {
                    colorI = 255;
                } else if (px > ColorMap.colorRange[0]) {
                    colorI = Math.round(((((float) i) / 255) - ColorMap.colorRange[0]) * (1f / colorRange) * 255f);
                }

                float r = ((float) Byte.toUnsignedInt(renderer.colorMap.originalColors[colorI * 3 + 0])) / 255;
                float g = ((float) Byte.toUnsignedInt(renderer.colorMap.originalColors[colorI * 3 + 1])) / 255;
                float b = ((float) Byte.toUnsignedInt(renderer.colorMap.originalColors[colorI * 3 + 2])) / 255;

                r = r * r * a;
                g = g * g * a;
                b = b * b * a;


                colors[i * 4 + 0] = (byte) Math.round(r * 255);
                colors[i * 4 + 1] = (byte) Math.round(g * 255);
                colors[i * 4 + 2] = (byte) Math.round(b * 255);
                colors[i * 4 + 3] = (byte) Math.round(a * 255);

            }


            hasChanges = false;

        }
        if (gl != null) {


            gl.glActiveTexture(GL_TEXTURE1);
            gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, 256, 1, 0, GL_RGBA, GL_UNSIGNED_BYTE, Buffers.newDirectByteBuffer(colors).rewind());
        }
    }
}
