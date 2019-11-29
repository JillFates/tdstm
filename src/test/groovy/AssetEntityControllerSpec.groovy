import com.tdsops.common.ui.Pagination
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import net.transitionmanager.asset.AssetEntityController
import net.transitionmanager.person.UserPreferenceService
import net.transitionmanager.project.Project
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll
/**
 * Unit tests for the CommonController
 */
class AssetEntityControllerSpec extends Specification implements ControllerUnitTest<AssetEntityController>, DataTest {


	@Shared
	UserPreferenceService userPreferenceService


	static Project project


	/**
	 * Used to create a test project and user that is logged in
	 */
	def setup() {

	}

	@Unroll
	def 'Test PaginationMethods.paginationMaxRowValue without user preferences'() {
		when: 'setting the max param to a value'
			controller.params.max = maxValue
		then: 'the PaginationMethod.paginationMaxRowValue method should return the expect result'
			result == controller.paginationMaxRowValue('max')

		where:
			maxValue                              | result
			'42'                                  | Pagination.MAX_DEFAULT        // a number is not amoung the acceptable values
			'text'                                | Pagination.MAX_DEFAULT        // a non-numeric value is provided
			Pagination.MAX_DEFAULT.toString()     | Pagination.MAX_DEFAULT        // a valid value is provided
			Pagination.MAX_OPTIONS[-1].toString() | Pagination.MAX_OPTIONS[-1]    // the last value in the Pagination.MAX_OPTIONS is passed
			null                                  | Pagination.MAX_DEFAULT        // a NULL value
			''                                    | Pagination.MAX_DEFAULT        // a blank value
	}

	def 'Test the PaginationMethods.paginationPage method'() {
		when: 'page parameter does not exist'
		then: 'the page should be 1'
			1 == controller.paginationPage()

		when: 'page param value is null'
			controller.params.page = null
		then: 'the page should be 1'
			1 == controller.paginationPage()

		when: 'page param value is non-numeric'
			controller.params.page = 'fubar'
		then: 'the page should be 1'
			1 == controller.paginationPage()

		when: 'page param value is is negative number'
			controller.params.page = '-42'
		then: 'the page should be 1'
			1 == controller.paginationPage()

		when: 'page param value is a positive number'
			controller.params.page = '42'
		then: 'the page should match the parameter'
			42 == controller.paginationPage()

		when: 'page param value is a positive number and using a different name'
			controller.params.bestQB = '12'  // Tom Brady!	
		then: 'the page should match the parameter'
			12 == controller.paginationPage('bestQB')
	}

	def 'Kick the tires on the PaginationMethods.paginationRowOffset method'() {
		expect:
			result == controller.paginationRowOffset(page, rows)

		where:
			page | rows | result
			1    | 100  | 0
			0    | 100  | 0
			2    | 100  | 100
			null | 100  | 0
			3    | 50   | 100

	}
}
