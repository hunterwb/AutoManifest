package com.hunterwb.automanifest;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

final class MainMethodFinder extends RootVisitor {

    private final Env env;

    private final String name;

    private final TypeMirror[][] potentialParameterTypes;

    private final TreeSet<String> classNames = new TreeSet<String>();

    private MainMethodFinder(Env env, String name, TypeMirror[]... potentialParameterTypes) {
        this.env = env;
        this.name = name;
        this.potentialParameterTypes = potentialParameterTypes;
    }

    @Override public Void visitType(TypeElement e, Void unused) {
        for (Element m : e.getEnclosedElements()) {
            m.accept(this, null);
        }
        return null;
    }

    @Override public Void visitExecutable(ExecutableElement e, Void unused) {
        if (isMainMethod(e)) {
            classNames.add(env.binaryName((TypeElement) e.getEnclosingElement()));
        }
        return null;
    }

    private boolean isMainMethod(ExecutableElement e) {
        if (e.getKind() != ElementKind.METHOD) return false;
        if (!e.getSimpleName().contentEquals(name)) return false;
        Set<Modifier> modifiers = e.getModifiers();
        if (!modifiers.contains(Modifier.PUBLIC) || !modifiers.contains(Modifier.STATIC)) return false;
        if (e.getReturnType().getKind() != TypeKind.VOID) return false;
        TypeMirror[] parameterTypes = Env.parameterTypes(e);
        for (TypeMirror[] pts : potentialParameterTypes) {
            if (Arrays.equals(parameterTypes, pts)) return true;
        }
        return false;
    }

    @Override public String getValue() {
        switch (classNames.size()) {
            case 0:
                env.warning("Found no " + name + " method");
                return null;
            case 1:
                return classNames.first();
            default:
                env.warning("Found multiple " + name + " methods: " + classNames);
                return null;
        }
    }

    static MainMethodFinder main(Env env) {
        TypeMirror string = env.typeElement("java.lang.String").asType();
        TypeMirror stringArray = env.arrayType(string);
        return new MainMethodFinder(env, "main", new TypeMirror[]{stringArray});
    }

    static MainMethodFinder premain(Env env) {
        return premain(env, "premain");
    }

    static MainMethodFinder agentmain(Env env) {
        return premain(env, "agentmain");
    }

    private static MainMethodFinder premain(Env env, String name) {
        TypeMirror string = env.typeElement("java.lang.String").asType();
        TypeMirror instrumentation = env.typeElement("java.lang.instrument.Instrumentation").asType();
        return new MainMethodFinder(env, name, new TypeMirror[]{string, instrumentation}, new TypeMirror[]{string});
    }
}
