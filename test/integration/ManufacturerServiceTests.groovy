import net.transitionmanager.domain.Model
import net.transitionmanager.domain.Manufacturer
import net.transitionmanager.domain.ModelAlias
import net.transitionmanager.domain.ManufacturerAlias
import com.tdsops.metaclass.CustomMethods
import grails.test.mixin.TestFor
import net.transitionmanager.service.ManufacturerService
import spock.lang.Specification


@TestFor(ManufacturerService)
class ManufacturerServiceTests extends Specification {
	
	ManufacturerService manufacturerService
	
	def '1. Test the isValidAlias method in many different situations' () {
		setup:
			def manCat = new Manufacturer(name: 'cat').save()
			def manDog = new Manufacturer(name: 'dog').save()
			def a1ias1 = new ManufacturerAlias(name:'feline', manufacturer:manCat).save()
			def a1ias2 = new ManufacturerAlias(name:'canine', manufacturer:manDog).save()
		
		
		// assertions without the local duplicate flag
		assert manufacturerService.isValidAlias('kitty', manCat) : 'VALID: manufacturer alias is completely unique'
		assert ! manufacturerService.isValidAlias('dog', manCat) : 'INVALID: manufacturer alias matches other manufacturer\'s name'
		assert ! manufacturerService.isValidAlias('canine', manCat) : 'INVALID: manufacturer alias matches other manufacturer\'s alias'
		assert ! manufacturerService.isValidAlias('cat', manCat) : 'INVALID: manufacturer alias matches the parent manufacturer\'s name'
		assert ! manufacturerService.isValidAlias('feline', manCat) : 'INVALID: manufacturer alias matches the parent manufacturer\'s alias'
		
		// assertions with the local duplicate flag
		assert manufacturerService.isValidAlias('kitty', manCat, true) : 'VALID: manufacturer alias is completely unique (local duplicates allowed)'
		assert ! manufacturerService.isValidAlias('dog', manCat, true) : 'INVALID: manufacturer alias matches other manufacturer\'s name (local duplicates allowed)'
		assert ! manufacturerService.isValidAlias('canine', manCat, true) : 'INVALID: manufacturer alias matches other manufacturer\'s alias (local duplicates allowed)'
		assert ! manufacturerService.isValidAlias('cat', manCat, true) : 'INVALID: manufacturer alias matches the parent manufacturer\'s name (local duplicates allowed)'
		assert manufacturerService.isValidAlias('feline', manCat, true) : 'VALID: manufacturer alias matches the parent manufacturer\'s alias (local duplicates allowed)'
		
		// test the manufacturer name parameter
		assert manufacturerService.isValidAlias('cat', manCat, false, 'rat eater') : 'VALID: manufacturer alias matches own name, using different name'
		
		
	}
}
