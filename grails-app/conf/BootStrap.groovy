import org.jsecurity.crypto.hash.Sha1Hash
class BootStrap {

    def init = { servletContext ->

println "\n\n ROLE TYPES \n\n" 		

		// -------------------------------
		// Role Types
		// -------------------------------
		// Notice that we are making the RoleType with code and description now
	    def adminRole = new RoleType( description:"Administrator" )
        adminRole.id = "ADMIN"
        adminRole.save( insert:true )
        
		def userRole = new RoleType( description:"User" )
        userRole.id = "USER"
        userRole.save( insert:true )

	    def clientRole = new RoleType( description:"Client" )
        clientRole.id = "CLIENT"
        clientRole.save( insert:true )

	    def partnerRole = new RoleType( description:"Partner" )
        partnerRole.id = "PARTNER"
        partnerRole.save( insert:true )

	    def staffRole = new RoleType( description:"Staff" )
        staffRole.id = "STAFF"
        staffRole.save( insert:true )

	    def companyRole = new RoleType( description:"Company" )
        companyRole.id ="COMPANY"
        companyRole.save( insert:true )

	    def projectRole = new RoleType( description:"Project" )
        projectRole.id ="PROJECT"
        projectRole.save( insert:true )

	    def employerRole = new RoleType( description:"Employer" )
        employerRole.id ="EMPLOYER"
        employerRole.save( insert:true )

	    def vendorRole = new RoleType( description:"Vendor" )
        vendorRole.id = "VENDOR"
        vendorRole.save( insert:true )

	    def techRole = new RoleType( description:"Technician" )
        techRole.id = "TECH"
        techRole.save( insert:true )

	    def pmRole = new RoleType( description:"Project Manager" )
        pmRole.id = "PROJ_MGR"
        pmRole.save( insert:true )

	    def moveMgrRole = new RoleType( description:"Move Manager" )
        moveMgrRole.id = "MOVE_MGR"
        moveMgrRole.save( insert:true )

	    def sysAdminRole = new RoleType( description:"System Administrator" )
        sysAdminRole.id = "SYS_ADMIN"
        sysAdminRole.save( insert:true )

	    def dbAdminRole = new RoleType( description:"Database Administrator" )
        dbAdminRole.id = "DB_ADMIN"
        dbAdminRole.save( insert:true )

	    def networkAdminRole = new RoleType( description:"Network Administrator" )
        networkAdminRole.id = "NETWORK_ADMIN"
        networkAdminRole.save( insert:true )

	    def accountMgrRole = new RoleType( description:"Account Manager" )
        accountMgrRole.id = "ACCT_MGR"
        accountMgrRole.save( insert:true )

	    def appOwnerRole = new RoleType( description:"Application Owner" )
        appOwnerRole.id = "APP_OWNER "
        appOwnerRole.save( insert:true )

	    def projectAdminRole = new RoleType( description:"Project Administrator" )
		projectAdminRole.id = "PROJECT_ADMIN"
		projectAdminRole.save( insert:true )

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
	
println "\n\n PARTY RELATIONSHIP TYPES\n\n" 		

		// -----------------------------------------
	    // Create PartyRelationshipType Details
		// -----------------------------------------
		def appType = new PartyRelationshipType( description:"Application" )
        appType.id = "APPLICATION"
        appType.save( insert:true )

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

	    def projectType = new PartyRelationshipType( description:"Project" )
        projectType.id = "PROJECT"
        projectType.save( insert:true )

println "\n\nPERSONS\n\n" 		

		// -------------------------------
		// Persons
		// -------------------------------
		def personJohn = new Person( firstName:'John', lastName:'Doherty', title:'Project Manager', 
			dateCreated:new Date(), active:'Y', partyType:personPartyType ).save()
		def personRalph = new Person( firstName:'Ralph', lastName:'King', title:'Move Manager', 
			dateCreated:new Date(), active:'Y', partyType:personPartyType  ).save()
		def personLisa = new Person( firstName:'Lisa', lastName:'Angel', title:'Move Manager', 
			dateCreated:new Date(), active:'Y', partyType:personPartyType  ).save()
		def personGeorge = new Person( firstName:'George', lastName:'Washington', title:'Move Manager', 
			dateCreated:new Date(), active:'Y', partyType:personPartyType  ).save()
		def personTim = new Person( firstName:'Tim', lastName:'Shutt', title:'Project Manager', 
			dateCreated:new Date(), active:'Y', partyType:personPartyType  ).save()
		def personRobin = new Person( firstName:'Robin', lastName:'Banks', title:'Project Manager', 
			dateCreated:new Date(), active:'Y', partyType:personPartyType  ).save()
		def personAnna = new Person( firstName:'Anna', lastName:'Graham',title:'Logistics Coordinator', 
			dateCreated:new Date(), active:'Y', partyType:personPartyType  ).save()
		def personReddy = new Person( firstName:'Lokanath', lastName:'Reddy',title:'Tech Lead', 
			dateCreated:new Date(), active:'Y', partyType:personPartyType  ).save()

println "\n\n USER DETAILS\n\n" 		

		// -------------------------------
		// Create User Details.
		// -------------------------------
	    def adminUserLisa = new UserLogin( person:personLisa, username: "lisa", password:new Sha1Hash("admin").toHex()  ).save()
	    def userJohn = new UserLogin( person:personJohn, username: "john", password:new Sha1Hash("admin").toHex() ).save()
	    def normalUserRalph = new UserLogin( person:personRalph, username:"ralph", password:new Sha1Hash("user").toHex() ).save()

println "\n\n PARTY GROUPS \n\n" 		

		// -------------------------------
		// Create Party Group (Companies)
		// -------------------------------
		def tds = new PartyGroup( dateCreated:new Date(), name:"TDS", partyType:groupPartyType ).save()
		def emc = new PartyGroup( dateCreated:new Date(), name:"EMC", partyType:groupPartyType ).save()
		def timeWarner = new PartyGroup( dateCreated:new Date(), name:"Time Warner", partyType:groupPartyType ).save()
		def ceders = new PartyGroup( dateCreated:new Date(), name:"Ceders-Sinai", partyType:groupPartyType ).save()		
		def sigma = new PartyGroup( dateCreated:new Date(), name:"SIGMA", partyType:groupPartyType ).save()
		def trucks = new PartyGroup( dateCreated:new Date(), name:"TrucksRUs", partyType:groupPartyType ).save()
		
println "\n\n PROJECTS \n\n" 		

		// -------------------------------
		// Create Projects
		// -------------------------------
		def cedersProject = new Project( dateCreated:new Date(), name:"Ceders-Sinai Move 1", projectCode:'CS1', 
			description:'100 servers', trackChanges:'Y', partyType:groupPartyType ).save();
		def twProject = new Project( dateCreated:new Date(), name:"Time Warner VA Move", projectCode:'TM-VA-1', 
			description:'500 servers', trackChanges:'N', partyType:groupPartyType ).save();

println "\n\n USER PREFERENCES \n\n" 		

		// -------------------------------
		// Create default Preference
		// -------------------------------
		def johnPref = new UserPreference( value: cedersProject.id )
		johnPref.userLogin = userJohn
		johnPref.preferenceCode = "CURR_PROJ"
		johnPref.save( insert: true)
		
println "\n\n PARTY ROLES \n\n" 		

		// -------------------------------
		// Create PartyRole Details
		// -------------------------------
	    def partyRoleForRalph = new PartyRole( party:personRalph, roleType:userRole ).save( insert:true )
	    def partyRoleForLisa = new PartyRole( party:personLisa, roleType:adminRole ).save( insert:true )
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
			[ clientType, tds, companyRole, ceders, clientRole ],
			[ clientType, tds, companyRole, timeWarner, clientRole ],
		   
			// Staff
			[ staffType, tds, companyRole, personJohn, staffRole ],
			[ staffType, tds, companyRole, personTim, staffRole ],
			[ staffType, tds, companyRole, personRalph, staffRole ],
			[ staffType, emc, companyRole, personLisa, staffRole ],
			[ staffType, emc, companyRole, personRobin, staffRole ],
			[ staffType, emc, companyRole, personAnna, staffRole ],
			[ staffType, sigma, companyRole, personReddy, staffRole ],
			[ staffType, ceders, companyRole, personGeorge, staffRole ],
			

			// Ceders-Sinai Relationships
			[ projCompanyType, cedersProject, projectRole, tds, companyRole ],
			[ projClientType, cedersProject, projectRole, ceders, clientRole ],
			[ projPartnerType, cedersProject, projectRole, emc, partnerRole ],
			[ projStaffType, cedersProject, projectRole, personRobin, pmRole ],
			[ projStaffType, cedersProject, projectRole, personJohn, moveMgrRole ],
			[ projStaffType, cedersProject, projectRole, personGeorge, networkAdminRole ],
					
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
			[cedersProject, serverAsset, "CSHSACADAFF", "XX-232-YAB", "12345"],
			[cedersProject, serverAsset, "CSHSACCESS2", "XX-138-YAB", "2343455"],
			[cedersProject, serverAsset, "CSHSBDGT1", "MM-2232", "1893045"],
			[cedersProject, kvmSwitchAsset, "Avocent", "", ""],
			[cedersProject, arrayAsset, "CSMCARM Juke", "RR-32-YAB", "SU023423LLK"]
		]
		// Insert the list 
		assets.each {
			def a = new Asset(
				project: it[0],
				assetType: it[1],
				assetName: it[2],
				assetTag: it[3],
				serialNumber: it[4]
			)
			def ok = a.validate()
			if (ok) ok = a.save()
			if ( ! ok ) {
				println "Save failed : ${it[2]} " 
				a.errors.allErrors.each { println it }
			} 
		}
	}

	def destroy = {
    }
}