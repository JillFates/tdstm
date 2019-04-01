import net.transitionmanager.asset.AssetEntityType
import com.tdsops.tm.enums.domain.SecurityRole
import com.tdsops.tm.enums.domain.SizeScale
import com.tdsops.tm.enums.domain.SpeedScale
import com.tdsops.tm.enums.domain.TaskDependencyType
import com.tdsops.tm.enums.domain.TimeConstraintType
import com.tdsops.tm.enums.domain.TimeScale
import spock.lang.Specification

class EnumTests extends Specification {

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
		enumTest SecurityRole, SecurityRole.ROLE_ADMIN, 'ROLE_ADMIN', 'Administrator', 'ROLE_USER', 'User'
	}

	/**
	 * Perform standard tests against an enum
	 * @param enumClass the Enum type under test
	 * @param e One of the Enum elements
	 * @param name The expected name of the object
	 * @param value The expected value of the object
	 * @param firstKey Then name of the expected first element in the Enum.keys()
	 * @param firstLabel Then name of the expected first element in the Enum.labels()
	 */
	private <E extends Enum<E>> boolean enumTest(Class<E> enumClass, E e, String name, String value,
	                                             String firstKey, String firstLabel) {
		/* List<E> */ def keys = enumClass.getKeys()
		/* List<String> */ def labels = enumClass.getLabels()

		assert name == e.name()
		assert name == e.toString()
		assert value == e.value
		assert value == e.value()
		assert keys instanceof List
		assert labels instanceof List
		assert e.asEnum(firstKey) == keys[0]
		assert firstLabel == labels[0]
		assert enumClass.asEnum(firstKey)
		assert !enumClass.asEnum('XYZZy123')

		true
	}
}
