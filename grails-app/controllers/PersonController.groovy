import grails.converters.JSON
import java.text.SimpleDateFormat
import org.jsecurity.crypto.hash.Sha1Hash
import com.tdssrc.grails.GormUtil
import java.text.DateFormat
import org.jmesa.facade.TableFacade
import org.jmesa.facade.TableFacadeImpl
import net.tds.util.jmesa.PersonBean
class PersonController {
    
	def partyRelationshipService
	def userPreferenceService
	
    def index = { redirect(action:list,params:params) }

    // the delete, save and update actions only accept POST requests
    def allowedMethods = [delete:'POST', save:'POST', update:'POST']
	// return Persons which are related to company
    def list = {
		//def	projectId = getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
        def companyId = params.id
        def personInstanceList
        def companiesList
        if ( companyId!= null && companyId != "" ) {
        	
        	personInstanceList = partyRelationshipService.getCompanyStaff( companyId )
        	def query = "from PartyGroup as p where partyType = 'COMPANY' "
            companiesList = PartyGroup.findAll( query )
        	//projectCompanies = partyRelationshipService.getProjectCompanies( projectId )        	
        } else {
        	flash.message = "Please select Company before navigating to Staff"
            redirect(controller:'partyGroup',action:'list')
        }
		List personsList = new ArrayList()
		personInstanceList.each{
			PersonBean personBean = new PersonBean()
			personBean.setId(it.id)
			personBean.setFirstName(it.firstName)
			personBean.setLastName(it.lastName)
			def userLogin = UserLogin.findByPerson(it);
			if(userLogin){
				personBean.setUserLogin(userLogin.username)
				personBean.setUserLoginId(userLogin.id)
			} else {
				personBean.setUserLogin("CREATE")
			}
			personBean.setDateCreated(it.dateCreated)
			personBean.setLastUpdated(it.lastUpdated)
			personsList.add(personBean)
		}
		// Statements for JMESA integration
    	TableFacade tableFacade = new TableFacadeImpl("tag",request)
        tableFacade.items = personsList
		
		return [ personsList: personsList, companyId:companyId,totalCompanies:companiesList ]
    }

    def show = {
			
        def personInstance = Person.get( params.id )
        def companyId = params.companyId
        if(!personInstance) {
            flash.message = "Person not found with id ${params.id}"
            redirect( action:list, params:[ id:companyId ] )
        }
        else { 
        	//def company = partyRelationshipService.getSatffCompany( personInstance )
        	
        	return [ personInstance : personInstance, companyId:companyId ] 
        }
    }

    def delete = {
			
		def personInstance = Person.get( params.id )
        def companyId = params.companyId
        if ( personInstance ) {
	        def partyInstance = Party.findById( personInstance.id )      
	        def partyRelnInst = PartyRelationship.findAll("from PartyRelationship pr where pr.partyIdTo = ${personInstance.id}")         
	        def partyRole = PartyRole.findAll("from PartyRole p where p.party =${partyInstance.id}")      
	        def loginInst = UserLogin.find("from UserLogin ul where ul.person = ${personInstance.id}")
	        if ( loginInst ) {
		        def preferenceInst = UserPreference.findAll("from UserPreference up where up.userLogin = ${loginInst.id}")
		        preferenceInst.each{
		          it.delete()
		          }
		        loginInst.delete()
	        }       
		    partyRelnInst.each{
		   	it.delete()
		    }
		    partyRole.each{
		    it.delete()
		    }      
	      	partyInstance.delete()      
            personInstance.delete()
        	if( personInstance.lastName == null ) {
        		personInstance.lastName = ""
        	}
            flash.message = "Person ${personInstance} deleted"
            redirect( action:list, params:[ id:companyId ] )
        }
        else {
            flash.message = "Person not found with id ${params.id}"
            redirect( action:list, params:[ id:companyId ] )
        }
    }
	// return person details to EDIT form
    def edit = {
        def personInstance = Person.get( params.id )
        def companyId = params.companyId
        if(!personInstance) {
            flash.message = "Person not found with id ${params.id}"
            redirect( action:list, params:[ id:companyId ] )
        }
        else {
        	
            return [ personInstance : personInstance, companyId:companyId ]
        }
    }

