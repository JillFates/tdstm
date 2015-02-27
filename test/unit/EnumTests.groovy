/**
 * Unit test cases for testing all of the Enum classes
 */

import com.tds.asset.AssetEntityType
import com.tdsops.tm.enums.domain.*
import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * Unit test cases for the Enum class
 */
class EnumTests extends Specification {
    
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
		def valid = true

		valid = valid && name.equals(obj.name())
		valid = valid && name.equals(obj.toString())
		valid = valid && value.equals(obj.value)
		valid = valid && value.equals(obj.value())
		valid = valid && (keys instanceof List)
		valid = valid && (labels instanceof List)
		valid = valid && obj.asEnum(firstKey).equals(keys[0])
		valid = valid && firstLabel.equals(labels[0])
		valid = valid && (e.asEnum(firstKey) != null)
		valid = valid && (e.asEnum('XYZZy123') == null)

		return valid
	}

	void testAssetEntityType() {
		expect:
			enumTest AssetEntityType, AssetEntityType.STORAGE, 'STORAGE', 'S', 'APPLICATION', 'A'
	}

	void testSizeScale() {
		expect:		
			enumTest SizeScale, SizeScale.GB, 'GB', 'Gigabyte', 'KB', 'Kilobyte'
	}

	void testSpeedScale() {
		expect:		
			enumTest SpeedScale, SpeedScale.MBps, 'MBps', 'MegaByte/sec', 'Kbps', 'Kilobit/sec'
	}

	void testTaskDependencyType() {
		expect:		
			enumTest TaskDependencyType, TaskDependencyType.FS, 'FS', 'Finish-Start', 'FR', 'Finish-Ready'
	}

	void testTimeConstraintType() {
		expect:		
			enumTest TimeConstraintType, TimeConstraintType.ASAP, 'ASAP', 'As Soon As Possible', 'ALAP', 'As Late As Possible'
	}

	void testTimeScale() {
		expect:
			enumTest TimeScale, TimeScale.W, 'W', 'Weeks', 'M', 'Minutes'
	}

	void testSecurityRole() {
		expect:
			enumTest SecurityRole, SecurityRole.ADMIN, 'ADMIN', 'Administrator', 'USER', 'User'
	}

	void testContextType() {
		expect:
			enumTest ContextType, ContextType.A, 'A', 'Application', 'E', 'Event'
	}

}
