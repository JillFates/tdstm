import grails.testing.services.ServiceUnitTest
import net.transitionmanager.person.PersonService
import spock.lang.Specification
import spock.lang.Unroll

class PersonServiceTests extends Specification implements ServiceUnitTest<PersonService> {

	@Unroll
	void "testParseName #name parses to first: #first, midle: #middle, last: #last, and suffix: #suffix"() {
		expect:
			Map map = service.parseName(name)

			map.first == first
			map.middle == middle
			map.last == last
			map.suffix == suffix
		where:
			name                    || first  | middle  | last       | suffix
			'John'                  || 'John' | ''      | ''         | ''
			'John Martin'           || 'John' | ''      | 'Martin'   | ''
			'John Van Zant'         || 'John' | ''      | 'Van Zant' | ''
			'Martin, John'          || 'John' | ''      | 'Martin'   | ''
			'John P. Martin'        || 'John' | 'P.'    | 'Martin'   | ''
			'John P. Martin Sr'     || 'John' | 'P.'    | 'Martin'   | 'Sr'
			'John P. Van Zant Sr'   || 'John' | 'P.'    | 'Van Zant' | 'Sr'
			'John P. Martin, Sr'    || 'John' | 'P.'    | 'Martin'   | 'Sr'
			'John P. T. Martin, Sr' || 'John' | 'P. T.' | 'Martin'   | 'Sr'
			'John P. T. Martin Sr'  || 'John' | 'P. T.' | 'Martin'   | 'Sr'
			'Martin, John P.'       || 'John' | 'P.'    | 'Martin'   | ''
			'Martin, John P. T.'    || 'John' | 'P. T.' | 'Martin'   | ''
			'Van Zant, John P. T.'  || 'John' | 'P. T.' | 'Van Zant' | ''
	}

	void testLastNameWithSuffix() {
		expect:
			"Martin, Sr." == service.lastNameWithSuffix(last: 'Martin', suffix: 'Sr.')
			"Martin" == service.lastNameWithSuffix(last: 'Martin', suffix: '')
			"Martin" == service.lastNameWithSuffix(last: 'Martin')
			"" == service.lastNameWithSuffix(last: '')
			"" == service.lastNameWithSuffix([:])
	}
}
