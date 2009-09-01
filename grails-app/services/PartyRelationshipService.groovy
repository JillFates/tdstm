import java.text.DateFormat
import java.text.SimpleDateFormat
import jxl.*
import jxl.write.*
import jxl.read.biff.*
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
    	def query = "from Person s where s.id in (select p.partyIdTo from PartyRelationship p where p.partyRelationshipType = 'APPLICATION'  and p.partyIdFrom = $companyId and p.roleTypeCodeTo = '$roleTypeTo' )"
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
     *  Method to update PartyIdFrom
     */
    def updatePartyRelationshipPartyIdFrom( def relationshipType, def partyIdFrom, def roleTypeIdFrom, def partyIdTo, def roleTypeIdTo ){
    	def partyRelationship = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = '$relationshipType' and p.partyIdTo = $partyIdTo.id and p.roleTypeCodeFrom = '$roleTypeIdFrom' and p.roleTypeCodeTo = '$roleTypeIdTo' ")
    	def newPartyRelationship
		def partyRelationshipType = PartyRelationshipType.findById( relationshipType )
		def roleTypeFrom = RoleType.findById( roleTypeIdFrom )
		def roleTypeTo = RoleType.findById( roleTypeIdTo )
    	if(partyRelationship == null){
            newPartyRelationship = new PartyRelationship( partyRelationshipType:partyRelationshipType, partyIdFrom:partyIdFrom, roleTypeCodeFrom:roleTypeFrom, partyIdTo:partyIdTo, roleTypeCodeTo:roleTypeTo, statusCode:"ENABLED" ).save( insert:true )
    	}else{
    		partyRelationship.delete()
            newPartyRelationship = new PartyRelationship( partyRelationshipType:partyRelationshipType, partyIdFrom:partyIdFrom, roleTypeCodeFrom:roleTypeFrom, partyIdTo:partyIdTo, roleTypeCodeTo:roleTypeTo, statusCode:"ENABLED" ).save( insert:true )
    		
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
     *  Method to create string list
     */
    def createString( def teamMembers ){
    	def team = new StringBuffer()
    	if(teamMembers){
	    	def teamSize = teamMembers.size()
	    	for (int i = 0; i < teamSize; i++) {
	    		if(i != teamSize -1){
	    			team.append("'"+teamMembers[i]+"',")
	    		}else{
	    			team.append("'"+teamMembers[i]+"'")
	    		}
	    	}
    	}
    	return team
    }
    /*
     *  Return the Project Available Staff
     */
    def getAvailableProjectStaff( def projectId, def teamMembers ){
    	def list = []
    	def query
    	if(teamMembers){
            def team = createString( teamMembers )
            query = "from PartyRelationship p where p.partyRelationshipType = 'PROJ_STAFF' and p.partyIdFrom = $projectId and p.roleTypeCodeFrom = 'PROJECT' and p.partyIdTo not in ( $team ) "
    	} else {
            query = "from PartyRelationship p where p.partyRelationshipType = 'PROJ_STAFF' and p.partyIdFrom = $projectId and p.roleTypeCodeFrom = 'PROJECT' "
    	}
    	def projectStaff = PartyRelationship.findAll( query )
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
     *  Return the Project Team Staff
     */
    def getProjectTeamStaff( def projectId, def teamMembers ){
    	def list = []
    	def query
    	if(teamMembers){
            def team = createString( teamMembers )
            query = "from PartyRelationship p where p.partyRelationshipType = 'PROJ_STAFF' and p.partyIdFrom = $projectId and p.roleTypeCodeFrom = 'PROJECT' and p.partyIdTo in ( $team ) "
            def projectStaff = PartyRelationship.findAll( query )
	    	projectStaff.each{staff ->
	    		def map = new HashMap()
	    		def company = PartyRelationship.findAll("from PartyRelationship p where p.partyRelationshipType = 'STAFF' and p.partyIdTo = $staff.partyIdTo.id and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'STAFF' ")
	    		map.put("company", company.partyIdFrom)
	    		map.put("name", staff.partyIdTo.firstName+" "+ staff.partyIdTo.lastName)
	    		map.put("role", staff.roleTypeCodeTo)
	    		map.put("staff", staff.partyIdTo)
	    		list<<map
	    	}
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
    /*
     *	Method to Create Project Team Members 
     */
    def createBundleTeamMembers( def projectTeam, def teamMembers ){
    	teamMembers.each{teamMember->
    		def personParty = Party.findById( teamMember )
    		def projectTeamRel = savePartyRelationship( "PROJ_TEAM", projectTeam, "TEAM", personParty, "TEAM_MEMBER" )
    	}
    	
    }
    /*
     *  Method will return the Project Team Members
     */
    def getBundleTeamMembers( def bundleTeam ){
    	def list = []
    	def query = "from PartyRelationship p where p.partyRelationshipType = 'PROJ_TEAM' and p.partyIdFrom = $bundleTeam.id and p.roleTypeCodeFrom = 'TEAM'  "
        def teamMembers = PartyRelationship.findAll(query)
        teamMembers.each{team ->
            def map = new HashMap()
            def company = PartyRelationship.findAll("from PartyRelationship p where p.partyRelationshipType = 'STAFF' and p.partyIdTo = $team.partyIdTo.id and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'STAFF' ")
            map.put("company", company.partyIdFrom)
            map.put("name", team.partyIdTo.firstName+" "+ team.partyIdTo.lastName)
            map.put("role", team.roleTypeCodeTo)
            map.put("staff", team.partyIdTo)
            list<<map
        }
        return list 
    }
    /*
     *  Method will return the party last name of members
     */
    def getBundleTeamMembersDashboard( def bundleTeamId ){
    	def query = "from PartyRelationship p where p.partyRelationshipType = 'PROJ_TEAM' and p.partyIdFrom = $bundleTeamId and p.roleTypeCodeFrom = 'TEAM'  "
        def teamMembers = PartyRelationship.findAll(query)
        def name = new StringBuffer()
        teamMembers.each{team ->
            name.append(team.partyIdTo.firstName.charAt(0))
            name.append(".")
            name.append(team.partyIdTo.lastName)
            name.append("/")
        }
        return name 
    }
    
  
    /*
     *  Return the Staff which are not assign to projectTeam
     */
    def getAvailableTeamMembers( def projectId, def projectTeam ){
    	def list = []
    	def query = "from PartyRelationship p where p.partyRelationshipType = 'PROJ_STAFF' and p.partyIdFrom = $projectId and p.roleTypeCodeFrom = 'PROJECT' and p.partyIdTo not in ( select pt.partyIdTo from PartyRelationship pt where pt.partyRelationshipType = 'PROJ_TEAM' and pt.partyIdFrom = $projectTeam.id and pt.roleTypeCodeFrom = 'TEAM' ) " 
    	def projectStaff = PartyRelationship.findAll( query )
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
     *  Return the Staff which are assign to ProjectTeam
     */
    def getBundleTeamInstanceList( def bundleInstance ){
    	def list = []
    	def bundleTeamList = ProjectTeam.findAllByMoveBundle( bundleInstance )
    	bundleTeamList.each{bundleTeam->
    		def map = new HashMap()
    		map.put("projectTeam",bundleTeam)
    		map.put("teamMembers",getBundleTeamMembers(bundleTeam))
    		list<<map
    	}
    	return list
    }
    /*
     * 	Return the PartyIdTo Details from PartyRelationship
     */
    def getPartyToRelationship( def partyRelationshipType, def partyIdFrom, def roleTypeFrom, def roleTypeTo ){
    	def partyToRelationship = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = '$partyRelationshipType' and p.partyIdFrom = $partyIdFrom and p.roleTypeCodeFrom = '$roleTypeFrom' and p.roleTypeCodeTo = '$roleTypeTo' ")
    	return partyToRelationship
    }
    /*-----------------------------------------------------------------
     * To Return the List of TeamMembersNames as a complete String to display in Reports
     * @author Srinivas
     * @param teamId
     * @return concat of  all teammemberNames 
     *---------------------------------------------------------------*/
    def getTeamMemberNames(def teamId )
    {
    	def roleTypeCodeTo ="TEAM_MEMBER"
    	def roleTypeInstance = RoleType.findById('TEAM_MEMBER')
    	def teamMembers = PartyRelationship.findAll(" from PartyRelationship pr where pr.partyIdFrom = $teamId and pr.roleTypeCodeTo = 'TEAM_MEMBER' ")
        def memberNames = new StringBuffer()
        teamMembers.each{team ->
        	memberNames.append(team.partyIdTo.firstName +" "+ team.partyIdTo.lastName)
            memberNames.append("/")
        }
    	if(memberNames.size() > 0) {
    		memberNames = memberNames.delete(memberNames.size()-1,memberNames.size())
    	}
    	return memberNames
    }
    /*------------------------------------------
     * @To get the TeamMembers List for a Team
     * @author Srinivas
     * @param teamId
     * @return list of TeamMembers 
     *-------------------------------------------*/
    def getTeamMembers(def teamId )
    {
    	def roleTypeCodeTo ="TEAM_MEMBER"
    	def roleTypeInstance = RoleType.findById('TEAM_MEMBER')
    	def teamMembers = PartyRelationship.findAll(" from PartyRelationship pr where pr.partyRelationshipType = 'PROJ_TEAM' and pr.roleTypeCodeFrom ='TEAM' and pr.partyIdFrom = $teamId and pr.roleTypeCodeTo = 'TEAM_MEMBER' ")
    	return teamMembers
    }
    /*--------------------------------------------------
     * To convert Date time into mm/dd/yy format
     * @author srinivas
     * @param 
     *---------------------------------------------------*/
     def convertDate(def date) {
    	 Date dt = date
    		String dtStr = dt.getClass().getName().toString();
    		String dtParam = dt.toString();	
    		DateFormat formatter ; 
    		formatter = new SimpleDateFormat("MM/dd/yy");
    		dtParam = formatter.format(dt);		
    		/* if null or any plain string */
    		if (dtParam != "null") {
    			dtParam = dtParam.trim();
    		}
    		return dtParam
 	}
     /*-------------------------------------------------------
      *  Return the Projectmanagers 
      *  @author srinivas
      *  @param projectId
      *-------------------------------------------------------*/
     def getProjectManagers( def projectId ){
    	 def list = []
    	 def projectManagers = PartyRelationship.findAll("from PartyRelationship p where p.partyRelationshipType = 'PROJ_STAFF' and p.partyIdFrom = $projectId and p.roleTypeCodeTo = 'PROJ_MGR' ")
    	 def managerNames = new StringBuffer()
    	 projectManagers.each{staff ->
    	 	managerNames.append(staff.partyIdTo.firstName+" "+ staff.partyIdTo.lastName)
    	 	managerNames.append(",")
    	 }
    	 if(managerNames.size() > 0) {
    		 managerNames = managerNames.delete(managerNames.size()-1,managerNames.size())
    	 }   
    	 return managerNames
     }
     /*-------------------------------------------------------
      *  TO Add the Title Info to MasterSpreadSheet
      *  @author srinivas
      *  @param Title Information as a Map and Workbook Sheet Object
      *-------------------------------------------------------*/
     def exportTitleInfo(def titleInfoMap,def titleSheet){
    	 def sheetContent
    	 def row=1;
    	 for (Object key: titleInfoMap.keySet()) {
    		 sheetContent = new Label(0,row,key)
    		 titleSheet.addCell(sheetContent)
    		 sheetContent = new Label(1,row,titleInfoMap.get(key).toString())
    		 titleSheet.addCell(sheetContent)
    		 row+=1;
    	 }
     }
}

