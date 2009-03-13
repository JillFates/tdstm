class PartyRelationshipService {

    boolean transactional = true
    /*
     * method to save party Relationship
     */
    def savePartyRelationship( def relationshipType, def partyIdFrom, def roleTypeIdFrom, def partyIdTo, def roleTypeIdTo ) {
    	try{
			def partyRelationshipType = PartyRelationshipType.findById( relationshipType )
			def roleTypeFrom = RoleType.findById( roleTypeIdFrom )
			def roleTypeTo = RoleType.findById( roleTypeIdTo )
			
			def partyRelationship = new PartyRelationship( partyRelationshipType:partyRelationshipType, partyIdFrom:partyIdFrom, roleTypeCodeFrom:roleTypeFrom, partyIdTo:partyIdTo, roleTypeCodeTo:roleTypeTo, statusCode:"ENABLED" ).save( insert:true )
	
			return partyRelationship
    	} catch (Exception e) {
			println"Exception-------------->"+e
		}
    }
    /*
     *  Method to return Staff Company
     */
    
    def getSatffCompany( def staff ) {
        def company = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'STAFF' and p.partyIdTo = $staff.id and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'STAFF'")
        return company
    }
    /*
     *  Method will return Company Staff
     */
    def getCompanyStaff( def companyId ){
    	
    	def query = "from Person s where s.id in (select p.partyIdTo from PartyRelationship p where p.partyRelationshipType = 'STAFF' and p.partyIdFrom = $companyId and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'STAFF' ) "
    	def personInstanceList = Person.findAll( query )
    	
    	return personInstanceList
    }
    /*
     *  Return the Application staff
     */
    def getApplicationStaff( def companyId, def roleTypeTo ){
    	def query = "from Person s where s.id in (select p.partyIdTo from PartyRelationship p where p.partyRelationshipType = 'APPLICATION'  and p.partyIdFrom = $companyId  )"
    	def applicationCompaniesStaff = Person.findAll(query)
      
    	return applicationCompaniesStaff
    	
    }
   
    /*
     *  method to return list of companies
     */
    def getCompaniesList(){
    	
    	def companies = PartyGroup.findAll( " from PartyGroup p where p.partyType = 'COMPANY' " )
    	return companies
    
    }
    
    /*
     *  Method to Update  the roleTypeTo 
     */
    def updatePartyRelationshipRoleTypeTo( def relationshipType, def partyFrom, def roleTypeIdFrom, def partyTo, def roleTypeIdTo ){
    	if(roleTypeIdTo != null && roleTypeIdTo != ""){
    		def partyRelationship = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = '$relationshipType' and p.partyIdFrom = $partyFrom.id and p.partyIdTo = $partyTo.id and p.roleTypeCodeFrom = '$roleTypeIdFrom' and p.roleTypeCodeTo = '$roleTypeIdTo' ")
    		def partyRelationshipType = PartyRelationshipType.findById( relationshipType )
    		def roleTypeTo = RoleType.findById( roleTypeIdTo )
    		def roleTypeFrom = RoleType.findById( roleTypeIdFrom )
    		if ( partyRelationship == null ) {
    			def otherRole = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = '$relationshipType' and p.partyIdFrom = $partyFrom.id and p.partyIdTo = $partyTo.id and p.roleTypeCodeFrom = '$roleTypeIdFrom' ")
                if ( otherRole != null && otherRole != "" ) {
                    otherRole.delete()
                    def newPartyRelationship = new PartyRelationship( partyRelationshipType:partyRelationshipType, partyIdFrom:partyFrom, roleTypeCodeFrom:roleTypeFrom, partyIdTo:partyTo, roleTypeCodeTo:roleTypeTo, statusCode:"ENABLED" ).save( insert:true )
                } else {
                    def newPartyRelationship = new PartyRelationship( partyRelationshipType:partyRelationshipType, partyIdFrom:partyFrom, roleTypeCodeFrom:roleTypeFrom, partyIdTo:partyTo, roleTypeCodeTo:roleTypeTo, statusCode:"ENABLED" ).save( insert:true )
                }
    		}
    	} 
    	/*else {
    		def otherRole = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = '$relationshipType' and p.partyIdFrom = $partyFrom.id and p.partyIdTo = $partyTo.id and p.roleTypeCodeFrom = '$roleTypeIdFrom'")
    		if ( otherRole != null && otherRole != "" ) {
    			otherRole.delete()
            }
		}*/
    }
    /*
     *  Method to update PartyIdTo
     */
    def updatePartyRelationshipPartyIdTo( def relationshipType, def partyIdFrom, def roleTypeIdFrom, def partyIdTo, def roleTypeIdTo ){
    	if ( partyIdTo != "" && partyIdTo != null ){
    		def partyRelationship = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = '$relationshipType' and p.partyIdFrom = $partyIdFrom and p.partyIdTo = $partyIdTo and p.roleTypeCodeFrom = '$roleTypeIdFrom' and p.roleTypeCodeTo = '$roleTypeIdTo' ")
    		def partyTo = Party.findById( partyIdTo )
    		def partyFrom= Party.findById( partyIdFrom )
    		def partyRelationshipType = PartyRelationshipType.findById( relationshipType )
    		def roleTypeFrom = RoleType.findById( roleTypeIdFrom )
    		def roleTypeTo = RoleType.findById( roleTypeIdTo )
    		// condition to check whether partner has changed or not
    		if ( partyRelationship == null ) {
        		def otherRelationship = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = '$relationshipType' and p.partyIdFrom = $partyIdFrom  and p.roleTypeCodeFrom = '$roleTypeIdFrom' and p.roleTypeCodeTo = '$roleTypeIdTo' ")
                if ( otherRelationship != null && otherRelationship != "" ) {
                    //	Delete existing partner and reinsert new partner For Project, if partner changed
                    otherRelationship.delete()
                    def newPartyRelationship = new PartyRelationship( partyRelationshipType:partyRelationshipType, partyIdFrom:partyFrom, roleTypeCodeFrom:roleTypeFrom, partyIdTo:partyTo, roleTypeCodeTo:roleTypeTo, statusCode:"ENABLED" ).save( insert:true )
                } else {
                    // Create Partner if there is no partner for this project
                    def newPartyRelationship = new PartyRelationship( partyRelationshipType:partyRelationshipType, partyIdFrom:partyFrom, roleTypeCodeFrom:roleTypeFrom, partyIdTo:partyTo, roleTypeCodeTo:roleTypeTo, statusCode:"ENABLED" ).save( insert:true )
                }
    		}
    	} else {
    		//	if user select a blank then remove Partner
    		def otherRelationship = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = '$relationshipType' and p.partyIdFrom = $partyIdFrom  and p.roleTypeCodeFrom = '$roleTypeIdFrom' and p.roleTypeCodeTo = '$roleTypeIdTo' ")
            if ( otherRelationship != null && otherRelationship != "" ) {
            	otherRelationship.delete()
            }
    	}
    }
    
    /*
     *  Return the Project Staff
     */
    def getProjectStaff( def projectId ){
    	def list = []
    	def projectStaff = PartyRelationship.findAll("from PartyRelationship p where p.partyRelationshipType = 'PROJ_STAFF' and p.partyIdFrom = $projectId and p.roleTypeCodeFrom = 'PROJECT' ")
	    	projectStaff.each{staff ->
	    		def map = new HashMap()
	    		def company = PartyRelationship.findAll("from PartyRelationship p where p.partyRelationshipType = 'STAFF' and p.partyIdTo = $staff.partyIdTo.id and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'STAFF' ")
	    		map.put("company", company.partyIdFrom)
	    		map.put("name", staff.partyIdTo.firstName+" "+ staff.partyIdTo.lastName)
	    		map.put("role", staff.roleTypeCodeTo)
	    		map.put("staff", staff.partyIdTo)
	    		list<<map
	    	}
    	return list
    }
    /*
     * 	Return the Companies Staff list, which are not associated with Project 
     */
    def getProjectCompaniesStaff( def projectId ) {
    	def list = []
    	def projectCompanyQuery = "select pr.partyIdTo from PartyRelationship pr where pr.partyRelationshipType in ('PROJ_CLIENT','PROJ_COMPANY','PROJ_PARTNER','PROJ_VENDOR ') and pr.partyIdFrom = $projectId and pr.roleTypeCodeFrom = 'PROJECT'  "
    	def projectStaffQuery = "select ps.partyIdTo from PartyRelationship ps where ps.partyRelationshipType = 'PROJ_STAFF' and ps.partyIdFrom = $projectId and ps.roleTypeCodeFrom = 'PROJECT'"
    	def query = " from PartyRelationship p where p.partyRelationshipType = 'STAFF' and p.partyIdFrom in ( $projectCompanyQuery ) and p.partyIdTo not in ( $projectStaffQuery ) and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'STAFF' "
    	def projectCompaniesStaff = PartyRelationship.findAll(query)
    		projectCompaniesStaff.each{staff ->
    			def map = new HashMap()
    			def company = PartyRelationship.findAll("from PartyRelationship p where p.partyRelationshipType = 'STAFF' and p.partyIdTo = $staff.partyIdTo.id and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'STAFF' ")
	    		map.put("company", company.partyIdFrom)
	    		map.put("name", staff.partyIdTo.firstName+" "+ staff.partyIdTo.lastName)
	    		map.put("staff", staff.partyIdTo)
	    		list<<map
    		}
    	return list
    }
    /*
     *  Will return the Companies list associated with the Project
     */
    def getProjectCompanies( def projectId ){
    	def projectCompanyQuery = "from PartyRelationship pr where pr.partyRelationshipType in ('PROJ_CLIENT','PROJ_COMPANY','PROJ_PARTNER','PROJ_VENDOR ') and pr.partyIdFrom = $projectId and pr.roleTypeCodeFrom = 'PROJECT'  "
    	def projectCompanies = PartyRelationship.findAll(projectCompanyQuery)
    	return projectCompanies 
    }
}
