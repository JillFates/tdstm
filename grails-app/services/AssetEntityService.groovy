import java.text.SimpleDateFormat
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.jsecurity.SecurityUtils

import com.tds.asset.AssetDependency
import com.tds.asset.AssetEntity
import com.tds.asset.AssetComment
import com.tds.asset.AssetNotes

import com.tdssrc.grails.GormUtil

class AssetEntityService {

    static transactional = true
	
	def mailService					// SendMail MailService class
	
	def createOrUpdateAssetEntityDependencies(def params, def assetEntityInstance) {
		
		def principal = SecurityUtils.subject.principal
		def loginUser = UserLogin.findByUsername(principal)
		
		def supportCount = Integer.parseInt(params.supportCount)
		AssetDependency.executeUpdate("delete AssetDependency where dependent = ? ",[assetEntityInstance])
		
		for(int i=0; i< supportCount; i++){
			def supportAsset = params["asset_support_"+i]
			if(supportAsset){
				def asset = AssetEntity.findByIdAndProject(supportAsset, assetEntityInstance.project)
				if(asset){
					def assetDependency = AssetDependency.findByAssetAndDependent(asset,assetEntityInstance)
					if(assetDependency){
						assetDependency.dataFlowFreq = params["dataFlowFreq_support_"+i]
						assetDependency.type = params["dtype_support_"+i]
						assetDependency.status = params["status_support_"+i]
						assetDependency.updatedBy = loginUser?.person
					} else {
						assetDependency = new AssetDependency(
																asset : asset,
																dependent : assetEntityInstance,
																dataFlowFreq : params["dataFlowFreq_support_"+i],
																type : params["dtype_support_"+i],
																status : params["status_support_"+i],
																updatedBy : loginUser?.person,
																createdBy : loginUser?.person
																)
					}
					if ( !assetDependency.validate() || !assetDependency.save() ) {
						def etext = "Unable to create assetDependency" +
						GormUtil.allErrorsString( assetDependency )
						   println etext
					}
				}
			}
		}
		
		def dependentCount = Integer.parseInt(params.dependentCount)
		AssetDependency.executeUpdate("delete AssetDependency where asset = ? ",[assetEntityInstance])
		
		for(int i=0; i< dependentCount; i++){
			def dependentAsset = params["asset_dependent_"+i]
			if(dependentAsset){
				def asset = AssetEntity.findByIdAndProject(dependentAsset, assetEntityInstance.project)
				if(asset){
					def assetDependency = AssetDependency.findByAssetAndDependent(assetEntityInstance,asset)
					if(assetDependency){
						assetDependency.dataFlowFreq = params["dataFlowFreq_dependent_"+i]
						assetDependency.type = params["dtype_dependent_"+i]
						assetDependency.status = params["status_dependent_"+i]
						assetDependency.updatedBy = loginUser?.person
					} else {
						assetDependency = new AssetDependency(
																asset : assetEntityInstance,
																dependent : asset,
																dataFlowFreq : params["dataFlowFreq_dependent_"+i],
																type : params["dtype_dependent_"+i],
																status : params["status_dependent_"+i],
																updatedBy : loginUser?.person,
																createdBy : loginUser?.person
																)
					}
					if ( !assetDependency.validate() || !assetDependency.save() ) {
						def etext = "Unable to create assetDependency" +
						GormUtil.allErrorsString( assetDependency )
						   println etext
					}
				}
			}
		}
	}
    def createOrUpdateApplicationDependencies(def params, def applicationInstance) {
		
		def principal = SecurityUtils.subject.principal
		def loginUser = UserLogin.findByUsername(principal)
		
		def supportCount = Integer.parseInt(params.supportCount)
		AssetDependency.executeUpdate("delete AssetDependency where dependent = ? ",[applicationInstance])
		
		for(int i=0; i< supportCount; i++){
			def supportAsset = params["asset_support_"+i]
			if(supportAsset){
				def asset = AssetEntity.findByIdAndProject(supportAsset, applicationInstance.project)
				if(asset){
					def assetDependency = AssetDependency.findByAssetAndDependent(asset,applicationInstance)
					if(assetDependency){
						assetDependency.dataFlowFreq = params["dataFlowFreq_support_"+i]
						assetDependency.type = params["dtype_support_"+i]
						assetDependency.status = params["status_support_"+i]
						assetDependency.updatedBy = loginUser?.person
					} else {
						assetDependency = new AssetDependency(
																asset : asset,
																dependent : applicationInstance,
																dataFlowFreq : params["dataFlowFreq_support_"+i],
																type : params["dtype_support_"+i],
																status : params["status_support_"+i],
																updatedBy : loginUser?.person,
																createdBy : loginUser?.person
																)
					}
					if ( !assetDependency.validate() || !assetDependency.save() ) {
						def etext = "Unable to create assetDependency" +
						GormUtil.allErrorsString( assetDependency )
						   println etext
					}
				}
			}
		}
		
		def dependentCount = Integer.parseInt(params.dependentCount)
		AssetDependency.executeUpdate("delete AssetDependency where asset = ? ",[applicationInstance])
		
		for(int i=0; i< dependentCount; i++){
			def dependentAsset = params["asset_dependent_"+i]
			if(dependentAsset){
				def asset = AssetEntity.findByIdAndProject(dependentAsset, applicationInstance.project)
				if(asset){
					def assetDependency = AssetDependency.findByAssetAndDependent(applicationInstance,asset)
					if(assetDependency){
						assetDependency.dataFlowFreq = params["dataFlowFreq_dependent_"+i]
						assetDependency.type = params["dtype_dependent_"+i]
						assetDependency.status = params["status_dependent_"+i]
						assetDependency.updatedBy = loginUser?.person
					} else {
						assetDependency = new AssetDependency(
																asset : applicationInstance,
																dependent : asset,
																dataFlowFreq : params["dataFlowFreq_dependent_"+i],
																type : params["dtype_dependent_"+i],
																status : params["status_dependent_"+i],
																updatedBy : loginUser?.person,
																createdBy : loginUser?.person
																)
					}
					if ( !assetDependency.validate() || !assetDependency.save() ) {
						def etext = "Unable to create assetDependency" +
						GormUtil.allErrorsString( assetDependency )
						   println etext
					}
				}
			}
		}
    }
	def createOrUpdateDatabaseDependencies(def params, def dbInstance) {
		
		def principal = SecurityUtils.subject.principal
		def loginUser = UserLogin.findByUsername(principal)
		
		def supportCount = Integer.parseInt(params.supportCount)
		AssetDependency.executeUpdate("delete AssetDependency where dependent = ? ",[dbInstance])
		
		for(int i=0; i< supportCount; i++){
			def supportAsset = params["asset_support_"+i]
			if(supportAsset){
				def asset = AssetEntity.findByIdAndProject(supportAsset, dbInstance.project)
				if(asset){
					def assetDependency = AssetDependency.findByAssetAndDependent(asset,dbInstance)
					if(assetDependency){
						assetDependency.dataFlowFreq = params["dataFlowFreq_support_"+i]
						assetDependency.type = params["dtype_support_"+i]
						assetDependency.status = params["status_support_"+i]
						assetDependency.updatedBy = loginUser?.person
					} else {
						assetDependency = new AssetDependency(
																asset : asset,
																dependent : dbInstance,
																dataFlowFreq : params["dataFlowFreq_support_"+i],
																type : params["dtype_support_"+i],
																status : params["status_support_"+i],
																updatedBy : loginUser?.person,
																createdBy : loginUser?.person
																)
					}
					if ( !assetDependency.validate() || !assetDependency.save(flush:true) ) {
						def etext = "Unable to create assetDependency" +
						GormUtil.allErrorsString( assetDependency )
						   println etext
					}
				}
			}
		}
		
		def dependentCount = Integer.parseInt(params.dependentCount)
		AssetDependency.executeUpdate("delete AssetDependency where asset = ? ",[dbInstance])
		
		for(int i=0; i< dependentCount; i++){
			def dependentAsset = params["asset_dependent_"+i]
			if(dependentAsset){
				def asset = AssetEntity.findByIdAndProject(dependentAsset, dbInstance.project)
				if(asset){
					def assetDependency = AssetDependency.findByAssetAndDependent(dbInstance,asset)
					if(assetDependency){
						assetDependency.dataFlowFreq = params["dataFlowFreq_dependent_"+i]
						assetDependency.type = params["dtype_dependent_"+i]
						assetDependency.status = params["status_dependent_"+i]
						assetDependency.updatedBy = loginUser?.person
					} else {
						assetDependency = new AssetDependency(
																asset : dbInstance,
																dependent : asset,
																dataFlowFreq : params["dataFlowFreq_dependent_"+i],
																type : params["dtype_dependent_"+i],
																status : params["status_dependent_"+i],
																updatedBy : loginUser?.person,
																createdBy : loginUser?.person
																)
					}
					if ( !assetDependency.validate() || !assetDependency.save() ) {
						def etext = "Unable to create assetDependency" +
						GormUtil.allErrorsString( assetDependency )
						   println etext
					}
				}
			}
		}
	}
	def createOrUpdateFilesDependencies(def params, def dbInstance) {
		
		def principal = SecurityUtils.subject.principal
		def loginUser = UserLogin.findByUsername(principal)
		
		def supportCount = Integer.parseInt(params.supportCount)
		AssetDependency.executeUpdate("delete AssetDependency where dependent = ? ",[dbInstance])
		
		for(int i=0; i< supportCount; i++){
			def supportAsset = params["asset_support_"+i]
			if(supportAsset){
				def asset = AssetEntity.findByIdAndProject(supportAsset, dbInstance.project)
				if(asset){
					def assetDependency = AssetDependency.findByAssetAndDependent(asset,dbInstance)
					if(assetDependency){
						assetDependency.dataFlowFreq = params["dataFlowFreq_support_"+i]
						assetDependency.type = params["dtype_support_"+i]
						assetDependency.status = params["status_support_"+i]
						assetDependency.updatedBy = loginUser?.person
					} else {
						assetDependency = new AssetDependency(
																asset : asset,
																dependent : dbInstance,
																dataFlowFreq : params["dataFlowFreq_support_"+i],
																type : params["dtype_support_"+i],
																status : params["status_support_"+i],
																updatedBy : loginUser?.person,
																createdBy : loginUser?.person
																)
					}
					if ( !assetDependency.validate() || !assetDependency.save(flush:true) ) {
						def etext = "Unable to create assetDependency" +
						GormUtil.allErrorsString( assetDependency )
						   println etext
					}
				}
			}
		}
		
		def dependentCount = Integer.parseInt(params.dependentCount)
		AssetDependency.executeUpdate("delete AssetDependency where asset = ? ",[dbInstance])
		
		for(int i=0; i< dependentCount; i++){
			def dependentAsset = params["asset_dependent_"+i]
			if(dependentAsset){
				def asset = AssetEntity.findByIdAndProject(dependentAsset, dbInstance.project)
				if(asset){
					def assetDependency = AssetDependency.findByAssetAndDependent(dbInstance,asset)
					if(assetDependency){
						assetDependency.dataFlowFreq = params["dataFlowFreq_dependent_"+i]
						assetDependency.type = params["dtype_dependent_"+i]
						assetDependency.status = params["status_dependent_"+i]
						assetDependency.updatedBy = loginUser?.person
					} else {
						assetDependency = new AssetDependency(
																asset : dbInstance,
																dependent : asset,
																dataFlowFreq : params["dataFlowFreq_dependent_"+i],
																type : params["dtype_dependent_"+i],
																status : params["status_dependent_"+i],
																updatedBy : loginUser?.person,
																createdBy : loginUser?.person
																)
					}
					if ( !assetDependency.validate() || !assetDependency.save() ) {
						def etext = "Unable to create assetDependency" +
						GormUtil.allErrorsString( assetDependency )
						   println etext
					}
				}
			}
		}
	}
	/**
	 * @patams, files path, file name startsWith
	 * Delete all files that are match with params criteria
	 */
	def deleteTempGraphFiles(path, startsWith){
		def filePath = ApplicationHolder.application.parentContext.getResource(path).file
		// Get file path
		def dir = new File( "${filePath.absolutePath}" )
		def children = dir.list()
		if ( children ) {
			for (int i=0; i<children.length; i++) {
				// Get filename
				def filename = children[i]
				if ( filename.startsWith(startsWith) ) {
					def jsonFile =  ApplicationHolder.application.parentContext.getResource( "${path}/${filename}" ).getFile()
					jsonFile?.delete()
				}
			}
		}
	}
	
