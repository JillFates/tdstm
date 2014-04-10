import org.apache.shiro.crypto.hash.Sha1Hash;
import org.apache.shiro.SecurityUtils;
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.TimeUtil
import com.tdssrc.grails.HtmlUtil
import com.tdsops.tm.enums.domain.ProjectSortProperty
import com.tdsops.tm.enums.domain.ProjectStatus
import com.tdsops.tm.enums.domain.SortOrder
import java.text.SimpleDateFormat
import grails.converters.JSON

class UserLoginController {
	
	def partyRelationshipService
	def userPreferenceService
	def securityService
	def projectService
	def jdbcTemplate

	def index = { redirect(action:list,params:params) }

	// the delete, save and update actions only accept POST requests
	def allowedMethods = [delete:'POST', save:'POST', update:'POST']

	def list = {
		def listJsonUrl
		
		def companyId = params.companyId ?: 'All'
		if(companyId && companyId != 'All'){
			def map = [controller:'userLogin', action:'listJson', id:"${companyId}"]
			listJsonUrl = HtmlUtil.createLink(map)
		} else {
			def map = [controller:'userLogin', action:'listJson']
			listJsonUrl = HtmlUtil.createLink(map)+'/All'
		}
		
		if(params.activeUsers){
			 session.setAttribute("InActive", params.activeUsers)
		}
		def project = securityService.getUserCurrentProject()
		def active = params.activeUsers ? params.activeUsers : session.getAttribute("InActive")
		if(!active){
			active = 'Y'
		}
		
		def partyGroupList = PartyGroup.findAllByPartyType( PartyType.read("COMPANY")).sort{it.name}
		
		return [companyId:companyId ,partyGroupList:partyGroupList,listJsonUrl:listJsonUrl]
	}
	
	def listJson = {
		def sortIndex = params.sidx ?: 'username'
		def sortOrder  = params.sord ?: 'asc'
		def maxRows = Integer.valueOf(params.rows?:'25')
		def currentPage = Integer.valueOf(params.page?:'1')
		def rowOffset = currentPage == 1 ? 0 : (currentPage - 1) * maxRows
		def companyId
		def userLoginInstanceList
		def userLogin = securityService.getUserLogin()
		def filterParams = ['username':params.username, 'fullname':params.fullname, 'roles':params.roles, 'company':params.company,
			'lastLogin':params.lastLogin, 'dateCreated':params.dateCreated, 'expiryDate':params.expiryDate]
		
		// Validate that the user is sorting by a valid column
		if( ! sortIndex in filterParams)
			sortIndex = 'username'
			
		def presentDate = TimeUtil.nowGMTSQLFormat()
		
		def active = params.activeUsers ? params.activeUsers : session.getAttribute("InActive")
		if(!active){
			active = 'Y'
		}
		
		def query = new StringBuffer("""SELECT * FROM ( SELECT GROUP_CONCAT(role_type_id) AS roles, p.person_id AS personId, first_name AS firstName,
			u.username as username, last_name as lastName, CONCAT(CONCAT(first_name, ' '), IFNULL(last_name,'')) as fullname, pg.name AS company, u.active, u.last_login AS lastLogin, u.expiry_date AS expiryDate, 
			u.created_date AS dateCreated, u.user_login_id AS userLoginId 
			FROM party_role pr 
			LEFT OUTER JOIN person p on p.person_id=pr.party_id 
			LEFT OUTER JOIN user_login u on u.person_id=p.person_id 
			LEFT OUTER JOIN party_relationship r ON r.party_relationship_type_id='STAFF' 
				AND role_type_code_from_id='COMPANY' AND role_type_code_to_id='STAFF' AND party_id_to_id=pr.party_id 
			LEFT OUTER JOIN party_group pg ON pg.party_group_id=r.party_id_from_id 
			WHERE u.active = '${active}'""")
		if(active=='Y')
			query.append(" AND u.expiry_date > '${presentDate}' ")
		else
			query.append(" OR u.expiry_date < '${presentDate}' ")
		if( RolePermissions.hasPermission("ShowAllUsers") ){
			if(params.id && params.id != "All" ){
				// If companyId is requested
				companyId = params.id
			}
			if( !companyId && params.id != "All" ){
				// Still if no companyId found trying to get companyId from the session
				companyId = session.getAttribute("PARTYGROUP")?.PARTYGROUP
				if(!companyId){
					// Still if no luck setting companyId as logged-in user's companyId .
					def person = userLogin.person
					companyId = partyRelationshipService.getStaffCompany(person)?.id
				}
			}
			if(companyId){
				query.append(" AND pg.party_group_id = $companyId ")
			}
			//query.append(" GROUP BY pr.party_id ORDER BY pr.role_type_id, pg.name, first_name, last_name ) as users")
			query.append(" GROUP BY pr.party_id ORDER BY " + sortIndex + " " + sortOrder + ") as users")
			
			// Handle the filtering by each column's text field
			def firstWhere = true
			filterParams.each {
				if(it.getValue())
					if(firstWhere){
						query.append(" WHERE users.${it.getKey()} LIKE '%${it.getValue()}%'")
						firstWhere = false
					} else {
						query.append(" AND users.${it.getKey()} LIKE '%${it.getValue()}%'")
					}
			}
			
			userLoginInstanceList = jdbcTemplate.queryForList(query.toString())
		} else {
			userLoginInstanceList = []
		}
		
		// Limit the returned results to the user's page size and number
		def totalRows = userLoginInstanceList.size()
		def numberOfPages = Math.ceil(totalRows / maxRows)
		if(totalRows > 0)
			userLoginInstanceList = userLoginInstanceList[rowOffset..Math.min(rowOffset+maxRows,totalRows-1)]
		else
			userLoginInstanceList = []
		
		def map = [controller:'userLogin', action:'listJson', id:"${params.companyId}"]
		def listJsonUrl = HtmlUtil.createLink(map)
		
		// Due to restrictions in the way jqgrid is implemented in grails, sending the html directly is the only simple way to have the links work correctly
		def results = userLoginInstanceList?.collect {
			[ cell: [ '<a href="'+HtmlUtil.createLink([controller:'userLogin', action:'show', id:"${it.userLoginId}"])+'">'+it.username+'</a>',
			'<a href="javascript:loadPersonDiv('+it.personId+',\'generalInfoShow\')">'+it.fullname+'</a>', 
			it.roles, it.company, it.lastLogin, it.dateCreated, it.expiryDate ], id: it.userLoginId ]}
			
		def jsonData = [rows: results, page: currentPage, records: totalRows, total: numberOfPages]
		render jsonData as JSON
	}
	
