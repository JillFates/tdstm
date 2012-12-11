import javax.management.relation.Role;

import com.tdssrc.grails.GormUtil;

def jdbcTemplate = ctx.getBean("jdbcTemplate")

//jdbcTemplate.update("DELETE FROM role_permissions")
//jdbcTemplate.update("DELETE FROM permissions")

def permissionsList = [
	
	[ PermissionGroup.NAVIGATION, "AdminMenuView"],
	[ PermissionGroup.NAVIGATION, "RackMenuView"],
	[ PermissionGroup.NAVIGATION, "AssetMenuView"],
	[ PermissionGroup.NAVIGATION, "EventMenuView"],
	[ PermissionGroup.NAVIGATION, "BundleMenuView"],
	[ PermissionGroup.NAVIGATION, "ReportMenuView"],
	[ PermissionGroup.NAVIGATION, "HelpMenuView"],
	[ PermissionGroup.NAVIGATION, "ConsoleMenuView"],
	[ PermissionGroup.NAVIGATION, "DashBoardMenuView"],
	[ PermissionGroup.NAVIGATION, "AssetTrackerMenuView"],
	
	[ PermissionGroup.ASSETENTITY, "CommentCrudView"],
	[ PermissionGroup.ASSETENTITY, "ModelDialogView"],
	[ PermissionGroup.ASSETENTITY, "EditAndDelete"],
	
	[ PermissionGroup.MOVEBUNDLE, "MoveBundleEditView"],
	[ PermissionGroup.MOVEBUNDLE, "MoveBundleShowView"],
	
	[ PermissionGroup.MOVEEVENT, "CreateNews"],
	[ PermissionGroup.MOVEEVENT, "MoveEventEditView"],
	[ PermissionGroup.MOVEEVENT, "MoveEventShowView"],
	[ PermissionGroup.MOVEEVENT, "MoveEventStatus"],
	
	[ PermissionGroup.DASHBOARD, "ViewPacingMeters"],
	[ PermissionGroup.DASHBOARD, "ManualOverride"],
	[ PermissionGroup.DASHBOARD, "DashBoardMenuView"],
	
	[ PermissionGroup.CONSOLE, "ShowMoveTechsAndAdmins"],
	[ PermissionGroup.CONSOLE, "ShowListNews"],
	[ PermissionGroup.CONSOLE, "ShowCartTracker"],
	[ PermissionGroup.CONSOLE, "TeamLinks"],
	[ PermissionGroup.CONSOLE, "BulkChangeStatus"],
	[ PermissionGroup.CONSOLE, "ShowActionColumn"],
	
	
	[ PermissionGroup.REPORTS, "ShowDiscovery"],
	[ PermissionGroup.REPORTS, "ShowMovePrep"],
	[ PermissionGroup.REPORTS, "ShowMoveDay"],
	
	[ PermissionGroup.PARTY, "PartyCreateView"],
	[ PermissionGroup.PARTY, "PartyEditView"],
	[ PermissionGroup.PARTY, "PartyTypeShowView"],
	
	[ PermissionGroup.PARTY, "PartyRelationshipTypeCreateView"],
	[ PermissionGroup.PARTY, "PartyRelationshipTypeEditView"],
	
	[ PermissionGroup.PERSON, "PersonCreateView"],
	[ PermissionGroup.PERSON, "PersonEditView"],
	
	[ PermissionGroup.PROJECT, "CreateProject"],
	[ PermissionGroup.PROJECT, "ProjectEditView"],
	[ PermissionGroup.PROJECT, "ProjectDelete"],
	[ PermissionGroup.PROJECT, "ShowAllProjects"],
	[ PermissionGroup.PROJECT, "EditProjectStaff"],
	
	[ PermissionGroup.ROLETYPE, "RoleTypeCreate"],
	[ PermissionGroup.ROLETYPE, "RoleTypeEditView"],
	
	[ PermissionGroup.ROOMLAYOUT, "RoomListActionColumn"],
	[ PermissionGroup.ROOMLAYOUT, "MergeRoom"],
	[ PermissionGroup.ROOMLAYOUT, "DeleteRoom"],
	[ PermissionGroup.ROOMLAYOUT, "RoomEditView"],
	
	[ PermissionGroup.USER, "CreateUserLogin"],
	[ PermissionGroup.USER, "EditUserLogin"],
	[ PermissionGroup.USER, "ShowAllUsers"],
	
	
	[ PermissionGroup.ASSETTRACKER, "ClientConsoleBulkEdit"],
	[ PermissionGroup.ASSETTRACKER, "ClientConsoleComment"],
	[ PermissionGroup.ASSETTRACKER, "ClientConsoleCheckBox"],
	[ PermissionGroup.ASSETTRACKER, "AssetTrackerMenuView"],
	
	[ PermissionGroup.CLIENTTEAMS, "ClientTeamsList"],
	
	[ PermissionGroup.MODEL, "ValidateModel"],
	[ PermissionGroup.PERSON, "AddPerson"],
	[ PermissionGroup.PERSON, "PersonExpiryDate"],
	[ PermissionGroup.RACKLAYOUT, "EditAssetInRackLayout"]

]

permissionsList.each {
  def permission = Permissions.findByPermissionGroupAndPermissionItem(it[0], it[1])
  if(!permission){
	  permission = new Permissions(
									permissionGroup: it[0],
									permissionItem: it[1],
									)
	  if ( !permission.validate() || ! permission.save() ) {
		  def etext = "Unable to create permission ${it[0]}" +
		  GormUtil.allErrorsString( permission )
		  println etext
	  }
  }
}
def permissionObjectsList = Permissions.list()
// Add All permissions to 
permissionObjectsList.each{ permission->
	def rolePermission = RolePermissions.findByPermissionAndRole(permission, "ADMIN")
	if(!rolePermission){
		rolePermission = new RolePermissions(
									  permission: permission,
									  role: "ADMIN",
									  )
		if ( !rolePermission.validate() || ! rolePermission.save() ) {
			def etext = "Unable to create RolePermission: ${rolePermission}" +
			GormUtil.allErrorsString( rolePermission )
			println etext
		}
	}
}























