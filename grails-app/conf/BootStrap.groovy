import com.tds.asset.AssetComment
import com.tds.asset.AssetEntity
import com.tdsops.metaclass.CustomMethods
import com.tdssrc.eav.EavAttributeSet
import com.tdssrc.eav.EavEntityType
import com.tdssrc.grails.GormUtil
import grails.util.Environment
import net.transitionmanager.domain.DataTransferSet
import net.transitionmanager.domain.Manufacturer
import net.transitionmanager.domain.Model
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.MoveEvent
import net.transitionmanager.domain.MoveEventNews
import net.transitionmanager.domain.Notice
import net.transitionmanager.domain.PartyGroup
import net.transitionmanager.domain.PartyRelationship
import net.transitionmanager.domain.PartyRelationshipType
import net.transitionmanager.domain.PartyRole
import net.transitionmanager.domain.PartyType
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.ProjectAssetMap
import net.transitionmanager.domain.ProjectTeam
import net.transitionmanager.domain.RoleType
import net.transitionmanager.domain.UserLogin
import net.transitionmanager.domain.UserPreference
import net.transitionmanager.domain.Workflow
import net.transitionmanager.service.AssetEntityAttributeLoaderService
import net.transitionmanager.service.QzSignService
import net.transitionmanager.service.StateEngineService
import net.transitionmanager.service.TaskService
import net.transitionmanager.service.LicenseAdminService
import org.grails.refcode.RefCode
import org.apache.log4j.Logger

import java.lang.management.ManagementFactory

class BootStrap {
	AssetEntityAttributeLoaderService assetEntityAttributeLoaderService
	StateEngineService stateEngineService
	TaskService taskService
	QzSignService qzSignService
	LicenseAdminService licenseAdminService

	def init = { servletContext ->
		checkForBlacklistedVMParameters()

		//Check required default Config Info
		checkConfigInfo()

		//initialize exception logger filter to mute GrailsExceptionResolver
		initializeExceptionLoggerFilter()

		CustomMethods.initialize()

		// Load all of the Workflow definitions into the StateEngine service
		Workflow.list().each { wf ->
			stateEngineService.loadWorkflowTransitionsIntoMap(wf.process, 'workflow')
		}

		taskService.init()

		Notice.registerObjectMarshaller()

		if (Environment.current == Environment.PRODUCTION) return

		// createInitialData()

		//LOAD TESTS for dev
		//testMemoryAllocation()
	}

	/**
	 * Check Config flags or alert about required information
	 */
	private checkConfigInfo(){
		//Call some methods to show error messages (if any) from Boot time
		qzSignService.getPassphrase()
		qzSignService.findPrivateKeyFile()
	}

	/**
	 * Check Config flags or alert about required information
	 */
	private initializeExceptionLoggerFilter(){

		Logger.rootLogger.allAppenders.each { appender ->
            ExceptionLoggerFilter filter = new ExceptionLoggerFilter()
            filter.loggerClass = "org.codehaus.groovy.grails.web.errors.GrailsExceptionResolver"
            filter.activateOptions()
            appender.addFilter(filter)
		}

	}

