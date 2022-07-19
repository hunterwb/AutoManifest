package com.hunterwb.automanifest;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

final class MainMethodFinder extends RootVisitor {

    private final ProcessingEnvironment env;

    private final String name;

    private final Collection<List<TypeMirror>> allParameterTypes;

    private final TreeSet<String> classNames = new TreeSet<String>();

    private MainMethodFinder(ProcessingEnvironment env, String name, Collection<List<TypeMirror>> allParameterTypes) {
        this.env = env;
        this.name = name;
        this.allParameterTypes = allParameterTypes;
    }

    @Override public Void visitType(TypeElement e, Void unused) {
        for (Element m : e.getEnclosedElements()) {
            m.accept(this, null);
        }
        return null;
    }

    @Override public Void visitExecutable(ExecutableElement e, Void unused) {
        if (isMainMethod(e)) {
            TypeElement enclosing = ((TypeElement) e.getEnclosingElement());
            String binaryName = env.getElementUtils().getBinaryName(enclosing).toString();
            classNames.add(binaryName);
        }
        return null;
    }

    private boolean isMainMethod(ExecutableElement e) {
        if (e.getKind() != ElementKind.METHOD) return false;
        if (!e.getSimpleName().contentEquals(name)) return false;
        Set<Modifier> modifiers = e.getModifiers();
        if (!modifiers.contains(Modifier.PUBLIC) || !modifiers.contains(Modifier.STATIC)) return false;
        if (e.getReturnType().getKind() != TypeKind.VOID) return false;
        List<? extends TypeMirror> parameterTypes = ((ExecutableType) e.asType()).getParameterTypes();
        return allParameterTypes.contains(parameterTypes);
    }

    @Override public String getValue() {
        switch (classNames.size()) {
            case 0:
                env.getMessager().printMessage(Diagnostic.Kind.WARNING, "Found no " + name + " method");
                return null;
            case 1:
                return classNames.first();
            default:
                env.getMessager().printMessage(Diagnostic.Kind.WARNING, "Found multiple " + name + " methods: " + classNames);
                return null;
        }
    }

    static MainMethodFinder main(ProcessingEnvironment env) {
        TypeMirror string = env.getElementUtils().getTypeElement("java.lang.String").asType();
        TypeMirror stringArray = env.getTypeUtils().getArrayType(string);
        return new MainMethodFinder(env, "main", Collections.singletonList(Collections.singletonList(stringArray)));
    }

    static MainMethodFinder premain(ProcessingEnvironment env) {
        return premain(env, "premain");
    }

    static MainMethodFinder agentmain(ProcessingEnvironment env) {
        return premain(env, "agentmain");
    }

    private static MainMethodFinder premain(ProcessingEnvironment env, String name) {
        TypeMirror string = env.getElementUtils().getTypeElement("java.lang.String").asType();
        TypeMirror instrumentation = env.getElementUtils().getTypeElement("java.lang.instrument.Instrumentation").asType();
        return new MainMethodFinder(env, name, Arrays.asList(Arrays.asList(string, instrumentation), Arrays.asList(string)));
    }
}
