package com.github.mahdilamb.vrii;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL3;

import static com.jogamp.opengl.GL.*;
import static com.jogamp.opengl.GL2ES3.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT;

public class ComputeTexture extends Texture {
    @Override
    public void init(GL2 gl) {
        super.init(gl);
        gl.glActiveTexture(GL_TEXTURE0);
        gl.glBindTexture(GL_TEXTURE_2D, getTextureID());
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA32F, Renderer.getWidth(), Renderer.getHeight(), 0, GL_RGBA, GL_FLOAT,
                null);
        gl.glBindImageTexture(0, getTextureID(), 0, false, 0, GL_WRITE_ONLY, GL_RGBA32F);
    }

    @Override
    public void render(GL2 gl) {
        final GL3 gl3 = gl.getGL3();
        gl3.glDispatchCompute(Renderer.getWidth(), Renderer.getHeight(), 1);
        gl3.glMemoryBarrier(GL_SHADER_IMAGE_ACCESS_BARRIER_BIT);

    }
}
