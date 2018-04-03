package com.tdssrc.grails

/**
 * ThreadLocal utility class, it helps to manage the thread local variable lifecycle individually.
 * The destroy() method can be invoked where you can safely remove all thread locals in the application.
 *
 * NOTE: Use with careful, bad usage of this class might cause memory leaks.
 *
 * @see <pre>https://www.javacodegeeks.com/2012/05/threading-stories-threadlocal-in-web.html</pre>
 */
class ThreadLocalUtil {

	private final static ThreadLocal<ThreadVariables> THREAD_VARIABLES = new ThreadLocal<ThreadVariables>() {
		/**
		 * @see java.lang.ThreadLocal#initialValue()
		 */
		@Override
		protected ThreadVariables initialValue() {
			return new ThreadVariables()
		}
	}

	/**
	 * Gets variable from ThreadLocal
	 * @param name
	 * @return
	 */
	static Object getThreadVariable(ThreadLocalVariable name) {
		return THREAD_VARIABLES.get().get(name)
	}

	/**
	 * Gets a variable from ThreadLocal or the default provided initial value
	 * @param name
	 * @param initialValue
	 * @return
	 */
	static Object getThreadVariable(ThreadLocalVariable name, InitialValue initialValue) {
		Object o = THREAD_VARIABLES.get().get(name)
		if (o == null) {
			THREAD_VARIABLES.get().put(name, initialValue.create())
			return getThreadVariable(name)
		} else {
			return o
		}
	}

	/**
	 * Sets a variable into ThreadLocal
	 * @param name
	 * @param value
	 */
	static void setThreadVariable(ThreadLocalVariable name, Object value) {
		THREAD_VARIABLES.get().put(name, value);
	}

	/**
	 * Destroy all ThreadLocal variables
	 */
	static void destroy() {
		THREAD_VARIABLES.remove()
	}

	/**
	 * Destroy only specified ThreadLocal variables
	 * @param variables
	 */
	static void destroy(ThreadLocalVariable... variables) {
		for (ThreadLocalVariable threadLocalVariable : variables) {
			THREAD_VARIABLES.get().remove(threadLocalVariable)
		}
	}

}

/**
 * Map used to store ThreadLocal variables values
 */
class ThreadVariables extends HashMap<ThreadLocalVariable, Object> {}

/**
 * Provides a ThreadLocal initial value implementation
 * @see java.lang.ThreadLocal#initialValue()
 */
abstract class InitialValue {
	abstract Object create()
}

/**
 * Supported and encapsulated ThreadLocal variables
 */
interface ThreadLocalVariable {

}
