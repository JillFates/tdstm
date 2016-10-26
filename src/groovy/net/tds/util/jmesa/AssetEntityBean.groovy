package net.tds.util.jmesa

import groovy.transform.CompileStatic

@CompileStatic
class AssetEntityBean implements Serializable {
	private static final long serialVersionUID = 1

	long id
	String assetTag
	String assetType
	String assetName
	String commentType
	Integer priority
	String sourceTeamMt
	String targetTeamMt
	String status
	String cssClass
	boolean checkVal

	String application
	String appOwner
	String appSme
	String moveBundle
	String planStatus

	String dbFormat

	String fileFormat
	Integer size

	Integer depUp 		// dependencies support count
	Integer depDown 	// dependencies down count
	Integer dependencyBundleNumber

	String model
	String sourceLocation
	String sourceRack
	String targetLocation
	String targetRack
	String serialNumber
	String validation

	String name
	String description
	String aka
	Integer modelCount
	Integer count

	String userName
	String person
	Date lastLogin
	String company
	Date dateCreated
	Date expiryDate
	String role
}
