package net.transitionmanager.service

import com.tds.asset.*
import com.tdsops.tm.enums.domain.AssetClass
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.StringUtil
import com.tdssrc.grails.TimeUtil
import com.tdssrc.grails.WorkbookUtil
import grails.transaction.Transactional
import groovy.util.logging.Slf4j
import net.transitionmanager.domain.DataTransferAttributeMap
import net.transitionmanager.domain.DataTransferSet
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Rack
import net.transitionmanager.domain.Room
import net.transitionmanager.domain.UserLogin
import net.transitionmanager.utils.Profiler
import org.apache.poi.openxml4j.util.ZipSecureFile
import org.apache.poi.ss.usermodel.*
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.web.json.JSONObject
import org.hibernate.*
import org.hibernate.transform.Transformers

@Slf4j
@Transactional
class AssetExportService {
    private static final ASSET_EXPORT_TEMPLATE = "/templates/TDSMaster_template.xlsx"
    private static final DEFAULT_EXPORT_FILE_EXTENSION = "xlsx"
    // Indicates the number of rows to process before performing a flush/clear of the Hibernate session queue
    private static final int HIBERNATE_BATCH_SIZE = 1000
    private static double MIN_INFLATE_RATIO = 0.0001d
    private static final String ALL_BUNDLES_OPTION = 'All'

    // TODO : JPM 9/2014 : determine if bundleMoveAndClientTeams is used as the team functionality has been RIPPED out of TM
    private static BUNDLE_MOVE_AND_CLIENT_TEAMS = ['sourceTeamMt', 'sourceTeamLog', 'sourceTeamSa', 'sourceTeamDba', 'targetTeamMt', 'targetTeamLog', 'targetTeamSa', 'targetTeamDba']

    GrailsApplication grailsApplication
    SessionFactory sessionFactory
    ProgressService progressService
    ProjectService projectService
    PartyRelationshipService partyRelationshipService
    CustomDomainService customDomainService

