package com.tdsops.etl

enum ETLDomain {

	Application, Device, Database, Storage, External, Task, Person, Comment


	/**
	 * Lookup for ETLDomain from a String.
	 * <code>
	 *     assert ETLDomain.lookup('Application') ==  ETLDomain.Application
	 *     assert ETLDomain.lookup('APPLICATION') ==  ETLDomain.Application
	 *     assert ETLDomain.lookup('FOO') ==  null
	 *     assert ETLDomain.lookup(null) ==  null
	 * </code>
	 * @param code
	 * @return
	 */
	static ETLDomain lookup(String code){
		for (ETLDomain value: values()){
			if (value.name().equalsIgnoreCase(code)) {
				return value
			}
		}
		return null
	}
}