package fr.logica.jsf.components.autocomplete;

import javax.faces.view.facelets.ComponentConfig;
import javax.faces.view.facelets.ComponentHandler;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.MetaRule;
import javax.faces.view.facelets.MetaRuleset;
import javax.faces.view.facelets.Metadata;
import javax.faces.view.facelets.MetadataTarget;
import javax.faces.view.facelets.TagAttribute;


public class AutocompleteHandler extends ComponentHandler {

    public AutocompleteHandler(ComponentConfig config) {
        super(config);
    }

    @Override
    protected MetaRuleset createMetaRuleset(@SuppressWarnings("rawtypes") Class type) {
        MetaRuleset metaRuleset = super.createMetaRuleset(type);
        metaRuleset.addRule(AUTOCOMPLETE_METHOD_META_RULE);
        return metaRuleset;
    }

    private static final MetaRule AUTOCOMPLETE_METHOD_META_RULE = new MetaRule() {

        @Override
        public Metadata applyRule(String name, TagAttribute attribute, MetadataTarget meta) {

            if (meta.isTargetInstanceOf(UIAutocomplete.class)) {

                if ("autocompleteMethod".equals(name)) {
                    return new MethodMetadata(attribute) {

                        public void applyMetadata(FaceletContext ctx, Object instance) {
                            ((UIAutocomplete) instance).setAutocompleteMethod(getMethodExpression(ctx));
                        }
                    };
                }
            }
            return null;
        }
    };

}