    /**
     * This is the actual method that will generate the Excel export which it typically called by a Quartz
     * job so that the user request can happen quickly. The job then will post progress updates to report back
     * to the user referencing the params.key.
     * The user's system timezone is critical to computing the datetimes in the spreadsheet since the application
     * runs in GMT but Excel/Apache POI assume that the spreadsheet is in the system timezone. So if we generate a
     * spreadsheet and stuff a
     * @param Datatransferset,Project,Movebundle
     *		params.key - the key to reference for progress status
     *		params.projectId - the project for which to export assets
     *		params.bundle - the bundle to export
     *		params.dataTransferSet -
     *		params.username - the user that made the request
     *		params.tzId - the user's timezone (IMPORTANT that this be their system TZ and not their user preference*)
     * 		params.userDTFormat - the user's Date Format (MIDDLE_ENDIAN, LITTLE_ENDIAN)
     *		params.asset - flag to export devices
     *		params.application - flag to export apps
     *		params.database - flag to export databases
     *		params.files - flag to export storage
     *		params.dependency - flag to export dependencies
     *		params.room - flag to export room details
     *		params.rack - flag to export rack data
     *		params.cable - flag to export cabling
     *		params.comment - flag to export comments
     **/
    void export(params) {
        def key = params.key
        def projectId = params.projectId

        boolean profilerEnabled = params[Profiler.KEY_NAME]==Profiler.KEY_NAME
        Profiler profiler = Profiler.create(profilerEnabled, key)

        final String mainProfTag = 'EXPORT'
        profiler.beginInfo(mainProfTag)

        // Helper closure that returns the size of an object or zero (0) if it is null
        // <SL> Use CollectionUtils.size()
        def sizeOf = { obj -> (obj ? obj.size : 0)}

        try {
            def progressCount = 0
            def progressTotal = 0
            def missingHeader = ""

            //get project Id
            def dataTransferSet = params.dataTransferSet

            def principal = params.username
            UserLogin loginUser = UserLogin.findByUsername(principal)

            List<String> bundle = params.bundle

            // Set flag if Use for Planning was one of the choosen options and remove from the list
            boolean useForPlanning = bundle.remove(MoveBundle.USE_FOR_PLANNING)
            boolean allBundles = bundle.remove(ALL_BUNDLES_OPTION)
            int bundleSize = bundle.size()

            // Determine what will be used for the name of the file + what is shown in the title sheet
            // Examples:
            //      project-BundleName-AssetTypes-date.xlsx
            //      project-2Bundles-AssetTypes-date.xlsx
            //      project-All-AssetTypes-date.xlsx
            //      project-Planning-AssetTypes-date.xlsx
            //      project-Planning+2+Bundles-AssetTypes-date.xlsx

            StringBuilder bundleNameList = new StringBuilder()

            if (allBundles) {
                bundleNameList.append('All Bundles')
            } else {
                if (useForPlanning) {
                    bundleNameList.append('Planning')
                    if (bundleSize) {
                        bundleNameList.append("+$bundleSize bundle" + (bundleSize > 1 ? 's' : ''))
                    }
                } else {
                    if (bundleSize==1) {
                        MoveBundle mb = MoveBundle.read( bundle[0] )
                        bundleNameList.append(mb.name)
                    } else {
                        bundleNameList.append("$bundleSize bundles")
                    }
                }
            }

            def dataTransferSetInstance = DataTransferSet.get( dataTransferSet )

            def project = Project.get(projectId)
            if ( project == null) {
                progressService.update(key, 100, 'Cancelled', 'Project is required')
                return
            }

            // Maps for each class for mapping spreadsheet column names to the domain properties
            // it contains the field specs mapping by AssetClass
            List<Map<String, ?>> serverDTAMap, appDTAMap, dbDTAMap, fileDTAMap

            // Will hold the list of the assets for each of the classes
            List asset, application, database, files

            Sheet serverSheet, appSheet, dbSheet, storageSheet, titleSheet
            def exportedEntity = ""

            def serverMap = [:]
            def appMap = [:]
            def dbMap = [:]
            def fileMap = [:]

            // Flags to indicate which tabs to export based on which checkboxes selected in the UI
            boolean doDevice = params.asset=='asset'
            boolean doApp = params.application=='application'
            boolean doDB = params.database=='database'
            boolean doStorage = params.files=='files'
            boolean doDependency = params.dependency=='dependency'
            boolean doRoom = params.room=='room'
            boolean doRack = params.rack=='rack'
            boolean doCabling = params.cabling=='cable'
            boolean doComment = params.comment=='comment'

            // Queries for main assets
            String deviceQuery = null
            String applicationQuery = null
            String databaseQuery = null
            String filesQuery = null

            //
            // Load the asset lists and property map files based on ALL or selected bundles for the classes selected for export
            //
            //def session = sessionFactory.currentSession
            String query = ' WHERE d.project=:project '
            Map<String, ?> queryParams = [project:project]

            if (! allBundles && useForPlanning) {
                query += ' AND d.moveBundle.useForPlanning = TRUE '
            }

            // Setup for multiple bundle selection
            if (! allBundles && bundleSize) {
                List bundleIds = []
                for ( int i=0; i < bundleSize ; i++ ) {
                    Long bid = NumberUtil.toPositiveLong(bundle[i], -1)
                    if (bid > 0) {
                        // TODO : Check if bundle is in the project
                        bundleIds << bid
                    }
                }
                String bundleStr = bundleIds.join(',')
                query += " AND d.moveBundle.id IN(${bundleStr})"
                //queryParams.bundles = bundles
            }

            /*
                The following size related variables are used to
                update the progress meter.
            */
            int assetSize = 0
            int appSize = 0
            int dbSize = 0
            int fileSize = 0
            int roomSize = 0
            int rackSize = 0
            int commentSize = 0
            int cablingSize = 0
            int dependencySize = 0

            // Workbook cell styles
            Map<Integer, CellStyle> workbookCellStyles = [:]

            def getAssetList = {q, qParams ->
                def hqlQuery = sessionFactory.currentSession.createQuery(q)
                hqlQuery.setReadOnly(true)
                //hqlQuery.setFetchSize(HIBERNATE_BATCH_SIZE)
                qParams.each{k, v ->
                    hqlQuery.setParameter(k, v)
                }
                return hqlQuery.list()
            }

            def getAssetListScrollable = { Session  session, String q, Map qParams ->
                def hqlQuery = session.createQuery(q)
                hqlQuery.setReadOnly(true)
                qParams.each { k, v ->
                    hqlQuery.setParameter(k, v)
                }
                return hqlQuery.scroll(ScrollMode.FORWARD_ONLY)
            }

            profiler.lap(mainProfTag, 'Initial loading')

            def countRows = { q, qParams ->
                def tmpQueryStr = "SELECT COUNT(*) " + q
                def countQuery = sessionFactory.currentSession.createQuery(tmpQueryStr)
                qParams.each{ k, v ->
                    countQuery.setParameter(k,v)
                }
                return ((Long)countQuery.uniqueResult()).intValue()
            }

            if ( doDevice ) {
                deviceQuery = "FROM AssetEntity d " + query + " AND d.assetClass='${AssetClass.DEVICE}'"
                assetSize = countRows(deviceQuery, queryParams)
                progressTotal += assetSize
            }
            if ( doApp ) {
                applicationQuery = "FROM Application d" + query
                appSize = countRows(applicationQuery, queryParams)
                progressTotal += appSize
            }
            if ( doDB ) {
                databaseQuery = "FROM Database d" + query
                dbSize = countRows(databaseQuery, queryParams)
                progressTotal += dbSize
            }
            if ( doStorage ) {
                filesQuery = "FROM Files d" + query
                fileSize = countRows(filesQuery, queryParams)
                progressTotal += fileSize
            }

            if (doDependency) {
                dependencySize  = AssetDependency.executeQuery("SELECT COUNT(*) FROM AssetDependency WHERE asset.project = ? ",[project])[0]
                progressTotal += dependencySize
            }

            if (doRoom) {
                roomSize = Room.executeQuery("SELECT COUNT(*) FROM Room WHERE project = ?", [project])[0]
                progressTotal += roomSize
            }

            if (doRack) {
                rackSize = Rack.executeQuery("SELECT COUNT(*) FROM Rack WHERE project = ?", [project])[0]
                progressTotal += rackSize
            }

            if (doCabling) {
                cablingSize = AssetCableMap.executeQuery( "select count(*) from AssetCableMap acm where acm.assetFrom.project = ?", [project])[0]
                progressTotal += cablingSize
            }

            if (doComment) {
                commentSize = AssetComment.executeQuery("SELECT COUNT(*) FROM AssetComment WHERE project = ? AND commentType = 'comment' AND assetEntity IS NOT NULL", [project])[0]
                progressTotal += commentSize
            }

            profiler.lap(mainProfTag, 'Got row counts')

            // This variable is used to determine when to call the updateProgress
            float updateOnPercent = 0.01

            // Used by the profile sampling to determine # of rows to profile and how often within the dataset
            double percentToProfile = 25.0      // Sample 5% of all of data
            double frequencyToProfile = 5.0     // Sample the data every 5% of the way

            // Have to load the maps because we update the column names across the top for all sheets
            serverDTAMap = getFieldSpecsForAssetClass(AssetClass.DEVICE)
            appDTAMap = getFieldSpecsForAssetClass(AssetClass.APPLICATION)
            dbDTAMap = getFieldSpecsForAssetClass(AssetClass.DATABASE)
            fileDTAMap = getFieldSpecsForAssetClass(AssetClass.STORAGE)

            def tzId = params.tzId
            def userDTFormat = params.userDTFormat
            def currDate = TimeUtil.nowGMT()
            def exportDate = TimeUtil.formatDateTimeWithTZ(tzId, userDTFormat, currDate, TimeUtil.FORMAT_DATE_TIME_5)

            profiler.lap(mainProfTag, 'Loaded DTAMaps')

            String adbSql = 'select adb.asset.id, adb.dependencyBundle from AssetDependencyBundle adb where project=:project'
            List assetDepBundleList = AssetDependencyBundle.executeQuery(adbSql, [project:project])
            Map assetDepBundleMap = new HashMap(assetDepBundleList.size())
            assetDepBundleList.each {
                assetDepBundleMap.put(it[0].toString(), it[1])
            }
            profiler.lap(mainProfTag, 'Created asset dep bundles')

            // Prevent! Zip bomb
            // Error: Zip bomb detected! The file would exceed the max. ratio of compressed file size to the size of the expanded data
            // Sets the ratio between de- and inflated bytes to detect zipbomb.
            ZipSecureFile.setMinInflateRatio(MIN_INFLATE_RATIO)

            Workbook initWorkbook = getInitWorkbookInstance()
            profiler.lap(mainProfTag, 'Loaded workbook template')

            // Load maps appropriately based on if we're exporting the tab
            def serverCol, appCol, dbCol, filesCol

            // Device
            String serverSheetName = "Devices"
            serverSheet = WorkbookUtil.getSheetFromWorkbook(initWorkbook, serverSheetName)
            serverMap = mapSheetColumnsToFields(serverSheet, serverDTAMap)

            // Application
            String appSheetName = "Applications"
            appSheet = WorkbookUtil.getSheetFromWorkbook(initWorkbook, appSheetName)
            appMap = mapSheetColumnsToFields(appSheet, appDTAMap)

            // Database
            String dbSheetName = "Databases"
            dbSheet = WorkbookUtil.getSheetFromWorkbook(initWorkbook, dbSheetName)
            dbMap = mapSheetColumnsToFields(dbSheet, dbDTAMap)

            // Storage
            String fileSheetName = "Storage"
            storageSheet = WorkbookUtil.getSheetFromWorkbook(initWorkbook, fileSheetName)
            fileMap = mapSheetColumnsToFields(storageSheet, fileDTAMap)

            // Rack
            def rackSheetColumns = []
            String rackSheetName = "Rack"
            def rackSheet = WorkbookUtil.getSheetFromWorkbook(initWorkbook, rackSheetName)
            Row refRackRow = rackSheet.getRow(0)
            for ( int c = 0; c < refRackRow.getLastCellNum(); c++ ) {
                rackSheetColumns << refRackRow.getCell(c).getStringCellValue()
            }

            profiler.lap(mainProfTag, 'Read Spreadsheet Tabs')

            // Helper closure to create a text list from an array for debugging
            def xportList = { list ->
                def out = ''
                def x = 0
                list.each { out += "$x=$it\n"; x++}
                return out
            }

            //Add Title Information to master SpreadSheet
            titleSheet = WorkbookUtil.getSheetFromWorkbook(initWorkbook, "Title")
            addCell(titleSheet, 1, 2, project.client.toString())
            addCell(titleSheet, 1, 3, projectId.toString())
            addCell(titleSheet, 2, 3, project.name.toString())
            addCell(titleSheet, 1, 4, partyRelationshipService.getProjectManagers(project))
            addCell(titleSheet, 1, 5, bundleNameList.toString())
            addCell(titleSheet, 1, 6, loginUser.person.toString())

            def exportedOn = TimeUtil.formatDateTimeWithTZ(tzId, userDTFormat, new Date(), TimeUtil.FORMAT_DATE_TIME_22)
            addCell(titleSheet, 1, 7, exportedOn)
            addCell(titleSheet, 1, 8, tzId)
            addCell(titleSheet, 1, 9, userDTFormat)

            addCell(titleSheet, 30, 0, "Note: All times are in ${tzId ? tzId : 'EDT'} time zone")

            profiler.lap(mainProfTag, 'Updated title sheet')

            // update column header
            updateColumnHeaders(AssetClass.DEVICE, serverSheet, serverMap)
            updateColumnHeaders(AssetClass.APPLICATION, appSheet, appMap)
            updateColumnHeaders(AssetClass.DATABASE, dbSheet, dbMap)
            updateColumnHeaders(AssetClass.STORAGE, storageSheet, fileMap)

            profiler.lap(mainProfTag, 'Updating spreadsheet headers')

            def validationSheet = WorkbookUtil.getSheetFromWorkbook(initWorkbook, "Validation")

            Map optionsSize = [:]

            def writeValidationColumn = { assetOptions, col, optionKey ->
                assetOptions.eachWithIndex{ option, idx ->
                    addCell(validationSheet, col, idx+1, "${option.value}")
                }
                optionsSize[optionKey] = assetOptions.size() + 1 // +1 because of the header.
            }

            def writeValidationSheet = {
                profiler.beginInfo "Validations"
                profiler.lap("Validations", "Starting to export Validation values")
                int col = 0
                writeValidationColumn(getAssetEnvironmentOptions(), col++, "Environment")
                writeValidationColumn(getAssetPriorityOptions(), col++, "Priority")
                writeValidationColumn(getAssetPlanStatusOptions(), col++, "PlanStatus")
                writeValidationColumn(getDependencyTypes(), col++, "DepType")
                writeValidationColumn(getDependencyStatuses(), col, "DepStatus")
                WorkbookUtil.makeSheetReadOnly(validationSheet)
                profiler.lap("Validations", "Finished exporting Validation values")
                profiler.endInfo("Validations", "Finished writing validation values to sheet.")
            }

            writeValidationSheet()

            def calcProfilerCriteria = { datasetSize ->
                int sampleQty = 1
                int sampleModulus = 1
                // int numOfSampleSets = (int)(1/frequencyToProfile)
                int numOfSampleSets = Math.round(100/frequencyToProfile)

                if (datasetSize > 0) {
                    sampleQty = Math.round( (datasetSize * percentToProfile / 100) / numOfSampleSets)
                    if (sampleQty == 0) {
                        sampleQty = 1
                    }

                    // sampleModulus = (int)(datasetSize / numOfSampleSets)
                    sampleModulus = Math.floor(datasetSize / numOfSampleSets)
                    if (sampleModulus == 0) {
                        sampleModulus = datasetSize
                    }
                }

                return [sampleQty, sampleModulus]
            }

            // The threshold (milliseconds) to warning on when the processing time is exceeded for a row
            Map profileRowThresholds = [
                    (AssetClass.DEVICE): 30,
                    (AssetClass.APPLICATION): 30
            ]

            // The maximum # of row level threshold violations to log
            final int profileThresholdLogLimit = 1000

            // Will log if setting a field exceeds this threadhold (msec)
            final int profileThresholdSettingField = 2

            final String thresholdWarnMsg = 'A total of %d row(s) exceeded the duration threshold of %d ms'

            // The following variables are used to control the profiling behavior
            //
            // Counter used to count the number of threshold violations
            int profileThresholdViolations
            int profileHighwaterMark = 0
            String profileHighwaterAsset = ''
            int profileSampleQty
            int profileSampleModulus
            int profileSamplingTics
            long lapDuration
            boolean profilingRow
            Map durationMatrix = [:]

            profiler.log(Profiler.LOG_TYPE.INFO, 'Initialization took (%s)', [profiler.getSinceStart(mainProfTag)])



            /***************************************************************************/

            // Save initialization workbook and use it as template for the streaming version of XSSFWorkbook
            Workbook workbook = WorkbookUtil.getStreamableWorkbookInstanceFromXSSFWorkbook(initWorkbook)
            initWorkbook = null

            // Refresh references to workbook sheets
            serverSheet = WorkbookUtil.getSheetFromWorkbook(workbook, serverSheetName)
            appSheet = WorkbookUtil.getSheetFromWorkbook(workbook, appSheetName)
            dbSheet = WorkbookUtil.getSheetFromWorkbook(workbook, dbSheetName)
            storageSheet = WorkbookUtil.getSheetFromWorkbook(workbook, fileSheetName)
            rackSheet = WorkbookUtil.getSheetFromWorkbook(workbook, rackSheetName)

            /***************************************************************************/

            //
            // Device Export
            //
            if ( doDevice ) {
                Session session = sessionFactory.openSession()
                ScrollableResults scrollableResults = getAssetListScrollable(session, deviceQuery, queryParams)

                profiler.beginInfo "Devices"

                profiler.lap("Devices", "Devices Started")

                WorkbookUtil.addCellValidation(validationSheet, serverSheet, 0, 1, optionsSize["Environment"], serverMap["Environment"]["order"], 1, assetSize)
                WorkbookUtil.addCellValidation(validationSheet, serverSheet, 1, 1, optionsSize["Priority"], serverMap["Priority"]["order"], 1, assetSize)
                WorkbookUtil.addCellValidation(validationSheet, serverSheet, 2, 1, optionsSize["PlanStatus"], serverMap["PlanStatus"]["order"], 1, assetSize)

                profiler.lap("Devices", "Validations added")

                exportedEntity += 'S'
                int deviceCount = 0
                profileThresholdViolations = 0
                profilingRow = false

                String silentTag = 'DevRowSilent'

                if (profilerEnabled ) {
                    (profileSampleQty, profileSampleModulus) = calcProfilerCriteria(assetSize)
                }

                profiler.lap('Devices', 'Entering while loop')

                int depGroupColNum = serverMap['DepGroup']["order"]

                while (scrollableResults.next()) {
                    AssetEntity currentAsset = (AssetEntity)scrollableResults.get()[0]
                    profiler.beginSilent(silentTag)
                    if (profilerEnabled) {
                        if (deviceCount == 0 || (deviceCount % profileSampleModulus == 0)) {
                            profilingRow = true
                            profileSamplingTics = profileSampleQty
                            profiler.lapReset('Devices')
                        }

                        if (profilingRow) {
                            if (profileSamplingTics == 0) {
                                profilingRow = false
                            } else {
                                profileSamplingTics--
                                profiler.begin 'Device Row'
                            }
                        }
                    }

                    deviceCount++
                    progressCount++

                    if (profilingRow) {
                        profiler.lap('Devices', 'Read device %d of %d, id=%d', [deviceCount, assetSize, currentAsset.id])
                    }

                    updateProgress(key, progressCount, progressTotal, 'In progress', updateOnPercent)

                    // Add assetId for walkthrough template only.
                    if( serverMap.containsKey("assetId") ) {
                        addCell(serverSheet, 0, deviceCount, currentAsset.id, Cell.CELL_TYPE_NUMERIC, workbookCellStyles)
                    }

                    if (profilingRow) {
                        profiler.lap('Devices', 'Update Progress')
                        profiler.begin('Device Fields')
                    }

                    for ( int coll = 1; coll < serverMap.size(); coll++ ) {

                        def attribute = serverMap.find { field -> field.value["order"] == coll }
                        def colName = attribute.key
                        def colNum = attribute.value["order"]
                        def a = currentAsset

                        if (profilingRow) {
                            // log.debug("SET VAR TIME = {}", profiler.getLapDuration('Devices').toMilliseconds() )
                            lapDuration = profiler.getLapDuration('Devices').toMilliseconds()
                            if (lapDuration > profileThresholdSettingField) {
                                profiler.log(Profiler.LOG_TYPE.INFO, 'Set var %s (%s msec)', [colName, lapDuration.toString()])
                            } else {
                                profiler.lapReset('Devices')
                            }
                        }

                        if (attribute) {
                            def propValue = colName == "DepGroup" ? null : a.(attribute.value["field"])

                            // Only update if value is not null or if a string is not blank
                            if ( ! ( propValue == null || ( (propValue instanceof String) && propValue.size() == 0 ))){
                                switch(colName) {
                                    case ~/usize|SourcePos|TargetPos/:
                                        def pos = a[attribute.value["field"]] ?: 0
                                        // Don't bother populating position if it is a zero
                                        if (pos == 0) {
                                            continue
                                        }
                                        addCell(serverSheet, colNum, deviceCount, (Double)pos, Cell.CELL_TYPE_NUMERIC, workbookCellStyles)
                                        break

                                    case ~/Retire|MaintExp/:
                                        addCell(serverSheet, colNum, deviceCount, TimeUtil.formatDate(userDTFormat, a[attribute.value["field"]], TimeUtil.FORMAT_DATE))
                                        break

                                    case ~/Modified Date/:
                                        if (a[attribute.value["field"]]) {
                                            addCell(serverSheet, colNum, deviceCount, TimeUtil.formatDateTimeWithTZ(tzId, userDTFormat, a[attribute.value["field"]], TimeUtil.FORMAT_DATE_TIME))
                                        }
                                        break

                                    case ~/Source Blade|Target Blade/:
                                        def chassis = a[attribute.value["field"]]
                                        def value = ""
                                        if (chassis) {
                                            value = "id:" + chassis.id + " " + chassis.assetName
                                        }
                                        addCell(serverSheet, colNum, deviceCount, value)
                                        break

                                    default:
                                        def value = StringUtil.defaultIfEmpty( String.valueOf(a[attribute.value["field"]]), '')
                                        addCell(serverSheet, colNum, deviceCount, value)
                                }
                            }
                        }

                        if (profilingRow) {
                            lapDuration = profiler.getLapDuration('Devices').toMilliseconds()
                            if (lapDuration > 2) {
                                profiler.log(Profiler.LOG_TYPE.INFO, 'Set CELL %s (%s msec)', [colName, lapDuration.toString()])
                            } else {
                                profiler.lapReset('Devices')
                            }
                        }

                    }

                    def depGroupId = assetDepBundleMap[currentAsset.id.toString()]
                    if (depGroupId != null) {
                        addCell(serverSheet, depGroupColNum, deviceCount, depGroupId)
                    }

                    if (profilingRow) {
                        profiler.end('Device Fields')
                        profiler.end('Device Row')
                    }

                    if (profilerEnabled) {
                        // If the row duration exceeds the threshold we'll report it
                        lapDuration = profiler.getLapDuration(silentTag).toMilliseconds()
                        if (lapDuration > profileRowThresholds[(AssetClass.DEVICE)]) {
                            profileThresholdViolations++
                            profiler.log(Profiler.LOG_TYPE.WARN, "Asset '${currentAsset.assetName}' (id:${currentAsset.id}) exceeded duration threshold ($lapDuration msec)")
                            if ( lapDuration > profileHighwaterMark ) {
                                // Log each row that bumps up the highwater mark
                                profileHighwaterMark = lapDuration
                                profileHighwaterAsset = "${currentAsset.assetName} (id:${currentAsset.id})"
                            }
                        }

                        // Compute the Duration Matrix to categorize all of the rows
                        String durationGroup = "${((int)Math.floor(lapDuration/10))*10}"
                        if (! durationMatrix.containsKey(durationGroup)) {
                            durationMatrix[durationGroup] = [
                                    count: 1,
                                    duration: lapDuration,
                                    average: lapDuration
                            ]
                        } else {
                            durationMatrix[durationGroup].count++
                            durationMatrix[durationGroup].duration += lapDuration
                            durationMatrix[durationGroup].average = (int)(durationMatrix[durationGroup].duration / durationMatrix[durationGroup].count)
                        }
                    }

                    profiler.endSilent(silentTag)

                    session.evict(currentAsset)
                } // asset.each
                //asset = null
                session.close()

                profiler.endInfo("Devices", "processed %d rows", [assetSize])
                if (profileThresholdViolations > 0) {
                    profiler.log(Profiler.LOG_TYPE.WARN, thresholdWarnMsg, [profileThresholdViolations, profileRowThresholds[(AssetClass.DEVICE)]])
                }

                if (profilerEnabled) {
                    if (profileHighwaterAsset) {
                        profiler.log(Profiler.LOG_TYPE.WARN, "Asset $profileHighwaterAsset set the highwater mark ($profileHighwaterMark msec)")
                    }

                    int durSum = durationMatrix.values().sum { it.duration }
                    if (durSum > 0) {
                        durationMatrix.each {k,v ->
                            durationMatrix[k].perc = Math.round(v.duration/durSum*100)
                        }
                    }
                    profiler.log(Profiler.LOG_TYPE.INFO, "Duration Matrix: $durationMatrix")
                }
            } else {
                // Add validations for the first row (which is blank)
                WorkbookUtil.addCellValidation(validationSheet, serverSheet, 0, 1, optionsSize["Environment"], serverMap["Environment"]["order"], 1, 1)
                WorkbookUtil.addCellValidation(validationSheet, serverSheet, 1, 1, optionsSize["Priority"], serverMap["Priority"]["order"], 1, 1)
                WorkbookUtil.addCellValidation(validationSheet, serverSheet, 2, 1, optionsSize["PlanStatus"], serverMap["PlanStatus"]["order"], 1, 1)
            }

            //flushAndClearSession()

            //
            // Application Export
            //
            // profiler.lapInfo("EXPORT")
            if ( doApp ) {
                Session session = sessionFactory.openSession()
                ScrollableResults scrollableResults = getAssetListScrollable(session, applicationQuery, queryParams)

                profiler.beginInfo "Applications"

                profiler.lap("Applications", "Adding Validations")

                WorkbookUtil.addCellValidation(validationSheet, appSheet, 0, 1, optionsSize["Environment"], appMap["Environment"]["order"], 1, appSize)
                WorkbookUtil.addCellValidation(validationSheet, appSheet, 2, 1, optionsSize["PlanStatus"], appMap["PlanStatus"]["order"], 1, appSize)

                profiler.lap("Applications", "Validations added.")

                exportedEntity += 'A'

                // This determines which columns are added as Number vs Label
                def numericCols = []
                def stringCols = ['Version']

                // Flag to know if the AppId Column exists
                def idColName = 'appId'
                def hasIdCol = appMap.containsKey(idColName)

                int applicationCount = 0
                while (scrollableResults.next()) {
                    Application app = (Application)scrollableResults.get()[0]

                    progressCount++

                    updateProgress(key, progressCount, progressTotal, 'In progress', updateOnPercent)
                    applicationCount++

                    // Add the appId column to column 0 if it exists
                    if (hasIdCol) {
                        addCell(appSheet, 0, applicationCount, app.id, Cell.CELL_TYPE_NUMERIC, workbookCellStyles)
                    }

                    for (int i = 1; i < appMap.size(); i++) {
                        def attribute = appMap.find { field -> field.value["order"] == i }
                        def colName = attribute.key

                        def colVal = ''
                        switch(colName) {
                            case 'AppId':
                                colVal = app.id
                                break
                            case 'AppOwner':
                                colVal = app.appOwner
                                break
                            case 'DepGroup':
                                // Find the Dependency Group that this app is bound to
                                colVal = assetDepBundleMap[app.id.toString()]
                                break
                            case ~/ShutdownBy|StartupBy|TestingBy/:
                                colVal = app[attribute.value["field"]] ? resolveByName(app[attribute.value["field"]], false)?.toString() : ''
                                break
                            case ~/ShutdownFixed|StartupFixed|TestingFixed/:
                                colVal = app[attribute.value["field"]] ? 'Yes' : 'No'
                                //log.info("export() : field class type= {}", app[assetColName].className())
                                break
                            case ~/Retire|MaintExp/:
                                colVal = app[attribute.value["field"]] ? TimeUtil.formatDate(userDTFormat, app[attribute.value["field"]], TimeUtil.FORMAT_DATE) : ''
                                break
                            case ~/Modified Date/:
                                colVal = app[attribute.value["field"]] ? TimeUtil.formatDateTimeWithTZ(tzId, userDTFormat, app[attribute.value["field"]], TimeUtil.FORMAT_DATE_TIME) : ''
                                break
                            default:
                                colVal = app[attribute.value["field"]]
                        }

                        if (!(colVal == null || ( (colVal instanceof String) && colVal.size() == 0 ))) {
                            if (colVal?.class.name == 'Person') {
                                colVal = colVal.toString()
                            }

                            if ( numericCols.contains(colName) )
                                addCell(appSheet, attribute.value["order"], applicationCount, (Double)colVal, Cell.CELL_TYPE_NUMERIC, workbookCellStyles)
                            else if ( stringCols.contains(colName) ){
                                addCell(appSheet, attribute.value["order"], applicationCount, colVal.toString(), Cell.CELL_TYPE_STRING, workbookCellStyles)
                            } else {
                                addCell(appSheet, attribute.value["order"], applicationCount, colVal.toString())
                            }
                        }
                    }
                    session.evict(app)
                } // application.each
                //application = null
                session.close()
                profiler.endInfo("Applications", "processed %d rows", [appSize])
            } else {
                // Add validation for the first row
                WorkbookUtil.addCellValidation(validationSheet, appSheet, 0, 1, optionsSize["Environment"], appMap["Environment"]["order"], 1, 1)
                WorkbookUtil.addCellValidation(validationSheet, appSheet, 2, 1, optionsSize["PlanStatus"], appMap["PlanStatus"]["order"], 1, 1)
            }

            //flushAndClearSession()

            //
            // Database
            //
            if ( doDB ) {
                Session session = sessionFactory.openSession()
                ScrollableResults scrollableResults = getAssetListScrollable(session, databaseQuery, queryParams)

                profiler.beginInfo "Databases"

                profiler.lap("Databases", "Adding Validations")

                WorkbookUtil.addCellValidation(validationSheet, dbSheet, 0, 1, optionsSize["Environment"], dbMap["Environment"]["order"], 1, dbSize)
                WorkbookUtil.addCellValidation(validationSheet, dbSheet, 2, 1, optionsSize["PlanStatus"], dbMap["PlanStatus"]["order"], 1, dbSize)

                profiler.lap("Databases", "Validations added.")

                exportedEntity += "D"
                int databaseCount = 0

                while (scrollableResults.next()) {
                    Database currentDatabase = (Database)scrollableResults.get()[0]
                    progressCount++
                    databaseCount++
                    updateProgress(key, progressCount, progressTotal, 'In progress', updateOnPercent)

                    //Add assetId for walkthrough template only.
                    if (dbMap.containsKey("dbId")) {
                        addCell(dbSheet, 0, databaseCount, (currentDatabase.id), Cell.CELL_TYPE_NUMERIC, workbookCellStyles)
                    }
                    for ( int coll = 1; coll < dbMap.size(); coll++ ) {
                        def attribute = dbMap.find { field -> field.value["order"] == coll }
                        log.debug("Processing Database Field: {}", attribute)
                        String colName = attribute.key
                        if (colName == "DepGroup") {
                            addCell(dbSheet, attribute.value["order"], databaseCount, assetDepBundleMap[currentDatabase.id.toString()])
                        } else if(attribute.value["field"] in ['retireDate', 'maintExpDate', 'lastUpdated']) {
                            def dateValue = currentDatabase.(attribute.value["field"])
                            if (dateValue) {
                                if (attribute.value["field"] == 'lastUpdated') {
                                    dateValue = TimeUtil.formatDateTimeWithTZ(tzId, userDTFormat, dateValue, TimeUtil.FORMAT_DATE_TIME)
                                } else {
                                    dateValue = TimeUtil.formatDate(userDTFormat, dateValue, TimeUtil.FORMAT_DATE)
                                }
                            } else {
                                dateValue = ""
                            }
                            addCell(dbSheet, attribute.value["order"], databaseCount, dateValue)
                        } else {
                            def prop = currentDatabase[attribute.value["field"]]
                            if ( !(prop == null || ( (prop instanceof String) && prop.size() == 0 )) ) {
                                if ( BUNDLE_MOVE_AND_CLIENT_TEAMS.contains(attribute.value["field"]) ) {
                                    addCell(dbSheet, attribute.value["order"], databaseCount, String.valueOf(currentDatabase[attribute.value["field"]].teamCode))
                                } else {
                                    addCell(dbSheet, attribute.value["order"], databaseCount, String.valueOf(currentDatabase[attribute.value["field"]]))
                                }
                            }
                        }
                    }
                    session.evict(currentDatabase)
                } // database.each
                //database = null
                session.close()
                profiler.endInfo("Databases", "processed %d rows", [dbSize])
            } else {
                // Adds validation to just the first row
                WorkbookUtil.addCellValidation(validationSheet, dbSheet, 0, 1, optionsSize["Environment"], dbMap["Environment"]["order"], 1, 1)
                WorkbookUtil.addCellValidation(validationSheet, dbSheet, 2, 1, optionsSize["PlanStatus"], dbMap["PlanStatus"]["order"], 1, 1)
            }

            //flushAndClearSession()

            //
            // Storage ( files )
            //
            if ( doStorage ) {
                Session session = sessionFactory.openSession()
                ScrollableResults scrollableResults = getAssetListScrollable(session, filesQuery, queryParams)

                profiler.beginInfo "Logical Storage"

                profiler.lap("Logical Storage", "Adding Validations")

                WorkbookUtil.addCellValidation(validationSheet, storageSheet, 0, 1, optionsSize["Environment"], fileMap["Environment"]["order"], 1, fileSize)
                WorkbookUtil.addCellValidation(validationSheet, storageSheet, 2, 1, optionsSize["PlanStatus"], fileMap["PlanStatus"]["order"], 1, fileSize)

                profiler.lap("Logical Storage", "Validations added.")

                exportedEntity += "F"

                int filesCount = 0
                while (scrollableResults.next()) {
                    Files currentFile = (Files)scrollableResults.get()[0]
                    progressCount++
                    filesCount++
                    updateProgress(key, progressCount, progressTotal, 'In progress', updateOnPercent)

                    // Add assetId for walkthrough template only.
                    if ( fileMap.containsKey("filesId") ) {
                        addCell(storageSheet, 0, filesCount, (currentFile.id), Cell.CELL_TYPE_NUMERIC, workbookCellStyles)
                    }

                    for ( int coll = 1; coll < fileMap.size(); coll++ ) {
                        def addContentToSheet
                        def attribute = fileMap.find { field -> field.value["order"] == coll }
                        def colName = attribute.key
                        if (colName == "DepGroup") {
                            addCell(storageSheet, attribute.value["order"], filesCount, assetDepBundleMap[currentFile.id.toString()] )
                        } else if(attribute.value["field"] in ['retireDate', 'maintExpDate', 'lastUpdated']) {
                            def dateValue = currentFile.(attribute.value["field"])
                            if (dateValue) {
                                if (attribute.value["field"] == 'lastUpdated') {
                                    dateValue = TimeUtil.formatDateTimeWithTZ(tzId, userDTFormat, dateValue, TimeUtil.FORMAT_DATE_TIME)
                                } else {
                                    dateValue = TimeUtil.formatDate(userDTFormat, dateValue, TimeUtil.FORMAT_DATE)
                                }
                            } else {
                                dateValue = ""
                            }
                            addCell(storageSheet, attribute.value["order"], filesCount, dateValue)
                        } else{
                            def prop = currentFile[attribute.value["field"]]
                            if ( !(prop == null || ( (prop instanceof String) && prop.size() == 0 )) ) {
                                addCell(storageSheet, attribute.value["order"], filesCount, String.valueOf(prop))
                            }
                        }

                    }
                    session.evict(currentFile)
                } // files.each
                //files = null
                session.close()
                profiler.endInfo("Logical Storage", "processed %d rows", [fileSize])
            } else {
                // Adds validations to the first row
                WorkbookUtil.addCellValidation(validationSheet, storageSheet, 0, 1, optionsSize["Environment"], fileMap["Environment"]["order"], 1, 1)
                WorkbookUtil.addCellValidation(validationSheet, storageSheet, 2, 1, optionsSize["PlanStatus"], fileMap["PlanStatus"]["order"], 1, 1)
            }

            //flushAndClearSession()

            //
            // Dependencies
            //
            def dependencySheet = WorkbookUtil.getSheetFromWorkbook(workbook, "Dependencies")
            List depProjectionFields = [
                    'id',
                    'asset.id',
                    'a.assetName',
                    'a.assetType',
                    'd.id',
                    'd.assetName',
                    'd.assetType',
                    'type',
                    'dataFlowFreq',
                    'dataFlowDirection',
                    'status',
                    'comment',
                    'c1',
                    'c2',
                    'c3',
                    'c4'
            ]

            if ( doDependency ) {
                profiler.beginInfo "Dependencies"
                exportedEntity += "X"

                profiler.lap("Dependencies", "Adding Validations")
                WorkbookUtil.addCellValidation(validationSheet, dependencySheet, 3, 1, optionsSize["DepType"],  depProjectionFields.indexOf("type"), 1, dependencySize)
                WorkbookUtil.addCellValidation(validationSheet, dependencySheet, 4, 1, optionsSize["DepStatus"],  depProjectionFields.indexOf("status"), 1, dependencySize)
                profiler.lap("Dependencies", "Validations added.")

                List results = AssetDependency.createCriteria().list {
                    createAlias("asset", "a")
                    createAlias("dependent", "d")
                    and {
                        eq("a.project", project)
                    }
                    projections {
                        depProjectionFields.each {
                            property(it)
                        }
                    }

                    readOnly true
                }

                //results.eachWithIndex { dependency, r ->
                int r = 0
                for (Object dependency : results) {
                    progressCount++
                    updateProgress(key, progressCount, progressTotal, 'In progress', updateOnPercent)

                    for(int i = 0; i < depProjectionFields.size(); i++){
                        def prop = dependency[i]
                        if ( !(prop == null || ( (prop instanceof String) && prop.size() == 0 )) ) {
                            addCell(dependencySheet, i ,r + 1, dependency[i])
                        }
                    }
                    r++
                } // result.each
                results = null
                profiler.endInfo("Dependencies", "processed %d rows", [dependencySize])
            } else {
                // Adds validations to the first dependency row
                WorkbookUtil.addCellValidation(validationSheet, dependencySheet, 3, 1, optionsSize["DepType"],  depProjectionFields.indexOf("type"), 1, 1)
                WorkbookUtil.addCellValidation(validationSheet, dependencySheet, 4, 1, optionsSize["DepStatus"],  depProjectionFields.indexOf("status"), 1, 1)
            }

            flushAndClearSession()

            //
            // Room Export
            //
            if( doRoom ) {
                profiler.beginInfo "Rooms"

                exportedEntity += "R"

                def roomSheet = WorkbookUtil.getSheetFromWorkbook(workbook, "Room")

                List projectionFields = [
                        "id",
                        "roomName",
                        "location",
                        "roomDepth",
                        "roomWidth",
                        "source",
                        "address",
                        "city",
                        "country",
                        "stateProv",
                        "postalCode",
                        "dateCreated",
                        "lastUpdated"
                ]

                // These fields can be exported as they are. 5 is intentionally omitted.
                List regularFields = [0, 1, 2, 3, 4, 6, 7, 8, 9, 10]
                // These fields are dates and need to be converted to the user's TZ.
                List dateFields = [11, 12]


                List results = Room.createCriteria().list {
                    and {
                        eq("project", project)
                    }

                    projections {
                        projectionFields.each {
                            property(it)
                        }
                    }

                    readOnly true
                }

                //results.eachWithIndex { room, r ->
                int r = 0
                for (Object room : results) {
                    def rowNum = r + 1 //Header Offset
                    progressCount++
                    updateProgress(key, progressCount, progressTotal, 'In progress', updateOnPercent)
                    // Export most fields.
                    regularFields.each { col ->
                        def prop = room[col]
                        if ( !(prop == null || ( (prop instanceof String) && prop.size() == 0 )) ) {
                            if (col == 0) {
                                addCell(roomSheet, 0, rowNum, room[0], Cell.CELL_TYPE_NUMERIC, workbookCellStyles)
                            } else {
                                addCell(roomSheet, col, rowNum, String.valueOf(prop ?: ""))
                            }
                        }

                    }
                    // Export date fields using the user's TZ.
                    dateFields.each{ col->
                        addCell(roomSheet, col, rowNum, room[col] ? TimeUtil.formatDateTimeWithTZ(tzId, userDTFormat, room[col], TimeUtil.FORMAT_DATE_TIME) : "")
                    }
                    // Export 'source' or 'target' accordingly.
                    addCell(roomSheet, 5, rowNum, String.valueOf(room[5] == 1 ? "Source" : "Target" ))
                    r++
                } // results.each
                profiler.endInfo("Rooms", "processed %d rows", [roomSize])
                results = null
            }

            flushAndClearSession()

            //
            // Rack Export
            //
            if ( doRack ) {
                profiler.beginInfo "Racks"
                exportedEntity +="r"

                def racks = Rack.createCriteria().list {
                    and {
                        eq("project", project)
                    }
                    resultTransformer(Transformers.ALIAS_TO_ENTITY_MAP)
                    readOnly true
                }

                def rackMap = ['rackId':'id', 'Tag':'tag', 'Location':'location', 'Room':'room', 'RoomX':'roomX',
                               'RoomY':'roomY', 'PowerA':'powerA', 'PowerB':'powerB', 'PowerC':'powerC', 'Type':'rackType',
                               'Front':'front', 'Model':'model', 'Source':'source', 'Model':'model'
                ]

                //racks.eachWithIndex { rack, idx ->
                int idx = 0
                for (Object rack : racks) {
                    def rowNum = idx + 1 //Header Offset
                    def currentRack = rack.get(Criteria.ROOT_ALIAS)
                    progressCount++
                    updateProgress(key, progressCount, progressTotal, 'In progress', updateOnPercent)

                    rackSheetColumns.eachWithIndex{column, i->
                        if(column == 'rackId'){
                            addCell(rackSheet, 0, rowNum, currentRack.id, Cell.CELL_TYPE_NUMERIC, workbookCellStyles)
                        } else {
                            def prop = currentRack[rackMap[column]]
                            if(column =="Source")
                                addCell(rackSheet, i, rowNum, String.valueOf(prop == 1 ? "Source" : "Target" ))
                            else{
                                if ( !(prop == null || ( (prop instanceof String) && prop.size() == 0 )) ) {
                                    addCell(rackSheet, i, rowNum, String.valueOf(prop))
                                }

                            }
                        }
                    }
                    idx++
                } // racks.each
                racks = null
                profiler.endInfo("Racks", "processed %d rows", [rackSize])
            }

            //}

            flushAndClearSession()

            //
            // Cabling Export
            //
            if ( doCabling ) {
                profiler.beginInfo "Cabling"

                exportedEntity += "c"

                if (cablingSize > 0) {
                    def cablingSheet = WorkbookUtil.getSheetFromWorkbook(workbook, "Cabling")
                    def cablingList = AssetCableMap.createCriteria().list {
                        createAlias("assetFrom", "af")
                        and {
                            eq("af.project", project)
                        }
                        resultTransformer(Transformers.ALIAS_TO_ENTITY_MAP)
                    }
                    cablingReportData(cablingList, cablingSheet, workbookCellStyles)
                    progressCount += cablingSize
                    cablingList = null
                }
                profiler.endInfo("Cabling", "processed %d rows", [cablingSize])
            }

            flushAndClearSession()

            //
            // Comments Export
            //
            if ( doComment ) {
                profiler.beginInfo "Comments"

                exportedEntity += "M"

                if (commentSize > 0) {
                    def commentSheet = WorkbookUtil.getSheetFromWorkbook(workbook, "Comments")

                    def c = AssetComment.createCriteria()
                    List commentList = c {
                        and {
                            eq('project', project)
                            eq('commentType', 'comment')
                            isNotNull('assetEntity')

                        }
                        resultTransformer(Transformers.ALIAS_TO_ENTITY_MAP)
                        setReadOnly true
                    }

                    //commentList.eachWithIndex { comm, idx ->
                    int idx = 0
                    for (Object comm : commentList) {
                        def rowNum = idx + 1 //HEADER Offset
                        def currentComment = comm.get(Criteria.ROOT_ALIAS)
                        progressCount++
                        updateProgress(key, progressCount, progressTotal, 'In progress', updateOnPercent)
                        def dateCommentCreated = ''
                        if (currentComment.dateCreated) {
                            dateCommentCreated = TimeUtil.formatDateTimeWithTZ(tzId, userDTFormat, currentComment.dateCreated, TimeUtil.FORMAT_DATE_TIME)
                        }
                        addCell(commentSheet, 0, rowNum, currentComment.id, Cell.CELL_TYPE_NUMERIC, workbookCellStyles)
                        addCell(commentSheet, 1, rowNum, currentComment.assetEntity.id, Cell.CELL_TYPE_NUMERIC, workbookCellStyles)
                        addCell(commentSheet, 2, rowNum, String.valueOf(currentComment.category))
                        addCell(commentSheet, 3, rowNum, String.valueOf(dateCommentCreated))
                        addCell(commentSheet, 4, rowNum, String.valueOf(currentComment.createdBy))
                        addCell(commentSheet, 5, rowNum, String.valueOf(currentComment.comment))
                        idx++
                    } // commentList.each
                    commentList = null
                }
                profiler.endInfo("Comments", "processed %d rows", [commentSize])
            }

            flushAndClearSession()

            //
            // Wrap up the process
            //
            profiler.lap(mainProfTag, 'All sheets populated')

            profiler.beginInfo("Set Force Formula Recalculation")
            workbook.setForceFormulaRecalculation(true)
            profiler.endInfo("Set Force Formula Recalculation")

            profiler.beginInfo("Create temporary export file")
            String savedWorkbookAbosolutePath = WorkbookUtil.saveWorkbook(workbook)
            progressService.updateData(key, 'filename', savedWorkbookAbosolutePath)
            profiler.endInfo("Create temporary export file")

            // The filename will consiste of the following:
            //    - Owner of the Project
            //    - Name of the Project or ID
            //    - Bundle name(s) selected or ALL
            //    - Letters symbolizing each of the tabs that were exported
            //    - The date that the spreadsheet was exported
            //    - The file extension to use
            project = Project.get(projectId)
            String filename = project.client.name + '-' +
                    ( project.name ?: project.id ) +
                    "-${bundleNameList}-${exportedEntity}-${exportDate}.${DEFAULT_EXPORT_FILE_EXTENSION}"

            filename = StringUtil.sanitizeAndStripSpaces(filename)

            def contentType = grailsApplication.config.grails.mime.types[DEFAULT_EXPORT_FILE_EXTENSION]
            progressService.updateData(key, 'contenttype', contentType)
            progressService.updateData(key, 'header', 'attachment; filename="' + filename + '"')
            progressService.update(key, 100, 'Completed')

            profiler.lap(mainProfTag, "Streamed spreadsheet to browser")
        } catch( Exception exportExp ) {
            log.error("Error: ${exportExp.message}", exportExp)
            progressService.update(key, 100, 'Cancelled', "An unexpected exception occurred while exporting to Excel")

            return
        } finally {
            profiler.endInfo(mainProfTag, "FINISHED")
        }
    }



