package fr.logica.jsf.components.autocomplete;

import javax.el.ELContext;
import javax.el.ELException;
import javax.el.MethodExpression;
import javax.el.MethodInfo;
import javax.el.MethodNotFoundException;
import javax.el.PropertyNotFoundException;

public class AutocompleteMethodExpression extends MethodExpression {

    private static final long serialVersionUID = 1L;

    private final MethodExpression delegate;
    private final String[] params;
    
    public AutocompleteMethodExpression(MethodExpression delegate, String... params) {
        super();
        this.delegate = delegate;
        this.params = params;
    }

    @Override
    public MethodInfo getMethodInfo(ELContext ctx) throws NullPointerException, PropertyNotFoundException, MethodNotFoundException,
            ELException {
        return delegate.getMethodInfo(ctx);
    }

    @Override
    public Object invoke(ELContext ctx, Object[] params) throws NullPointerException, PropertyNotFoundException, MethodNotFoundException,
            ELException {

        String[] allParams = new String[this.params.length + params.length];
        System.arraycopy(this.params, 0, allParams, 0, this.params.length);
        System.arraycopy(params, 0, allParams, this.params.length, params.length);
        return delegate.invoke(ctx, allParams);
    }

    @Override
    public boolean equals(Object other) {
        return delegate.equals(other);
    }

    @Override
    public String getExpressionString() {
        return delegate.getExpressionString();
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public boolean isLiteralText() {
        return delegate.isLiteralText();
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

}
