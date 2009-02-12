import org.jsecurity.crypto.hash.Sha1Hash;
class BootStrap {

    def init = { servletContext ->
     	
     	// Party And Person Details
		// TODO : Correct the parameters (i.e. partyName no longer valid)
		def person = new Person(firstName:'Lokanath',lastName:'Reddy',partyName:'Sigma',partyCreatedDate:new Date(),personCreatedDate:new Date(),active:'Y').save()
	    //   Administrator user and role.
		
		// TODO : Create 3 people objects - no need to create parties due to inheritence.
		
		// TODO : Complete the logic here to assign 2 of the above people to user logins with ADMIN and USER roles	
		// Notice that we are making the RoleType with code and description now
		
     	def adminRole = new RoleType( id:"ADMIN", description:"Administrator" ).save()
	    def adminUser = new UserLogin(username: "admin", password: new Sha1Hash("admin").toHex(),person:person).save()
		new PartyRole(party: adminUser, roleType: adminRole).save
		 
		//  new JsecUserRoleRel(user: adminUser, role: adminRole).save()
     
	    // A normal user.

	    def userRole = new RoleType( id:"USER", description:"User" ).save()
			
		// TODO : FIX INDENTATION!
		
		def normalUser = new UserLogin(username: "lokanath", password: new Sha1Hash("lokanath").toHex(),person:person).save()
			//new JsecUserRoleRel(user: normalUser, role: userRole).save()
			
			// Create AssetType records
			new AssetType(id:"KVM").save();
			new AssetType(id:"KVM Switch").save();
			new AssetType(id:"Server").save();
			new AssetType(id:"Switch").save();
			new AssetType(id:"Array").save();
			
			// Create Projects
			new Project( name:"Acme, Inc", description:'100 servers', trackChanges:'N').save();
			new Project( name:"Mario Brothers Co.", description:'500 servers', trackChanges:'N').save();
		
		// TODO : Create PartyRelationship Types (Partners, Vendors, Staff, Project) Note: the code should be CAPS
		
		// TODO : Create some of the PartyRelationship associations as shown in Requiments Appedix A for PartyRelationship

	}
    
	def destroy = {
    }
} 