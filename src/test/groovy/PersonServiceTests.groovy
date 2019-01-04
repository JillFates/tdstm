import grails.test.mixin.TestFor
import net.transitionmanager.service.PersonService
import spock.lang.Specification

@TestFor(PersonService)
class PersonServiceTests extends Specification {

	private int count = 0

	void testParseName() {
		expect:
		validName('John', 'John')
		validName('John Martin', 'John', '', 'Martin')
		validName('John Van Zant', 'John', '', 'Van Zant')
		validName('Martin, John', 'John', '', 'Martin')
		validName('John P. Martin', 'John', 'P.', 'Martin')
		validName('John P. Martin Sr', 'John', 'P.', 'Martin', 'Sr')
		validName('John P. Van Zant Sr', 'John', 'P.', 'Van Zant', 'Sr')
		validName('John P. Martin, Sr', 'John', 'P.', 'Martin', 'Sr')
		validName('John P. T. Martin, Sr', 'John', 'P. T.', 'Martin', 'Sr')
		validName('John P. T. Martin Sr', 'John', 'P. T.', 'Martin', 'Sr')
		validName('Martin, John P.', 'John', 'P.', 'Martin')
		validName('Martin, John P. T.', 'John', 'P. T.', 'Martin')
		validName('Van Zant, John P. T.', 'John', 'P. T.', 'Van Zant')
	}

	void testLastNameWithSuffix() {
		expect:
		"Martin, Sr." == service.lastNameWithSuffix(last: 'Martin', suffix: 'Sr.')
		"Martin" == service.lastNameWithSuffix(last: 'Martin', suffix: '')
		"Martin" == service.lastNameWithSuffix(last: 'Martin')
		"" == service.lastNameWithSuffix(last: '')
		"" == service.lastNameWithSuffix([:])
	}

	private boolean validName(String name, String first, String middle = '', String last = '', String suffix = '') {
		count++

		Map map = service.parseName(name)

		assert map.first == first
		assert map.last == last
		assert map.middle == middle
		assert map.suffix == suffix

		true
	}
}
