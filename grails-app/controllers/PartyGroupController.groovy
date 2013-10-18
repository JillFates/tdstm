import grails.converters.JSON
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.HtmlUtil

class PartyGroupController {
    
	def partyRelationshipService
    def userPreferenceService
	def jdbcTemplate
	
    def index = { redirect(action:list,params:params) }

    // the delete, save and update actions only accept POST requests
    def allowedMethods = [delete:'POST', save:'POST', update:'POST']
	// Will Return PartyGroup list where PartyType = COMPANY
	def list = {
        return [ listJsonUrl:HtmlUtil.createLink([controller:'person', action:'listJson']) ]
    }
	
	def listJson = {
		def sortIndex = params.sidx ?: 'companyName'
		def sortOrder  = params.sord ?: 'asc'
		def maxRows = Integer.valueOf(params.rows?:'25')
		def currentPage = Integer.valueOf(params.page?:'1')
		def rowOffset = currentPage == 1 ? 0 : (currentPage - 1) * maxRows
		def companyInstanceList
		def filterParams = ['companyName':params.companyName, 'dateCreated':params.dateCreated, 'lastUpdated':params.lastUpdated]
		
		// Validate that the user is sorting by a valid column
		if( ! sortIndex in filterParams)
			sortIndex = 'companyName'
		
		def active = params.activeUsers ? params.activeUsers : session.getAttribute("InActive")
		if(!active){
			active = 'Y'
		}
		
		def query = new StringBuffer("""SELECT * FROM ( SELECT name as companyName, party_group_id as companyId, p.date_created as dateCreated, p.last_updated AS lastUpdated 
			FROM party_group pg
			INNER JOIN party p ON party_type_id='COMPANY' AND p.party_id=pg.party_group_id
			GROUP BY party_group_id ORDER BY """ + sortIndex + """ """ + sortOrder + """) as companies""")
		
		// Handle the filtering by each column's text field
		def firstWhere = true
		filterParams.each {
			if(it.getValue())
				if(firstWhere){
					query.append(" WHERE companies.${it.getKey()} LIKE '%${it.getValue()}%'")
					firstWhere = false
				} else {
					query.append(" AND companies.${it.getKey()} LIKE '%${it.getValue()}%'")
				}
		}
		
		companyInstanceList = jdbcTemplate.queryForList(query.toString())
		
		// Limit the returned results to the user's page size and number
		def totalRows = companyInstanceList.size()
		def numberOfPages = Math.ceil(totalRows / maxRows)
		if(totalRows > 0)
			companyInstanceList = companyInstanceList[rowOffset..Math.min(rowOffset+maxRows,totalRows-1)]
		else
			companyInstanceList = []
		
		def showUrl = HtmlUtil.createLink([controller:'partyGroup', action:'show'])
		
		// Due to restrictions in the way jqgrid is implemented in grails, sending the html directly is the only simple way to have the links work correctly
		def results = companyInstanceList?.collect {[ cell: ['<a href="' + showUrl + '/' + it.companyId + '">'+it.companyName+'</a>', it.dateCreated, it.lastUpdated], id: it.companyId ]}
		def jsonData = [rows: results, page: currentPage, records: totalRows, total: numberOfPages]
		render jsonData as JSON
	}

    def show = {
        def partyGroupInstance = PartyGroup.get( params.id )
		userPreferenceService.setPreference( "PARTYGROUP", "${partyGroupInstance?.id}" )

        if(!partyGroupInstance) {
            flash.message = "PartyGroup not found with id ${params.id}"
            redirect(action:list)
        }
        else { return [ partyGroupInstance : partyGroupInstance ] }
    }

    def delete = {
		try{
	        def partyGroupInstance = PartyGroup.get( params.id )
	        if(partyGroupInstance) {
	            partyGroupInstance.delete(flush:true)
	            flash.message = "PartyGroup ${partyGroupInstance} deleted"
	            redirect(action:list)
	        }
	        else {
	            flash.message = "PartyGroup not found with id ${params.id}"
	            redirect(action:list)
	        }
		} catch(Exception ex){
    		flash.message = ex
    		redirect(action:list)
    	}
    }

    def edit = {
        def partyGroupInstance = PartyGroup.get( params.id )
		userPreferenceService.setPreference( "PARTYGROUP", "${partyGroupInstance?.id}" )
        if(!partyGroupInstance) {
            flash.message = "PartyGroup not found with id ${params.id}"
            redirect(action:list)
        }
        else {
            return [ partyGroupInstance : partyGroupInstance ]
        }
    }

    def update = {
        def partyGroupInstance = PartyGroup.get( params.id )
        //partyGroupInstance.lastUpdated = new Date()
        if(partyGroupInstance) {
            partyGroupInstance.properties = params
			
            if( !partyGroupInstance.hasErrors() && partyGroupInstance.save()) {
                flash.message = "PartyGroup ${partyGroupInstance} updated"
                redirect(action:show,id:partyGroupInstance.id)
            }   else {
            	flash.message = "Unable to update due to: " + GormUtil.errorsToUL(partyGroupInstance)
                render(view:'edit',model:[partyGroupInstance:partyGroupInstance])
            }
        }
        else {
            flash.message = "PartyGroup not found with id ${params.id}"
            redirect(action:edit,id:params.id)
        }
    }

    def create = {
        def partyGroupInstance = new PartyGroup()
        partyGroupInstance.properties = params
        return ['partyGroupInstance':partyGroupInstance]
    }

    def save = {
        def partyGroupInstance = new PartyGroup(params)
        //partyGroupInstance.dateCreated = new Date()
        if(!partyGroupInstance.hasErrors() && partyGroupInstance.save()) {
        	def partyType = partyGroupInstance.partyType
        	//	Statements to create CLIENT PartyRelationship with  TDS Company
        	if( partyType != null && partyType.id == "COMPANY" ){
        	
	        	def companyParty = PartyGroup.findByName( "TDS" )
	        	def partyRelationship = partyRelationshipService.savePartyRelationship( "CLIENTS", companyParty, "COMPANY", partyGroupInstance, "CLIENT" )

        	}
            flash.message = "PartyGroup ${partyGroupInstance} created"
            redirect(action:show,id:partyGroupInstance.id)
        }
        else {
            render(view:'create',model:[partyGroupInstance:partyGroupInstance])
        }
    }
}
