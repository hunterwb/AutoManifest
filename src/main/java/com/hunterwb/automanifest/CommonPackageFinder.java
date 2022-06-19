package com.hunterwb.automanifest;

import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;

final class CommonPackageFinder extends RootVisitor {

    private final Env env;

    private final CommonPrefix<String> pkg = new CommonPrefix<String>();

    CommonPackageFinder(Env env) {
        this.env = env;
    }

    @Override protected Void defaultAction(Element e, Void unused) {
        PackageElement p = env.packageOf(e);
        if (p != null) pkg.apply(Env.packageNameComponents(p));
        return null;
    }

    @Override public String getValue() {
        if (pkg.components.length == 0) {
            env.warning("Found no common package");
            return null;
        }
        return Env.joinToString(pkg.components, '.');
    }
}