	def show = {
		def userLoginInstance = UserLogin.get( params.id )
		def companyId = params.companyId
		if(!userLoginInstance) {
			flash.message = "UserLogin not found with id ${params.id}"
			redirect( action:list, params:[ id:companyId ] )
		} else { 
			def roleList = RoleType.findAll("from RoleType r where r.description like 'system%' order by r.description ")
			def assignedRoles = userPreferenceService.getAssignedRoles( userLoginInstance.person )
			return [ userLoginInstance : userLoginInstance, companyId:companyId, roleList:roleList, assignedRoles:assignedRoles ] 
		}
	}

	def delete = {
		def userLoginInstance = UserLogin.get( params.id )
		def companyId = params.companyId
		if(userLoginInstance) {
			userLoginInstance.delete(flush:true)
			flash.message = "UserLogin ${userLoginInstance} deleted"
			redirect( action:list, params:[ id:companyId ] )
		}
		else {
			flash.message = "UserLogin not found with id ${params.id}"
			redirect( action:list, params:[ id:companyId ] )
		}
	}
	/*
	 *  Return userdetails and roles to Edit form
	 */
	def edit = {
		def userLoginInstance = UserLogin.get( params.id )
		def companyId = params.companyId
		if(!userLoginInstance) {
			flash.message = "UserLogin not found with id ${params.id}"
			redirect( action:list, params:[ id:companyId ] )
		}
		else {
			def person = userLoginInstance.person
			def availableRoles = userPreferenceService.getAvailableRoles( person )
			def assignedRoles = userPreferenceService.getAssignedRoles( person )
			def roleList = RoleType.findAll("from RoleType r where r.description like 'system%' order by r.description ")
			def projectList = Project.list(sort:'name', order:'asc')
			def projectId = userPreferenceService.getPreferenceByUserAndCode(userLoginInstance, "CURR_PROJ")
			return [ userLoginInstance : userLoginInstance, availableRoles:availableRoles, assignedRoles:assignedRoles, companyId:companyId, roleList:roleList,
					projectList:projectList,  projectId:projectId  ]
		}
	}
	/*
	 * update user details and set the User Roles to the Person
	 */
	def update = {
		UserLogin.withTransaction { status ->
			def userLoginInstance = UserLogin.get( params.id )
			def companyId = params.companyId
			if(userLoginInstance) {
				try{
					def formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm a")
					def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ
					def expiryDate = params.expiryDate
					if(expiryDate){
						params.expiryDate =  GormUtil.convertInToGMT(formatter.parse( expiryDate ), tzId)
					}
				} catch (Exception ex){
					println"Invalid date format"
				}
				def password = params.password
				def oldPassword = userLoginInstance.password
				if(!securityService.validPassword(params['username'], params['password']) && password != ""){
					flash.message = "The password must meet all the requirements"
					redirect( action:edit, id:userLoginInstance.id, params:[ companyId:companyId ] )
				} else{
					userLoginInstance.properties = params
					if(params.isLocal){
						if(params.forcePasswordChange)
							userLoginInstance.forcePasswordChange = 'Y'
						else
							userLoginInstance.forcePasswordChange = 'N'
					}else{
						userLoginInstance.isLocal = false
						userLoginInstance.forcePasswordChange = 'N'
					}
					if(password != ""){
						//	convert password onto Hash code
						userLoginInstance.password = new Sha1Hash(params['password']).toHex()
						userLoginInstance.passwordChangedDate = TimeUtil.nowGMT()
					}else{
						userLoginInstance.password = oldPassword
					}
					if( !userLoginInstance.hasErrors() && userLoginInstance.save(flush:true) ) {
						def assignedRoles = request.getParameterValues("assignedRole");
						def person = params.person.id
						def personInstance = Person.get(person)
						personInstance.active = userLoginInstance.active
						partyRelationshipService.updatePartyRoleByType('system', personInstance, assignedRoles)
						userPreferenceService.setUserRoles(assignedRoles, person)
						userPreferenceService.addOrUpdatePreferenceToUser(userLoginInstance, "CURR_PROJ", params.project)
						flash.message = "UserLogin ${userLoginInstance} updated"
						redirect( action:show, id:userLoginInstance.id, params:[ companyId:companyId ] )
					} else {
						def person = userLoginInstance.person
						def availableRoles = userPreferenceService.getAvailableRoles( person )
						def assignedRoles = userPreferenceService.getAssignedRoles( person )
						status.setRollbackOnly()
						render(view:'edit',model:[userLoginInstance:userLoginInstance, availableRoles:availableRoles, assignedRoles:assignedRoles, companyId:companyId ])
					}
				}
			}
			else {
				flash.message = "UserLogin not found with id ${params.id}"
				redirect( action:edit, id:params.id, params:[ companyId:companyId ])
			}
		}
	}
	
