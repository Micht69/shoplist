package fr.logica.jsf.components.autocomplete;

import java.util.List;

import javax.el.MethodExpression;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.Metadata;
import javax.faces.view.facelets.TagAttribute;

public abstract class MethodMetadata extends Metadata {

    private final TagAttribute attribute;

    public MethodMetadata(TagAttribute attribute) {
        this.attribute = attribute;
    }

    protected MethodExpression getMethodExpression(FaceletContext ctx) {
        String[] params = getArguments();
        Class<?>[] paramTypes = new Class<?>[params.length + 1];

        for (int i = 0; i <= params.length; i++) {
            paramTypes[i] = String.class;
        }
        MethodExpression expression = ctx.getExpressionFactory().createMethodExpression(ctx, getMethodExpression(), List.class, paramTypes);
        return new AutocompleteMethodExpression(expression, params);
    }

    private String getMethodExpression() {
        String expr = attribute.getValue();
        int bracketIndex = expr.indexOf('(');

        if (bracketIndex > -1) {
            return expr.substring(0, bracketIndex) + "}";
        }
        return expr;
    }

    private String[] getArguments() {
        String expr = attribute.getValue();
        int bracketIndex = expr.indexOf('(');

        if (bracketIndex > -1) {
            expr = expr.substring(bracketIndex + 1, expr.indexOf(')'));
            String[] params = expr.split(",");

            for (int i = 0; i < params.length; i++) {
                params[i] = params[i].replaceAll("'", "").trim(); 

                if ("null".equals(params[i])) {
                	params[i] = null;
                }
            }
            return params;
        }
        return new String[0];
    }

}