	private void createInitialData() {

		def save = { instance ->
			instance.save()
			if (instance.hasErrors()) {
				String error = 'Unable to create ' + instance.getClass().simpleName + GormUtil.allErrorsString(instance)
				println error
				log.error error
			}
			instance
		}

		def createAssignedId = { Class type, String id, Map data ->
			def instance = type.newInstance(data)
			instance.id = id
			save instance
		}

		// -------------------------------
		// Role Types
		// The description now classifies groups of roles.  Eventually this will be implemented
		// like ofBiz where there is a parent id.
		// -------------------------------
		println "ROLE TYPES "

		[
			["ADMIN",           "System : Administrator"],
			["USER",            "System : User"],
			["MANAGER",         "System : Manager"],
			["OBSERVER",        "System : Observer"],
			["WORKSTATION",     "System : Work Station"],
			["CLIENT",          "Party : Client"],
			["PARTNER",         "Party : Partner"],
			["STAFF",           "Party : Staff"],
			["COMPANY",         "Party : Company"],
			["VENDOR",          "Party : Vendor"],
			["PROJECT",         "Party : Project"],
			["APP_ROLE",        "Party : Application"],
			["TEAM",            "Party : Team"],
			["TEAM_MEMBER",     "Team : Team Member"],
			["TECH",            "Staff : Technician"],
			["PROJ_MGR",        "Staff : Project Manager"],
			["MOVE_MGR",        "Staff : Move Manager"],
			["SYS_ADMIN",       "Staff : System Administrator"],
			["DB_ADMIN",        "Staff : Database Administrator"],
			["NETWORK_ADMIN",   "Staff : Network Administrator"],
			["ACCT_MGR",        "Staff : Account Manager"],
			["PROJECT_ADMIN",   "Staff : Project Administrator"],
			["APP_OWNER",       "App : Application Owner"],
			["APP_SME",         "App : Subject Matter Expert"],
			["APP_1ST_CONTACT", "App : Primary Contact"],
			["APP_2ND_CONTACT", "App : Secondary Contact"],
			["MOVE_BUNDLE",     "Proj: Move Bundle"],
			["COMPANY_ADMIN",   "Staff: Company Administrator"]
		].each {
			createAssignedId RoleType, it[0], [description: it[1]]
		}

		// -------------------------------
		// Party Types
		// -------------------------------
		println "PARTY TYPES"

		[
			["PERSON",      "Person"],
			["PARTY_GROUP", "PartyGroup"],
			["COMPANY",     "Company"],
			["PROJECT",     "Project"],
			["APP_TYPE",    "Application"],
		].each {
			createAssignedId PartyType, it[0], [description: it[1]]
		}

		// -----------------------------------------
		// Create PartyRelationshipType Details
		// -----------------------------------------
		println "PARTY RELATIONSHIP TYPES"

		[
			["STAFF",             "Staff"],
			["PROJ_STAFF",        "Project Staff"],
			["PROJ_COMPANY",      "Project Company"],
			["PROJ_TEAM",         "Project Team"],
			["PROJ_PARTNER",      "Project Partner"],
			["PROJ_CLIENT",       "Project Client"],
			["CLIENTS",           "Clients"],
			["PARTNERS",          "Partners"],
			["VENDORS",           "Vendors"],
			["PROJ_BUNDLE_STAFF", "Bundle Staff"],
			["APPLICATION",       "Application"]
		].each {
			createAssignedId PartyRelationshipType, it[0], [description: it[1]]
		}

		// Don't think we need this (John)
		//	    def projectRelaType = new PartyRelationshipType( description:"Project" )
		//        projectRelaType.id = "PROJECT"
		//        projectRelaType.save()

		// -------------------------------
		// Persons
		// -------------------------------
		println "PERSONS"

		def createPerson = { String firstName, String lastName, String title, String active = 'Y' ->
			save new Person(firstName: firstName, lastName: lastName, title: title, active: active, partyType: personPartyType)
		}

		def personJohn = createPerson('John', 'Doherty', 'Project Manager')
		def personJimL = createPerson('Jim', 'Laucher', 'Tech')
		def personLisa = createPerson('Lisa', 'Carr', 'Move Manager')
		def personGenePoole = createPerson('Gene', 'Poole', 'Move Manager', 'N')
		def personTim = createPerson('Tim', 'Schutt', 'Project Manager')
		def personRobin = createPerson('Robin', 'Banks', 'Project Manager')
		def personAnna = createPerson('Anna', 'Graham', 'Sys Opp')
		def personReddy = createPerson('Lokanath', 'Reddy', 'Tech Lead')
		def personBrock = createPerson('Brock', 'Lee', 'Tech')
		def personTransport = createPerson('Tran', 'Sport', 'Transport')
		def personRita = createPerson('Rita', 'Booke', 'MANAGER')
		def personWarren = createPerson('Warren', 'Peace', 'OBSERVER')

		// This person account will actual share 3 logins (move tech, clean tech, and mover)
		def personWorkStation = createPerson('Work', 'Station', 'Work Station User')

		// Cedars staff for Raiser's Edge application
		def personNBonner = createPerson('Nancy', 'Bonner', '')
		def personAMaslac = createPerson('Alan', 'Maslac', '')
		def personHKim = createPerson('Hongki', 'Kim', '')
		def personLCoronado = createPerson('Leo', 'Coronado', '')

		// -------------------------------
		// Create User Login.
		// -------------------------------
		println "USER LOGIN"

		def createUserLogin = { Person person, String username, String password ->
			save new UserLogin(person: person, username: username, password: SecurityUtil.encryptLegacy(password), active: 'Y')
		}

		def adminUserLisa = createUserLogin(personLisa, "lisa", 'admin')
		def userJohn = createUserLogin(personJohn, "john", 'admin')
		def normalUserRalph = createUserLogin(personJimL, "ralph", 'user')
		def userRita = createUserLogin(personRita, "rbooke", 'manager')
		def userWarren = createUserLogin(personWarren, "wpeace", 'observer')
		// Create the Move Tech, Logistics Tech and Mover (keep short for barcode)
		def userMoveTech = createUserLogin(personWorkStation, "mt", 'xyzzy')
		def userCleanTech = createUserLogin(personWorkStation, "ct", 'xyzzy')
		def userMover = createUserLogin(personWorkStation, "mv", 'xyzzy')

		// -------------------------------
		// Create Party Group (Companies)
		// -------------------------------
		println "PARTY GROUPS "
		def tds = save new PartyGroup(name: "TDS", partyType: companyType)
		def emc = save new PartyGroup(name: "EMC", partyType: companyType)
		def timeWarner = save new PartyGroup(name: "Time Warner", partyType: companyType)
		def cedars = save new PartyGroup(name: "Cedars-Sinai", partyType: companyType)
		def sigma = save new PartyGroup(name: "SIGMA", partyType: companyType)
		def trucks = save new PartyGroup(name: "TrucksRUs", partyType: companyType)

		// -------------------------------
		// Create PartyRole for Companies
		// -------------------------------
		[tds, emc, timeWarner, cedars, sigma, trucks].each {
			save new PartyRole(party: it, roleType: companyRole)
		}

		// -------------------------------
		// Create Projects
		// -------------------------------
		println "PROJECTS "
		def cedarsProject = save new Project(name: "Cedars-Sinai Move 1", projectCode: 'CS1', client: cedars,
				description: '100 servers', partyType: groupPartyType,
				startDate: new Date(), completionDate: new Date() + 10, workflowCode: 'STD_PROCESS')
		def twProject = save new Project(name: "Time Warner VA Move", projectCode: 'TM-VA-1', client: timeWarner,
				description: '500 servers', partyType: groupPartyType,
				startDate: new Date(), completionDate: new Date() + 10, workflowCode: 'STD_PROCESS')

		// -------------------------------
		// Create moveEvent Details
		// -------------------------------
		println "MOVE EVENT"
		def moveEvent = save new MoveEvent(project: cedarsProject, name: "Move Event 1")

		// -------------------------------
		// Create MoveBundle Details
		// -------------------------------
		println "MOVE BUNDLE"

		def createMoveBundle = { Project project, String name, int stDelta, int ctDelta, int order, MoveEvent event = null ->
			save new MoveBundle(project: project, name: name, moveEvent: event, startTime: new Date() + stDelta,
			                    completionTime: new Date() + ctDelta, operationalOrder: order)
		}
		def cedarsProjectMoveBundle1 = createMoveBundle(cedarsProject, "Bundle 1", 0, 1, 1, moveEvent)
		def cedarsProjectMoveBundle2 = createMoveBundle(cedarsProject, "Bundle 2", 1, 2, 1)
		def cedarsProjectMoveBundle3 = createMoveBundle(cedarsProject, "Bundle 3", 2, 3, 1)
		def cedarsProjectMoveBundle4 = createMoveBundle(cedarsProject, "Bundle 4", 3, 4, 1)
		def twProjectMoveBundle = createMoveBundle(twProject, "TW Bundle", 12, 15, 2)

		// -------------------------------
		// Create ProjectTeam
		// -------------------------------
		println "PROJECT TEAM"
		def cedarsGreenProjectTeam = save new ProjectTeam(name: "MoveTeam 1", teamCode: "1", moveBundle: cedarsProjectMoveBundle1)
		def cedarsRedProjectTeam = save new ProjectTeam(name: "MoveTeam 2", teamCode: "2", moveBundle: cedarsProjectMoveBundle1)
		def cedarsCleanProjectTeam = save new ProjectTeam(name: "Logistics", teamCode: "Logistics", moveBundle: cedarsProjectMoveBundle1)
		def cedarsTransportProjectTeam = save new ProjectTeam(name: "Transport", teamCode: "Transport", moveBundle: cedarsProjectMoveBundle1)
		def twGreenProjectTeam = save new ProjectTeam(name: "MoveTeam 1", teamCode: "1", moveBundle: twProjectMoveBundle)
		def twRedProjectTeam = save new ProjectTeam(name: "MoveTeam 2", teamCode: "2", moveBundle: twProjectMoveBundle)

		// -------------------------------
		// Create default Preference
		// -------------------------------
		println "USER PREFERENCES"
		def johnPref = save new UserPreference(value: cedarsProject.id, userLogin: userJohn, preferenceCode: "CURR_PROJ")

		// -------------------------------
		// Create PartyRole Details
		// -------------------------------
		println "PARTY ROLES"
		[
			[personJimL,        userRole],
			[personLisa,        userRole],
			[personJohn,        adminRole],
			[personJohn,        projectAdminRole],
			[personJohn,        companyAdmin],
			[personWorkStation, workStationRole],
			[personRita,        managerRole],
			[personWarren,      observerRole]
		].each {
			save new PartyRole(party: it[0], roleType: it[1])
		}

		// -------------------------------
		// create Party Relationship
		// -------------------------------
		println "PARTY RELATIONSHIPS "
		// Save all the rows in list
		int i = 0
		def pr = [
			// Partners, Clients and Vendors
			[partnerType, tds, companyRole, emc, partnerRole],
			[partnerType, tds, companyRole, sigma, partnerRole],
			[vendorType, tds, companyRole, trucks, vendorRole],
			[clientType, tds, companyRole, cedars, clientRole],
			[clientType, tds, companyRole, timeWarner, clientRole],

			// Staff
			[staffType, tds, companyRole, personJohn, staffRole],
			[staffType, tds, companyRole, personTim, staffRole],
			[staffType, tds, companyRole, personJimL, staffRole],
			[staffType, tds, companyRole, personAnna, staffRole],
			[staffType, tds, companyRole, personBrock, staffRole],
			[staffType, tds, companyRole, personTransport, staffRole],
			[staffType, emc, companyRole, personLisa, staffRole],
			[staffType, emc, companyRole, personRobin, staffRole],
			[staffType, sigma, companyRole, personReddy, staffRole],
			[staffType, cedars, companyRole, personGenePoole, staffRole],
			[staffType, cedars, companyRole, personNBonner, staffRole],
			[staffType, cedars, companyRole, personAMaslac, staffRole],
			[staffType, cedars, companyRole, personHKim, staffRole],
			[staffType, cedars, companyRole, personLCoronado, staffRole],

			// cedars-Sinai Relationships
			[projCompanyType, cedarsProject, projectRole, tds, companyRole],
			[projClientType, cedarsProject, projectRole, cedars, clientRole],
			[projPartnerType, cedarsProject, projectRole, emc, partnerRole],
			// Project Staff roles
			[projStaffType, cedarsProject, projectRole, personRobin, pmRole],
			[projStaffType, cedarsProject, projectRole, personJohn, moveMgrRole],
			[projStaffType, cedarsProject, projectRole, personGenePoole, networkAdminRole],
			[projStaffType, cedarsProject, projectRole, personAnna, techRole],
			[projStaffType, cedarsProject, projectRole, personJimL, techRole],
			[projStaffType, cedarsProject, projectRole, personBrock, techRole],
			[projStaffType, cedarsProject, projectRole, personTransport, techRole],
			[projStaffType, cedarsProject, projectRole, personRita, managerRole],
			[projStaffType, cedarsProject, projectRole, personWarren, observerRole],

			// TimeWarner Relationships
			[projCompanyType, twProject, projectRole, tds, companyRole],
			[projClientType, twProject, projectRole, timeWarner, clientRole],
			[projStaffType, twProject, projectRole, personTim, pmRole],
			[projStaffType, twProject, projectRole, personJohn, moveMgrRole],

			// Project Team Relationships with staff
			[teamType, cedarsGreenProjectTeam, teamRole, personJimL, teamMemberRole],
			[teamType, cedarsGreenProjectTeam, teamRole, personAnna, teamMemberRole],
			[teamType, cedarsRedProjectTeam, teamRole, personJimL, teamMemberRole],
			[teamType, cedarsCleanProjectTeam, teamRole, personBrock, teamMemberRole],
			[teamType, cedarsTransportProjectTeam, teamRole, personTransport, teamMemberRole],
			[teamType, cedarsRedProjectTeam, teamRole, personJohn, teamMemberRole],
			[teamType, twGreenProjectTeam, teamRole, personTim, teamMemberRole],
			[teamType, twGreenProjectTeam, teamRole, personJohn, teamMemberRole]
		]
		pr.each {
			// println "row $i"
			i++
			// println "${it[0].id} : ${it[1].id} : ${it[2].id} : ${it[3].id} : ${it[4].id}"

			save new PartyRelationship(
					partyRelationshipType: it[0],
					partyIdFrom: it[1],
					roleTypeCodeFrom: it[2],
					partyIdTo: it[3],
					roleTypeCodeTo: it[4])
		}

		//--------------------------------
		// Create EavEntityType and EavAttributeSet records
		//--------------------------------
		println "ENTITY TYPE & ATTRIBUTE SET"

		def entityType = save new EavEntityType(entityTypeCode: 'AssetEntity', domainName: 'AssetEntity', isAuditable: 1)

		// This line was causing RTE because table is not created
		def attributeSet = save new EavAttributeSet(attributeSetName: 'Server', entityType: entityType, sortOrder: 10)

		//---------------------------------
		//  Create Models
		//---------------------------------
		println "MODEL"
		[
			["railType", "StorageWorks", 0],
			["railType", "Ultrium Tape", 0]
		].each {
			save new RefCode(
					domain: it[0],
					value: it[1],
					sortOrder: it[2])
		}

		def dellManu = save new Manufacturer(name: "DELL")
		def hclManu = save new Manufacturer(name: "HCL")

		[
			["server", dellManu],
			["leaptop", dellManu],
			["mouse", dellManu],
			["hardisk", dellManu],
			["monitor", hclManu],
			["keyboard", hclManu],
			["cpu", hclManu],
			["charger", hclManu]
		].each {
			save new Model(
					modeName: it[0],
					manufacturer: it[1])
		}

		//---------------------------------
		//  Create Asset Entity
		//---------------------------------
		println "ASSET ENTITY"
		[
			["105C31D", "Workstation B2600", "XX-232-YAB", "XX-232-YABB", "rackad1", "rackad11", "1", "11", "12",
			 attributeSet, cedarsProject, "Server", "C2A133", "ASD12345", "Mail", cedarsProject.client,
			 cedarsProjectMoveBundle1, cedarsGreenProjectTeam, cedarsRedProjectTeam, 1, "shelf1", 1],
			["105D74C CSMEDI", "7028-6C4", "XX-138-YAB", "XX-138-YABB", "rackad2", "rackad22", "2", "22", "12",
			 attributeSet, cedarsProject, "Server", "C2A134", "ASD2343455", "SAP", cedarsProject.client,
			 cedarsProjectMoveBundle1, cedarsGreenProjectTeam, cedarsRedProjectTeam, 2, "shelf1", 2],
			["AIX Console HMC3", "KVM", "MM-2232", "MM-22322", "rackad3", "rackad33", "4", "44", "1", attributeSet,
			 cedarsProject, "Server", "C2A135", "ASD1893045", "SAP", cedarsProject.client, cedarsProjectMoveBundle1,
			 cedarsGreenProjectTeam, cedarsRedProjectTeam, 6, "shelf1", 3],
			["105D74C CSMEDI", "AutoView 3100", "RR-32-YAB", "RR-32-YABB", "rackad4", "rackad44", "3", "33", "1",
			 attributeSet, cedarsProject, "KVM Switch", "C2A136", "ASD189234", "SAP", cedarsProject.client,
			 cedarsProjectMoveBundle1, cedarsGreenProjectTeam, cedarsRedProjectTeam, 5, "shelf1", 1],
			["CED14P", "Proliant 1600R", "RR-32-YAB", "RR-32-YABB", "rackad5", "rackad55", "6", "66", "5", attributeSet,
			 cedarsProject, "KVM Switch", "C2A137", "SU02325456", "SAP", cedarsProject.client, cedarsProjectMoveBundle1,
			 cedarsGreenProjectTeam, cedarsRedProjectTeam, 1, "shelf1", 1],
			["AIX Console HMC2", "V490", "RR-32-YAB", "RR-32-YABB", "rackad1", "rackad66", "7", "77", "5", attributeSet,
			 cedarsProject, "KVM Switch", "C2A138", "ASD1765454", "Mail", cedarsProject.client, cedarsProjectMoveBundle1,
			 cedarsGreenProjectTeam, cedarsRedProjectTeam, 1, "shelf1", 1],
			["AXPNTSA", "Proliant DL380 G3", "RR-32-YAB", "RR-32-YABB", "rackad2", "rackad77", "5", "55", "3", attributeSet,
			 cedarsProject, "KVM Switch", "ASD12345", "ASD1765454", "Mail", cedarsProject.client, cedarsProjectMoveBundle2,
			 cedarsGreenProjectTeam, cedarsRedProjectTeam, 1, "shelf1", 1],
			["CEDCONSOLE1", "StorageWorks", "RR-32-YAB", "RR-32-YABB", "rackad3", "rackad88", "8", "88", "6", attributeSet,
			 cedarsProject, "KVM Switch", "C2A140", "ASD2343455", "Mail", cedarsProject.client, cedarsProjectMoveBundle2,
			 cedarsGreenProjectTeam, cedarsRedProjectTeam, 1, "shelf1", 1],
			["CSEGP2 = CSENSD1 IO Drawer 1", "Ultrium Tape", "RR-32-YAB", "RR-32-YABB", "rackad4", "rackad99", "9", "99",
			 "7", attributeSet, cedarsProject, "KVM Switch", "C2A141", "SU0234423", "Mail", cedarsProject.client,
			 cedarsProjectMoveBundle2, cedarsGreenProjectTeam, cedarsRedProjectTeam, 1, "shelf1", 1]
		].each {
			save new AssetEntity(
					assetName: it[0],
					model: it[1],
					sourceLocation: it[2],
					targetLocation: it[3],
					sourceRack: it[4],
					targetRack: it[5],
					sourceRackPosition: it[6],
					targetRackPosition: it[7],
					attributeSet: it[9],
					project: it[10],
					assetType: it[11],
					assetTag: it[12],
					serialNumber: it[13],
					application: it[14],
					owner: it[15],
					moveBundle: it[16],
					cart: it[19],
					shelf: it[20],
					priority: it[21])
		}
		//--------------------------------
		// Create ProjectAssetMap for Development Process
		//--------------------------------
		println "PROJECT_ASSET_MAP"
		[
			[cedarsProject, AssetEntity.get(1), 10],
			[cedarsProject, AssetEntity.get(2), 60],
			[cedarsProject, AssetEntity.get(3), 60],
			[cedarsProject, AssetEntity.get(4), 10],
			[cedarsProject, AssetEntity.get(5), 150],
			[cedarsProject, AssetEntity.get(6), 60],
			[cedarsProject, AssetEntity.get(7), 150],
			[cedarsProject, AssetEntity.get(8), 60],
			[cedarsProject, AssetEntity.get(9), 10]
		].each {
			save new ProjectAssetMap(
					project: it[0],
					asset: it[1],
					currentStateId: it[2])
		}

		//--------------------------------
		// Create DataTransferSet
		//--------------------------------
		[
			// project, type, name, asset tag, s/n, AssetOwner
			["TDS Master Spreadsheet", "B", "/templates/TDSMaster_template.xls", "MASTER"],
			["TDS Walkthru", "B", "/templates/walkthrough_template.xls", "WALKTHROUGH"]
		].each {
			save new DataTransferSet(
					title: it[0],
					transferMode: it[1],
					templateFilename: it[2],
					setCode: it[3])
		}

		//--------------------------------
		// Create AssetComment
		//--------------------------------
		[
			["Switch powersupply to 220V", "instruction", 1, AssetEntity.get(1), personJohn],
			["Tape the SCSI cable to the server", "instruction", 1, AssetEntity.get(1), personJohn],
			["After move we should upgrade this", "comment", 0, AssetEntity.get(1), personJohn],
			["The server is going to moved right after the move so don't bother dressing the cabling.", "issue", 0, AssetEntity.get(2), personJohn]
		].each {
			save new AssetComment(
					comment: it[0],
					commentType: it[1],
					mustVerify: it[2],
					assetEntity: it[3],
					createdBy: it[4])
		}

		//		--------------------------------
		// Create MoveEventNews
		//--------------------------------
		[
			[moveEvent, 'The truck has just arrived', personJohn],
			[moveEvent, 'Customer backups are delaying more start', personJohn],
			[moveEvent, 'After move we should upgrade this', personJohn],
			[moveEvent, 'The server is going to moved right after the move', personJohn]
		].each {
			save new MoveEventNews(
					moveEvent: it[0],
					message: it[1],
					createdBy: it[2])
		}

		/*
		 * Getting Stream of object on AssetEntity_Attributes.xls and storing Stream as records in database
		 * using assetEntityAttributeLoaderService
		 */
		InputStream stream
		try {
			stream = servletContext.getResourceAsStream("/resource/AssetEntity_Attributes.xls")
		}
		catch (e) {
			println "exception while reading AssetEntity_Attributes file"

		}
		assetEntityAttributeLoaderService.uploadEavAttribute(stream)
	}

