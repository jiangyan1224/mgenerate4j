package uk.dioxic.mgenerate.core.resolver;

import uk.dioxic.mgenerate.common.Resolvable;
import uk.dioxic.mgenerate.core.TemplateStateCache;
import uk.dioxic.mgenerate.core.codec.TemplateCodec;

public class DocumentKeyResolver implements Resolvable {
    private final String documentKey;

    DocumentKeyResolver(String documentKey) {
        this.documentKey = documentKey;
        TemplateCodec.enableStateCaching();
    }

    @Override
    public Object resolve() {
        return TemplateStateCache.get(documentKey);
    }

}