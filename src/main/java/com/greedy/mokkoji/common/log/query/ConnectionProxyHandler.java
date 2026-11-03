package com.greedy.mokkoji.common.log.query;

import lombok.RequiredArgsConstructor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;

@RequiredArgsConstructor
public class ConnectionProxyHandler implements InvocationHandler {

    private static final String QUERY_PREPARE_STATEMENT = "prepareStatement";

    private final Connection connection;
    private final QueryCounter queryCounter;

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        final Object proxyTarget;
        try {
            proxyTarget = method.invoke(connection, args);
        } catch (InvocationTargetException exception) {
            throw exception.getCause();
        }

        if (proxyTarget instanceof PreparedStatement && method.getName().equals(QUERY_PREPARE_STATEMENT)) {
            return Proxy.newProxyInstance(
                    proxyTarget.getClass().getClassLoader(),
                    proxyTarget.getClass().getInterfaces(),
                    new PreparedStatementProxyHandler((PreparedStatement) proxyTarget, queryCounter)
            );
        }

        return proxyTarget;
    }
}
