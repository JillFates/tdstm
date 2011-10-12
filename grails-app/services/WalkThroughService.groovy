import com.tds.asset.AssetEntity
import com.tds.asset.AssetTransition

/*------------------------------------------------------------
 * Service for Walkthru Process
 * @author : Lokanath Reddy
 *----------------------------------------------------------*/
class WalkThroughService {
	
	def stateEngineService
    boolean transactional = true

    /*------------------------------------------------------------
	 * @author : Lokanath Reddy
	 * @param  : rackslist, bundle, location, audit type and view type
	 * @return : Will return rack list view as string
	 *----------------------------------------------------------*/
	def generateRackListView(def racksList, def moveBundle, def location, def auditType, def viewType){
    	def rackListView = new StringBuffer()
    	if(racksList){
    		def rackListSize = racksList.size()
    		def moveBundleInstance = MoveBundle.findById( moveBundle )
    		def walkthruState 
    		def type = 'source'
    		if(auditType == 'source'){
    			walkthruState = stateEngineService.getStateId(moveBundleInstance?.project?.workflowCode,"SourceWalkthru")
    		} else {
    			type = 'target'
    			walkthruState = stateEngineService.getStateId(moveBundleInstance?.project?.workflowCode,"TargetWalkthru")
    		}
    		for (int i = 0; i < rackListSize; i++) {
    			def rackList = racksList[i] 
    			def cssClass = "asset_ready"
    			def flag = true
    			if(i == 6 && rackListSize != 7 ){
    				rackListView.append("<TR class='jump'><TD colspan='3' align='center'>"+
    						"<A class='nav_button' name='racklist"+i+"' href='#racklist"+i+"'>Page Down</A></TD></TR>")
    			} else if(i > 6 && (i - 6) % 13 == 0 && (rackListSize - 7 ) % 13 != 0){
    				rackListView.append("<TR class='jump'><TD colSpan='3' align='middle'><A class='nav_button' href='#select_rack'>Top</A>"+ 
    					"<A class='nav_button' href='#racklist"+(i-13)+"'>Page Up</A>"+ 
    					"<A class='nav_button'  name='racklist"+i+"' href='#racklist"+i+"'>Page Down</A></TD></TR>")
    			}
				 
    			def doneQuery = new StringBuffer("select count(a.id) from AssetEntity a where a.${type}Location = ? and a.id in "+
    					"(select t.assetEntity from AssetTransition t where t.voided = 0 and t.stateTo = $walkthruState) and a.moveBundle = $moveBundle ")
				def args = [location]
    			def constructedQuery = constructQuery( rackList[0], rackList[1], location, type )
    			doneQuery.append( constructedQuery.query )
    			args = constructedQuery.args
    			doneQuery.append(" group by a.${type}Room, a.${type}Rack ")
    			def doneList = AssetEntity.executeQuery(doneQuery.toString(),args)
    			def availTotal = rackList[2] - (doneList[0] ? doneList[0] : 0) 
    			if(availTotal == 0){
    				cssClass = "asset_done"
				}
    			if( availTotal == 0 && viewType == 'todo' ){
    				flag = false
				}
				if(flag){
					rackListView.append("<tr class='$cssClass' onclick=\"showAssets('${moveBundle}','${location}','${rackList[0]}','${rackList[1]}')\">"+
							"<td class='center'>${rackList[0] ? rackList[0] : 'blank'}</td><td class='center'>${rackList[1] ? rackList[1] : 'blank'}</td>")
					rackListView.append("<td class='center'>${availTotal} of ${rackList[2]}</td></tr>")
			 	}
			 }
			 if(rackListSize > 10){
				 rackListView.append("<TR class='jump'><TD colSpan='3' align='middle'><A class='nav_button' href='#select_rack'>Top</A>"+ 
				 		"<A class='nav_button' href='#racklist6'>Page Up</A></TD></TR>")
			 }
			 if(!rackListView){
				 rackListView.append("<TR class='jump'><TD colSpan='3' align='middle' class='norecords_display'>No records found</TD></TR>")
			 }
		 } else {
			 rackListView.append("<TR class='jump'><TD colSpan='3' align='middle' class='norecords_display'>No records found</TD></TR>") 
		 }
		 return rackListView.toString()
	}
	/*------------------------------------------------------------
	 * @author : Lokanath Reddy
	 * @param  : rackslist, bundle, location, audit type and view type
	 * @return : Will return rack list view as string
	 *----------------------------------------------------------*/
	def generateAssetListView(def assetsList, def location, def auditType, def viewType){
    	def assetsListView = new StringBuffer()
    	if(assetsList){
    		def assetsListSize = assetsList.size()
    		for (int i = 0; i < assetsListSize; i++) {
    			def assetEntity = assetsList[i]
                def position = assetEntity?.sourceRackPosition
                if(auditType !="source"){
                	position = assetEntity?.targetRackPosition
                }
    			if(assetEntity?.moveBundle){
	    			def moveBundle = MoveBundle.findById( assetEntity?.moveBundle?.id )
	        		def walkthruState = stateEngineService.getStateId(moveBundle?.project?.workflowCode,"SourceWalkthru")
	    			def cssClass = "asset_ready"
	    			def flag = true
	    			if(i == 6 && assetsListSize != 7 ){
	    				assetsListView.append("<TR class=jump><TD align=middle colspan='3'>"+
	    						"<A class=nav_button name='assetList"+i+"' href='#assetList"+i+"'>Page Down</A></TD></TR>")
	    			} else if(i > 6 && (i - 6) % 13 == 0 && (assetsListSize - 7 ) % 13 != 0){
	    				assetsListView.append("<TR class=jump><TD colSpan=3 align=middle><A class=nav_button href='#select_asset'>Top</A>&nbsp;&nbsp;&nbsp;"+ 
	    					"<A class=nav_button href='#assetList"+(i-13)+"'>Page Up</A>&nbsp;&nbsp;&nbsp;"+ 
	    					"<A class=nav_button name='assetList"+i+"' href='#assetList"+i+"'>Page Down</A></TD></TR>")
	    			}
					def doneAsset = AssetTransition.find("from AssetTransition t where t.assetEntity = ${assetEntity.id} "+
														"and t.voided = 0 and t.stateTo = ${walkthruState}")
	    			if(doneAsset){
	    				cssClass = "asset_done"
					}
	    			if( doneAsset && viewType == 'todo' ){
	    				flag = false
					}
					if(flag){
						assetsListView.append("<tr class='$cssClass' onclick=\"showAssetMenu('${assetEntity?.id}','${assetEntity?.assetName}','${moveBundle?.id}','${moveBundle?.name}')\">"+
								"<td class='center'>${position}</td><td class='center'>${assetEntity?.assetTag}</td>"+
								"<td class='center'>${assetEntity?.assetName}</td></tr>")
				 	}
    			}
			 }
			 if(assetsListSize > 10){
				 assetsListView.append("<TR class=jump><TD colSpan=3 align=middle><A class=nav_button href='#select_asset'>Top</A>&nbsp;&nbsp;&nbsp;"+ 
				 		"<A class=nav_button href='#assetList6'>Page Up</A></TD></TR>")
			 }
			 if( !assetsListView ){
				 assetsListView.append("<TR class='jump'><TD colSpan='3' align='middle' class='norecords_display'>No records found</TD></TR>")
			 }
		 } else {
			 assetsListView.append("<TR class='jump'><TD colSpan='3' align='middle' class='norecords_display'>No records found</TD></TR>") 
		 }
		 return assetsListView.toString()
	}
	/*-----------------------------------------------------
	 * @author : Lokanath Reddy
	 * @param  : AuditRoom, AuditRack, AudutLocation
	 * @return : constructed query and args
	 *---------------------------------------------------*/
	def constructQuery(def auditRoom, def auditRack, def auditLocation, def type ){
		def args = [auditLocation]
        def query = new StringBuffer()
		if(auditRoom && auditRack ){
			query.append(" and a.${type}Room = ? and a.${type}Rack = ? ")
			args = [auditLocation,auditRoom,auditRack]
		} else if(!auditRoom && auditRack){
			query.append(" and (a.${type}Room is null or a.${type}Room = '') and a.${type}Rack = ? ")
			args = [auditLocation,auditRack]
		} else if(auditRoom && !auditRack){
			query.append(" and a.${type}Room = ? and (a.${type}Rack is null or a.${type}Rack = '' )")
			args = [auditLocation,auditRoom]
		} else {
			query.append(" and (a.${type}Room is null or a.${type}Room = '') and (a.${type}Rack is null or a.${type}Rack = '' )")
			args = [auditLocation]
		}
		return [ args : args, query : query.toString() ]
	}
}
