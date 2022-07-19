package com.hunterwb.automanifest;

import javax.annotation.processing.Completion;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public final class AutoManifest implements Processor {

    private static final String OPTION_NAME = "automanifest";

    private static final String CREATED_BY = "Created-By";
    private static final String AUTOMATIC_MODULE_NAME = "Automatic-Module-Name";
    private static final String MAIN_CLASS = "Main-Class";
    private static final String PREMAIN_CLASS = "Premain-Class";
    private static final String AGENT_CLASS = "Agent-Class";
    private static final String LAUNCHER_AGENT_CLASS = "Launcher-Agent-Class";
    private static final String BUILT_BY = "Built-By";
    private static final String BUILD_JDK_SPEC = "Build-Jdk-Spec";
    private static final String BUILD_JDK = "Build-Jdk";
    private static final String BUILD_OS = "Build-Os";

    private static final String MANIFEST_VERSION_VALUE = "1.0";

    private static final String MANIFEST_NAME = "META-INF/MANIFEST.MF";

    private ProcessingEnvironment env;

    private final Map<Attributes.Name, Object> entries = new LinkedHashMap<Attributes.Name, Object>();

    @Override public Set<String> getSupportedOptions() {
        return Collections.singleton(OPTION_NAME);
    }

    @Override public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton("*");
    }

    @Override public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override public Iterable<? extends Completion> getCompletions(Element element, AnnotationMirror annotation, ExecutableElement member, String userText) {
        return Collections.emptyList();
    }

    @Override public void init(ProcessingEnvironment processingEnv) {
        if (env != null) throw new IllegalStateException();
        env = Util.requireNonNull(processingEnv);
        try {
            init();
        } catch (Exception e) {
            env.getMessager().printMessage(Diagnostic.Kind.ERROR, Util.getStackTraceAsString(e));
        }
    }

    private void init() {
        String options = Util.getOrDefault(env.getOptions(), OPTION_NAME, MAIN_CLASS);
        for (String option : options.split(",", -1)) {
            int colon = option.indexOf(':');
            if (colon == -1) {
                Attributes.Name name = name(option);
                if (name != null) {
                    Object value = getValue(name);
                    if (value != null) {
                        addEntry(name, value);
                    }
                }
            } else {
                Attributes.Name name = name(option.substring(0, colon));
                if (name != null) {
                    String value = option.substring(colon + 1);
                    addEntry(name, value);
                }
            }
        }
    }

    private Attributes.Name name(String s) {
        Attributes.Name name;
        if (s.startsWith("From") || s.equalsIgnoreCase("Name")) {
            name = null;
        } else {
            try {
                name = new Attributes.Name(s);
            } catch (IllegalArgumentException e) {
                name = null;
            }
        }
        if (name == null) {
            env.getMessager().printMessage(Diagnostic.Kind.WARNING, "Illegal name: " + s);
        }
        return name;
    }

    private void addEntry(Attributes.Name name, Object value) {
        if (entries.put(name, value) != null) {
            env.getMessager().printMessage(Diagnostic.Kind.WARNING, "Duplicate name: " + name);
        }
    }

    private Object getValue(Attributes.Name name) {
        String s = name.toString();
        if (s.equalsIgnoreCase(CREATED_BY) || s.equalsIgnoreCase(BUILD_JDK)) {
            return System.getProperty("java.version") + " (" + System.getProperty("java.vendor") + ')';
        } else if (s.equalsIgnoreCase(AUTOMATIC_MODULE_NAME)) {
            return new CommonPackageFinder(env);
        } else if (s.equalsIgnoreCase(MAIN_CLASS)) {
            return MainMethodFinder.main(env);
        } else if (s.equalsIgnoreCase(PREMAIN_CLASS)) {
            return MainMethodFinder.premain(env);
        } else if (s.equalsIgnoreCase(AGENT_CLASS) || s.equalsIgnoreCase(LAUNCHER_AGENT_CLASS)) {
            return MainMethodFinder.agentmain(env);
        } else if (s.equalsIgnoreCase(BUILT_BY)) {
            return System.getProperty("user.name");
        } else if (s.equalsIgnoreCase(BUILD_JDK_SPEC)) {
            return System.getProperty("java.specification.version");
        } else if (s.equalsIgnoreCase(BUILD_OS)) {
            return System.getProperty("os.name") + " (" + System.getProperty("os.version") + "; " + System.getProperty("os.arch") + ')';
        }
        env.getMessager().printMessage(Diagnostic.Kind.WARNING, "Unrecognized name: " + s);
        return null;
    }

    @Override public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (!entries.isEmpty()) {
            try {
                if (roundEnv.processingOver()) {
                    writeManifest();
                } else {
                    for (Object v : entries.values()) {
                        if (v instanceof RootVisitor) {
                            ((RootVisitor) v).visitRootElements(roundEnv.getRootElements());
                        }
                    }
                }
            } catch (Exception e) {
                env.getMessager().printMessage(Diagnostic.Kind.ERROR, Util.getStackTraceAsString(e));
            }
        }
        return false;
    }

    private void writeManifest() throws IOException {
        InputStream inputStream = Util.openResourceInput(env, MANIFEST_NAME);
        Manifest manifest = inputStream == null ? new Manifest() : Util.readManifestClose(inputStream);
        updateManifest(manifest);
        OutputStream outputStream = Util.openResourceOutput(env, MANIFEST_NAME);
        Util.writeManifestClose(manifest, new BufferedOutputStream(outputStream));
    }

    private void updateManifest(Manifest m) {
        Attributes attr = m.getMainAttributes();
        Util.putIfAbsent(attr, Attributes.Name.MANIFEST_VERSION, MANIFEST_VERSION_VALUE);
        for (Map.Entry<Attributes.Name, Object> e : entries.entrySet()) {
            Attributes.Name name = e.getKey();
            Object value = e.getValue();
            if (value instanceof RootVisitor) {
                value = ((RootVisitor) value).getValue();
                if (value == null) continue;
            }
            attr.put(name, value);
            env.getMessager().printMessage(Diagnostic.Kind.NOTE, name + ": " + value);
        }
    }
}
