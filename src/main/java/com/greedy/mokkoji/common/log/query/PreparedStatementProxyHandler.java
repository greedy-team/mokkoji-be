package com.greedy.mokkoji.common.log.query;

import lombok.RequiredArgsConstructor;
import org.springframework.web.context.request.RequestContextHolder;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.util.Objects;
import java.util.Set;

@RequiredArgsConstructor
public class PreparedStatementProxyHandler implements InvocationHandler {
    private static final Set<String> EXECUTE_METHODS = Set.of("execute", "executeQuery", "executeUpdate");

    private final PreparedStatement preparedStatement;
    private final QueryCounter queryCounter;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        countQuery(method);
        return method.invoke(preparedStatement, args);
    }

    private void countQuery(Method method) {
        if (isExecuteMethod(method) && isRequest()) {
            queryCounter.increaseCount();
        }
    }

    private boolean isExecuteMethod(Method method) {
        return EXECUTE_METHODS.contains(method.getName());
    }

    private boolean isRequest() {
        return Objects.nonNull(RequestContextHolder.getRequestAttributes());
    }
}