    /* -------------------------------------------------------
	 * To check the sheet headers
	 * @param attributeList, SheetColumnNames
	 * @author Mallikarjun
	 * @return bollenValue
	 *------------------------------------------------------- */
    boolean checkHeader(list, serverSheetColumnNames, missingHeader = "") {
        def listSize = list.size()
        for ( int coll = 0; coll < listSize; coll++ ) {
            if( serverSheetColumnNames.containsKey( list[coll] ) || list[coll] == "DepGroup") {
                //Nonthing to perform.
            } else {
                missingHeader = missingHeader + ", " + list[coll]
            }
        }
        return missingHeader == ""
    }

    /**
     * This method is used to update sheet's column header with custom labels
     * @param assetClass AssetClass sheet
     * @param sheet sheet's instance
     * @param sheetColumnsMap column names
     * @return
     */
    private Sheet updateColumnHeaders(AssetClass assetClass, Sheet sheet, Map<String, ?> sheetColumnsMap) {
        log.info("Updating sheet columns headers for: {}", assetClass)
        Set<String> sheetColumnNames = sheetColumnsMap.sort { a, b -> a.value["order"] <=> b.value["order"] }.keySet()
        sheetColumnNames.eachWithIndex { String columnHeader, int index ->
            def cellData = sheet.getRow(0).getCell(index)?.getStringCellValue()
            if (!cellData) {
                addCell(sheet, index, 0, columnHeader)
            }
        }

        return sheet
    }

