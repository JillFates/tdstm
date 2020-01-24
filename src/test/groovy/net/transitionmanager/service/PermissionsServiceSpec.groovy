package net.transitionmanager.service

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import net.transitionmanager.security.Permissions
import net.transitionmanager.security.PermissionsService
import net.transitionmanager.security.RolePermissions
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

class PermissionsServiceSpec extends Specification implements DataTest, ServiceUnitTest<PermissionsService> {

	void setupSpec() {
		mockDomains Permissions, RolePermissions
	}

	@ConfineMetaClassChanges([RolePermissions])
	void 'test can update permissions using a Map'() {

		given: 'a permissions domain instance already saved in database'
			RolePermissions.metaClass.static.executeUpdate = {String query -> RolePermissions.deleteAll()}
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
