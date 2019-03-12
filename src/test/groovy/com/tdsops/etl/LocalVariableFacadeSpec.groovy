package com.tdsops.etl

import spock.lang.Specification
import spock.lang.Unroll

class LocalVariableFacadeSpec extends Specification {

	@Unroll
	void 'test can override asBoolean for wrappedObject #wrappedObject'() {

		setup:
			LocalVariableFacade localVariableFacade = new LocalVariableFacade(wrappedObject, Mock(ETLProcessor))

		expect:
			!!localVariableFacade == booleanResult

		where:
			wrappedObject      || booleanResult
			true               || true
			new Boolean(false) || false
			'FOOBAR'           || true
			''                 || false
			1000               || true
			0                  || false
	}

	@Unroll
	void 'test can forward methods without arguments for wrappedObject #wrappedObject'() {

		setup:
			LocalVariableFacade localVariableFacade = new LocalVariableFacade(wrappedObject, Mock(ETLProcessor))

		expect:
			localVariableFacade."${method}"() == result

		where:
			wrappedObject      | method        || result
			true               | 'asBoolean'   || true
			new Boolean(false) | 'asBoolean'   || false
			'FOOBAR'           | 'toLowerCase' || 'foobar'
			''                 | 'size'        || 0
			1000               | 'toFloat'     || 1000.0
			0                  | 'toFloat'     || 0.0
	}

	@Unroll
	void 'test can forward methods with arguments for wrappedObject #wrappedObject'() {

		setup:
			LocalVariableFacade localVariableFacade = new LocalVariableFacade(wrappedObject, Mock(ETLProcessor))

		expect:
			localVariableFacade."${method}"(argument) == result

		where:
			wrappedObject | method       | argument   || result
			true          | 'compareTo'  | true       || 0
			'FOOBAR'      | 'replaceAll' | ['O', 'A'] || 'FAABAR'
			''            | 'startsWith' | 'FOO'      || false
	}

	@Unroll
	void 'test can forward access to properties for wrappedObject #wrappedObject'() {

		setup:
			LocalVariableFacade localVariableFacade = new LocalVariableFacade(wrappedObject, Mock(ETLProcessor))

		expect:
			localVariableFacade."${property}" == result

		where:
			wrappedObject | property || result
			'FOOBAR'      | 'number' || false
	}

	@Unroll
	void 'test can forward equals method for wrappedObject #wrappedObject'() {

		setup:
			LocalVariableFacade localVariableFacade = new LocalVariableFacade(wrappedObject, Mock(ETLProcessor))

		expect:
			(localVariableFacade.equals(otherObject)) == result

		where:
			wrappedObject      | otherObject || result
			true               | true        || true
			new Boolean(false) | true        || false
			'FOOBAR'           | 'FOOBAR'    || true
			'FOOBAR'           | ''          || false
			'FOOBAR'           | null        || false
			1000               | 1000        || true
			1000               | 10          || false
			null               | 10          || false
			1000               | null        || false
			null               | null        || true
	}
}
