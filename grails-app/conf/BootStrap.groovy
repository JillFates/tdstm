import org.jsecurity.crypto.hash.Sha1Hash
class BootStrap {

    def init = { servletContext ->

println "\n\n ROLE TYPES \n\n" 		

		// -------------------------------
		// Role Types
		// The description now classifies groups of roles.  Eventually this will be implemented 
		// like ofBiz where there is a parent id.
		// -------------------------------
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
        teamRole.id ="APP_ROLE"
        teamRole.save( insert:true )

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

println "\n\n PARTY TYPES\n\n" 		

		// -------------------------------
		// Party Types
		// -------------------------------
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
		println "\n\n PARTY RELATIONSHIP TYPES\n\n" 		

		// -----------------------------------------
	    // Create PartyRelationshipType Details
		// -----------------------------------------
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

// Don't think we need this (John)		
//	    def projectRelaType = new PartyRelationshipType( description:"Project" )
//        projectRelaType.id = "PROJECT"
//        projectRelaType.save( insert:true )

	    def appRelaType = new PartyRelationshipType( description:"Application" )
        appRelaType.id = "APPLICATION"
        appRelaType.save( insert:true )

println "\n\nPERSONS\n\n" 		

		// -------------------------------
		// Persons
		// -------------------------------
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
			
println "\n\n USER DETAILS\n\n" 		

		// -------------------------------
		// Create User Details.
		// -------------------------------
	    def adminUserLisa = new UserLogin( person:personLisa, username: "lisa", password:new Sha1Hash("admin").toHex(), active:'Y'  ).save()
	    def userJohn = new UserLogin( person:personJohn, username: "john", password:new Sha1Hash("admin").toHex(), active:'Y' ).save()
	    def normalUserRalph = new UserLogin( person:personJimL, username:"ralph", password:new Sha1Hash("user").toHex(), active:'Y' ).save()

println "\n\n PARTY GROUPS \n\n" 		

		// -------------------------------
		// Create Party Group (Companies)
		// -------------------------------
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

		println "\n\n PROJECTS \n\n"

		// -------------------------------
		// Create Projects
		// -------------------------------
		def cedarsProject = new Project( name:"Cedars-Sinai Move 1", projectCode:'CS1', 
			description:'100 servers', trackChanges:'Y', partyType:groupPartyType ).save();
		def twProject = new Project( name:"Time Warner VA Move", projectCode:'TM-VA-1', 
			description:'500 servers', trackChanges:'N', partyType:groupPartyType ).save();

			
		// -------------------------------
		// Create Applications
		// -------------------------------
		def raiserApp = new Application(
			name: "Raiser's Edge", 
			appCode: "RE", environment:"Production", 
			owner: cedars).save()
		
println "\n\n USER PREFERENCES \n\n" 		

		// -------------------------------
		// Create default Preference
		// -------------------------------
		def johnPref = new UserPreference( value: cedarsProject.id )
		johnPref.userLogin = userJohn
		johnPref.preferenceCode = "CURR_PROJ"
		johnPref.save( insert: true)
		
println "\n\n PARTY ROLES \n\n" 		

		// -------------------------------
		// Create PartyRole Details
		// -------------------------------
	    def partyRoleForRalph = new PartyRole( party:personJimL, roleType:userRole ).save( insert:true )
	    def partyRoleForLisa = new PartyRole( party:personLisa, roleType:userRole ).save( insert:true )
	    def partyRoleForJohn = new PartyRole( party:personJohn, roleType:adminRole ).save( insert:true )
	    def projectAdminPartyRoleForJohn = new PartyRole( party:personJohn, roleType:projectAdminRole ).save( insert:true )

println "\n\n ASSET TYPES \n\n" 		

		// -------------------------------
		// Create AssetType records
		// -------------------------------
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

println "\n\n PARTY RELATIONSHIPS \n\n" 		

		// -------------------------------
	    // create Party Relationship
		// -------------------------------
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
			[ projStaffType, cedarsProject, projectRole, personAnna, personJimL ],

			// Application / staff relationships
			[ appRelaType, raiserApp, applicationRole, personNBonner, appOwnerRole ],
			[ appRelaType, raiserApp, applicationRole, personAMaslac, appSMERole ],
			[ appRelaType, raiserApp, applicationRole, personHKim, appPCRole ],
			[ appRelaType, raiserApp, applicationRole, personLCoronado, appSCRole ],

			// TimeWarner Relationships
			[ projCompanyType, twProject, projectRole, tds, companyRole ],
			[ projClientType, twProject, projectRole, timeWarner, clientRole ],
			[ projStaffType, twProject, projectRole, personTim, pmRole ],
			[ projStaffType, twProject, projectRole, personJohn, moveMgrRole ]
		]
		// Save all the rows in list
		def i = 0;
		pr.each {
		    println "row $i"
			i++
		    println "${it[0].id} : ${it[1].id} : ${it[2].id} : ${it[3].id} : ${it[4].id}" 
			
			new PartyRelationship( 
				partyRelationshipType: it[0], 
				partyIdFrom: it[1], 
				roleTypeCodeFrom: it[2],
				partyIdTo: it[3],
				roleTypeCodeTo: it[4]
				).save( insert:true )
		}

		def assets = [
			// project, type, name, asset tag, s/n
			[cedarsProject, serverAsset, "CSHSACADAFF", "XX-232-YAB", "12345"],
			[cedarsProject, serverAsset, "CSHSACCESS2", "XX-138-YAB", "2343455"],
			[cedarsProject, serverAsset, "CSHSBDGT1", "MM-2232", "1893045"],
			[cedarsProject, kvmSwitchAsset, "Avocent", "", ""],
			[cedarsProject, arrayAsset, "CSMCARM Juke", "RR-32-YAB", "SU023423LLK"]
		]
		// Insert the list 
		assets.each {
			def asset = new Asset(
				project: it[0],
				assetType: it[1],
				assetName: it[2],
				assetTag: it[3],
				serialNumber: it[4]
			)
			def ok = asset.validate()
			if (ok) ok = asset.save()
			if ( ok ) {
				// Create the association between these servers and the application
				new ApplicationAssetMap(application:raiserApp, asset:asset).save()
				
			} else {
				println "Asset Save failed : ${it[2]} " 
				asset.errors.allErrors.each { println it }
			} 
			
			
		}
	}

	def destroy = {
    }
}