package fr.ifremer.sensornanny.sync.advice;

import java.lang.reflect.Method;
import java.util.logging.Logger;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.StringUtils;

/**
 * Advice permettant de logger les différents temps de traitements entrées et sorties de méthodes
 * 
 * @author athorel
 *
 */
public class LogAdvice implements MethodInterceptor {

    /**
     * 
     */
    private static final Logger LOGGER = Logger.getLogger(LogAdvice.class.getName());

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        String threadName = Thread.currentThread().getName();
        Method method = invocation.getMethod();
        String methodName = method.getName();
        String args = StringUtils.join(invocation.getArguments(), ",");
        String className = method.getDeclaringClass().getSimpleName();
        long timeBeforeCall = System.currentTimeMillis();
        Object result = invocation.proceed();
        System.currentTimeMillis();
        long timeTook = System.currentTimeMillis() - timeBeforeCall;
        LOGGER.info(String.format("[%s] Call %s#%s(%s) - Time : %d ms", threadName, className, methodName, args,
                timeTook));
        return result;
    }

}