    /**
     * Get field specs for a given AssetClass and current user project
     * @param assetClass
     * @return
     */
    private List<Map<String, ?>> getFieldSpecsForAssetClass(AssetClass assetClass) {
        Map fieldSpecs = customDomainService.allFieldSpecs(assetClass.toString())
        return fieldSpecs[assetClass.toString()]["fields"]
    }

    /**
     * Map and match sheet columns header names to field specs
     * @param sheet sheet's instance
     * @param fieldSpecs AssetClass field specs
     * @return
     */
    private Map mapSheetColumnsToFields(Sheet sheet, List<JSONObject> fieldSpecs) {
        Map columnsMap = [:]
        Row refServerRow = sheet.getRow(0)
        Cell cell = null;
        int colNum = 0;
        while (true) {
            cell = refServerRow.getCell(colNum)
            if (cell && !StringUtil.isBlank(cell.getStringCellValue())) {
                String cellContent = cell.getStringCellValue()
                Map<String, ?> cellRef = ["field": cellContent, "order": colNum++]
                columnsMap.put(cellContent, cellRef)
                log.debug("{}: {}", sheet.getSheetName(), cellRef)
            } else {
                break
            }
        }
        if (!columnsMap.containsKey("DepGroup")) {
            columnsMap.put("DepGroup", ["field": "DepGroup", "order": colNum++])
        }
        for (JSONObject fieldSpec : fieldSpecs) {
            Map<String, ?> columnMap = columnsMap[fieldSpec["label"]]
            if (!columnMap) {
                columnsMap.put(fieldSpec["label"], ["field": fieldSpec["field"], "order": colNum++])
            } else {
                columnMap["field"] = fieldSpec["field"]
            }
        }
        return columnsMap
    }

