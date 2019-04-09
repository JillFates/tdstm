package net.transitionmanager.asset

import com.tdssrc.grails.GormUtil
import groovy.transform.CompileStatic

@CompileStatic
enum AssetType {

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

	final String name

	private AssetType(String name) {
		this.name = name
	}

	String toString() { name }

	/**
	 * ONLY virtual servers
	 */
	static final List<String> virtualServerTypes = ['VM', 'Virtual'].asImmutable()

	/**
	 * VIRTUAL servers as a quote comma delimited string
	 */
	static final String virtualServerTypesAsString = GormUtil.asQuoteCommaDelimitedString(virtualServerTypes)

	/**
	 * ONLY physical servers
	 */
	static final List<String> physicalServerTypes = ['Server', 'Appliance', 'Blade'].asImmutable()

	/**
	 * PHYSICAL servers as a quote comma delimited string
	 */
	static final String physicalServerTypesAsString = GormUtil.asQuoteCommaDelimitedString(physicalServerTypes)

	/**
	 * Blade chassis
	 */
	static final List<String> bladeChassisTypes = ['Blade Chassis', 'Chassis'].asImmutable()

	/**
	 * Blade Chassis as a quote comma delimited string
	 */
	static final String bladeChassisAsString = GormUtil.asQuoteCommaDelimitedString(bladeChassisTypes)

	/**
	 * Servers both physical and virtual
	 */
	static final List<String> allServerTypes = (physicalServerTypes + virtualServerTypes).asImmutable()

	/**
	 * ALL Server types as a quote comma delimited string
	 */
	static final String allServerTypesAsString = GormUtil.asQuoteCommaDelimitedString(allServerTypes)

	/**
	 * Storage
	 */
	static final List<String> storageTypes = ['Array', 'Disk', 'NAS', 'SAN', 'SAN Switch', 'Storage',
	                                          'Tape', 'Tape Library', 'Virtual Tape Library'].asImmutable()

	/**
	 * Storage as a quote comma delimited string
	 */
	static final String storageTypesAsString = GormUtil.asQuoteCommaDelimitedString(storageTypes)

	/**
	 * Network devices
	 */
	static final List<String> networkDeviceTypes = ['Encoder', 'Load Balancer', 'Modem', 'Module', 'Multiplexer',
	                                                'Network', 'Probe', 'Receiver', 'Router', 'Switch', 'Telecom',
	                                                'Terminal Server', 'VPN'].asImmutable()

	/**
	 * Non-server Types.
	 */
	static final List<String> nonOtherTypes = (storageTypes + allServerTypes).asImmutable()

	/**
	 * Non-server Types as a quote comma delimited string
	 */
	static final String nonOtherTypesAsString = GormUtil.asQuoteCommaDelimitedString(nonOtherTypes)

	/**
	 * Non-Server Types.
	 */
	static final List<String> nonPhysicalTypes = (['Application', 'Database'] + virtualServerTypes +
			storageTypes + networkDeviceTypes).asImmutable()

	/**
	 * Server Types which differs the physical list and ServerList.
	 */
	static final List<String> serverTypes = (physicalServerTypes + virtualServerTypes).asImmutable()

	/**
	 * All non network types
	 */
	static final List<String> nonNetworkTypes = ['Server', 'Application', 'VM', 'Files', 'Database', 'Blade'].asImmutable()
}
