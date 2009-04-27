import org.jsecurity.crypto.hash.Sha1Hash
import com.tdssrc.eav.*
import com.tdssrc.grails.GormUtil

import java.text.SimpleDateFormat

class BootStrap {
	def assetEntityAttributeLoaderService
    def init = { servletContext ->
    	
    	// -------------------------------
		// Role Types
		// The description now classifies groups of roles.  Eventually this will be implemented
		// like ofBiz where there is a parent id.
		// -------------------------------
		println "ROLE TYPES "
		def adminRole = new RoleType( description:"System : Administrator" )
		adminRole.id = "ADMIN"
		adminRole.save( insert:true )

		def userRole = new RoleType( description:"System : User" )
		userRole.id = "USER"
		userRole.save( insert:true )

		def workStationRole = new RoleType( description:"System : Work Station" )
		workStationRole.id = "WORKSTATION"
		workStationRole.save( insert:true )

		def clientRole = new RoleType( description:"Party : Client" )
		clientRole.id = "CLIENT"
		clientRole.save( insert:true )

		def partnerRole = new RoleType( description:"Party : Partner" )
		partnerRole.id = "PARTNER"
		partnerRole.save( insert:true )

		def staffRole = new RoleType( description:"Party : Staff" )
		staffRole.id = "STAFF"
		staffRole.save( insert:true )

		def companyRole = new RoleType( description:"Party : Company" )
		companyRole.id ="COMPANY"
		companyRole.save( insert:true )

		def vendorRole = new RoleType( description:"Party : Vendor" )
		vendorRole.id = "VENDOR"
		vendorRole.save( insert:true )

		def projectRole = new RoleType( description:"Party : Project" )
		projectRole.id ="PROJECT"
		projectRole.save( insert:true )

		def applicationRole = new RoleType( description:"Party : Application" )
		applicationRole.id ="APP_ROLE"
		applicationRole.save( insert:true )

		def teamRole = new RoleType( description:"Party : Team" )
		teamRole.id ="TEAM"
		teamRole.save( insert:true )

		def teamMemberRole = new RoleType( description:"Team : Team Member" )
		teamMemberRole.id ="TEAM_MEMBER"
		teamMemberRole.save( insert:true )

		def techRole = new RoleType( description:"Staff : Technician" )
		techRole.id = "TECH"
		techRole.save( insert:true )

		def pmRole = new RoleType( description:"Staff : Project Manager" )
		pmRole.id = "PROJ_MGR"
		pmRole.save( insert:true )

		def moveMgrRole = new RoleType( description:"Staff : Move Manager" )
		moveMgrRole.id = "MOVE_MGR"
		moveMgrRole.save( insert:true )

		def sysAdminRole = new RoleType( description:"Staff : System Administrator" )
		sysAdminRole.id = "SYS_ADMIN"
		sysAdminRole.save( insert:true )

		def dbAdminRole = new RoleType( description:"Staff : Database Administrator" )
		dbAdminRole.id = "DB_ADMIN"
		dbAdminRole.save( insert:true )

		def networkAdminRole = new RoleType( description:"Staff : Network Administrator" )
		networkAdminRole.id = "NETWORK_ADMIN"
		networkAdminRole.save( insert:true )

		def accountMgrRole = new RoleType( description:"Staff : Account Manager" )
		accountMgrRole.id = "ACCT_MGR"
		accountMgrRole.save( insert:true )

		def projectAdminRole = new RoleType( description:"Staff : Project Administrator" )
		projectAdminRole.id = "PROJECT_ADMIN"
		projectAdminRole.save( insert:true )

		def appOwnerRole = new RoleType( description:"App : Application Owner" )
		appOwnerRole.id = "APP_OWNER"
		appOwnerRole.save( insert:true )

		def appSMERole = new RoleType( description:"App : Subject Matter Expert" )
		appSMERole.id = "APP_SME"
		appSMERole.save( insert:true )

		def appPCRole = new RoleType( description:"App : Primary Contact" )
		appPCRole.id = "APP_1ST_CONTACT"
		appPCRole.save( insert:true )

		def appSCRole = new RoleType( description:"App : Secondary Contact" )
		appSCRole.id = "APP_2ND_CONTACT"
		appSCRole.save( insert:true )
		
		def bundleRole = new RoleType( description:"Proj: Move Bundle" )
		bundleRole.id = "MOVE_BUNDLE"
		bundleRole.save( insert:true )
		
		// -------------------------------
		// Party Types
		// -------------------------------
		println "PARTY TYPES"
		def personPartyType = new PartyType( description:"Person" )
		personPartyType.id = "PERSON"
		personPartyType.save( insert:true )

		def groupPartyType = new PartyType( description:"PartyGroup" )
		groupPartyType.id = "PARTY_GROUP"
		groupPartyType.save( insert:true )

		def companyType = new PartyType( description:"Company" )
		companyType.id = "COMPANY"
		companyType.save( insert:true )

		def projectType = new PartyType( description:"Project" )
		projectType.id = "PROJECT"
		projectType.save( insert:true )

		def appType = new PartyType( description:"Application" )
		appType.id = "APP_TYPE"
		appType.save( insert:true )

		// -----------------------------------------
		// Create PartyRelationshipType Details
		// -----------------------------------------
		println "PARTY RELATIONSHIP TYPES"
		def staffType = new PartyRelationshipType( description:"Staff" )
		staffType.id = "STAFF"
		staffType.save( insert:true )

		def projStaffType = new PartyRelationshipType( description:"Project Staff" )
		projStaffType.id = "PROJ_STAFF"
		projStaffType.save( insert:true )

		def projCompanyType = new PartyRelationshipType( description:"Project Company" )
		projCompanyType.id = "PROJ_COMPANY"
		projCompanyType.save( insert:true )

		def teamType = new PartyRelationshipType( description:"Project Team" )
		teamType.id = "PROJ_TEAM"
		teamType.save( insert:true )

		def projPartnerType = new PartyRelationshipType( description:"Project Partner" )
		projPartnerType.id = "PROJ_PARTNER"
		projPartnerType.save( insert:true )

		def projClientType = new PartyRelationshipType( description:"Project Client" )
		projClientType.id = "PROJ_CLIENT"
		projClientType.save( insert:true )

		def clientType = new PartyRelationshipType( description:"Clients" )
		clientType.id = "CLIENTS"
		clientType.save( insert:true )

		def partnerType = new PartyRelationshipType( description:"Partners" )
		partnerType.id = "PARTNERS"
		partnerType.save( insert:true )

		def vendorType = new PartyRelationshipType( description:"Vendors" )
		vendorType.id = "VENDORS"
		vendorType.save( insert:true )
		
		def projBundleStaffType = new PartyRelationshipType( description:"Bundle Staff" )
		projBundleStaffType.id = "PROJ_BUNDLE_STAFF"
		projBundleStaffType.save( insert:true )

        // Don't think we need this (John)
        //	    def projectRelaType = new PartyRelationshipType( description:"Project" )
        //        projectRelaType.id = "PROJECT"
        //        projectRelaType.save( insert:true )

		def appRelaType = new PartyRelationshipType( description:"Application" )
		appRelaType.id = "APPLICATION"
		appRelaType.save( insert:true )

		// -------------------------------
		// Persons
		// -------------------------------
		println "PERSONS"
		def personJohn = new Person( firstName:'John', lastName:'Doherty', title:'Project Manager',
			partyType:personPartyType ).save()
		def personJimL = new Person( firstName:'Jim', lastName:'Laucher', title:'Tech',
			partyType:personPartyType ).save()
		def personLisa = new Person( firstName:'Lisa', lastName:'Carr', title:'Move Manager',
			partyType:personPartyType ).save()
		def personGenePoole = new Person( firstName:'Gene', lastName:'Poole', title:'Move Manager',
			active:'N', partyType:personPartyType ).save()
		def personTim = new Person( firstName:'Tim', lastName:'Shutt', title:'Project Manager',
			partyType:personPartyType ).save()
		def personRobin = new Person( firstName:'Robin', lastName:'Banks', title:'Project Manager',
			partyType:personPartyType ).save()
		def personAnna = new Person( firstName:'Anna', lastName:'Graham',title:'Sys Opp',
			partyType:personPartyType ).save()
		def personReddy = new Person( firstName:'Lokanath', lastName:'Reddy',title:'Tech Lead',
			partyType:personPartyType ).save()
		def personBrock = new Person( firstName:'Brock', lastName:'Lee',title:'Tech',
			partyType:personPartyType ).save()
		def personTransport = new Person( firstName:'Tran', lastName:'Sport',title:'Transport',
			partyType:personPartyType ).save()

		// This person account will actual share 3 logins (move tech, clean tech, and mover)
		def personWorkStation = new Person( firstName:'Work', lastName:'Station', title:'Work Station User',
			partyType:personPartyType ).save()	
		
		// Cedars staff for Raiser's Edge application
		def personNBonner = new Person( firstName:'Nancy', lastName:'Bonner', title:'',
			partyType:personPartyType  ).save()
		def personAMaslac = new Person( firstName:'Alan', lastName:'Maslac', title:'',
			partyType:personPartyType  ).save()
		def personHKim = new Person( firstName:'Hongki', lastName:'Kim', title:'',
			partyType:personPartyType  ).save()
		def personLCoronado = new Person( firstName:'Leo', lastName:'Coronado', title:'',
			partyType:personPartyType  ).save()

		// -------------------------------
		// Create User Login.
		// -------------------------------
		println "USER LOGIN"
		def adminUserLisa = new UserLogin( person:personLisa, username: "lisa", password:new Sha1Hash("admin").toHex(), active:'Y'  ).save()
		def userJohn = new UserLogin( person:personJohn, username: "john", password:new Sha1Hash("admin").toHex(), active:'Y' ).save()
		def normalUserRalph = new UserLogin( person:personJimL, username:"ralph", password:new Sha1Hash("user").toHex(), active:'Y' ).save()

		// Create the Move Tech, Cleaning Tech and Mover (keep short for barcode)
		def userMoveTech = new UserLogin( person:personWorkStation, username:"mt", password:new Sha1Hash("xyzzy").toHex(), active:'Y' ).save()
		def userCleanTech = new UserLogin( person:personWorkStation, username:"ct", password:new Sha1Hash("xyzzy").toHex(), active:'Y' ).save()
		def userMover = new UserLogin( person:personWorkStation, username:"mv", password:new Sha1Hash("xyzzy").toHex(), active:'Y' ).save()

		// -------------------------------
		// Create Party Group (Companies)
		// -------------------------------
		println "PARTY GROUPS "
		def tds = new PartyGroup( name:"TDS", partyType:companyType ).save()
		def emc = new PartyGroup( name:"EMC", partyType:companyType ).save()
		def timeWarner = new PartyGroup( name:"Time Warner", partyType:companyType ).save()
		def cedars = new PartyGroup( name:"Cedars-Sinai", partyType:companyType ).save()
		def sigma = new PartyGroup( name:"SIGMA", partyType:companyType ).save()
		def trucks = new PartyGroup( name:"TrucksRUs", partyType:companyType ).save()

		// -------------------------------
		// Create PartyRole for Companies
		// -------------------------------
		def companies = [tds, emc, timeWarner, cedars, sigma, trucks]
		companies.each {
			new PartyRole( party: it, roleType: companyRole).save(insert: true)
		}

		// -------------------------------
		// Create Projects
		// -------------------------------
		println "PROJECTS "
		def cedarsProject = new Project( name:"Cedars-Sinai Move 1", projectCode:'CS1', client:cedars,
			description:'100 servers', trackChanges:'Y', partyType:groupPartyType ).save();
		def twProject = new Project( name:"Time Warner VA Move", projectCode:'TM-VA-1', client:timeWarner,
			description:'500 servers', trackChanges:'N', partyType:groupPartyType ).save();


		// -------------------------------
		// Create Applications
		// -------------------------------
		def raiserApp = new Application(
			name: "Raiser's Edge",
			appCode: "RE", environment:"Production",
			owner: cedars).save()


		// -------------------------------
		// Create MoveBundle Details
		// -------------------------------
		println "MOVE BUNDLE"
		def cedarsProjectMoveBundle1 = new MoveBundle( project: cedarsProject, name: "Bundle 1",
				startTime: new Date(), completionTime: new Date()+1, operationalOrder:1 ).save( insert:true )
		def cedarsProjectMoveBundle2 = new MoveBundle( project: cedarsProject, name: "Bundle 2",
				startTime: new Date()+1, completionTime: new Date()+2, operationalOrder:1 ).save( insert:true )
		def cedarsProjectMoveBundle3 = new MoveBundle( project: cedarsProject, name: "Bundle 3",
				startTime: new Date()+2, completionTime: new Date()+3, operationalOrder:1 ).save( insert:true )
		def cedarsProjectMoveBundle4 = new MoveBundle( project: cedarsProject, name: "Bundle 4",
				startTime: new Date()+3, completionTime: new Date()+4, operationalOrder:1 ).save( insert:true )
		def twProjectMoveBundle = new MoveBundle( project: twProject, name: "TW Bundle",
				startTime: new Date()+12, completionTime: new Date()+15, operationalOrder:2 ).save( insert:true )
		
		// -------------------------------
		// Create ProjectTeam
		// -------------------------------
		println "PROJECT TEAM"
		def cedarsGreenProjectTeam = new ProjectTeam( name: "MoveTeam 1",	teamCode: "1", moveBundle:cedarsProjectMoveBundle1 ).save()
		def cedarsRedProjectTeam = new ProjectTeam( name: "MoveTeam 2",	teamCode: "2", moveBundle:cedarsProjectMoveBundle1 ).save()
		def cedarsCleanProjectTeam = new ProjectTeam( name: "Cleaning",	teamCode: "Cleaning", moveBundle:cedarsProjectMoveBundle1 ).save()
		def cedarsTransportProjectTeam = new ProjectTeam( name: "Transport",	teamCode: "Transport", moveBundle:cedarsProjectMoveBundle1 ).save()
		def twGreenProjectTeam = new ProjectTeam( name: "MoveTeam 1",	teamCode: "1", moveBundle:twProjectMoveBundle ).save()
		def twRedProjectTeam = new ProjectTeam( name: "MoveTeam 2",	teamCode: "2", moveBundle:twProjectMoveBundle ).save()

		// -------------------------------
		// Create default Preference
		// -------------------------------
		println "USER PREFERENCES"
		def johnPref = new UserPreference( value: cedarsProject.id )
		johnPref.userLogin = userJohn
		johnPref.preferenceCode = "CURR_PROJ"
		johnPref.save( insert: true)

		// -------------------------------
		// Create PartyRole Details
		// -------------------------------
		println "PARTY ROLES"
		new PartyRole( party:personJimL, roleType:userRole ).save( insert:true )
		new PartyRole( party:personLisa, roleType:userRole ).save( insert:true )
		new PartyRole( party:personJohn, roleType:adminRole ).save( insert:true )
		new PartyRole( party:personJohn, roleType:projectAdminRole ).save( insert:true )
		new PartyRole( party:personWorkStation, roleType:workStationRole).save( insert:true )

		// -------------------------------
        // create Party Relationship
		// -------------------------------
		println "PARTY RELATIONSHIPS "
		// Save all the rows in list
		def i = 0;
		def pr = [
			// Partners, Clients and Vendors
			[ partnerType, tds, companyRole, emc, partnerRole ],
			[ partnerType, tds, companyRole, sigma, partnerRole ],
			[ vendorType, tds, companyRole, trucks, vendorRole ],
			[ clientType, tds, companyRole, cedars, clientRole ],
			[ clientType, tds, companyRole, timeWarner, clientRole ],

			// Staff
			[ staffType, tds, companyRole, personJohn, staffRole ],
			[ staffType, tds, companyRole, personTim, staffRole ],
			[ staffType, tds, companyRole, personJimL, staffRole ],
			[ staffType, tds, companyRole, personAnna, staffRole ],
			[ staffType, tds, companyRole, personBrock, staffRole ],
			[ staffType, tds, companyRole, personTransport, staffRole ],
			[ staffType, emc, companyRole, personLisa, staffRole ],
			[ staffType, emc, companyRole, personRobin, staffRole ],
			[ staffType, sigma, companyRole, personReddy, staffRole ],
			[ staffType, cedars, companyRole, personGenePoole, staffRole ],
			[ staffType, cedars, companyRole, personNBonner, staffRole ],
			[ staffType, cedars, companyRole, personAMaslac, staffRole ],
			[ staffType, cedars, companyRole, personHKim, staffRole ],
			[ staffType, cedars, companyRole, personLCoronado, staffRole ],

			// cedars-Sinai Relationships
			[ projCompanyType, cedarsProject, projectRole, tds, companyRole ],
			[ projClientType, cedarsProject, projectRole, cedars, clientRole ],
			[ projPartnerType, cedarsProject, projectRole, emc, partnerRole ],
			// Project Staff roles
			[ projStaffType, cedarsProject, projectRole, personRobin, pmRole ],
			[ projStaffType, cedarsProject, projectRole, personJohn, moveMgrRole ],
			[ projStaffType, cedarsProject, projectRole, personGenePoole, networkAdminRole ],
			[ projStaffType, cedarsProject, projectRole, personAnna, techRole ],
			[ projStaffType, cedarsProject, projectRole, personJimL, techRole ],
			[ projStaffType, cedarsProject, projectRole, personBrock, techRole ],
			[ projStaffType, cedarsProject, projectRole, personTransport, techRole ],

			// Application / staff relationships
			[ appRelaType, raiserApp, applicationRole, personNBonner, appOwnerRole ],
			[ appRelaType, raiserApp, applicationRole, personAMaslac, appSMERole ],
			[ appRelaType, raiserApp, applicationRole, personHKim, appPCRole ],
			[ appRelaType, raiserApp, applicationRole, personLCoronado, appSCRole ],

			// TimeWarner Relationships
			[ projCompanyType, twProject, projectRole, tds, companyRole ],
			[ projClientType, twProject, projectRole, timeWarner, clientRole ],
			[ projStaffType, twProject, projectRole, personTim, pmRole ],
			[ projStaffType, twProject, projectRole, personJohn, moveMgrRole ],

			// Project Team Relationships with staff
			[ teamType, cedarsGreenProjectTeam, teamRole, personJimL, teamMemberRole ],
			[ teamType, cedarsGreenProjectTeam, teamRole, personAnna, teamMemberRole ],
			[ teamType, cedarsRedProjectTeam, teamRole, personJimL, teamMemberRole ],
			[ teamType, cedarsCleanProjectTeam, teamRole, personBrock, teamMemberRole ],
			[ teamType, cedarsTransportProjectTeam, teamRole, personTransport, teamMemberRole ],
			[ teamType, cedarsRedProjectTeam, teamRole, personJohn, teamMemberRole ],
			[ teamType, twGreenProjectTeam, teamRole, personTim, teamMemberRole ],
			[ teamType, twGreenProjectTeam, teamRole, personJohn, teamMemberRole ]
		]
		pr.each {
			// println "row $i"
			i++
			// println "${it[0].id} : ${it[1].id} : ${it[2].id} : ${it[3].id} : ${it[4].id}"

			new PartyRelationship(
				partyRelationshipType: it[0],
				partyIdFrom: it[1],
				roleTypeCodeFrom: it[2],
				partyIdTo: it[3],
				roleTypeCodeTo: it[4]
            ).save( insert:true )
		}

		//--------------------------------
		// Create EavEntityType and EavAttributeSet records
		//--------------------------------
		println "ENTITY TYPE & ATTRIBUTE SET"

		def entityType = new EavEntityType( entityTypeCode:'AssetEntity', domainName:'AssetEntity', isAuditable:1  ).save()

		// This line was causing RTE because table is not created
		def attributeSet = new EavAttributeSet( attributeSetName:'Server', entityType:entityType, sortOrder:10 ).save()
		//---------------------------------
		//  Create Asset Entity
		//---------------------------------
		def assetEntityList = [
		  			// project, type, name, asset tag, s/n, AssetOwner
		  			["105C31D", "Workstation B2600", "XX-232-YAB", "XX-232-YABB", "rackad1", "rackad11", "1", "11", "12", attributeSet, cedarsProject, "Server", "C2A133", "ASD12345", "Mail", cedarsProject.client, cedarsProjectMoveBundle1, cedarsGreenProjectTeam, cedarsRedProjectTeam, 1, "shelf1" ],
		  			["105D74C CSMEDI","7028-6C4", "XX-138-YAB", "XX-138-YABB", "rackad2", "rackad22", "2", "22",  "12", attributeSet, cedarsProject, "Server", "C2A134", "ASD2343455", "SAP", cedarsProject.client, cedarsProjectMoveBundle1, cedarsGreenProjectTeam, cedarsRedProjectTeam, 2, "shelf1" ],
		  			["AIX Console HMC3", "KVM", "MM-2232", "MM-22322", "rackad3", "rackad33", "4", "44", "1", attributeSet, cedarsProject, "Server", "C2A135", "ASD1893045", "SAP", cedarsProject.client, cedarsProjectMoveBundle1, cedarsGreenProjectTeam, cedarsRedProjectTeam, 6, "shelf1" ],
		  			["105D74C CSMEDI", "AutoView 3100", "RR-32-YAB", "RR-32-YABB", "rackad4", "rackad44", "3", "33", "1", attributeSet, cedarsProject, "KVM Switch", "C2A136", "ASD189234", "SAP", cedarsProject.client, cedarsProjectMoveBundle1, cedarsGreenProjectTeam, cedarsRedProjectTeam, 5, "shelf1" ],
		  			["CED14P", "Proliant 1600R", "RR-32-YAB", "RR-32-YABB", "rackad5", "rackad55", "6", "66", "5", attributeSet, cedarsProject, "KVM Switch", "C2A137", "SU02325456", "SAP", cedarsProject.client, cedarsProjectMoveBundle1, cedarsGreenProjectTeam, cedarsRedProjectTeam, 1,"shelf1"],
		  			["AIX Console HMC2", "V490", "RR-32-YAB", "RR-32-YABB", "rackad1", "rackad66", "7", "77", "5", attributeSet, cedarsProject, "KVM Switch", "C2A138", "ASD1765454", "Mail", cedarsProject.client, cedarsProjectMoveBundle1, cedarsGreenProjectTeam, cedarsRedProjectTeam, 1,"shelf1"],
		  			["AXPNTSA", "Proliant DL380 G3", "RR-32-YAB", "RR-32-YABB", "rackad2", "rackad77", "5", "55", "3", attributeSet, cedarsProject, "KVM Switch", "ASD12345",  "ASD1765454", "Mail", cedarsProject.client, cedarsProjectMoveBundle2, cedarsGreenProjectTeam, cedarsRedProjectTeam, 1,"shelf1"],
		  			["CEDCONSOLE1", "StorageWorks", "RR-32-YAB", "RR-32-YABB", "rackad3", "rackad88", "8", "88", "6", attributeSet, cedarsProject, "KVM Switch", "C2A140", "ASD2343455", "Mail", cedarsProject.client, cedarsProjectMoveBundle2, cedarsGreenProjectTeam, cedarsRedProjectTeam, 1,"shelf1"],
		  			["CSEGP2 = CSENSD1 IO Drawer 1", "Ultrium Tape", "RR-32-YAB", "RR-32-YABB", "rackad4", "rackad99", "9", "99", "7", attributeSet, cedarsProject, "KVM Switch", "C2A141", "SU0234423", "Mail", cedarsProject.client, cedarsProjectMoveBundle2, cedarsGreenProjectTeam, cedarsRedProjectTeam, 1,"shelf1"]
		]

		// Insert the List of assetEntity
		assetEntityList.each {
			def assetEntity = new AssetEntity(
				assetName: it[0],
				model: it[1],
				sourceLocation: it[2],
				targetLocation: it[3],
				sourceRack: it[4],
				targetRack: it[5],
				sourceRackPosition: it[6],
				targetRackPosition: it[7],
				usize: it[8],
				attributeSet: it[9],
				project: it[10],
				assetType: it[11],
				assetTag: it[12],
				serialNumber: it[13],
				application: it[14],
				owner: it[15],
				moveBundle: it[16],
				sourceTeam: it[17],
				targetTeam: it[18],
				cart:it[19],
				shelf:it[20]
            )
			if ( ! assetEntity.validate() || ! assetEntity.save() ) {
				def etext = "Unable to create asset ${it[0]}" +
					 GormUtil.allErrorsString( assetEntity )
				println etext
				log.error( etext )
			}
		}
		//def asset = new AssetEntity(serverName:"CSHMC3", model:"AutoView 3100", room:"XX-232-YAB", rack:"rackad1", position:"1", uSize:"12", attributeSet:attributeSet)
		// -------------------------------
		// Create MoveBundleAsset Details
		// -------------------------------
		/*println "MOVE BUNDLE ASSET"
		def moveBundle1Asset = new MoveBundleAsset( moveBundle: cedarsProjectMoveBundle1, asset: AssetEntity.get(1),sourceTeam: cedarsGreenProjectTeam,targetTeam: cedarsRedProjectTeam,cart : 1,shelf: 2 ).save( insert:true )
		def moveBundle2Asset = new MoveBundleAsset( moveBundle: cedarsProjectMoveBundle1, asset: AssetEntity.get(2),sourceTeam: cedarsGreenProjectTeam,targetTeam: twGreenProjectTeam,cart : 2,shelf: 3 ).save( insert:true )
		def moveBundle3Asset = new MoveBundleAsset( moveBundle: cedarsProjectMoveBundle2, asset: AssetEntity.get(3),sourceTeam: cedarsGreenProjectTeam,targetTeam: twGreenProjectTeam,cart : 6,shelf: 4 ).save( insert:true )
		def moveBundle4Asset = new MoveBundleAsset( moveBundle: cedarsProjectMoveBundle3, asset: AssetEntity.get(4),sourceTeam: cedarsGreenProjectTeam,targetTeam: twGreenProjectTeam,cart : 5,shelf: 8 ).save( insert:true )
		def moveBundle5Asset = new MoveBundleAsset( moveBundle: cedarsProjectMoveBundle2, asset: AssetEntity.get(5),sourceTeam: cedarsGreenProjectTeam,targetTeam: twGreenProjectTeam,cart : 3,shelf: 9 ).save( insert:true )
		def moveBundle6Asset = new MoveBundleAsset( moveBundle: twProjectMoveBundle, asset: AssetEntity.get(3),sourceTeam: cedarsGreenProjectTeam,targetTeam: twGreenProjectTeam,cart : 1,shelf: 7 ).save( insert:true )*/

		
		//--------------------------------
		// Create DataTransferSet 
		//--------------------------------
		def dataTransferSetList = [
	    // project, type, name, asset tag, s/n, AssetOwner
		    ["TDS Master Spreadsheet", "B", "/templates/TDSMaster_template.xls", "MASTER" ],
		    ["TDS Walkthru", "B", "/templates/walkthrough_template.xls", "WALKTHROUGH" ]
		     ]
		                      
		// Insert the List of DataTransferSet
		dataTransferSetList.each {
		    def dataTransferSet = new DataTransferSet(
		    title: it[0],
		    transferMode: it[1],
		    templateFilename: it[2],
		    setCode: it[3]
		  ).save()
		}
		
		//--------------------------------
		// Create AssetComment 
		//--------------------------------
		def assetCommentList = [
		  			
		  			["Master Spreadsheet comments", "instruction", 0, AssetEntity.get(1)],
		  			["Walkthru comments", "issue", 0, AssetEntity.get(2) ]
		]
		//Insert the List of AssetComment
		assetCommentList.each {
			def assetComment = new AssetComment(
				comment: it[0],
				commentType: it[1],
				mustVerify: it[2],
				assetEntity: it[3]
            ).save()
		}
		/*
		 * Getting Stream of object on AssetEntity_Attributes.xls and storing Stream as records in database 
		 * using assetEntityAttributeLoaderService
		 */
		InputStream stream
		try {
			stream = servletContext.getResourceAsStream("/resource/AssetEntity_Attributes.xls")
                
        } catch (Exception ex) {
            println "exception while reading AssetEntity_Attributes file"
            
        }
        assetEntityAttributeLoaderService.uploadEavAttribute(stream)


	}

	def destroy = {
	}
}