    /**
     * Add a new cell to the given sheet at col, row with provided value, cell type and style
     * @param sheet
     * @param columnIdx
     * @param rowIdx
     * @param value
     * @param cellType
     * @param workbookCellStyles
     */
    void addCell(Sheet sheet, int columnIdx, int rowIdx, Object value, Integer cellType, Map<Integer, CellStyle> workbookCellStyles) {
        if (cellType == null) {
            addCell(sheet, columnIdx, rowIdx, value)
        } else {
            WorkbookUtil.addCell(sheet, columnIdx, rowIdx, value, cellType, getCellStyle(sheet, cellType, workbookCellStyles))
        }
    }

    /**
     * Add a new cell to the given sheet at col, row with provided value
     * @param sheet
     * @param columnIdx
     * @param rowIdx
     * @param value
     */
    void addCell(Sheet sheet, int columnIdx, int rowIdx, Object value) {
        WorkbookUtil.addCell(sheet, columnIdx, rowIdx, value, Cell.CELL_TYPE_STRING, null)
    }

    /**
     * creates a new cell style or get one from cache
     * @param sheet
     * @param cellType
     * @param workbookCellStyles
     * @return
     */
    CellStyle getCellStyle(Sheet sheet, int cellType, Map<Integer, CellStyle> workbookCellStyles) {
        CellStyle cellStyle = workbookCellStyles[cellType]
        if (cellStyle == null) {
            cellStyle = WorkbookUtil.createCellStyle(sheet, cellType)
            workbookCellStyles[cellType] = cellStyle
        }
        return cellStyle
     }

