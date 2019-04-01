import grails.gorm.transactions.Rollback
import grails.test.mixin.integration.Integration
import net.transitionmanager.manufacturer.Manufacturer
import net.transitionmanager.manufacturer.ManufacturerAlias
import net.transitionmanager.service.ManufacturerService
import spock.lang.Specification

@Integration
@Rollback
class ManufacturerServiceTests extends Specification {

	ManufacturerService manufacturerService

	def '1. Test the isValidAlias method in many different situations' () {
		setup:
			def manCat = new Manufacturer(name: 'cat').save()
			def manDog = new Manufacturer(name: 'dog').save()
			def a1ias1 = new ManufacturerAlias(name:'feline', manufacturer:manCat).save()
			def a1ias2 = new ManufacturerAlias(name:'canine', manufacturer:manDog).save()

		expect:
			// assertions without the local duplicate flag

			// 'VALID: manufacturer alias is completely unique'
			manufacturerService.isValidAlias('kitty', manCat)
			// 'INVALID: manufacturer alias matches other manufacturer\'s name'
			! manufacturerService.isValidAlias('dog', manCat)
			// 'INVALID: manufacturer alias matches other manufacturer\'s alias'
			! manufacturerService.isValidAlias('canine', manCat)
			// 'INVALID: manufacturer alias matches the parent manufacturer\'s name'
			! manufacturerService.isValidAlias('cat', manCat)
			// 'INVALID: manufacturer alias matches the parent manufacturer\'s alias'
			! manufacturerService.isValidAlias('feline', manCat)

			// assertions with the local duplicate flag

			// 'VALID: manufacturer alias is completely unique (local duplicates allowed)'
			manufacturerService.isValidAlias('kitty', manCat, true)
			// 'INVALID: manufacturer alias matches other manufacturer\'s name (local duplicates allowed)'
			! manufacturerService.isValidAlias('dog', manCat, true)
			// 'INVALID: manufacturer alias matches other manufacturer\'s alias (local duplicates allowed)'
			! manufacturerService.isValidAlias('canine', manCat, true)
			// 'INVALID: manufacturer alias matches the parent manufacturer\'s name (local duplicates allowed)'
			! manufacturerService.isValidAlias('cat', manCat, true)
			// 'VALID: manufacturer alias matches the parent manufacturer\'s alias (local duplicates allowed)'
			manufacturerService.isValidAlias('feline', manCat, true)

			// test the manufacturer name parameter

			// 'VALID: manufacturer alias matches own name, using different name'
			manufacturerService.isValidAlias('cat', manCat, false, 'rat eater')

	}
}
