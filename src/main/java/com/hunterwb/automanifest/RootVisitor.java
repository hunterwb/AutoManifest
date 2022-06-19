package com.hunterwb.automanifest;

import javax.lang.model.element.Element;
import javax.lang.model.util.SimpleElementVisitor6;
import java.util.Set;

@SuppressWarnings("deprecation")
abstract class RootVisitor extends SimpleElementVisitor6<Void, Void> {

    abstract String getValue();

    final void visitRootElements(Set<? extends Element> rootElements) {
        for (Element rootElement : rootElements) {
            rootElement.accept(this, null);
        }
    }

    @Override public Void visitUnknown(Element e, Void unused) {
        return defaultAction(e, unused);
    }
}