    /**
     * Used to get the list of Type used to assign to AssetDependency.type
     * @return List of the types
     */
    def getDependencyTypes() {
        return AssetOptions.findAllByType(AssetOptions.AssetOptionsType.DEPENDENCY_TYPE)?.value
    }

    /**
     * Used to get the list of Status used to assign to AssetDependency.status
     * @return List of the types
     */
    def getDependencyStatuses() {
        return AssetOptions.findAllByType(AssetOptions.AssetOptionsType.DEPENDENCY_STATUS)?.value
    }

    /**
     * Use to get the list of Asset Environment options
     * @return List of options
     */
    def getAssetEnvironmentOptions() {
        return AssetOptions.findAllByType(AssetOptions.AssetOptionsType.ENVIRONMENT_OPTION)?.value
    }

    /**
     * Use to get the list of Asset Environment options
     * @return List of options
     */
    def getAssetPlanStatusOptions() {
        return AssetOptions.findAllByType(AssetOptions.AssetOptionsType.STATUS_OPTION)?.value
    }

    /**
     * Use to get the list of Priority Options
     * @return List of Priority values
     */
    def getAssetPriorityOptions() {
        return AssetOptions.findAllByType(AssetOptions.AssetOptionsType.PRIORITY_OPTION)?.value
    }

