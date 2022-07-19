package com.hunterwb.automanifest;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.PackageElement;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Map;
import java.util.jar.Manifest;

final class Util {

    private Util() {}

    static InputStream openResourceInput(ProcessingEnvironment env, String name) {
        try {
            return env.getFiler().getResource(StandardLocation.CLASS_OUTPUT, "", name).openInputStream();
        } catch (IOException e) {
            return null;
        }
    }

    static OutputStream openResourceOutput(ProcessingEnvironment env, String name) throws IOException {
        return env.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", name).openOutputStream();
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

    static String[] nameComponents(PackageElement p) {
        return p.isUnnamed() ? new String[0] : p.getQualifiedName().toString().split("\\.", -1);
    }

    static String getStackTraceAsString(Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        throwable.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }

    static String joinToString(Object[] array, char delimiter) {
        if (array.length == 0) return "";
        StringBuilder sb = new StringBuilder();
        sb.append(array[0]);
        for (int i = 1; i < array.length; i++) {
            sb.append(delimiter).append(array[i]);
        }
        return sb.toString();
    }

    static int matchLength(Object[] a, Object[] b) {
        int n = Math.min(a.length, b.length);
        int i = 0;
        while (i < n && a[i].equals(b[i])) {
            i++;
        }
        return i;
    }

    static <T> T[] resize(T[] array, int length) {
        return length == array.length ? array : Arrays.copyOf(array, length);
    }

    static <T> T requireNonNull(T obj) {
        if (obj == null) throw new NullPointerException();
        return obj;
    }

    static <K, V> V putIfAbsent(Map<K, V> map, K key, V value) {
        V v = map.get(key);
        if (v == null) v = map.put(key, value);
        return v;
    }

    static <K, V> V getOrDefault(Map<K, V> map, K key, V value) {
        V v = map.get(key);
        if (v == null) v = value;
        return v;
    }
}
