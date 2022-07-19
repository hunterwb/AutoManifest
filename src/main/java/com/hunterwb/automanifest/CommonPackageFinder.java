package com.hunterwb.automanifest;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.tools.Diagnostic;

final class CommonPackageFinder extends RootVisitor {

    private final ProcessingEnvironment env;

    private String[] pkg;

    CommonPackageFinder(ProcessingEnvironment env) {
        this.env = env;
    }

    @Override protected Void defaultAction(Element e, Void unused) {
        PackageElement p = env.getElementUtils().getPackageOf(e);
        if (p != null) {
            apply(Util.nameComponents(p));
        }
        return null;
    }

    private void apply(String[] pkg2) {
        if (pkg == null) {
            pkg = pkg2;
        } else {
            int n = Util.matchLength(pkg, pkg2);
            if (n != pkg.length) {
                pkg = Util.resize(pkg2, n);
            }
        }
    }

    @Override public String getValue() {
        if (pkg == null || pkg.length == 0) {
            env.getMessager().printMessage(Diagnostic.Kind.WARNING, "Found no common package");
            return null;
        }
        return Util.joinToString(pkg, '.');
    }
}