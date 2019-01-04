package com.tdssrc.grails

import com.tdsops.tm.enums.domain.ApiActionHttpMethod
import spock.lang.Specification

class EnumUtilSpec extends Specification {

	void 'Test is valid enum with provided test table'() {
		expect:
			result == EnumUtil.isValidEnum(clazz, enumName)
		where:
			clazz 						| enumName 		| result
			ApiActionHttpMethod.class 	| 'GET' 		| true
			ApiActionHttpMethod.class 	| 'POST' 		| true
			ApiActionHttpMethod.class 	| 'PUT' 		| true
			ApiActionHttpMethod.class 	| 'PATCH' 		| true
			ApiActionHttpMethod.class 	| 'DELETE' 		| true
			ApiActionHttpMethod.class 	| 'OPTIONS' 	| true
			ApiActionHttpMethod.class 	| 'HEAD' 		| true
			ApiActionHttpMethod.class 	| '' 			| false
			ApiActionHttpMethod.class 	| null 			| false
	}

}
