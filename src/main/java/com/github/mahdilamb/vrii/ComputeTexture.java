package com.github.mahdilamb.vrii;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL3;

import static com.jogamp.opengl.GL.*;
import static com.jogamp.opengl.GL2ES3.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT;
import static org.joml.Math.max;

public class ComputeTexture extends Texture {
    final Renderer renderer;
    final int localSize; // should match the local_size_* in compute glsl
    int lastWidth;
    int lastHeight;

    public ComputeTexture(Renderer renderer, int localSize) {
        this.renderer = renderer;
        this.localSize = localSize;
    }

    @Override
    public void init(GL2 gl) {
        super.init(gl);
        lastHeight = renderer.getHeight();
        lastWidth = renderer.getWidth();

        gl.glActiveTexture(GL_TEXTURE0);
        gl.glBindTexture(GL_TEXTURE_2D, getTextureID());
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA32F, lastWidth, lastHeight, 0, GL_RGBA, GL_FLOAT,
                null);
        gl.glBindImageTexture(0, getTextureID(), 0, false, 0, GL_WRITE_ONLY, GL_RGBA32F);
    }

    static int nextPowerOfTwo(int x) {
        x--;
        x |= x >> 1; // handle 2 bit numbers
        x |= x >> 2; // handle 4 bit numbers
        x |= x >> 4; // handle 8 bit numbers
        x |= x >> 8; // handle 16 bit numbers
        x |= x >> 16; // handle 32 bit numbers
        x++;
        return x;
    }

    static int nextPowerOfTwo(int x, int minVal) {
        return max(minVal, nextPowerOfTwo(x));
    }

    @Override
    public void render(GL2 gl) {
        if (lastWidth != renderer.getWidth() || lastHeight != renderer.getHeight()) {
            gl.glActiveTexture(GL_TEXTURE0);
            gl.glBindTexture(GL_TEXTURE_2D, getTextureID());
            lastHeight = renderer.getHeight();
            lastWidth = renderer.getWidth();
            gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA32F, lastWidth, lastHeight, 0, GL_RGBA, GL_FLOAT, null);
        }
        final GL3 gl3 = gl.getGL3();
        final int worksizeX = nextPowerOfTwo(lastWidth, localSize);
        final int worksizeY = nextPowerOfTwo(lastHeight, localSize);

        gl3.glDispatchCompute(worksizeX / localSize, worksizeY / localSize, 1);
        gl3.glMemoryBarrier(GL_SHADER_IMAGE_ACCESS_BARRIER_BIT);

    }
}
