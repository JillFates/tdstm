import org.jsecurity.crypto.hash.Sha1Hash
class BootStrap {

    def init = { servletContext ->
		//  Create PartyType records
		def empParty = new PartyType( description:"Employees" )
			empParty.id = "EMPLOYEE"
			empParty.save( insert:true )
	
	    def contractParty = new PartyType( description:"Contractors" )
			contractParty.id = "CONTRACT"
			contractParty.save( insert:true )
	
	    def consultParty = new PartyType( description:"Consultants" )
			consultParty.id = "CONSULTANT"
			consultParty.save( insert:true )

		// TODO : Correct the parameters (i.e. partyName no longer valid)
		// TODO : Create 3 people objects - no need to create parties due to inheritence.
		// create Person Details
		def personJohn = new Person( firstName:'John', lastName:'D', dateCreated:new Date(), active:'Y' ).save()
		def personRalph = new Person( firstName:'Ralph', lastName:'D', dateCreated:new Date(), active:'Y' ).save()
		def personLisa = new Person( firstName:'Lisa', lastName:'D', dateCreated:new Date(), active:'Y' ).save()
		def personGeorge = new Person( firstName:'George', lastName:'D', dateCreated:new Date(), active:'Y' ).save()

		// Create Projects
		// need to create parent(party,partyGroup) records also due to inheritence
		def acmeProject = new Project( dateCreated:new Date(), name:"Acme, Inc", projectCode:'ACME', description:'100 servers', trackChanges:'N' ).save();
		def marioProject = new Project( dateCreated:new Date(), name:"Mario Brothers Co.", projectCode:'MARIO', description:'500 servers', trackChanges:'N' ).save();

		// Create Party Group
		def tds = new PartyGroup( dateCreated:new Date(), name:"TDS" ).save()
		def emc = new PartyGroup( dateCreated:new Date(), name:"EMC" ).save()
		def trucks = new PartyGroup( dateCreated:new Date(), name:"TrucksRUs" ).save()

		// Create User Details.
	    def adminUserLisa = new UserLogin( person:personLisa, username: "lisa", password:new Sha1Hash("admin").toHex()  ).save()
	    def userJohn = new UserLogin( person:personJohn, username: "john", password:new Sha1Hash("admin").toHex() ).save()
	    def normalUserRalph = new UserLogin( person:personRalph, username:"ralph", password:new Sha1Hash("user").toHex() ).save()

		// TODO : Complete the logic here to assign 2 of the above people to user logins with ADMIN and USER roles
		// Notice that we are making the RoleType with code and description now
     	// create Roles.
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

		// Create PartyRole Details

	    def partyRoleForRalph = new PartyRole( party:personRalph, roleType:userRole ).save( insert:true )
	    def partyRoleForLisa = new PartyRole( party:personLisa, roleType:adminRole ).save( insert:true )
	    def partyRoleForJohn = new PartyRole( party:personJohn, roleType:adminRole ).save( insert:true )

	    // TODO : FIX INDENTATION!
		// Create AssetType records
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

	    // TODO : Create PartyRelationship Types (Partners, Vendors, Staff, Project) Note: the code should be CAPS
	    // Create PartyRelationshipType Details

		def appType = new PartyRelationshipType( description:"Application" )
        appType.id = "APPLICATION"
        appType.save( insert:true )

		def empType = new PartyRelationshipType( description:"Employment" )
        empType.id = "EMPLOYMENT"
        empType.save( insert:true )

		def teamType = new PartyRelationshipType( description:"Project Team" )
        teamType.id = "PROJ_TEAM"
        teamType.save( insert:true )

		def partnerType = new PartyRelationshipType( description:"Project Partner" )
        partnerType.id = "PROJ_PARTNER"
        partnerType.save( insert:true )

		def clientType = new PartyRelationshipType( description:"Project Client" )
        clientType.id = "PROJ_CLIENT"
        clientType.save( insert:true )

	    def vendorType = new PartyRelationshipType( description:"Vendors" )
        vendorType.id = "VENDORS"
        vendorType.save( insert:true )

	    def projectType = new PartyRelationshipType( description:"Project" )
        projectType.id = "PROJECT"
        projectType.save( insert:true )

		// TODO : Create some of the PartyRelationship associations as shown in Requiments Appedix A for PartyRelationship
	    // create Party Relationship

	    def tdsEmc = new PartyRelationship( partyRelationshipType:partnerType, partyIdFrom:tds, roleTypeCodeFrom:companyRole, partyIdTo:emc, roleTypeCodeTo:partnerRole, statusCode:"ENABLED" ).save( insert:true )
	    def tdsTrucks = new PartyRelationship( partyRelationshipType:vendorType, partyIdFrom:tds, roleTypeCodeFrom:companyRole, partyIdTo:trucks, roleTypeCodeTo:vendorRole, statusCode:"ENABLED" ).save( insert:true )
	    def tdsAcme = new PartyRelationship( partyRelationshipType:clientType, partyIdFrom:tds, roleTypeCodeFrom:companyRole, partyIdTo:acmeProject, roleTypeCodeTo:clientRole, statusCode:"ENABLED" ).save( insert:true )
	    def tdsJohn = new PartyRelationship( partyRelationshipType:empType, partyIdFrom:tds, roleTypeCodeFrom:employerRole, partyIdTo:personJohn, roleTypeCodeTo:pmRole, statusCode:"ENABLED" ).save( insert:true )
	    def tdsRalph = new PartyRelationship( partyRelationshipType:empType, partyIdFrom:tds, roleTypeCodeFrom:employerRole, partyIdTo:personRalph, roleTypeCodeTo:techRole, statusCode:"ENABLED" ).save( insert:true )
	    def emcLisa = new PartyRelationship( partyRelationshipType:empType, partyIdFrom:emc, roleTypeCodeFrom:employerRole, partyIdTo:personLisa, roleTypeCodeTo:pmRole, statusCode:"ENABLED" ).save( insert:true )
	    def acmeGeorge = new PartyRelationship( partyRelationshipType:empType, partyIdFrom:acmeProject, roleTypeCodeFrom:employerRole, partyIdTo:personGeorge, roleTypeCodeTo:networkAdminRole, statusCode:"ENABLED" ).save( insert:true )
	    def marioAcme = new PartyRelationship( partyRelationshipType:projectType, partyIdFrom:marioProject, roleTypeCodeFrom:projectRole, partyIdTo:acmeProject, roleTypeCodeTo:clientRole, statusCode:"ENABLED" ).save( insert:true )
	    def marioEmc = new PartyRelationship( partyRelationshipType:projectType, partyIdFrom:marioProject, roleTypeCodeFrom:projectRole, partyIdTo:emc, roleTypeCodeTo:pmRole, statusCode:"ENABLED" ).save( insert:true )
	    def marioJohn = new PartyRelationship( partyRelationshipType:projectType, partyIdFrom:marioProject, roleTypeCodeFrom:projectRole, partyIdTo:personJohn, roleTypeCodeTo:moveMgrRole, statusCode:"ENABLED" ).save( insert:true )
	    def marioRalph = new PartyRelationship( partyRelationshipType:projectType, partyIdFrom:marioProject, roleTypeCodeFrom:projectRole, partyIdTo:personRalph, roleTypeCodeTo:techRole, statusCode:"ENABLED" ).save( insert:true )

	}

	def destroy = {
    }
}