	/**
	 * Used to send the Task email to the appropriate user for the comment passed to the method
	 * @param assetComment
	 * @param tzId
	 * @return
	 */
	def sendTaskEMail(taskId, tzId){
		def createdBy
		def dtCreated
		def dtResolved
		def resolvedBy
		def owner
		def dueDate
		def formatter = new SimpleDateFormat("MM-dd-yyyy hh:mm a");
		def dateFormatter = new SimpleDateFormat("MM/dd/yyyy ");
		
		def assetComment = AssetComment.get(taskId)
		if (! assetComment) {
			log.error "Invalid AssetComment ID [${taskId}] referenced in call"
			return
		}
		if (!assetComment.owner?.email) {
			log.error "No valid email address for task owner"
			return
		}
		
		if(assetComment.createdBy){
			createdBy = assetComment.createdBy
			dtCreated = formatter.format(GormUtil.convertInToUserTZ(assetComment.dateCreated, tzId));
		}
		if(assetComment.resolvedBy){
			resolvedBy = assetComment.resolvedBy
			dtResolved = formatter.format(GormUtil.convertInToUserTZ(assetComment.dateResolved, tzId));
		}
		if(assetComment.owner){
			owner = assetComment.owner
		}
		if(assetComment.dueDate){
			dueDate = dateFormatter.format(assetComment.dueDate);
		}
		def assetNotes = assetComment.notes?.sort{it.dateCreated}
		
		def sub = leftString(getLine(assetComment.comment,0), 40)
		sub = (sub == null || sub.size() == 0) ? "Task ${assetComment.id}" : sub

		mailService.sendMail {
			to assetComment.owner.email
			subject "Re: ${sub}"
			body ( 
				view:"/assetEntity/_taskEMailTemplate",
				model:[assetComment:assetComment, createdBy:createdBy, dtCreated:dtCreated, resolvedBy:resolvedBy,
				   owner:owner,dueDate:dueDate,assetNotes:assetNotes] )
		}
	}
	
	// TODO : move these methods into a reusable class - perhaps extending string with @Delegate
	
	/**
	 * Returns the left of a string to an optional length limit
	 * @param str - string to return
	 * @param len - optional length of string to return
	 * @return String
	 */
	def leftString(str, len=null) {
		if (str == null) return null
		def size = str.size()
		size = (len != null && size > len) ? len : size
		size = size==0 ? 1 : size
		return str[0..(size-1)]
	}

	/**
	 * Returns a specified line within a string and null if line number does not exist, defaulting to the first if no	
	 * @param str - string to act upon
	 * @param lineNum - line number to return starting with zero, default of 0
	 * @return String
	 */
	def getLine(str, lineNum=0) {
		ArrayList lines = str.readLines()
		return ( (lineNum+1) > lines.size() ) ? null : lines[lineNum]
	}
	
}
