import org.codehaus.groovy.grails.plugins.web.taglib.ValidationTagLib

/**
 * Created by octavio on 10/10/16.
 */
class NoticeService {
	SecurityService securityService

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
			return Notice.findAllByType(type)
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
