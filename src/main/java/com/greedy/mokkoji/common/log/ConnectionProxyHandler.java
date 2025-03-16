package com.greedy.mokkoji.common.log;

import lombok.RequiredArgsConstructor;

import java.lang.reflect.InvocationHandler;
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
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object proxyTarget = method.invoke(connection, args);

        if (proxyTarget instanceof PreparedStatement && method.getName().equals(QUERY_PREPARE_STATEMENT)) {
            return  Proxy.newProxyInstance(
                    proxyTarget.getClass().getClassLoader(),
                    proxyTarget.getClass().getInterfaces(),
                    new PreparedStatementProxyHandler((PreparedStatement) proxyTarget, queryCounter)
            );
        }

        return proxyTarget;
    }
}
