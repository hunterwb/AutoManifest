package com.hunterwb.automanifest;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.jar.Manifest;

final class Env {

    private final ProcessingEnvironment env;

    private boolean error;

    Env(ProcessingEnvironment env) {
        if (env == null) throw new NullPointerException();
        this.env = env;
    }

    Map<String, String> options() {
        return env.getOptions();
    }

    void note(String msg) {
        env.getMessager().printMessage(Diagnostic.Kind.NOTE, msg);
    }

    void warning(String msg) {
        env.getMessager().printMessage(Diagnostic.Kind.WARNING, msg);
    }

    void error(String msg) {
        error = true;
        env.getMessager().printMessage(Diagnostic.Kind.ERROR, msg);
    }

    void error(Exception e) {
        error(getStackTraceAsString(e));
    }

    boolean error() {
        return error;
    }

    String binaryName(TypeElement type) {
        return env.getElementUtils().getBinaryName(type).toString();
    }

    TypeElement typeElement(String name) {
        return env.getElementUtils().getTypeElement(name);
    }

    PackageElement packageOf(Element e) {
        return env.getElementUtils().getPackageOf(e);
    }

    ArrayType arrayType(TypeMirror componentType) {
        return env.getTypeUtils().getArrayType(componentType);
    }

    InputStream openResourceInput(String name) {
        try {
            return env.getFiler().getResource(StandardLocation.CLASS_OUTPUT, "", name).openInputStream();
        } catch (IOException e) {
            return null;
        }
    }

    OutputStream openResourceOutput(String name) throws IOException {
        return env.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", name).openOutputStream();
    }

    static String[] packageNameComponents(PackageElement p) {
        return p.isUnnamed() ? new String[0] : p.getQualifiedName().toString().split("\\.", -1);
    }

    static TypeMirror[] parameterTypes(ExecutableElement e) {
        List<? extends VariableElement> params = e.getParameters();
        TypeMirror[] types = new TypeMirror[params.size()];
        for (int i = 0; i < types.length; i++) {
            types[i] = params.get(i).asType();
        }
        return types;
    }

    static Manifest readManifestClose(InputStream inputStream) throws IOException {
        Manifest m = null;
        IOException e = null;
        try {
            m = new Manifest(inputStream);
        } catch (IOException readException) {
            e = readException;
        }
        try {
            inputStream.close();
        } catch (IOException closeException) {
            if (e == null) e = closeException;
        }
        if (e != null) throw e;
        return m;
    }

    static void writeManifestClose(Manifest m, OutputStream outputStream) throws IOException {
        IOException e = null;
        try {
            m.write(outputStream);
        } catch (IOException writeException) {
            e = writeException;
        }
        try {
            outputStream.close();
        } catch (IOException closeException) {
            if (e == null) e = closeException;
        }
        if (e != null) throw e;
    }

    static String getStackTraceAsString(Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        throwable.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }

    static String joinToString(Object[] array, char delimiter) {
        if (array.length == 0) return "";
        StringBuilder sb = (new StringBuilder()).append(array[0]);
        for (int i = 1; i < array.length; i++) {
            sb.append(delimiter).append(array[i]);
        }
        return sb.toString();
    }

    static <T> int matchLength(T[] a1, T[] a2) {
        int n = Math.min(a1.length, a2.length);
        int i = 0;
        while (i < n && a1[i].equals(a2[i])) {
            i++;
        }
        return i;
    }

    static <T> T[] withLength(T[] array, int length) {
        return length == array.length ? array : Arrays.copyOf(array, length);
    }

    static <K, V> void putIfAbsent(Map<K, V> map, K key, V value) {
        if (!map.containsKey(key)) map.put(key, value);
    }
}
