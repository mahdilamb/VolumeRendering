package com.github.mahdilamb.vrii;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL2;

import java.nio.Buffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.Vector;

public abstract class Texture {
    static final List<Texture> textures = new Vector<>();
    int id;

    public void init(GL2 gl) {
        IntBuffer intBuffer = IntBuffer.allocate(1);
        gl.glGenTextures(1, intBuffer);
        id = intBuffer.get(0);
        textures.add(this);

    }

    public abstract void render(GL2 gl);

    public void destroy(GL2 gl) {

        gl.glDeleteTextures(1, Buffers.newDirectIntBuffer(new int[]{id}));
    }

    public static void destroyAllTextures(GL2 gl) {
        for (final Texture texture : textures) {
            texture.destroy(gl);
        }
    }

    public int getTextureID(){
        return id;
    }
}
