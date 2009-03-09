class PartyRelationshipService {

    boolean transactional = true
    /*
     * method to save party Relationship
     */
    def savePartyRelationship( def relationshipType, def partyIdFrom, def roleTypeIdFrom, def partyIdTo, def roleTypeIdTo ) {
    	
		def partyRelationshipType = PartyRelationshipType.findById( relationshipType )
		def roleTypeFrom = RoleType.findById( roleTypeIdFrom )
		def roleTypeTo = RoleType.findById( roleTypeIdTo )
		
		def partyRelationship = new PartyRelationship( partyRelationshipType:partyRelationshipType, partyIdFrom:partyIdFrom, roleTypeCodeFrom:roleTypeFrom, partyIdTo:partyIdTo, roleTypeCodeTo:roleTypeTo, statusCode:"ENABLED" ).save( insert:true )

		return partyRelationship
    }
    /*
     *  Method to return Staff Company
     */
    
    def getSatffCompany( def staff ) {
        def company = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'STAFF' and p.partyIdTo = $staff.id and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'STAFF'")
        return company
    }
    /*
     *  method to return list of companies
     */
    def getCompaniesList(){
    	
    	def companies = PartyGroup.findAll( " from PartyGroup p where p.partyType = 'COMPANY' " )
    	return companies
    
    }
    
    /*
     *  Method to Update Staff Company
     */
    def updateStaffCompany( def staff, def companyId ){
    	
    	if ( companyId != "" && companyId != null ){
    		
    		def staffCompanyRel = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'STAFF' and p.partyIdFrom = $companyId and p.partyIdTo = $staff.id and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'STAFF' ")
    		def companyParty = Party.findById( companyId )
    		def relationshipType = PartyRelationshipType.findById( "STAFF" )
    		def roleTypeTo = RoleType.findById( "STAFF" )
    		def roleTypeFrom = RoleType.findById( "COMPANY" )
    		// condition to check whether partner has changed or not
    		if ( staffCompanyRel == null ) {
        		def otherCompany = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'STAFF' and p.partyIdTo = $staff.id and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'STAFF' ")
                if ( otherCompany != null && otherCompany != "" ) {
                    //	Delete existing partner and reinsert new partner For Project, if partner changed
                    otherCompany.delete()
                    def staffRel = new PartyRelationship( partyRelationshipType:relationshipType, partyIdFrom:companyParty, roleTypeCodeFrom:roleTypeFrom, partyIdTo:staff, roleTypeCodeTo:roleTypeTo, statusCode:"ENABLED" ).save( insert:true )
                } else {
                	
                    // Create Partner if there is no partner for this project
                    def staffRel = new PartyRelationship( partyRelationshipType:relationshipType, partyIdFrom:companyParty, roleTypeCodeFrom:roleTypeFrom, partyIdTo:staff, roleTypeCodeTo:roleTypeTo, statusCode:"ENABLED" ).save( insert:true )
                }
    		}
    	} else {
    		//	if user select a blank then remove Partner
    		def otherCompany = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'STAFF' and p.partyIdTo = $staff.id and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'STAFF' ")
            if ( otherCompany != null && otherCompany != "" ) {
            	otherCompany.delete()
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
    	
    	def projectCompanyQuery = "select pr.partyIdTo from PartyRelationship pr where pr.partyRelationshipType in ('PROJ_CLIENT','PROJ_COMPANY','PROJ_PARTNER','PROJ_VENDOR ') and pr.partyIdFrom = $projectId and pr.roleTypeCodeFrom = 'PROJECT'  "
    	def projectStaffQuery = "select ps.partyIdTo from PartyRelationship ps where ps.partyRelationshipType = 'PROJ_STAFF' and ps.partyIdFrom = $projectId and ps.roleTypeCodeFrom = 'PROJECT'"
    	def query = " from PartyRelationship p where p.partyRelationshipType = 'STAFF' and p.partyIdFrom in ( $projectCompanyQuery ) and p.partyIdTo not in ( $projectStaffQuery ) and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'STAFF' "
    	def projectCompaniesStaff = PartyRelationship.findAll(query)
    	
    	return projectCompaniesStaff
    }
}
