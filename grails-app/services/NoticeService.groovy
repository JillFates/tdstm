import org.codehaus.groovy.grails.plugins.web.taglib.ValidationTagLib

/**
 * Created by octavio on 10/10/16.
 */
class NoticeService {
	SecurityService securityService
	PersonService personService

	/**
	 * Set Acknowledment of a Note for a User
	 * TODO: (oluna) What Happen if the user doesn't have a Person associated should we change this to UserLogin Type?
	 * @param id
	 * @param username
	 * @return
	 */
	public ack(id, username){
		log.info("ID: $id, username: $username")
		NoticeAcknowledgment notack = new NoticeAcknowledgment()
		def notice = Notice.get(id)

		log.info("Notice: $notice")
		if(!notice){ return false }
		notack.notice = notice

		def person = personService.findByUsername(username)
		log.info("Person: $person")

		def person2 = personService.findByUsername("john.gore")
		log.info("Person2: $person2")
		if(!person){ return false }
		notack.person = person

		if(!notack.save()){
			return false
		}

		return true
	}

	/**
	 * Deletes a single Notice record
	 */
	public delete(id){
		if(id != null) {
			Notice notice = Notice.get(id)
			if (notice) {
				notice.delete()
				return true
			}
		}

		//if we get this far there's nothing to delete
		return false
	}

	/*
	save - Saves changes to an existing Notice record
	get - Used to retrieve the details of a single Notice record
	fetch - Used to fetch a list of messages for a given use-case
	acknowledge
	*/

	/**
	 * Used to fetch a list of messages for a given use-case
	 */
	public fetch(Notice.NoticeType type = null){
		if(type){
			return Notice.findAllByTypeId(type)
		}else{
			return Notice.findAll()
		}

	}

	public get(id){
		Notice notice = null
		if(id){
			notice = Notice.get(id)
		}
		return notice
	}

	public update(id, json){
		def result = null
		if(id) {
			def notice = get(id)
			if(notice != null){
				result = saveUpdate(json, notice)
			}else{
				result=[
				        status:false,
						errors:["Resource not found with id [$id]"]
				]
			}
		}else{
			result=[
					status:false,
					errors:["id not provided"]
			]
		}

		return result
	}

	public create(json){
		Notice notice = new Notice()
		notice.createdBy = securityService.userLoginPerson
		return saveUpdate(json, notice)
	}

	// HELPER METHODS /////////////////////////////////////////
	private saveUpdate(json, Notice notice){
		[
				[prop:"title",           type:String],
				[prop:"rawText",         type:String],
				[prop:"htmlText",        type:String],
				[prop:"typeId",          type:Notice.NoticeType],
				[prop:"acknowledgeable", type:String],
				[prop:"active",          type:Boolean],
				[prop:"projectId",       type:Project],
				[prop:"activationDate",  type:Date],
				[prop:"expirationDate",  type:Date],
		].each { prop ->
			def p = prop.prop
			def t = prop.type
			def val = json[p]
			if(val != null){
				switch (t){
					case Date:
						val = javax.xml.bind.DatatypeConverter.parseDateTime(val).getTime()
						break

					case Notice.NoticeType:
						val = Notice.NoticeType.forId(val)
						break

					case Project:
						val = Project.get(val)
						break

				}

				notice[p] = val
			}
		}

		def status = notice.save()

		def result = [
				status:status,
				data:[:]
		]

		if(!status){
			def g = new ValidationTagLib()
			result.data.errors = notice.errors.allErrors.collect{g.message([error : it])}
		}else{
			result.data.notice = notice
		}

		return result
	}

}