    /**
     * Updates progress for the give key only when is around 5%
     * @param key to update
     * @param progressCount progress count
     * @param progressTotal total number of items
     * @param description description used on update
     * @param updateOnPercent used to determine the step size for updating the progress bar.
     **/
    void updateProgress(key, progressCount, progressTotal, description, updateOnPercent = 0.05) {
        int stepSize = Math.round(progressTotal * updateOnPercent)
        if ((progressTotal > 0) && (stepSize > 0) && ((progressCount % stepSize) == 0)) {
            progressService.update(key, ((int)((progressCount / progressTotal) * 100)), description)
        }
    }

    /**
	 * export cabling data.
     * @param assetCablesList
     * @param cablingSheet
     */
    void cablingReportData(List assetCablesList, Sheet cablingSheet, Map<Integer, CellStyle> workbookCellStyles) {
        int rowNum = 2
        for (Object element : assetCablesList) {
            def cabling = element["this"] // <SL> element comes as a map with (this, af) keys, so "this" is the one that has all needed information
            addCell(cablingSheet, 0, rowNum, String.valueOf(cabling.assetFromPort?.type))
            addCell(cablingSheet, 1, rowNum, cabling.assetFrom ? cabling.assetFrom?.id : "", Cell.CELL_TYPE_NUMERIC, workbookCellStyles)
            addCell(cablingSheet, 2, rowNum, String.valueOf(cabling.assetFrom ? cabling.assetFrom.assetName : ""))
            addCell(cablingSheet, 3, rowNum, String.valueOf(cabling.assetFromPort?.label))
            addCell(cablingSheet, 4, rowNum, cabling.assetTo ? cabling.assetTo?.id : "", Cell.CELL_TYPE_NUMERIC, workbookCellStyles)
            addCell(cablingSheet, 5, rowNum, String.valueOf(cabling.assetTo ? cabling.assetTo?.assetName : ""))
            if (cabling.assetFromPort && cabling.assetFromPort.type && cabling.assetFromPort.type != 'Power') {
                addCell(cablingSheet, 6, rowNum, String.valueOf(cabling.assetToPort ? cabling.assetToPort?.label : ""))
            } else {
                addCell(cablingSheet, 6, rowNum, String.valueOf(cabling.toPower ?: ""))
            }
            addCell(cablingSheet, 7, rowNum, String.valueOf(cabling.cableComment ?: ""))
            addCell(cablingSheet, 8, rowNum, String.valueOf(cabling.cableColor ?: ""))
            if (cabling.assetFrom?.sourceRoom) {
                addCell(cablingSheet, 9, rowNum, String.valueOf(cabling.assetFrom?.rackSource?.location + "/" + cabling.assetFrom?.sourceRoom + "/" + cabling.assetFrom?.sourceRack))
            } else if (cabling.assetFrom?.targetRoom) {
                addCell(cablingSheet, 9, rowNum, String.valueOf(cabling.assetFrom?.rackTarget?.location + "/" + cabling.assetFrom?.targetRoom + "/" + cabling.assetFrom?.targetRack))
            } else {
                addCell(cablingSheet, 9, rowNum, '')
            }
            addCell(cablingSheet, 10, rowNum, String.valueOf(cabling.cableStatus ?: ""))
            addCell(cablingSheet, 11, rowNum, String.valueOf(cabling.assetLoc ?: ""))
            rowNum++
        }
    }

