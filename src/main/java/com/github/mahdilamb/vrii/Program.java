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

public class Program {
    static final List<Program> programs = new Vector<>();
    private final String name;

    Integer programId = null;
    private final String vertexShader;
    private final String fragmentShader;

    int vertexShaderId;
    int fragmentShaderId;
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
        final File directory = new File(file.getParent());
        final String mask = file.getName();
        this.name = null;
        this.vertexShader = Files.readAllLines(Paths.get(directory.getPath(), mask + ".vert")).stream().collect(Collectors.joining("\n"));
        this.fragmentShader = Files.readAllLines(Paths.get(directory.getPath(), mask + ".frag")).stream().collect(Collectors.joining("\n"));

    }

    Program(String name, File vertexShaderFile, File fragmentShaderFile) throws IOException {
        this.name = name;
        this.vertexShader = Files.readAllLines(Paths.get(vertexShaderFile.getAbsolutePath())).stream().collect(Collectors.joining("\n"));
        this.fragmentShader = Files.readAllLines(Paths.get(fragmentShaderFile.getAbsolutePath())).stream().collect(Collectors.joining("\n"));

    }

    public void init(GL2 gl) {
        if (programId != null) {
            return;
        }
        final ByteBuffer infoLog = ByteBuffer.allocate(512);
        final IntBuffer success = IntBuffer.allocate(1);

        vertexShaderId = gl.glCreateShader(GL_VERTEX_SHADER);
        gl.glShaderSource(vertexShaderId, 1, new String[]{vertexShader}, null);
        gl.glCompileShader(vertexShaderId);
        gl.glGetShaderiv(vertexShaderId, GL_COMPILE_STATUS, success);
        if (success.get(0) != 1) {
            gl.glGetShaderInfoLog(vertexShaderId, 512, null, infoLog);
            System.out.println(new String(infoLog.array()));
        }

        fragmentShaderId = gl.glCreateShader(GL_FRAGMENT_SHADER);
        gl.glShaderSource(fragmentShaderId, 1, new String[]{fragmentShader}, null);
        gl.glCompileShader(fragmentShaderId);
        gl.glGetShaderiv(fragmentShaderId, GL_COMPILE_STATUS, success);
        if (success.get(0) != 1) {
            gl.glGetShaderInfoLog(fragmentShaderId, 512, null, infoLog);
            System.out.println(new String(infoLog.array()));
        }

        programId = gl.glCreateProgram();
        gl.glAttachShader(programId, vertexShaderId);
        gl.glAttachShader(programId, fragmentShaderId);
        gl.glLinkProgram(programId);

        gl.glGetProgramiv(programId, GL_LINK_STATUS, success);
        if (success.get(0) != 1) {
            gl.glGetProgramInfoLog(programId, 512, null, infoLog);
            System.out.println(new String(infoLog.array()));
        }
        gl.glDetachShader(programId,vertexShaderId);
        gl.glDetachShader(programId,fragmentShaderId);
        gl.glDeleteShader(vertexShaderId);
        gl.glDeleteShader(fragmentShaderId);
        programs.add(this);


    }

    public void updateAttributePointers(GL2 gl) {



    }

    public void destroy(GL2 gl) {
        if (programId != null) {
            gl.glDetachShader(programId, vertexShaderId);
            gl.glDetachShader(programId, fragmentShaderId);
            gl.glDeleteShader(vertexShaderId);
            gl.glDeleteShader(fragmentShaderId);
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
