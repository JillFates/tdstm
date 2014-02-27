package com.tds.asset

import com.tdssrc.grails.GormUtil

public enum AssetType {
	SERVER('Server'),
	VM('VM'),
	APPLICATION('Application'),
	DATABASE('Database'),
	FILES('Files'),
	STORAGE('Storage'),
	NETWORK('Network'),
	BLADE('Blade'),
	BLADE_CHASSIS('Blade Chassis'),
	APPLIANCE('Appliance')

	String name
	AssetType(String name) {
		this.name = name
	}
	
	String toString() { name }

	/**
	 * The list of types that represent ONLY virtual servers 
	 * @return List<String>
	 */
	static List getVirtualServerTypes() {
		return ['VM','Virtual']
	}

	/**
	 * The list of types represent VIRTUAL servers as a quote comma delimited string
	 * @return String quote comma delimited list of the types
	 */
	static String getVirtualServerTypesAsString() {
		GormUtil.asQuoteCommaDelimitedString(getVirtualServerTypes())
	}

	/**
	 * The list of types that represent ONLY physical servers 
	 * @return List<String>
	 */
	static List getPhysicalServerTypes() {
		return ['Server', 'Appliance', 'Blade']
	}

	/**
	 * The list of types represent PHYSICAL servers as a quote comma delimited string
	 * @return String quote comma delimited list of the types
	 */
	static String getPhysicalServerTypesAsString() {
		GormUtil.asQuoteCommaDelimitedString(getPhysicalServerTypes())
	}

	/**
	 * The list of types that represent blade chassis
	 * @return List<String>
	 */
	static List getBladeChassisTypes() {
		return ['Blade Chassis', 'Chassis']
	}

	/**
	 * The list of types represent Blade Chassis as a quote comma delimited string
	 * @return String quote comma delimited list of the types
	 */
	static String getBladeChassisAsString() {
		GormUtil.asQuoteCommaDelimitedString(getBladeChassisTypes())
	}

	/**
	 * The list of types that represent servers both physical and virtual
	 * @return List<String>
	 */
	static List getAllServerTypes() {
		return AssetType.getPhysicalServerTypes() + AssetType.getVirtualServerTypes()
	}

	/**
	 * The list of types represent ALL Server types as a quote comma delimited string
	 * @return String quote comma delimited list of the types
	 */
	static String getAllServerTypesAsString() {
		GormUtil.asQuoteCommaDelimitedString(getAllServerTypes())
	}

	/**
	 * The list of types that represent storage
	 * @return List<String>
	 */
	static List getStorageTypes() {
		return ['Files', 'Storage']
	}

	/**
	 * The list of types represent storage as a quote comma delimited string
	 * @return String quote comma delimited list of the types
	 */
	static String getStorageTypesAsString() {
		GormUtil.asQuoteCommaDelimitedString(getStorageTypes())
	}
	
	/**
	 * The list of types represent all non Server Types.
	 * @return
	 */
	static List getNonPhysicalTypes() {
		return ['Application','Files','Database','VM']
	}
	/**
	 * The list of types represent all Server Types which differs the physical list and ServerList.
	 * @return
	 */
	static List getServerTypes() {
		return ['Server', 'Appliance', 'Blade','VM']
	}

}