    def update = {       
	        
        def personInstance = Person.get( params.id )
	        
        //personInstance.lastUpdated = new Date()
	        
        def companyId = params.company
        if(personInstance) {
            personInstance.properties = params
            if ( !personInstance.hasErrors() && personInstance.save() ) {
            	if(companyId != ""){
    	            def companyParty = Party.findById(companyId)
    	            partyRelationshipService.updatePartyRelationshipPartyIdFrom("STAFF", companyParty, 'COMPANY', personInstance, "STAFF")
                }
                flash.message = "Person ${params.firstName} ${params.lastName} updated"
                redirect( action:list, params:[ id:companyId ])
            }
            else {
                flash.message = "Person ${params.firstName} ${params.lastName} not updated "
                redirect( action:list, params:[ id:companyId ])
            }
        }
        else {
            flash.message = "Person not found with id ${params.id}"
            redirect( action:list, params:[ id:companyId ])
        }
    }
    // return person instance and companies 
    def create = {
        def personInstance = new Person()
        personInstance.properties = params
        // def companies = partyRelationshipService.getCompaniesList()
       	def companyId = params.companyId 
        return [ 'personInstance':personInstance, companyId:companyId ]
    }
    //Save the Person Detais
    def save = {
		
        def personInstance = new Person( params )
        
        //personInstance.dateCreated = new Date()
        if ( !personInstance.hasErrors() && personInstance.save() ) {
        	def fullName 
			if(params.lastName == ""){
				fullName = params.firstName
			}else{
				fullName = params.firstName+" "+params.lastName
			}
            def companyId = params.company
            if ( companyId != "" ) {
                def companyParty = Party.findById( companyId )
                def partyRelationship = partyRelationshipService.savePartyRelationship( "STAFF", companyParty, "COMPANY", personInstance, "STAFF" )
            }
            flash.message = "Person ${fullName} created"
            //redirect( action:list, id:personInstance.id , params:[companyId:companyId] )
            redirect( action:list, params:[ id:companyId ] )
        }
        else {
            def companyId = params.company
            flash.message = " Person FirstName cannot be blank. "
            redirect( action:list, params:[ id:companyId ] )
        }
    }
    //	Ajax Overlay for show
    def editShow = {
        
        def personInstance = Person.get( params.id )        
        def companyId = params.companyId
        def companyParty = Party.findById(companyId)
		def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ
		DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
        def dateCreatedByFormat = formatter.format(GormUtil.convertInToUserTZ( personInstance.dateCreated, tzId ) )
        def lastUpdatedFormat = formatter.format(GormUtil.convertInToUserTZ( personInstance.lastUpdated, tzId ) )
        if(!personInstance) {
            flash.message = "Person not found with id ${params.id}"
            redirect( action:list, params:[ id:companyId ] )
        }
        else {       	

        	def items = [id: personInstance.id, firstName: personInstance.firstName, lastName: personInstance.lastName, 
						 nickName: personInstance.nickName, title: personInstance.title, active: personInstance.active, 
						 dateCreated: dateCreatedByFormat, lastUpdated: lastUpdatedFormat, companyId: companyId,
						 companyParty:companyParty, email: personInstance.email]
            render items as JSON
        }
    }
    //ajax overlay for Edit
    def editStaff = {
        def map = new HashMap()
        def personInstance = Person.get( params.id )
        def role = params.role
        def company = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'STAFF' and p.partyIdTo = $personInstance.id and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'STAFF'")
        if(company == null){
        	map.put("companyId","")
        }else{
            map.put("companyId",company.partyIdFrom.id)
        }
        map.put("id", personInstance.id)
        map.put("firstName", personInstance.firstName)
        map.put("lastName", personInstance.lastName)
        map.put("nickName", personInstance.nickName)
        map.put("title", personInstance.title)
        map.put("email", personInstance.email)
		map.put("active", personInstance.active)
        map.put("role", role)
        render map as JSON
    }
	/*
	 *  Remote method to update Staff Details
	 */
	def updateStaff = {
    	def personInstance = Person.get( params.id )
    	def projectId = params.projectId
    	def roleType = params.roleType
    	def companyId = params.company
    	//personInstance.lastUpdated = new Date()
    	if(personInstance) {
    		personInstance.properties = params
    		if(personInstance.lastName == null){
				personInstance.lastName = ""	
			}
            if ( !personInstance.hasErrors() && personInstance.save() ) {
	            def projectParty = Project.findById(projectId)
	            if(companyId != ""){
                    def companyParty = Party.findById(companyId)
                    partyRelationshipService.updatePartyRelationshipPartyIdFrom("STAFF", companyParty, 'COMPANY', personInstance, "STAFF")
	            }
	            def partyRelationship = partyRelationshipService.updatePartyRelationshipRoleTypeTo("PROJ_STAFF", projectParty, 'PROJECT', personInstance, roleType)
            	 
	            flash.message = "Person ${personInstance} updated"
                redirect( action:projectStaff, params:[ projectId:projectId ])
            } else {
            	flash.message = "Person ${personInstance} not updated"
            	redirect( action:projectStaff, params:[ projectId:projectId ])
            }
    	} else {
    		flash.message = "Person not found with id ${params.id}"
    		redirect( action:projectStaff, params:[ projectId:projectId ])
    	}
	}
	/*
	 *  Return Project Staff 
	 */
	def projectStaff = {
		def projectId = params.projectId
		def submit = params.submit
		def projectStaff = partyRelationshipService.getProjectStaff( projectId )	
		def companiesStaff = partyRelationshipService.getProjectCompaniesStaff( projectId )
		def projectCompanies = partyRelationshipService.getProjectCompanies( projectId )
		return [ projectStaff:projectStaff, companiesStaff:companiesStaff, projectCompanies:projectCompanies, projectId:projectId, submit:submit ]
	}
	/*
	 *	Method to add Staff to project through Ajax Overlay 
	 */
	def saveProjectStaff = {
    	def projectId = params.projectId
    	def personId = params.person
    	def roleType = params.roleType
    	def submit = params.submit
    	def projectParty = Project.findById( projectId )
    	def personParty = Person.findById( personId )
    	def projectStaff = partyRelationshipService.savePartyRelationship("PROJ_STAFF", projectParty, "PROJECT", personParty, roleType )
    	redirect(action:'projectStaff', params:[projectId:projectId, submit:submit] )
    }
	/*
	 * Method to save person detais and create party relation with Project as well 
	 */
	def savePerson = {
		def personInstance = new Person( params )
		
		//personInstance.dateCreated = new Date()
		def companyId = params.company
		def projectId = params.projectId
		def roleType = params.roleType
		if ( !personInstance.hasErrors() && personInstance.save() ) {
			
			if ( companyId != null && companyId != "" ) {
				def companyParty = Party.findById( companyId )
				def partyRelationship = partyRelationshipService.savePartyRelationship( "STAFF", companyParty, "COMPANY", personInstance, "STAFF" )
			}
			if ( projectId != null && projectId != "" && roleType != null) {
				def projectParty = Party.findById( projectId )
				def partyRelationship = partyRelationshipService.savePartyRelationship( "PROJ_STAFF", projectParty, "PROJECT", personInstance, roleType )
			}
			if(personInstance.lastName == null){
				personInstance.lastName = ""	
			}
			flash.message = "Person ${personInstance} created"
			redirect( action:'projectStaff', params:[ projectId:projectId, submit:'Add' ] )
		}
		else {
			flash.message = " Person FirstName cannot be blank. "
			redirect( action:'projectStaff', params:[ projectId:projectId,submit:'Add' ] )
		}
    }
	/*-----------------------------------------------------------
	 * Will return person details for a given personId as JSON
	 * @author : Lokanada Reddy 
	 * @param  : person id
	 * @return : person details as JSON
	 *----------------------------------------------------------*/
	def getPersonDetails = {
    	def personId = params.id
		def person = Person.findById( personId  )
		def userLogin = UserLogin.findByPerson( person )
		
		def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ
		def formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm a")
        def expiryDate = userLogin.expiryDate ? formatter.format(GormUtil.convertInToUserTZ(userLogin.expiryDate,tzId)) : ""
		
        def personDetails = [person:person, expiryDate: expiryDate]
		render personDetails as JSON
    }
	/*-----------------------------------------------------------
	 * Update the person details and user password, Return person first name
	 * @author : Lokanada Reddy 
	 * @param  : person details and user password
	 * @return : person firstname
	 *----------------------------------------------------------*/
	def updatePerson = {
			def personInstance = Person.get(params.id)
			def ret = []
			personInstance.properties = params
			if ( !personInstance.hasErrors() && personInstance.save() ) {
				def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ
				def formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm a")
				getSession().setAttribute( "LOGIN_PERSON", ['name':personInstance.firstName, "id":personInstance.id ])
				def userLogin = UserLogin.findByPerson( personInstance )
				def password = params.password
				if( password ){
					userLogin.password = new Sha1Hash( password ).toHex()
				}
				def expiryDate = params.expiryDate
				if(expiryDate){
					userLogin.expiryDate =  GormUtil.convertInToGMT(formatter.parse( expiryDate ), tzId)
				}
				userLogin.save();
				
				userPreferenceService.setPreference( "CURR_TZ", params.timeZone )
				userPreferenceService.loadPreferences("CURR_TZ")
			}
			ret << [name:personInstance.firstName, tz:getSession().getAttribute( "CURR_TZ" )?.CURR_TZ]
			render  ret as JSON
    }
}
