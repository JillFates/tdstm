import grails.converters.JSON
import org.jsecurity.SecurityUtils
import com.tdssrc.grails.GormUtil

class RackLayoutsController {
	def userPreferenceService
	def jdbcTemplate
	def supervisorConsoleService
	def static final statusDetails = ["missing":"Unknown", "cabledDetails":"Assigned","empty":"Empty","cabled":"Cabled"]
	def create = {
		def currProj = getSession().getAttribute( "CURR_PROJ" )
		def projectId = currProj.CURR_PROJ
		def projectInstance = Project.findById( projectId )
		def moveBundleInstanceList = MoveBundle.findAllByProject( projectInstance )
		userPreferenceService.loadPreferences("CURR_BUNDLE")
		def currentBundle = getSession().getAttribute("CURR_BUNDLE")?.CURR_BUNDLE
		/* set first bundle as default if user pref not exist */
		def isCurrentBundle = true
		if(!currentBundle){
			currentBundle = moveBundleInstanceList[0]?.id?.toString()
			isCurrentBundle = false
		}
		return [moveBundleInstanceList: moveBundleInstanceList, projectInstance:projectInstance, 
				currentBundle:currentBundle, isCurrentBundle : isCurrentBundle]
	}
	
	def save = {
		List bundleId = request.getParameterValues("moveBundle")
		def maxUSize = 42
		if(bundleId == "null") {
			return [errorMessage: "Please Select a Bundle."]
		} else {
			def includeOtherBundle = params.otherBundle
			def includeBundleName = params.bundleName
			def printQuantity = params.printQuantity
			def frontView = params.frontView
			def backView = params.backView
			def sourceRacks = new ArrayList()
			def targetRacks = new ArrayList()
			def projectId = getSession().getAttribute("CURR_PROJ").CURR_PROJ
			def rackLayout = []
			def project = Project.findById(projectId)
			def moveBundles = MoveBundle.findAllByProject( project )
			if(!bundleId.contains("all")){
				def bundlesString = bundleId.toString().replace("[","(").replace("]",")")
				moveBundles = MoveBundle.findAll("from MoveBundle m where id in ${bundlesString} ")
			}
			def isAdmin = SecurityUtils.getSubject().hasRole("PROJ_MGR")
			if( !isAdmin ) {
				isAdmin = SecurityUtils.getSubject().hasRole("PROJECT_ADMIN")
			}
			
			if(request.getParameterValues("sourcerack") != ['none']) {
				def rack = request.getParameterValues("sourcerack")
				if(rack[0] == "") {
					moveBundles.each{ bundle->
						bundle.sourceRacks.each{ sourceRack->
							if( !sourceRacks.contains( sourceRack ) )
								sourceRacks.add( sourceRack )		
						}
					}
				} else {
					rack.each {
						def thisRack = Rack.get(new Long(it))
						if( !sourceRacks.contains( thisRack ) )
							sourceRacks.add( thisRack )
					}
				}
				sourceRacks = sourceRacks.sort { it.tag }
			}

			if(request.getParameterValues("targetrack") != ['none']) {
				def rack = request.getParameterValues("targetrack")
				if(rack[0] == "") {
					moveBundles.each{ bundle->
						bundle.targetRacks.each{ targetRack->
							if( !targetRacks.contains( targetRack ) )
								targetRacks.add( targetRack	)
						}
					}
				} else {
					rack.each {
						def thisRack = Rack.get(new Long(it))
						if( !targetRacks.contains( thisRack ) )
							targetRacks.add( thisRack )
					}
				}
				targetRacks = targetRacks.sort { it.tag }
			}
			
			def racks = sourceRacks + targetRacks
			def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ	
			racks.each { rack ->
				def assetDetails = []
				def assetDetail = []
				def finalAssetList = []
				def racksByFilter
				if(includeOtherBundle){
					racksByFilter = rack.assets.findAll { it.assetType !='Blade' && it.moveBundle && it.project == project }.sort { rack?.source == 1 ? it.sourceRackPosition ? it.sourceRackPosition * -1 : 0 : it.targetRackPosition ? it.targetRackPosition * -1 : 0}
				} else {
					racksByFilter = rack.assets.findAll { it.assetType !='Blade' && moveBundles?.id?.contains(it.moveBundle?.id) && it.project == project }.sort { rack?.source == 1 ? it.sourceRackPosition ? it.sourceRackPosition * -1 : 0 : it.targetRackPosition ? it.targetRackPosition * -1 : 0}
				}
				racksByFilter.each { assetEntity ->
				
					def overlapError = false
					def rackPosition = rack.source == 1 ? assetEntity.sourceRackPosition : assetEntity.targetRackPosition
					if(rackPosition == 0 || rackPosition == null)
						rackPosition = 1
					
					def rackSize = assetEntity?.model?.usize == 0 || assetEntity?.model?.usize == null ? 1 : assetEntity?.model?.usize?.toInteger()
					def position = rackPosition + rackSize - 1
					def newHigh = position
					def newLow = rackPosition
					if(assetDetail.size() > 0) {
						def flag = true
						assetDetail.each { asset ->
							flag = true
							def currentHigh = asset.currentHigh
							def currentLow = asset.currentLow
							def ignoreLow = (currentLow <= newLow && currentHigh >= newHigh )
							def changeBoth = (currentLow >= newLow && currentHigh <= newHigh )
							def changeLow = (currentLow >= newLow && currentHigh >= newHigh && currentLow <= newHigh)
							def changeHigh = (currentLow <= newLow && currentHigh <= newHigh && currentHigh <= newLow)
							if(position > maxUSize) {
								asset.position = maxUSize
								asset.rowspan = 1 
								asset.assetTag = asset.assetTag +"<br/>"+assetEntity.assetTag+ ' ~- ' + assetEntity.assetName
								asset.overlapError = true
								asset.cssClass = "rack_error"
								flag = false
							} else if(ignoreLow) {
								asset.position = currentHigh
								asset.rowspan = currentHigh - currentLow + 1 
								asset.assetTag = asset.assetTag +"<br/>"+assetEntity.assetTag+ ' ~- ' + assetEntity.assetName
								asset.overlapError = true
								asset.cssClass = "rack_error"
								flag = false
							} else if(changeBoth) {
								asset.currentHigh = newHigh
								asset.currentLow = newLow
								asset.position = newHigh
								asset.rowspan = newHigh - newLow + 1
								asset.assetTag = asset.assetTag +"<br/>"+assetEntity.assetTag+ ' ~- ' + assetEntity.assetName
								asset.overlapError = true
								asset.cssClass = "rack_error"
								flag = false
							} else if(changeHigh) {
								asset.currentHigh = newHigh
								asset.position = newHigh
								asset.rowspan = newHigh - currentLow  + 1
								asset.assetTag = asset.assetTag +"<br/>"+assetEntity.assetTag+ ' ~- ' + assetEntity.assetName
								asset.overlapError = true
								asset.cssClass = "rack_error"
								flag = false
							} else if(changeLow) {
								asset.currentLow = newLow
								asset.position = currentHigh
								asset.rowspan = currentHigh - newLow +1
								asset.assetTag = asset.assetTag +"<br/>"+assetEntity.assetTag+ ' ~- ' + assetEntity.assetName
								asset.overlapError = true
								asset.cssClass = "rack_error"
								flag = false
							}
						}
							
						if(flag) {
							if(position > maxUSize) {
								position = maxUSize
								newLow = maxUSize
								//assetEntity?.model?.usize = 1
								overlapError = true
							}
							assetDetail << [assetEntity:assetEntity, assetTag:assetEntity.assetTag + ' ~- ' + assetEntity.assetName, position:position, overlapError:overlapError, 
											rowspan:rackSize, currentHigh : position, currentLow : newLow, source:rack.source ]
						}
					} else {
						if(position > maxUSize) {
							position = maxUSize
							newLow = maxUSize
							//assetEntity?.model?.usize = 1
							overlapError = true
						}
						assetDetail << [assetEntity:assetEntity, assetTag:assetEntity.assetTag + ' ~- ' + assetEntity.assetName, position:position, overlapError:overlapError, 
										rowspan:rackSize, currentHigh : position, currentLow : newLow, source:rack.source ]
					}
				}
				for (int i = maxUSize; i > 0; i--) {
					def cssClass = "empty"
					def rackStyle = "rack_past"
					def assetEnitiesAtPosition = assetDetail.findAll { it.position == i }

					if(assetEnitiesAtPosition.size() > 1) {
						cssClass = 'rack_error'
						rackStyle = 'rack_error'
	            		assetDetails<<[asset:assetEnitiesAtPosition[0], rack:i, cssClass:cssClass, rackStyle:rackStyle, source:rack.source ]
					} else if(assetEnitiesAtPosition.size() == 1) {
						def assetEnity = assetEnitiesAtPosition[0]
						if(assetEnity.overlapError) {
							cssClass = 'rack_error'
							rackStyle = 'rack_error'
						} else if(bundleId && !moveBundles?.id?.contains(assetEnity.assetEntity?.moveBundle?.id) ) {
							def currentTime = GormUtil.convertInToGMT( "now", tzId ).getTime()
							def startTime = assetEnity.assetEntity?.moveBundle?.startTime ? assetEnity.assetEntity?.moveBundle?.startTime.getTime() : 0
							if(startTime < currentTime){
								cssClass = 'rack_past'
							} else {
								cssClass = "rack_future"
							}
						} else {
							cssClass = 'rack_current'
							rackStyle = 'rack_current'
						}
						if(assetEnity.position == 0 || assetEnity.assetEntity?.model?.usize == 0 || assetEnity.assetEntity?.model?.usize == null ) {
							rackStyle = 'rack_error'
						}
						assetDetails << [asset:assetEnity, rack:i, cssClass:cssClass, rackStyle:rackStyle, source:rack.source ]
					} else {
						assetDetails << [asset:null, rack:i, cssClass:cssClass, rackStyle:rackStyle, source:rack.source ]
					}
				}
				def backViewRows
				def frontViewRows
				if(backView) {
					backViewRows = getRackLayout(isAdmin, assetDetails, includeBundleName, backView)
				}
				if(frontView) {
					frontViewRows = getRackLayout(isAdmin, assetDetails, includeBundleName, null)
				}
				rackLayout << [ assetDetails : assetDetails, rack : rack.tag , room : rack.room,
								frontViewRows : frontViewRows, backViewRows : backViewRows ]
			}
			return [rackLayout : rackLayout, frontView : frontView, backView : backView]
		}
	}
	
