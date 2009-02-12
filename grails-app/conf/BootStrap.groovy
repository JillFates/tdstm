import org.jsecurity.crypto.hash.Sha1Hash;
class BootStrap {

     def init = { servletContext ->
     	
     	// Party And Person Details
     	 def person = new Person(firstName:'Lokanath',lastName:'Reddy',partyName:'Sigma',partyCreatedDate:new Date(),personCreatedDate:new Date(),active:'Y').save()
	    //   Administrator user and role.
     	 def adminRole = new RoleType(roleTypeCode: "Administrator").save()
	     def adminUser = new UserLogin(username: "admin", password: new Sha1Hash("admin").toHex(),person:person).save()
	   //  new JsecUserRoleRel(user: adminUser, role: adminRole).save()
     
	     // A normal user.

	     	def userRole = new RoleType(roleTypeCode: "User").save()
			def normalUser = new UserLogin(username: "lokanath", password: new Sha1Hash("lokanath").toHex(),person:person).save()
			//new JsecUserRoleRel(user: normalUser, role: userRole).save()
			new AssetType(assetType:"KVM").save();
			new AssetType(assetType:"KVM Switch").save();
			new AssetType(assetType:"Server").save();
			new AssetType(assetType:"Switch").save();
			new AssetType(assetType:"Array").save();
			new Project(projectName:"TDS",trackChanges:'Y').save();
     }
     def destroy = {
     }
} 