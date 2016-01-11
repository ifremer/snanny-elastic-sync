package fr.ifremer.sensornanny.sync.advice;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import com.google.inject.matcher.AbstractMatcher;

public class LogAdviceSimpleMatcher extends AbstractMatcher<Method> implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Override
    public boolean matches(Method t) {
        if (t.isSynthetic()) {
            return false;
        }
        int modifiers = t.getModifiers();
        if (Modifier.isStatic(modifiers)) {
            return false;
        }
        if (Modifier.isPrivate(modifiers) || Modifier.isProtected(modifiers)) {
            return false;
        }
        if (Runnable.class.equals(t.getReturnType())) {
            return false;
        }

        return true;
    }

}
