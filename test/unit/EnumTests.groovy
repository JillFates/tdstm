/**
 * Unit test cases for testing all of the Enum classes
 */

import grails.test.GrailsUnitTestCase

import com.tds.asset.AssetEntityType
import com.tdsops.tm.enums.domain.*

class EnumTests extends GrailsUnitTestCase {

	/** 
	 * closure used to perform a number of standard tests against an enum
	 * @param e 	the Enum under test
	 * @param obj 	One of the Enum elements
	 * @param name	The expected name of the object
	 * @param value	The expected value of the object
	 * @param firstKey	Then name of the expected first element in the Enum.keys()
	 * @param firstLabel	Then name of the expected first element in the Enum.labels()
	 */
	def enumTest = { e, obj, name, value, firstKey, firstLabel ->
		def label = obj.getClass().name

		def keys = e.getKeys()
		def labels = e.getLabels()

		assertEquals "$label name", name, obj.name()
		assertEquals "$label toString", name, obj.toString()
		assertEquals "$label value", value, obj.value
		assertEquals "$label value()", value, obj.value()
		assertTrue	'$label getKeys() is a List', (keys instanceof List)
		assertTrue	'$label getLabels() is a List', (labels instanceof List)
		assertEquals "$label getKeys()[0]", obj.asEnum(firstKey), keys[0]
		assertEquals "$label getLabels()[0]", firstLabel, labels[0]

		assertTrue "$label asEnum() positive", (e.asEnum(firstKey) != null)
		assertTrue "$label asEnum() negative", (e.asEnum('XYZZy123') == null)

	}

	void testAssetEntityType() {
		enumTest AssetEntityType, AssetEntityType.STORAGE, 'STORAGE', 'S', 'APPLICATION', 'A'
	}

	void testSizeScale() {
		enumTest SizeScale, SizeScale.GB, 'GB', 'Gigabyte', 'KB', 'Kilobyte'
	}

	void testSpeedScale() {
		enumTest SpeedScale, SpeedScale.MBps, 'MBps', 'MegaByte/sec', 'Kbps', 'Kilobit/sec'
	}

	void testTaskDependencyType() {
		enumTest TaskDependencyType, TaskDependencyType.FS, 'FS', 'Finish-Start', 'FR', 'Finish-Ready'
	}

	void testTimeConstraintType() {
		enumTest TimeConstraintType, TimeConstraintType.ASAP, 'ASAP', 'As Soon As Possible', 'ALAP', 'As Late As Possible'
	}

	void testTimeScale() {
		enumTest TimeScale, TimeScale.W, 'W', 'Weeks', 'M', 'Minutes'
	}

	void testSecurityRole() {
		enumTest SecurityRole, SecurityRole.ADMIN, 'ADMIN', 'Administrator', 'USER', 'User'
	}

	void testContextType() {
		enumTest ContextType, ContextType.A, 'A', 'Application', 'E', 'Event'
	}

}
