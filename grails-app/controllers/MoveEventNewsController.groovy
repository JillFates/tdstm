import grails.converters.JSON
import com.tdsops.tm.enums.domain.AssetCommentStatus
class MoveEventNewsController {
	// *** Initialize Services
	def jdbcTemplate
	def stateEngineService
	/* will return the list of AssetComments and MoveEventNews
	 * @param : moveEventId?type=[N|I}&state=[L|A]&maxLen= n &sort=[A|D]
	 * @return : union (AssetComments , MoveEventNews) 
	 */
	def list = {
			def projectId = getSession().getAttribute("CURR_PROJ")?.CURR_PROJ
			def moveEventId = params.id
			def moveEvent
			def state = params.state
			def order = params.sort
			def maxLen = params.maxLen
			def type = params.type
			def totalComments
			if(moveEventId){ 
				moveEvent = MoveEvent.get(moveEventId)
			}
				//def offsetTZ = ( new Date().getTimezoneOffset() / 60 ) 
			
			if(moveEvent){
				def holdId = stateEngineService.getStateId( moveEvent.project.workflowCode, "Hold" )
				def assetCommentsQuery = new StringBuffer( """SELECT ac.asset_comment_id as id,  'I' as type,
									DATE_FORMAT( date_created,'%Y/%m/%d %r') as created,  
									if(display_option = 'G' || ae.current_status = $holdId , CONCAT_WS(':',ae.asset_name, 'is on hold' ), comment) as text, 
									if(is_resolved = 0, 'L','A') as state from asset_comment ac 
									left join asset_entity ae on (ae.asset_entity_id = ac.asset_entity_id) 
									left join move_bundle mb on (mb.move_bundle_id = ae.move_bundle_id)
									left join move_event me on ( me.move_event_id = mb.move_event_id ) 
									left join project p on (p.project_id = ae.project_id) 
									where mb.move_event_id = ${moveEvent.id} and  ac.comment_type = 'issue' and p.project_id = ${projectId} 
									and ac.status = '${AssetCommentStatus.HOLD}'""" )
				def moveEventNewsQuery = new StringBuffer( """SELECT mn.move_event_news_id as id,  'N' as type, 
									DATE_FORMAT( date_created,'%Y/%m/%d %r') as created,  
									message as text, if(is_archived = 0, 'L','A') as state  from move_event_news mn 
									left join move_event me on ( me.move_event_id = mn.move_event_id ) 
									left join project p on (p.project_id = me.project_id) where mn.move_event_id = ${moveEvent.id} and p.project_id = ${projectId}""" )
				if(state == "L"){
					assetCommentsQuery.append(" and ac.is_resolved = 0 ")
					moveEventNewsQuery.append(" and mn.is_archived = 0 ")
				} else if(state == "A"){
					assetCommentsQuery.append(" and ac.is_resolved = 1 ")
					moveEventNewsQuery.append(" and mn.is_archived = 1 ")
				}
				def queryForCommentsList = new StringBuffer()
				
				if(type =="I"){
					queryForCommentsList.append(assetCommentsQuery.toString())
				} else if(type =="N"){
					queryForCommentsList.append(moveEventNewsQuery.toString())
				} else {
					queryForCommentsList.append(assetCommentsQuery.toString() +" union all "+ moveEventNewsQuery.toString())
				}
					
				if(order =="A"){
					queryForCommentsList.append( " order by created asc" )
				} else {
					queryForCommentsList.append( " order by created desc" )
				}
				
				totalComments = jdbcTemplate.queryForList( queryForCommentsList.toString() )
				totalComments.each{
					if(maxLen){
						it.text = truncate(it.text, maxLen)
					}
				}
				
			}
			
			render totalComments as JSON
	}
	/* will truncate the test to specified length 
	 * @param : text as value
	 * @param : length to truncate
	 */
	def truncate (def value, def length){
		if(value ){
			def size = value.size()
			if(size > length){
				return value.substring(0,length -3 )+'...'
			} else {
				return value
			}
		}
	}	
	
}