	def getRackDetails = {
		def bundleIds = params.bundles
		def moveBundles
		if(bundleIds.contains('all')){
			def projectId = getSession().getAttribute("CURR_PROJ").CURR_PROJ
			moveBundles = MoveBundle.findAllByProject( Project.get( projectId ) )
		} else {
			moveBundles = MoveBundle.findAll( "from MoveBundle m where m.id in ($bundleIds)" )
		}
		
		def sourceRacks = new ArrayList()
		def targetRacks = new ArrayList()
		
		moveBundles.each{moveBundle ->
			moveBundle.sourceRacks.each{
				if( !sourceRacks.contains(it) )
					sourceRacks.add(it)
			}
			moveBundle.targetRacks.each{
				if( !targetRacks.contains(it) )
					targetRacks.add(it)
			}
		}
		
		def rackDetails = [[sourceRackList:sourceRacks, targetRackList:targetRacks]]
		render rackDetails as JSON
	}

	private getRackLayout( def isAdmin, def asset, def includeBundleName, def backView){
		def rows = new StringBuffer()
		def rowspan = 1
		def cssClass = "empty"
		def rackStyle = ""
		asset.each {
			def row = new StringBuffer("<tr>")
			if(it.asset) {
				rowspan = it.asset?.rowspan != 0 ? it.asset?.rowspan : 1
				rackStyle = it.rackStyle
				def location = it.source
				def assetEntity = it.asset?.assetEntity
				def assetTagsList = (it.asset?.assetTag).split("<br/>")
				def moveBundle = "" 
				def assetTag = ""
				if(it.cssClass == "rack_error")
					assetTag += "Devices Overlap:<br />"
						def hasBlades = false
				assetTagsList.each{ assetTagValue ->
					def index = assetTagValue.indexOf('~-')
					def tagValue
					if (index != -1) {
						tagValue = assetTagValue?.substring(0,index)
					} else {
						tagValue = assetTagValue
					}
					def overlappedAsset
					def overlappedAssets
					def bladeTable = ""
					if(location == 1)
						overlappedAssets = AssetEntity.findAllWhere( project:assetEntity.project, assetTag : tagValue, sourceRack: assetEntity.sourceRack )
					else 
						overlappedAssets = AssetEntity.findAllWhere( project:assetEntity.project, assetTag : tagValue, targetRack: assetEntity.targetRack )
					if(overlappedAssets.size() > 1) {
						overlappedAssets.each{ overlapAsset ->
							moveBundle += (overlapAsset?.moveBundle ? overlapAsset?.moveBundle.name : "") + "<br/>"
							if(overlappedAsset.model && overlapAsset.model.assetType == 'Blade Chassis'){
								hasBlades = true
								bladeTable = generateBladeLayout(it, overlapAsset, isAdmin)
							}
							if(isAdmin)
								assetTag += "<a href='javascript:openAssetEditDialig(${overlapAsset?.id})' >"+trimString(assetTagValue.replace('~-','-'))+"</a> <br/>"+bladeTable+"</br>"
							else
								assetTag += trimString(assetTagValue.replace('~-','-'))+"<br/>"+bladeTable+"</br>"
						}
					} else if(overlappedAssets.size() > 0){
						overlappedAsset = overlappedAssets[0]
						moveBundle += (overlappedAsset?.moveBundle ? overlappedAsset?.moveBundle.name : "") + "<br/>"
						if(overlappedAsset.model && overlappedAsset.model.assetType == 'Blade Chassis'){
							hasBlades = true
							bladeTable = generateBladeLayout(it, overlappedAsset,isAdmin)
						}
						if(isAdmin)
							assetTag += "<a href='javascript:openAssetEditDialig(${overlappedAsset?.id})' >"+trimString(assetTagValue.replace('~-','-'))+"</a> <br/>"+bladeTable+"</br>"
						else
							assetTag += trimString(assetTagValue.replace('~-','-'))+"<br/>"+bladeTable+"</br>"
					}
				}
					
				if( hasBlades ){
					row.append("<td class='${it.rackStyle}'>${it.rack}</td><td colspan='2' rowspan='${rowspan}' class='${it.cssClass}'>${assetTag}</td>")
				} else {
					row.append("<td class='${it.rackStyle}'>${it.rack}</td><td rowspan='${rowspan}' class='${it.cssClass}'>${assetTag}</td>")
					if(includeBundleName)
						row.append("<td rowspan='${rowspan}' class='${it.cssClass}'>${moveBundle}</td>")
					else
						row.append("<td rowspan='${rowspan}' class='${it.cssClass}'></td>")
				}
				
				if(backView) {
					if(it.cssClass != "rack_error") {
						/*def cablingString = "${it.asset?.assetEntity?.pduPort ? 'PDU: '+ it.asset?.assetEntity?.pduPort +' | ' : '' }"+ 
											"${it.asset?.assetEntity?.nicPort ? 'NIC: '+ it.asset?.assetEntity?.nicPort +' | ' : ''}"+
											"${it.asset?.assetEntity?.kvmDevice && it.asset?.assetEntity?.kvmDevice != 'blank / blank'? 'KVM: '+ it.asset?.assetEntity?.kvmDevice +' | ' : ''}"+
											"${it.asset?.assetEntity?.remoteMgmtPort ? 'RMgmt: '+ it.asset?.assetEntity?.remoteMgmtPort +' | ': ''}"+
											"${it.asset?.assetEntity?.fiberCabinet && it.asset?.assetEntity?.fiberCabinet != 'blank / blank / blank' ? 'Fiber: '+ it.asset?.assetEntity?.fiberCabinet +' | ' : ''}"
						
						if ( cablingString )
							cablingString = cablingString.substring( 0, cablingString.length() - 2 )
						*/
						def assetCables = AssetCableMap.findByFromAsset(it.asset?.assetEntity)
						if ( assetCables )
							row.append("<td rowspan='${rowspan}' class='${it.cssClass}'><a href='#' onclick='openCablingDiv(${it.asset?.assetEntity.id})'>view</a></td>")
						else
							row.append("<td rowspan='${rowspan}' class='${it.cssClass}'>&nbsp;</td>")
						
					} else {
						row.append("<td rowspan='${rowspan}' class='${it.cssClass}'>Devices Overlap</td>")
					}
				}
			} else if(rowspan <= 1) {
				rowspan = 1
				rackStyle = it.rackStyle
				row.append("<td class='empty'>${it.rack}</td><td rowspan=1 class=${it.cssClass}>&nbsp;</td><td>&nbsp;</td>")
				if(backView)
					row.append("<td>&nbsp;</td>")
				
			} else {
				row.append("<td class='${rackStyle}'>${it.rack}</td>")
				rowspan--
			}
			// Remove right U-position number 
			//row.append("<td class='${rackStyle}'>${it.rack}</td>")
			row.append("</tr>")
			rows.append(row.toString())
		}
		return rows
	}
	/*************************************************
	 * Construct Balde layout for RackLayouts report
	 **************************************************/
	def generateBladeLayout(def assetDetails, def assetEntity, def isAdmin){
		
		def bladeTable = '<table class="bladeTable"><tr>'
		def rowspan = assetDetails.asset?.rowspan != 0 ? assetDetails.asset?.rowspan : 1
		def tdHeight = rowspan * 7
		def blades = []
		if(assetDetails.asset.source == 1)
			blades = AssetEntity.findAllWhere(project:assetEntity.project, moveBundle:assetEntity.moveBundle, assetType:'Blade', sourceBladeChassis:assetEntity.assetTag)
		else
			blades = AssetEntity.findAllWhere(project:assetEntity.project, moveBundle:assetEntity.moveBundle, assetType:'Blade', targetBladeChassis:assetEntity.assetTag)

		def fullRows = []
		def chassisRows = assetEntity.model.bladeRows
		def bladesPerRow = (assetEntity.model.bladeCount / chassisRows ).intValue()
		def bladeLabelCount = assetEntity.model.bladeLabelCount

		for(int k = 1; k <= chassisRows; k++){
			int initialColumn = (k-1)*bladesPerRow + 1
			for(int i = initialColumn; i <= k*bladesPerRow; i++){
				def matching = []
				if(assetDetails.asset.source == 1)
					matching = blades.findAll { it.sourceBladePosition == i }
				else
					matching = blades.findAll { it.targetBladePosition == i }
				
				if(fullRows.contains(i))
					bladeTable += ''
				else if(matching.size() > 1)
					bladeTable += "<td class='errorBlade' style='height:${tdHeight}px'>Conflict</td>"
				else if(matching.size() == 1) {
					def blade = matching[0]
					def tag = blade.assetTag
					if(tag.length() >= bladeLabelCount){
						tag = tag.substring(0,bladeLabelCount)
					}
					tag = tag.split('')[1..-1].join('<br/>')
					def taglabel = "<div>"+tag.substring(0,tag.length())+"</div>"
					def bladeSpan = blade.bladeSize == 'Full' ? chassisRows : 1
					if(bladeSpan == chassisRows){
						for(int y = i; y <= chassisRows*bladesPerRow; y += bladesPerRow ){
							fullRows << y + bladesPerRow
						}
					}
					def hasError = assetDetails.asset.source == 1 ? blades.findAll { it.sourceBladePosition == i + bladeLabelCount }.size() > 0 : blades.findAll { it.targetBladePosition == i + bladeLabelCount }.size() > 0
					if((bladeSpan == 2) &&  hasError )
						bladeTable += "<td class='errorBlade' style='height:${tdHeight}px'>&nbsp;</td>"
					else if(isAdmin)
						bladeTable += "<td class='blade' rowspan='${bladeSpan}' style='height:${tdHeight}px'><a href='javascript:openAssetEditDialig(${blade.id})' title='${tag.replace('<br/>','')}'>${taglabel}</a></td>"
					else
						bladeTable += "<td class='blade' rowspan='${bladeSpan}' style='height:${tdHeight}px' title='${tag.replace('<br/>','')}'>${taglabel}</td>"
				} else
					bladeTable += "<td class='emptyBlade' style='height:${tdHeight}px'>&nbsp;</td>"
			}
			bladeTable += k == chassisRows ? "</tr>" : "</tr><tr>"
		}
		
		bladeTable += '</table>'
	}
	/********************************************
	 * Trim Name if over 30 characters and add "...".
	 ********************************************/
	def private trimString( name ){
		def trimmedVal = name
		def length = name.length()
		if(length > 30){
			trimmedVal = trimmedVal.substring(0,30)+"..."
		}
		return trimmedVal
	}
	def modelTemplate = {
			return [params:params]
	}
	/*
	 * Return AssetCableMap record details to display at RackLayout cabling screen
	 */
	def getCablingDetails = {
		def assetId = params.assetId
		def assetEntity = assetId ? AssetEntity.get(assetId) : null
		def assetCableMapList
		def title =  ""
		if( assetEntity ){
			assetCableMapList = AssetCableMap.findAllByFromAsset( assetEntity )
			title = assetEntity.assetName+" ( "+assetEntity.model.manufacturer+" / "+assetEntity.model+" )"
		}
		def assetCablingDetails = []
		assetCableMapList.each {
			def rackUposition = it.toConnectorNumber ? it.toAssetRack+"/"+it.toAssetUposition+"/"+it.toConnectorNumber.label : ""
			def toAssetId
			def toTitle =  ""
			if(it.fromConnectorNumber.type == "Power"){
				rackUposition = it.toPower ? it.toAssetRack+"/"+it.toAssetUposition+"/"+it.toPower : ""
			} else if(it.toAsset){
				toAssetId = it.toAsset.id
				toTitle = it.toAsset.assetName+" ( "+it.toAsset.model.manufacturer+" / "+it.toAsset.model+" )"
			}
			
			assetCablingDetails << [model:assetEntity.model.id, id:it.id, connector : it.fromConnectorNumber.connector, 
									type:it.fromConnectorNumber.type, connectorPosX:it.fromConnectorNumber.connectorPosX,
									labelPosition:it.fromConnectorNumber.labelPosition,
									connectorPosY:it.fromConnectorNumber.connectorPosY, status:it.status,displayStatus:statusDetails[it.status], 
									label:it.fromConnectorNumber.label, hasImageExist:assetEntity.model.rearImage && assetEntity.model?.useImage ? true : false,
									usize:assetEntity?.model?.usize, rackUposition : rackUposition, toAssetId : toAssetId, toTitle:toTitle, title:title]
		}
		render assetCablingDetails as JSON
	}
	/*
	 * Update the AssetCablingMap with the date send from RackLayout cabling screen
	 */
	def updateCablingDetails = {
		def assetId = params.asset
		def assetCableId = params.assetCable
		
		if(assetCableId){
			def currProj = getSession().getAttribute( "CURR_PROJ" )
			def projectId = currProj.CURR_PROJ
			def project = Project.get( projectId )
			def actionType = params.actionType
			def status = "missing"
			def toAssetRack
			def toAssetUposition
			def toConnector
			def toAsset
			def toPower
			def connectorType = params.connectorType
			def assetCableMap = AssetCableMap.findById( assetCableId )
			if(connectorType != "Power"){
				def fromAssetCableMap = AssetCableMap.findByToAssetAndToConnectorNumber( assetCableMap.fromAsset, assetCableMap.fromConnectorNumber )
				if(fromAssetCableMap){
					AssetCableMap.executeUpdate("""Update AssetCableMap set status='missing', toAsset=null,toConnectorNumber=null,toAssetRack=null,toAssetUposition=null
												where toAsset = ? and toConnectorNumber = ?""",[assetCableMap.fromAsset, assetCableMap.fromConnectorNumber])
			}
			}
			switch(actionType){
				case "emptyId" : status = "empty" ; break;
				case "cabledId" : status = "cabled"; break;
				case "assignId" : 
					status = "cabledDetails"; 
					if(connectorType != "Power"){
						toAssetRack = params.rack
						toAssetUposition = params.uposition
						def rack = Rack.findWhere(tag:toAssetRack,source:0,project:project)
						def assetEntity = rack?.targetAssets?.find{it.targetRackPosition == Integer.parseInt(params.uposition)}
						def modelConnectors
						if(assetEntity?.model){
							modelConnectors = ModelConnector.findAllByModel(assetEntity?.model)
							toAsset = assetEntity 
							toConnector = modelConnectors?.find{it.label.equalsIgnoreCase(params.connector) }
							AssetCableMap.executeUpdate("""Update AssetCableMap set status='missing',toAsset=null,
															toConnectorNumber=null,toAssetRack=null,toAssetUposition=null
															where toAsset = ? and toConnectorNumber = ?""",[toAsset, toConnector])
						}
					} else {
						toAsset = assetCableMap.fromAsset
						toAssetRack = assetCableMap?.fromAsset?.rackTarget?.tag
						toAssetUposition = 0
						toConnector = null
						toPower = params.staticConnector
					}
					break;
			}
			assetCableMap.status = status
			assetCableMap.toAsset = toAsset
			assetCableMap.toConnectorNumber = toConnector
			assetCableMap.toAssetRack = toAssetRack
			assetCableMap.toAssetUposition = connectorType != "Power" ? toAsset?.targetRackPosition : toAssetUposition
			assetCableMap.toPower = toPower
			
			if(assetCableMap.save(flush:true) && toAsset && connectorType != "Power"){
				def toAssetCableMap = AssetCableMap.findByFromAssetAndFromConnectorNumber( toAsset, toConnector )
				toAssetCableMap.status = status
				toAssetCableMap.toAsset = assetCableMap.fromAsset
				toAssetCableMap.toConnectorNumber = assetCableMap.fromConnectorNumber
				toAssetCableMap.toAssetRack = assetCableMap.fromAsset?.rackTarget?.tag
				toAssetCableMap.toAssetUposition = assetCableMap.fromAsset?.targetRackPosition
			}
		}
		def assetCable = [assetId : assetId, assetTag : AssetEntity.get(assetId)?.assetTag ]
		render assetCable as JSON
	}
	/*
	 *  Provide the Rack auto complete details and connector, uposition validation 
	 */
	def getAutoCompleteDetails = {
		def currProj = getSession().getAttribute( "CURR_PROJ" )
		def projectId = currProj.CURR_PROJ
		def project = Project.get( projectId )
		def data
		def field = params.field
		def value = params.value
		switch(field){
			case "rack" :
				data = Rack.executeQuery( "select distinct r.tag from Rack r where r.source = 0 and r.project = $projectId " )
				break;
			case "isValidRack":
				data = Rack.findAllWhere(tag:value,source:0,project:project)
				break;
			case "uposition":
				def rack = Rack.findWhere(tag:params.rack,source:0,project:project)
				data = rack?.targetAssets?.targetRackPosition
				break;
			case "isValidUposition":
				def rack = Rack.findWhere(tag:params.rack,source:0,project:project)
				data = rack?.targetAssets?.findAll{it.targetRackPosition == Integer.parseInt(params.value)} 
				break;
			case "connector":
				def rack = Rack.findWhere(tag:params.rack,source:0,project:project)
				def assetEntity = rack?.targetAssets?.findAll{it.targetRackPosition == Integer.parseInt(params.uposition)}
				def modelConnectors
				if(assetEntity?.model[0])
					data = ModelConnector.findAllByModel(assetEntity?.model[0])?.label
				break;
			case "isValidConnector":
				def rack = Rack.findWhere(tag:params.rack,source:0,project:project)
				def assetEntity = rack?.targetAssets?.findAll{it.targetRackPosition == Integer.parseInt(params.uposition)}
				def modelConnectors
				if(assetEntity?.model[0])
					modelConnectors = ModelConnector.findAllByModel(assetEntity?.model[0])
				data = modelConnectors?.findAll{it.label.equalsIgnoreCase(params.value) }
				break;
		}
		if(!data)
			data = []
		render data as JSON
	}
}
