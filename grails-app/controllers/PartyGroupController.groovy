import grails.converters.JSON
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.HtmlUtil

class PartyGroupController {
	
    def controllerService
	def partyRelationshipService
    def securityService
    def userPreferenceService
	def jdbcTemplate

	def index() { redirect(action:"list",params:params) }

	// the delete, save and update actions only accept POST requests
	def allowedMethods = [delete:'POST', save:'POST', update:'POST']
	// Will Return PartyGroup list where PartyType = COMPANY

	def list() {
		return [ listJsonUrl:HtmlUtil.createLink([controller:'person', action:'listJson']) ]
	}

	def listJson() {
	
    	Person whom = securityService.getUserLoginPerson()

		def sortIndex = params.sidx ?: 'companyName'
		def sortOrder  = params.sord ?: 'asc'
		def maxRows = Integer.valueOf(params.rows?:'25')
		def currentPage = Integer.valueOf(params.page?:'1')
		def rowOffset = currentPage == 1 ? 0 : (currentPage - 1) * maxRows
		def companyInstanceList
		def filterParams = ['companyName':params.companyName, 'dateCreated':params.dateCreated, 'lastUpdated':params.lastUpdated, 'partner':params.partner]
		
		// Validate that the user is sorting by a valid column
		if( ! sortIndex in filterParams)
			sortIndex = 'companyName'
		
		def active = params.activeUsers ? params.activeUsers : session.getAttribute("InActive")
		if(!active){
			active = 'Y'
		}
		
		def query = new StringBuffer("""SELECT * FROM ( 
			SELECT name as companyName, party_group_id as companyId, p.date_created as dateCreated, p.last_updated AS lastUpdated, IF(pr.party_id_from_id IS NULL, '','Yes') as partner  
			FROM party_group pg
			INNER JOIN party p ON party_type_id='COMPANY' AND p.party_id=pg.party_group_id
			LEFT JOIN party_relationship pr ON pr.party_relationship_type_id = 'PARTNERS' AND pr.role_type_code_from_id = 'COMPANY' and pr.role_type_code_to_id = 'PARTNER' and pr.party_id_to_id = pg.party_group_id
			WHERE party_group_id in (
				SELECT party_id_to_id FROM party_relationship
				WHERE party_relationship_type_id = 'CLIENTS' AND role_type_code_from_id='COMPANY' 
				AND role_type_code_to_id='CLIENT' AND party_id_from_id=${whom.company.id}
				)
			GROUP BY party_group_id ORDER BY 
		""")
		query.append( " $sortIndex $sortOrder ) as companies")
		
		// Handle the filtering by each column's text field
		def firstWhere = true
		filterParams.each {
			if(it.getValue()) {
				if(firstWhere){
					query.append(" WHERE companies.${it.getKey()} LIKE '%${it.getValue()}%'")
					firstWhere = false
				} else {
					query.append(" AND companies.${it.getKey()} LIKE '%${it.getValue()}%'")
				}
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
		def results = companyInstanceList?.collect {[ cell: ['<a href="' + showUrl + '/' + it.companyId + '">'+it.companyName+'</a>', it.partner, it.dateCreated, it.lastUpdated], id: it.companyId ]}
		def jsonData = [rows: results, page: currentPage, records: totalRows, total: numberOfPages]
		render jsonData as JSON
	}

	def show() {
		def partyGroupInstance = PartyGroup.get( params.id )
		userPreferenceService.setPreference( "PARTYGROUP", "${partyGroupInstance?.id}" )

		if(!partyGroupInstance) {
			flash.message = "PartyGroup not found with id ${params.id}"
			redirect(action:"list")
		}
		else { return [ partyGroupInstance : partyGroupInstance, partner: isAPartner(partyGroupInstance) ] }
	}

	def delete() {
		try{
			def partyGroupInstance = PartyGroup.get( params.id )
			if(partyGroupInstance) {
				PartyGroup.withNewSession { s -> 
					def parties
					parties = PartyRelationship.findAllByPartyIdFrom(partyGroupInstance)
					parties*.delete()
					parties = PartyRelationship.findAllByPartyIdTo(partyGroupInstance)
					parties*.delete()
					s.flush()
					s.clear()
				}

				partyGroupInstance.delete(flush:true)
				flash.message = "PartyGroup ${partyGroupInstance} deleted"
				redirect(action:"list")
			}
			else {
				flash.message = "PartyGroup not found with id ${params.id}"
				redirect(action:"list")
			}
		} catch(Exception ex){
			flash.message = ex
			redirect(action:"list")
		}
	}

	def edit() {
		def partyGroupInstance = PartyGroup.get( params.id )
		userPreferenceService.setPreference( "PARTYGROUP", "${partyGroupInstance?.id}" )
		if(!partyGroupInstance) {
			flash.message = "PartyGroup not found with id ${params.id}"
			redirect(action:"list")
		}
		else {
			return [ partyGroupInstance : partyGroupInstance, partner: isAPartner(partyGroupInstance) ]
		}
	}

	def update() {
		def partyGroupInstance = PartyGroup.get( params.id )
		//partyGroupInstance.lastUpdated = new Date()
		if(partyGroupInstance) {
			partyGroupInstance.properties = params
			
			if( !partyGroupInstance.hasErrors() && partyGroupInstance.save()) {

				if (params.partner && params.partner == "Y" && !isAPartner(partyGroupInstance)) {
					def personCompany = partyRelationshipService.getStaffCompany( securityService.getUserLogin().person )
					if (personCompany) {
						partyRelationshipService.savePartyRelationship( "PARTNERS", personCompany, "COMPANY", partyGroupInstance, "PARTNER" )
					}
				}

				flash.message = "PartyGroup ${partyGroupInstance} updated"
				redirect(action:"show",id:partyGroupInstance.id)
			} else {
				flash.message = "Unable to update due to: " + GormUtil.errorsToUL(partyGroupInstance)
				render(view:'edit',model:[partyGroupInstance:partyGroupInstance])
			}
		}
		else {
			flash.message = "PartyGroup not found with id ${params.id}"
			redirect(action:"edit",id:params.id)
		}
	}

    def create() {
    	log.debug "**** Got to the create() method"
        def partyGroupInstance = new PartyGroup()
        partyGroupInstance.properties = params
        return ['partyGroupInstance':partyGroupInstance]
    }

    def save() {
    	if (! controllerService.checkPermission(this, 'PartyCreateView', true)) {
    		return
    	}

    	Person whom = securityService.getUserLoginPerson()

    	PartyType partyType = PartyType.read(params['partyType.id'])
    	if (! partyType) {
    		flash.message = 'Invalid PartyType was specified in the request'
    		render( view:'create', model:[name:params.name, comment:params.comment])
    		return
    	}

        PartyGroup partyGroup = new PartyGroup()
        partyGroup.name = params.name
        partyGroup.comment = params.comment
        partyGroup.partyType = partyType

        //partyGroupInstance.dateCreated = new Date()
        if (!partyGroup.hasErrors() && partyGroup.save()) {
        	//	Statements to create CLIENT PartyRelationship with the user's Company
        	if ( partyType.id == "COMPANY" ){
        	
	        	def companyParty = whom.company
	        	def partyRelationship = partyRelationshipService.savePartyRelationship( "CLIENTS", companyParty, "COMPANY", partyGroup, "CLIENT" )
        	}
            flash.message = "PartyGroup ${partyGroup} created"
            redirect(action:'show',id:partyGroup.id)
        }
        else {
            render(view:'create',model:[partyGroupInstance:partyGroup])
        }
    }

	private def isAPartner(partyGroupInstance) {
		def partner
		def personCompany = partyRelationshipService.getStaffCompany( securityService.getUserLogin().person )
		if (personCompany) {
			partner = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'PARTNERS' and p.partyIdFrom = $personCompany.id and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'PARTNER' and	p.partyIdTo = $partyGroupInstance.id")
		}
		return (partner != null)
	}

}
