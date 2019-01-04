import com.tds.asset.AssetEntity
import net.transitionmanager.service.ImportService

import spock.lang.*

class AssetImportServiceIntegrationTests extends Specification {

	ImportService importService

	def '1. Test assignWhomHelperByEmail'() {
		when: 'Try finding valid email address exact case'
			String email = 'Ben.Here@Before.com'
			List staff = [ [id:123, email: email], [id: 456, email:''] ]
			Map result = [whom:'', errMsg:'']
			importService.assignWhomHelperByEmail(email, staff, result)
		then:
			result.whom.id == 123
			! result.errMsg

		when: 'Try valid email with case sensitivity'
			result = [whom:'', errMsg:'']
			importService.assignWhomHelperByEmail(email.toUpperCase(), staff, result)
		then:
			result.whom.id == 123
			! result.errMsg

		when: 'See if a bogus email results in an error'
			result = [whom:'', errMsg:'']
			importService.assignWhomHelperByEmail('bogus@email.com', staff, result)
		then:
			result.errMsg
	}

	def '2. Test assignWhomHelperByPersonId'() {
		when: 'Try finding valid person by ID'
			String id = '123'
			List staff = [ [id:123L], [id:456L] ]
			Map result = [errMsg:'']
			importService.assignWhomHelperByPersonId('123', staff, result)
		then:
			result.whom.id == 123
			! result.errMsg

		when: 'Try finding a 2nd valid person by id'
			result = [errMsg:'']
			importService.assignWhomHelperByPersonId('456', staff, result)
		then:
			result.whom.id == 456
			! result.errMsg

		when: 'See that missing ID returns an error'
			result = [errMsg:'']
			importService.assignWhomHelperByPersonId('789', staff, result)
		then:
			result.errMsg

		when: 'See that invalid ID returns an error'
			result = [errMsg:'']
			importService.assignWhomHelperByPersonId('2x4', staff, result)
		then:
			result.errMsg
	}

	def '3. Test assignWhomHelperByHashtag'() {
		when: 'See that accessing a real properties works'
			String hashtag = '#custom1'
			AssetEntity asset = new AssetEntity()
			Map result = [whom:'', errMsg:'']
			importService.assignWhomHelperByHashtag(hashtag, asset, result)
		then:
			result.whom == hashtag
			! result.errMsg

		when: 'Make sure that bad field references work correctly'
			hashtag = '#nonexistentProperty'
			result = [errMsg:'', whom:'']
			importService.assignWhomHelperByHashtag(hashtag, asset, result)
		then:
			! result.whom
			result.errMsg
	}

	def '4. Test assignWhomHelperByEmail'() {
		when: 'See that accessing a real email finds a person'
			String email = 'John.McClane@diehard.com'
			List staff = [ [id:123, email:email], [id:456, email:''] ]
			Map result = [whom:'', errMsg:'']
			importService.assignWhomHelperByEmail(email, staff, result)
		then:
			result.whom.id == 123
			! result.errMsg

		when: 'Validate that accessing a bogus email errors'
			result = [whom:'', errMsg:'']
			importService.assignWhomHelperByEmail('harry.back@example.com', staff, result)
		then:
			! result.whom
			result.errMsg
	}
}
