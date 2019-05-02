package net.transitionmanager.service

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import net.transitionmanager.security.Permissions
import net.transitionmanager.security.PermissionsService
import org.springframework.jdbc.core.JdbcTemplate
import spock.lang.Specification

@TestFor(PermissionsService)
@TestMixin(GrailsUnitTestMixin)
@Mock([Permissions])
class PermissionsServiceSpec extends Specification {

	def setup() {
		service.jdbcTemplate = Mock(JdbcTemplate)
	}

	void 'test can findAll permissions saved in database'() {

		given:
			new Permissions(permissionItem: 'AssetExplorerView', description: 'AssetExplorerView UNIT TEST').save(flush: true)
			new Permissions(permissionItem: 'AssetExplorerShow', description: 'AssetExplorerShow UNIT TEST').save(flush: true)
			new Permissions(permissionItem: 'AssetExplorerUpdate', description: 'AssetExplorerUpdate UNIT TEST').save(flush: true)
			new Permissions(permissionItem: 'AssetExplorerDelete', description: 'AssetExplorerDelete UNIT TEST').save(flush: true)

		when:
			List<Permissions> permissions = service.findAll()

		then:
			permissions.size() == 4

	}

	void 'test can update permissions using a Map'() {

		given: 'a permissions domain instance already saved in database'
			Permissions viewPermission = new Permissions(permissionItem: 'AssetExplorerView', description: 'AssetExplorerView UNIT TEST').save(flush: true)

		when: 'service is invoked to update permissions'
			service.update([
				column: [
					viewPermission.id.toString()
				],
				("role_${viewPermission.id}_ADMIN".toString())       : 'on',
				("role_${viewPermission.id}_CLIENT_ADMIN".toString()): 'on',
				("description_${viewPermission.id}".toString())      : 'Modified by test spec'
			])

		then: 'permissiones ares modified in database'
			with(Permissions.list()[0], Permissions){
				permissionItem == 'AssetExplorerView'
				description == 'Modified by test spec'
			}

	}


}