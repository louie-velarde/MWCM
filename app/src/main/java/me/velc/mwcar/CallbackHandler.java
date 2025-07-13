package me.velc.mwcar;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

abstract class CallbackHandler implements InvocationHandler {

	private static final Method hashCodeMethod;
	private static final Method equalsMethod;
	private static final Method toStringMethod;

	static {
		try {
			hashCodeMethod = Object.class.getMethod("hashCode");
			equalsMethod = Object.class.getMethod("equals", Object.class);
			toStringMethod = Object.class.getMethod("toString");
		} catch (NoSuchMethodException e) {
			throw (Error) (new NoSuchMethodError().initCause(e));
		}
	}

	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Class<?> declaringClass = method.getDeclaringClass();

		if (declaringClass == Object.class) {
			if (method.equals(hashCodeMethod)) {
				return proxyHashCode(proxy);
			} else if (method.equals(equalsMethod)) {
				return proxyEquals(proxy, args[0]);
			} else if (method.equals(toStringMethod)) {
				return proxyToString(proxy);
			} else {
				throw new InternalError("Unexpected Object method dispatched: " + method);
			}
		}
		return handleCallback(proxy, method, args);
	}

	protected abstract Object handleCallback(Object proxy, Method method, Object[] args)
			throws Throwable;

	protected Integer proxyHashCode(Object proxy) {
		return System.identityHashCode(proxy);
	}

	protected Boolean proxyEquals(Object proxy, Object other) {
		return proxy == other;
	}

	protected String proxyToString(Object proxy) {
		return proxy.getClass().getName() + '@' + Integer.toHexString(proxy.hashCode());
	}
}