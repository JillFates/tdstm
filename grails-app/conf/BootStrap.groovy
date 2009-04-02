import org.jsecurity.crypto.hash.Sha1Hash
import com.tdssrc.eav.*
class BootStrap {
	def assetEntityAttributeLoaderService
    def init = { servletContext ->
    	
    	// -------------------------------
		// Role Types
		// The description now classifies groups of roles.  Eventually this will be implemented
		// like ofBiz where there is a parent id.
		// -------------------------------
		println "\n\n ROLE TYPES \n\n"
		def adminRole = new RoleType( description:"System : Administrator" )
		adminRole.id = "ADMIN"
		adminRole.save( insert:true )

		def userRole = new RoleType( description:"System : User" )
		userRole.id = "USER"
		userRole.save( insert:true )

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
		println "\n\n PARTY TYPES\n\n"
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
		println "\n\n PARTY RELATIONSHIP TYPES\n\n"
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
		println "\n\nPERSONS\n\n"
		def personJohn = new Person( firstName:'John', lastName:'Doherty', title:'Project Manager',
			partyType:personPartyType ).save()
		def personJimL = new Person( firstName:'Jim', lastName:'Laucher', title:'Tech',
			partyType:personPartyType ).save()
		def personLisa = new Person( firstName:'Lisa', lastName:'Angel', title:'Move Manager',
			partyType:personPartyType ).save()
		def personGeorge = new Person( firstName:'George', lastName:'Washington', title:'Move Manager',
			active:'N', partyType:personPartyType ).save()
		def personTim = new Person( firstName:'Tim', lastName:'Shutt', title:'Project Manager',
			partyType:personPartyType ).save()
		def personRobin = new Person( firstName:'Robin', lastName:'Banks', title:'Project Manager',
			partyType:personPartyType ).save()
		def personAnna = new Person( firstName:'Anna', lastName:'Graham',title:'Sys Opp',
			partyType:personPartyType ).save()
		def personReddy = new Person( firstName:'Lokanath', lastName:'Reddy',title:'Tech Lead',
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
		// Create User Details.
		// -------------------------------
		println "\n\n USER DETAILS\n\n"
		def adminUserLisa = new UserLogin( person:personLisa, username: "lisa", password:new Sha1Hash("admin").toHex(), active:'Y'  ).save()
		def userJohn = new UserLogin( person:personJohn, username: "john", password:new Sha1Hash("admin").toHex(), active:'Y' ).save()
		def normalUserRalph = new UserLogin( person:personJimL, username:"ralph", password:new Sha1Hash("user").toHex(), active:'Y' ).save()

		// -------------------------------
		// Create Party Group (Companies)
		// -------------------------------
		println "\n\n PARTY GROUPS \n\n"
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
		println "\n\n PROJECTS \n\n"
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
		println "\n\n MOVE BUNDLE\n\n"
		def cedarsProjectMoveBundle1 = new MoveBundle( project: cedarsProject, name: "Cedars Bundle1", startTime: new Date(), completionTime: new Date(), operationalOrder:1 ).save( insert:true )
		def cedarsProjectMoveBundle2 = new MoveBundle( project: cedarsProject, name: "Cedars Bundle2", startTime: new Date(), completionTime: new Date(), operationalOrder:1 ).save( insert:true )
		def cedarsProjectMoveBundle3 = new MoveBundle( project: cedarsProject, name: "Cedars Bundle3", startTime: new Date(), completionTime: new Date(), operationalOrder:1 ).save( insert:true )
		def cedarsProjectMoveBundle4 = new MoveBundle( project: cedarsProject, name: "Cedars Bundle4", startTime: new Date(), completionTime: new Date(), operationalOrder:1 ).save( insert:true )
		def twProjectMoveBundle = new MoveBundle( project: twProject, name: "TW Bundle", startTime: new Date(), completionTime: new Date(), operationalOrder:2 ).save( insert:true )
		
		// -------------------------------
		// Create ProjectTeam
		// -------------------------------
		println "\n\n PROJECT TEAM \n\n"
		def cedarsGreenProjectTeam = new ProjectTeam( name: "Cedars's Green Team",	teamCode: "Green", moveBundle:cedarsProjectMoveBundle1, dateCreated: new Date()).save()
		def cedarsRedProjectTeam = new ProjectTeam( name: "Cedars's Red Team",	teamCode: "Red", moveBundle:cedarsProjectMoveBundle1, dateCreated: new Date()).save()
		def twGreenProjectTeam = new ProjectTeam( name: "TM's Green Team",	teamCode: "Green", moveBundle:twProjectMoveBundle, dateCreated: new Date()).save()
		def twRedProjectTeam = new ProjectTeam( name: "TM's Red Team",	teamCode: "Red", moveBundle:twProjectMoveBundle, dateCreated: new Date()).save()


		// -------------------------------
		// Create default Preference
		// -------------------------------
		println "\n\n USER PREFERENCES \n\n"
		def johnPref = new UserPreference( value: cedarsProject.id )
		johnPref.userLogin = userJohn
		johnPref.preferenceCode = "CURR_PROJ"
		johnPref.save( insert: true)


		// -------------------------------
		// Create PartyRole Details
		// -------------------------------
		println "\n\n PARTY ROLES \n\n"
		def partyRoleForRalph = new PartyRole( party:personJimL, roleType:userRole ).save( insert:true )
		def partyRoleForLisa = new PartyRole( party:personLisa, roleType:userRole ).save( insert:true )
		def partyRoleForJohn = new PartyRole( party:personJohn, roleType:adminRole ).save( insert:true )
		def projectAdminPartyRoleForJohn = new PartyRole( party:personJohn, roleType:projectAdminRole ).save( insert:true )


		// -------------------------------
		// Create AssetType records
		// -------------------------------
		println "\n\n ASSET TYPES \n\n"
		def kvmAsset = new AssetType()
		kvmAsset.id = "KVM"
		kvmAsset.save( insert:true )

		def kvmSwitchAsset = new AssetType()
		kvmSwitchAsset.id = "KVM Switch"
		kvmSwitchAsset.save( insert:true )

		def serverAsset = new AssetType()
		serverAsset.id = "Server"
		serverAsset.save( insert:true )

		def switchAsset = new AssetType()
		switchAsset.id = "Switch"
		switchAsset.save( insert:true )

		def arrayAsset = new AssetType()
		arrayAsset.id = "Array"
		arrayAsset.save( insert:true )

		// -------------------------------
        // create Party Relationship
		// -------------------------------
		println "\n\n PARTY RELATIONSHIPS \n\n"
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
			[ staffType, emc, companyRole, personLisa, staffRole ],
			[ staffType, emc, companyRole, personRobin, staffRole ],
			[ staffType, sigma, companyRole, personReddy, staffRole ],
			[ staffType, cedars, companyRole, personGeorge, staffRole ],
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
			[ projStaffType, cedarsProject, projectRole, personGeorge, networkAdminRole ],
			[ projStaffType, cedarsProject, projectRole, personAnna, techRole ],
			[ projStaffType, cedarsProject, projectRole, personJimL, techRole ],

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
			[ teamType, cedarsRedProjectTeam, teamRole, personJohn, teamMemberRole ],
			[ teamType, twGreenProjectTeam, teamRole, personTim, teamMemberRole ],
			[ teamType, twGreenProjectTeam, teamRole, personJohn, teamMemberRole ]
		]
		// Save all the rows in list
		def i = 0;
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
/*
		def assets = [
			// project, type, name, asset tag, s/n, AssetOwner
			[cedarsProject, serverAsset, "CSHSACADAFF", "XX-232-YAB", "12345",cedars],
			[cedarsProject, serverAsset, "CSHSACCESS2", "XX-138-YAB", "2343455",cedars],
			[cedarsProject, serverAsset, "CSHSBDGT1", "MM-2232", "1893045",cedars],
			[cedarsProject, kvmSwitchAsset, "Avocent", "", "",cedars],
			[cedarsProject, arrayAsset, "CSMCARM Juke", "RR-32-YAB", "SU023423LLK",cedars]
		]
		// Insert the list
		// println "cedars"+cedars
		assets.each {
			def asset = new Asset(
				project: it[0],
				assetType: it[1],
				assetName: it[2],
				assetTag: it[3],
				serialNumber: it[4],
			    owner:it[5]
			)
			def ok = asset.validate()
			if (ok) ok = asset.save()
			if ( ok ) {
				// Create the association between these servers and the application
				new ApplicationAssetMap(application:raiserApp, asset:asset).save()

			} else {
				println "Asset save failed : ${it[2]} "
				asset.errors.allErrors.each { println it }
			}
		}
		*/
		//--------------------------------
		// Create EavEntityType and EavAttributeSet records
		//--------------------------------
		println "\n\n ENTITY TYPE & ATTRIBUTE SET \n\n"

		def entityType = new EavEntityType( entityTypeCode:'AssetEntity', domainName:'AssetEntity', isAuditable:1  ).save()

		// This line was causing RTE because table is not created
		def attributeSet = new EavAttributeSet( attributeSetName:'TDS Master List', entityType:entityType, sortOrder:10 ).save()
		//---------------------------------
		//  Create Asset Entity
		//---------------------------------
		def assetEntityList = [
		  			// project, type, name, asset tag, s/n, AssetOwner
		  			["105C31D", "Workstation B2600", "XX-232-YAB", "rackad1", "1", "12", attributeSet, cedarsProject, serverAsset, "CSHSACADAFF", "C2A133", "ASD12345", "Mail", cedarsProject.client],
		  			["105D74C CSMEDI","7028-6C4", "XX-138-YAB", "rackad2", "2", "12", attributeSet, cedarsProject, serverAsset, "CSHSACCESS2", "C2A134", "ASD2343455", "SAP", cedarsProject.client],
		  			["AIX Console HMC3", "KVM", "MM-2232", "rackad3", "4", "1", attributeSet, cedarsProject, serverAsset, "CSHSBDGT1", "C2A135", "ASD1893045", "SAP", cedarsProject.client],
		  			["105D74C CSMEDI", "AutoView 3100", "RR-32-YAB", "rackad4", "3", "1", attributeSet, cedarsProject, kvmSwitchAsset, "Avocent", "C2A136", "ASD189234", "SAP", cedarsProject.client],
		  			["CED14P", "Proliant 1600R", "RR-32-YAB", "rackad5", "6", "5", attributeSet, cedarsProject, arrayAsset, "CSMCARM Juke", "C2A137", "SU02325456", "SAP", cedarsProject.client],
		  			["AIX Console HMC2", "V490", "RR-32-YAB", "rackad1", "7", "5", attributeSet, cedarsProject, kvmSwitchAsset, "Avocent", "C2A138", "ASD1765454", "Mail", cedarsProject.client],
		  			["AXPNTSA", "Proliant DL380 G3", "RR-32-YAB", "rackad2", "5", "3", attributeSet, cedarsProject, serverAsset, "CSHSACADAFF", "C2A139", "ASD12345", "Mail", cedarsProject.client],
		  			["CEDCONSOLE1", "StorageWorks", "RR-32-YAB", "rackad3", "8", "6", attributeSet, cedarsProject, serverAsset, "CSHSACCESS2", "C2A140", "ASD2343455", "Mail", cedarsProject.client],
		  			["CSEGP2 = CSENSD1 IO Drawer 1", "Ultrium Tape", "RR-32-YAB", "rackad4", "9", "7", attributeSet, cedarsProject, arrayAsset, "CSMCARM Juke", "C2A141", "SU0234423", "Mail", cedarsProject.client]
		]
		
		// Insert the List of assetEntity
		assetEntityList.each {
			def assetEntity = new AssetEntity(
				serverName: it[0],
				model: it[1],
				sourceLocation: it[2],
				sourceRack: it[3],
				position: it[4],
				usize: it[5],
				attributeSet: it[6],
				project: it[7],
				assetType: it[8],
				assetName: it[9],
				assetTag: it[10],
				serialNumber: it[11],
				application: it[12],
				owner: it[13]
		
            ).save()
		}
		def assete = new AssetEntity(serverName:"CSHMC3", model:"AutoView 3100", room:"XX-232-YAB", rack:"rackad1", position:"1", uSize:"12", attributeSet:attributeSet)
		// -------------------------------
		// Create MoveBundleAsset Details
		// -------------------------------
        println "\n\n MOVE BUNDLE ASSET \n\n"
		def moveBundle1Asset = new MoveBundleAsset( moveBundle: cedarsProjectMoveBundle1, asset: AssetEntity.get(1),sourceTeam: cedarsGreenProjectTeam,targetTeam: cedarsRedProjectTeam ).save( insert:true )
		def moveBundle2Asset = new MoveBundleAsset( moveBundle: cedarsProjectMoveBundle1, asset: AssetEntity.get(2),sourceTeam: cedarsGreenProjectTeam,targetTeam: twGreenProjectTeam ).save( insert:true )
		def moveBundle3Asset = new MoveBundleAsset( moveBundle: cedarsProjectMoveBundle2, asset: AssetEntity.get(3),sourceTeam: cedarsGreenProjectTeam,targetTeam: twGreenProjectTeam ).save( insert:true )
		def moveBundle4Asset = new MoveBundleAsset( moveBundle: cedarsProjectMoveBundle3, asset: AssetEntity.get(4),sourceTeam: cedarsGreenProjectTeam,targetTeam: twGreenProjectTeam ).save( insert:true )
		def moveBundle5Asset = new MoveBundleAsset( moveBundle: cedarsProjectMoveBundle2, asset: AssetEntity.get(5),sourceTeam: cedarsGreenProjectTeam,targetTeam: twGreenProjectTeam ).save( insert:true )
		def moveBundle6Asset = new MoveBundleAsset( moveBundle: twProjectMoveBundle, asset: AssetEntity.get(3),sourceTeam: cedarsGreenProjectTeam,targetTeam: twGreenProjectTeam ).save( insert:true )

		
		//--------------------------------
		// Create DataTransferSet 
		//--------------------------------
		def dataTransferSet = new DataTransferSet( title:'TDS Master Spreadsheet', transferMode:'B' ).save()
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