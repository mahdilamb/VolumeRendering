package com.github.mahdilamb.vrii;

import com.jogamp.opengl.GL2;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static com.jogamp.opengl.GL2ES2.*;
import static com.jogamp.opengl.GL3ES3.GL_COMPUTE_SHADER;

public class Program {
    static final List<Program> programs = new Vector<>();
    private final String name;

    Integer programId = null;
    private final Map<Integer, String> shaderCode = new HashMap<>();
    private final Map<Integer, Integer> shaderIds = new HashMap<>();


    static final File vertexShaderFile = new File("D:\\Documents\\idea\\VolumeRenderingMark2\\src\\main\\resources\\shaders\\vertex.vert");
    public static Program specular;
    public static Program basic;
    public static Program maxIntensity;
    public static Program shaded;
    public static Program realistic;
    public static Program edges;
    public static Program test;

    final static Map<String, Integer> attributeLocations = new HashMap<>();
    final static Map<String, Integer> uniformLocations = new HashMap<>();
    final static Map<String, BiConsumer<GL2, Integer>> uniforms = new HashMap<>();


    static {
        try {

            maxIntensity = new Program(
                    "MaximumIntensity",
                    vertexShaderFile,
                    new File("D:\\Documents\\idea\\VolumeRenderingMark2\\src\\main\\resources\\shaders\\maxValue.frag")
            );

            test = new Program(
                    "Test",
                    new File("D:\\Documents\\idea\\VolumeRenderingMark2\\src\\main\\resources\\shaders\\test.vert"),
                    new File("D:\\Documents\\idea\\VolumeRenderingMark2\\src\\main\\resources\\shaders\\test.frag")
            );

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Program(File file) throws IOException {
        this.name = null;

        final File directory = file.isDirectory() ? file : new File(file.getParent());
        final String mask = file.isDirectory() ? "" : file.getName();
        for (final File tmpFile : directory.listFiles()) {

            if (tmpFile.isDirectory() || !tmpFile.getName().startsWith(mask)) {
                continue;
            }
            System.out.println(tmpFile);
            switch (tmpFile.getName().substring(tmpFile.getName().lastIndexOf(".") + 1)) {
                case "frag":
                case "fs":
                    shaderCode.put(GL_FRAGMENT_SHADER, Files.readAllLines(Paths.get(tmpFile.getAbsolutePath())).stream().collect(Collectors.joining("\n")));
                    break;
                case "vert":
                case "vs":
                    shaderCode.put(GL_VERTEX_SHADER, Files.readAllLines(Paths.get(tmpFile.getAbsolutePath())).stream().collect(Collectors.joining("\n")));
                    break;
                case "cs":
                case "compute":
                    shaderCode.put(GL_COMPUTE_SHADER, Files.readAllLines(Paths.get(tmpFile.getAbsolutePath())).stream().collect(Collectors.joining("\n")));
                    break;
            }

        }
    }

    Program(String name, File vertexShaderFile, File fragmentShaderFile) throws IOException {
        this.name = name;
        shaderCode.put(GL_VERTEX_SHADER, Files.readAllLines(Paths.get(vertexShaderFile.getAbsolutePath())).stream().collect(Collectors.joining("\n")));
        shaderCode.put(GL_FRAGMENT_SHADER, Files.readAllLines(Paths.get(fragmentShaderFile.getAbsolutePath())).stream().collect(Collectors.joining("\n")));


    }

    public void init(GL2 gl) {
        if (programId != null) {
            return;
        }
        final ByteBuffer infoLog = ByteBuffer.allocate(512);
        final IntBuffer success = IntBuffer.allocate(1);
        for (final Map.Entry<Integer, String> shader : shaderCode.entrySet()) {
            final int shaderId = gl.glCreateShader(shader.getKey());
            shaderIds.put(shader.getKey(), shaderId);
            gl.glShaderSource(shaderId, 1, new String[]{shader.getValue()}, null);
            gl.glCompileShader(shaderId);
            gl.glGetShaderiv(shaderId, GL_COMPILE_STATUS, success);
            if (success.get(0) != 1) {
                gl.glGetShaderInfoLog(shaderId, 512, null, infoLog);
                System.out.println(new String(infoLog.array()));
            }
        }


        programId = gl.glCreateProgram();
        for (final Map.Entry<Integer, Integer> shaderId : shaderIds.entrySet()) {

            gl.glAttachShader(programId, shaderId.getValue());
        }
        programs.add(this);

        if (shaderIds.size() == 0) {
            return;
        }
        gl.glLinkProgram(programId);

        gl.glGetProgramiv(programId, GL_LINK_STATUS, success);
        if (success.get(0) != 1) {
            gl.glGetProgramInfoLog(programId, 512, null, infoLog);
            System.out.println(new String(infoLog.array()));
        }
        for (final Map.Entry<Integer, Integer> shaderId : shaderIds.entrySet()) {

            gl.glDetachShader(programId, shaderId.getValue());
            gl.glDeleteShader(shaderId.getValue());
        }


    }


    public void destroy(GL2 gl) {
        if (programId != null) {
            for (final Map.Entry<Integer, Integer> shaderId : shaderIds.entrySet()) {

                gl.glDetachShader(programId, shaderId.getValue());
                gl.glDeleteShader(shaderId.getValue());
            }
            gl.glDeleteProgram(programId);
        }
    }

    public void use(GL2 gl) {
        init(gl);
        gl.glUseProgram(programId);
    }

    public void allocateUniform(GL2 gl, String uniformName, BiConsumer<GL2, Integer> function) {
        init(gl);
        final int uniformLocation = gl.glGetUniformLocation(programId, uniformName);
        uniformLocations.put(uniformName, uniformLocation);
        uniforms.put(uniformName, function);

    }

    public void setUniforms(GL2 gl) {

        for (final Map.Entry<String, BiConsumer<GL2, Integer>> uniform : uniforms.entrySet()) {
            uniform.getValue().accept(gl, uniformLocations.get(uniform.getKey()));
        }
    }

    public static void destroyAllPrograms(GL2 gl) {
        for (final Program program : programs) {
            program.destroy(gl);
        }
    }

    public static void unUse(GL2 gl) {
        gl.glUseProgram(0);
    }

    @Override
    public String toString() {
        if (name == null) {
            return super.toString();
        }
        return name;
    }
}