    /**
     * Resolves the display string for the shutdownBy, startupBy, testingBy fields by either
     * getting the name of the person or stripping the prefix for SME/AppOwner or Role
     * @param byValue - application's shutdownBy, startupBy, or testingBy raw value
     * @param stripPrefix - if true or not specified, the function will remove the # or @ character from the string
     * @return value to display
     */
    def resolveByName(byValue, stripPrefix = true) {
        def byObj = ''
        if (byValue) {
            if (byValue.isNumber()) {
                byObj = Person.read(Long.parseLong(byValue))
            } else {
                byObj = (stripPrefix && ['@', '#'].contains(byValue[0])) ? byValue[1..-1] : byValue
            }
        }
        return byObj
    }

    /**
     * Get a readable/writable instance of the workbook template so application can override title sheet and custom headers
     * @return a Workbook instance
     */
    Workbook getInitWorkbookInstance() {
        File workbookTemplate =  grailsApplication.parentContext.getResource(ASSET_EXPORT_TEMPLATE).getFile()
        if (!workbookTemplate) {
            throw new RuntimeException("Unable to find template ${ASSET_EXPORT_TEMPLATE}")
        }
        return WorkbookUtil.getInitWorkbookInstance(workbookTemplate)
    }

    /**
     * Used to clear out the hibernate session of objects no longer needed to help performance. It will also merge the existing
     */
    void flushAndClearSession() {
        GormUtil.flushAndClearSession(sessionFactory)
    }
}