	private checkForBlacklistedVMParameters() {
		def blacklist = ["-Xnoclassgc"]

		def inputArguments = ManagementFactory.getRuntimeMXBean().getInputArguments()
		def blackListArgs = inputArguments.grep { arg ->
			return blacklist.any { el -> arg.contains(el) }
		}

		if (blackListArgs.size() > 0) {
			log.warn "*** WARNING ***\n BLACK LISTED ARGUMENTS FOUND IN THE CONFIGURATION!\n This can cause unexpected behaviour on the application, please check that you really want to do this: \n\t $blacklist\n***********************************************************"
		}
	}

	/** SOME LOAD TEST ***********/
	/**
	 * This test is to check the memory allocation and the PermGen, don't use in production
	 * related to: TM-4157 (https://support.transitionmanager.com/browse/TM-4157)
	 */
	private testMemoryAllocation() {
		//Use only in development mode
		if (Environment.current != Environment.DEVELOPMENT) return

		log.info "THREAD INICIALIZANDO!!!! LETS KILL THIS GUY"
		Thread.start {
			log.info "Generate a very Big MAP groovy String Code"
			def strMap = "["
			for (int i = 0; i < 1000; i++) {
				if (i > 0) {
					strMap += ","
				}
				strMap += "'$i':'value $i'"
			}
			strMap += "]"

			log.info "sz: ${strMap.size()}"

			for (int i = 10; i > 0; i--) {
				log.info "THREAD will start in T - $i seconds"
				Thread.sleep(1000)
			}

			for (int i = 0; i < 100000; i++) {
				//new Person(firstName:'Octavio', lastName:'Luna',title:'Dev')
				def daMap = Eval.me(strMap)
				Thread.sleep(50)
			}

			log.info "THREAD TERMINADO!"
		}
	}
}