	// set the User Roles to the Person
	def addRoles = {
			def assignedRoles = params.assignedRoleId.split(',')
			def person = params.person
			def actionType = params.actionType
			if(actionType != "remove"){
				userPreferenceService.setUserRoles(assignedRoles, person)
			} else {
				userPreferenceService.removeUserRoles(assignedRoles, person)
			}
		render true
	}
	
	// return userlogin details to create form
	def create = {
		def personId = params.id
		def companyId = params.companyId
		def person
		if ( personId != null ) {
			person = Person.findById( personId )
			if (person.lastName == null) {
				person.lastName = ""
			}
		}
		
		def now = TimeUtil.nowGMT()
		
		def userLoginInstance = new UserLogin()
		userLoginInstance.properties = params
		def expiryDate = new Date(now.getTime() + 7776000000) // 3 Months
		userLoginInstance.expiryDate = expiryDate
		def roleList = RoleType.findAll("from RoleType r where r.description like 'system%' order by r.description ")
		def project = securityService.getUserCurrentProject()

		def currentUser = securityService.getUserLogin()
		def projectList = projectService.getUserProjectsOrderBy(currentUser, false, ProjectStatus.ACTIVE)

		return ['userLoginInstance':userLoginInstance, personInstance:person, companyId:companyId, roleList:roleList, projectList:projectList,
			 	project:project]
	}
	/*
	 *  Save the User details and set the user roles for Person
	 */
	def save = {
		try{
			def formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm a")
			def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ
			def expiryDate = params.expiryDate
			if(expiryDate){
				params.expiryDate =  GormUtil.convertInToGMT(formatter.parse( expiryDate ), tzId)
			}
		} catch (Exception ex){
			println"Invalid date format"
		}
		def userLoginInstance = new UserLogin(params)
		//userLoginInstance.createdDate = new Date()
		def companyId = params.companyId
		//convert password onto Hash code
		def success = false
		def token = true
		if(params.isLocal)
			token = securityService.validPassword(params['username'], params['password'])
		
		if(token) {
			userLoginInstance.password = new Sha1Hash(params['password']).toHex()
			if(params.isLocal){
				if(params.forcePasswordChange)
					userLoginInstance.forcePasswordChange = 'Y'
				else
					userLoginInstance.forcePasswordChange = 'N'
			}else{
				userLoginInstance.isLocal = false
				userLoginInstance.forcePasswordChange = 'N'
			}
			if(!userLoginInstance.hasErrors() && userLoginInstance.save()) {
				def assignedRoles = request.getParameterValues("assignedRole");
				def person = params.person.id
				def personInstance = Person.findById( person )
				personInstance.active = userLoginInstance.active
				userPreferenceService.setUserRoles(assignedRoles, person)
				userPreferenceService.addOrUpdatePreferenceToUser(userLoginInstance, "START_PAGE", "Current Dashboard")
				userPreferenceService.addOrUpdatePreferenceToUser(userLoginInstance, "CURR_PROJ", params.project)
				def tZPreference = new UserPreference()
				tZPreference.userLogin = userLoginInstance
				tZPreference.preferenceCode = "CURR_TZ"
				tZPreference.value = "EDT"
				tZPreference.save( insert: true)
				flash.message = "UserLogin ${userLoginInstance} created"
				redirect( action:show, id:userLoginInstance.id, params:[ companyId:companyId ] )
				success = true
			}
		}
		if(!success){
			def assignedRole = request.getParameterValues("assignedRole");
			def personId = params.personId
			def personInstance
			if(personId != null ){
				personInstance = Person.findById( personId )
			}
				flash.message = "Password must follow all the requirements"
			render(view:'create',model:[ userLoginInstance:userLoginInstance,assignedRole:assignedRole,personInstance:personInstance, companyId:companyId ])
		}
	}
	/*======================================================
	 *  Update recent page load time into userLogin
	 *=====================================================*/
	def updateLastPageLoad = {
		def principal = SecurityUtils.subject?.principal
		if( principal ){
			def userLogin = UserLogin.findByUsername( principal )
			userLogin.lastPage = TimeUtil.nowGMT()
			userLogin.save(flush:true)
			session.REDIRECT_URL = params.url
		}
		render "SUCCESS"
	}
	def changePassword = {
		def principal = SecurityUtils.subject?.principal
		def userLoginInstance = UserLogin.findByUsername(principal)
		render(view:'changePassword',model:[ userLoginInstance:userLoginInstance])
		return [ userLoginInstance : userLoginInstance]
	}
	def updatePassword = {
		def subject = SecurityUtils.subject
		def principal = subject.principal
		def userLoginInstance = UserLogin.findByUsername(principal)
		if(userLoginInstance && securityService.validPassword(userLoginInstance.username, params.password)) {
			def newPassword = new Sha1Hash(params.password).toHex()
			if(userLoginInstance.password == newPassword){
				flash.message = "New password must be different from the old password"
				redirect(action:'changePassword', params:[ userLoginInstance:userLoginInstance ])
				return
			}
			userLoginInstance.setPassword(newPassword)
			userLoginInstance.passwordChangedDate = TimeUtil.nowGMT()
			userLoginInstance.forcePasswordChange = 'N'
			flash.message = "Password updated"
			redirect(controller:'project', action:'show', params:[ userLoginInstance:userLoginInstance ])
		} else {
			flash.message = "Password must follow all the requirements"
			redirect(action:'changePassword', params:[ userLoginInstance:userLoginInstance ])
		}
		return
	}
}
