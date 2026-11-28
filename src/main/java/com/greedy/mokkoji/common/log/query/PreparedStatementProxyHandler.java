package com.greedy.mokkoji.common.log.query;

import lombok.RequiredArgsConstructor;
import org.springframework.web.context.request.RequestContextHolder;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.util.Objects;
import java.util.Set;

@RequiredArgsConstructor
public class PreparedStatementProxyHandler implements InvocationHandler {

    private static final Set<String> EXECUTE_METHODS = Set.of(
            "execute",
            "executeQuery",
            "executeUpdate",
            "executeLargeUpdate",
            "executeBatch",
            "executeLargeBatch"
    );

    private final PreparedStatement preparedStatement;
    private final QueryCounter queryCounter;

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        if (isExecuteMethod(method) && isRequest()) {
            queryCounter.increaseCount();
            final long startedAtNanos = System.nanoTime();
            try {
                final Object result = invokeTarget(method, args);
                queryCounter.addSuccessQueryTime(System.nanoTime() - startedAtNanos);
                return result;
            } catch (Throwable exception) {
                queryCounter.addFailedQueryTime(System.nanoTime() - startedAtNanos);
                throw exception;
            }
        }
        return invokeTarget(method, args);
    }

    private Object invokeTarget(final Method method, final Object[] args) throws Throwable {
        try {
            return method.invoke(preparedStatement, args);
        } catch (InvocationTargetException exception) {
            // JdbcTemplate이 중복 키 등의 DB 오류를 변환할 수 있도록 원본 JDBC 예외를 전달한다.
            throw exception.getCause();
        }
    }

    private boolean isExecuteMethod(final Method method) {
        return EXECUTE_METHODS.contains(method.getName());
    }

    private boolean isRequest() {
        return Objects.nonNull(RequestContextHolder.getRequestAttributes());
    }
}
