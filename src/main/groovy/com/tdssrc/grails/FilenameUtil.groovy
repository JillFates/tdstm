package com.tdssrc.grails

import com.tdsops.tm.enums.FilenameFormat
import groovy.util.logging.Slf4j
import net.transitionmanager.project.MoveBundle
import net.transitionmanager.project.MoveEvent
import org.apache.commons.io.FilenameUtils

/**
 * The FilenameUtil class contains a set of file name formats that can be used to simplify the creation
 * of names for Reports, Export/Import Sheets and more, creating uniformity in file naming schemes
 * throughout the app. It can also be easily extended with new formats.
 * Created by ecantu on 12/11/2017.
 */
@Slf4j(value='logger')
class FilenameUtil {

	static final String CSV_SUFIX = 'csv'

	static final String JSON_SUFIX = 'json'

	/**
	 * Main method for creating a file name.
	 *
	 * @param nameFormat  The file name format to be used to create the name scheme.
	 * If none is chosen, the default format will be used.
	 * @param nameValues  A Map with the properties/values to construct the file name.
	 * This will vary depending on the nameFormat chosen.
	 * @param fileExtension  The extension of the resulting file name (optional)
	 * @param date  The date to be used in the file name. Optional, if no date is provided one will be created.
	 * @return  The resulting file name for the given format.
	 */
	static String buildFilename(FilenameFormat nameFormat=FilenameFormat.DEFAULT, Map nameValues, String fileExtension=null, Date date = new Date()) {
		String filename =''
		switch (nameFormat) {
			case FilenameFormat.CLIENT_PROJECT_EVENT_DATE:
				filename = clientProjectEventDateFormatter(nameValues, date)
				break
			case FilenameFormat.CLIENT_PROJECT_BUNDLE_CHECKBOXCODES_DATE:
				filename = clientProjectBundleCheckboxesDateFormatter(nameValues, date)
				break
			case FilenameFormat.PROJECT_VIEW_DATE:
				filename = projectViewDateFormatter(nameValues, date)
				break
			default:
				filename = filenameDefaultFormat(nameValues, date)
				break
		}
		if (filename && fileExtension) {
			filename = filename << '.' + fileExtension  // appends file extension, if exists
		}
		return filename
	}

	/**
	 * This format consist of a set of property values separated by '-'.
	 * If the properties have spaces in between, those will be replaced with underscores (_)
	 * (ej: 'Sample Project' will be 'Sample_Project').
	 * The complete resulting format is as follows: "Project_Client-Project_Code-Event(s)-yyyymmdd_hhmm"
	 * For more information see TM-8124.
	 * For tests on this see TM-8125, and refer to FilenameUtilTests class.
	 *
	 * @param nameValues  A Map with the properties needed to construct the file name.
	 *
	 * project:  Mandatory
	 * The project entity to use.
	 * The client's name and ProjectCode will be extracted to write in the resulting file name.
	 * <p>
	 * moveEvent:  Mandatory (unless some optional params are present, see the details below).
	 * The Event entity to use. The event's name will be used in the file name.([moveEvent:anEvent])
	 * You can also send a list with just one event, for the same result. (moveEvent:anEvent or moveEvent:[anEvent])
	 * If more than one event is sent in the list, it will return the number of events + '_events' string.
	 * (eg: for moveEvent:[eventOne, eventTwo] will be '2_events').
	 * <p>
	 * allEvents:  Optional.
	 * If true, the event part will be 'ALL' and not event's name ([allEvents:true]).
	 * In this case the mandatory property 'moveEvent' is not required, and it will be ignored if present.
	 * <p>
	 * If any mandatory values or properties are missing, an empty String is returned.
	 * @param date  The date to be used in the file name.
	 * @return  The resulting file name.
	 */
	private static String clientProjectEventDateFormatter(Map nameValues, Date date) {

		String projectClient = safeFilename(nameValues.project?.client?.name)
		String projectCode = safeFilename(nameValues.project?.projectCode)
		String eventName = buildEventName(nameValues)
		String formattedDate = TimeUtil.formatDate(TimeUtil.BIG_ENDIAN, date, TimeUtil.FORMAT_DATE_TIME_26)
		if(projectClient == null || projectCode == null || eventName == null) {
			logger.error 'FilenameUtil: Error while creating file name - Some required properties are missing'
			return ''
		}
		return projectClient + '-' + projectCode + '-' + eventName + '-' + formattedDate
	}

	/**
	 * This format consist of a set of property values separated by '-'.
	 * If the properties have spaces in between, those will be replaced with underscores (_)
	 * (ej: 'Sample Project' will be 'Sample_Project').
	 * The complete resulting format is as follows: "Project_Client-Project_Code-Bundle(s)-CheckboxCodes-yyyymmdd_hhmm"
	 * For more information see TM-7958.
	 * For tests on this see TM-8097, and refer to FilenameUtilTests class.
	 *
	 * @param nameValues  A Map with the properties needed to construct the file name.
	 *
	 * project:  Mandatory
	 * The project entity to use.
	 * The client's name and ProjectCode will be extracted to write in the resulting file name.
	 * <p>
	 * moveBundle:  Mandatory (unless some optional params are present, see the details below).
	 * The moveBundle entity to use. The bundle's name will be used in the file name.([moveBundle:aBundle])
	 * You can also send a list with just one bundle, for the same result. (moveBundle:aBundle or moveBundle:[aBundle])
	 * If more than one bundle is sent in the list, it will return the number of bundles + '_bundles' string.
	 * (eg: for moveBundle:[bundleOne, bundleTwo] will be '2_bundles').
	 * <p>
	 * allBundles:  Optional.
	 * If true, the bundle part will be 'ALL' and not bundle's name ([allBundles:true]).
	 * In this case the mandatory property 'moveBundle' is not required, and will be ignored if present.
	 * <p>
	 * useForPlanning:  Optional. Has precedence over moveBundle and allBundles options.
	 * If true the bundle part will be 'PLANNING' and not the bundle's name ([useForPlanning:true]).
	 * In this case the mandatory property 'moveBundle' is not required, and will be ignored if present.
	 * <p>
	 * exportedEntities:  Optional.
	 * Represents the checkboxes on the export assets page.
	 * If present, if will be added to the filename after the bundle part (eg: [exportedEntities:'SADFXRrcM']).
	 * (S = Devices, A = Application, D = db, F = Storage, X = Dependency, R = Room, r = Rack, c = Cabling, M = Comments)
	 * <p>
	 * If any mandatory values or properties are missing, an empty String is returned.
	 * @param date  The date to be used in the file name.
	 * @return  The resulting file name.
	 */
	private static String clientProjectBundleCheckboxesDateFormatter(Map nameValues, Date date) {

		String projectClient = safeFilename(nameValues.project?.client?.name)
		String projectCode = safeFilename(nameValues.project?.projectCode)
		String bundleName = buildBundleName(nameValues)
		String useForPlanning = nameValues.useForPlanning ? 'PLANNING' : ''
		String exportedEntities = nameValues.exportedEntities
		String formattedDate = TimeUtil.formatDate(TimeUtil.BIG_ENDIAN, date, TimeUtil.FORMAT_DATE_TIME_26)
		if(projectClient == null || projectCode == null || !(useForPlanning || bundleName)) {  // either useForPlanning or bundleName has to have a value
			logger.error 'FilenameUtil: Error while creating file name - Some required properties are missing'
			return ''
		}
		return projectClient + '-' + projectCode + '-' + (useForPlanning ?: bundleName) + '-' + (exportedEntities ? exportedEntities + '-' : '' ) + formattedDate
	}

	/**
	 * This format consist of a set of property values separated by '-'.
	 * If the properties have spaces in between, those will be replaced with underscores (_)
	 * (ej: 'Sample Project' will be 'Sample_Project').
	 * The complete resulting format is as follows: "Project_Code-View_Name-yyyymmdd_hhmm"
	 * For more information see TM-7872.
	 *
	 * @param nameValues  A Map with the properties needed to construct the file name.
	 *
	 * project:  Mandatory
	 * The project entity to use.
	 * The ProjectCode will be extracted to write in the resulting file name.
	 * viewName:  Mandatory
	 * The moveBundle entity to use. The name of the view to use in the filename.
	 * <p>
	 * excludeDate:  Optional.
	 * If true, the date part of the filename will not be returned, leaving it as blank.
	 * In this case the mandatory property 'moveBundle' is not required, and will be ignored if present.
	 * <p>
	 * If any mandatory values or properties are missing, an empty String is returned.
	 * @param date  The date to be used in the file name.
	 * @return  The resulting file name.
	 */
	private static String projectViewDateFormatter(Map nameValues, Date date) {

		String projectCode = safeFilename(nameValues.project?.projectCode)
		String viewName = safeFilename(nameValues.viewName)
		String formattedDate = TimeUtil.formatDate(TimeUtil.BIG_ENDIAN, date, TimeUtil.FORMAT_DATE_TIME_26)
		if( projectCode == null || viewName == null) {
			logger.error 'FilenameUtil: Error while creating file name - Some required properties are missing'
			return ''
		}
		return projectCode + '-' + viewName + (nameValues.excludeDate ? '': '-' + formattedDate)
	}

	/**
	 * This is the default format used if none is chosen.
	 * @param nameValues  The properties needed to construct the file name.
	 * @return  The resulting file name.
	 */
	private static String filenameDefaultFormat(Map nameValues, Date date) {
		return clientProjectEventDateFormatter(nameValues, date)
	}

	/**
	 * This returns the bundleName part of the filename, depending on the nameValues present.
	 * If the 'allBundles' param is present and true, it will return ALL_BUNDLES.
	 * Otherwise, if multiple bundles are present in the moveBundle param,
	 * it will return the number of bundles concatenated with the '_bundles' string.
	 * (eg: for moveBundle:[bundleOne, bundleTwo] will be '2_bundles').
	 * Finally if it's just one bundle, it will return the bundle's name.
	 * (This can be received in two forms, moveBundle:aBundle or moveBundle:[aBundle], with the same result.)
	 *
	 * @param nameValues  The properties needed to construct the file name.
	 * @return  The resulting file name.
	 */
	private static String buildBundleName(Map nameVaules) {
		String bundleName = ''
		boolean allBundles = nameVaules.allBundles
		if (!allBundles) {
			def moveBundle = nameVaules.moveBundle
			if (moveBundle instanceof List) {
				if(moveBundle.size() == 1) { // if the list has just one bundle, use the name of that
					bundleName = safeFilename(moveBundle[0].name)
				} else if (moveBundle.size() > 1) { // return 'N_bundles'
						bundleName = moveBundle.size() + '_bundles'
					}
			} else if (moveBundle instanceof MoveBundle) {
					bundleName = safeFilename(moveBundle?.name)
				}
		}
		else {
			bundleName = 'ALL_BUNDLES'
		}
	}

	/**
	 * This returns the eventName part of the filename, depending on the nameValues present.
	 * If the 'allEvents' param is present and true, it will return ALL.
	 * Otherwise, if multiple events are present in the moveEvent param,
	 * it will return the number of events concatenated with the '_events' string.
	 * (eg: for moveEvent:[eventOne, eventTwo] will be '2_events').
	 * Finally if it's just one event, it will return the event's name.
	 * (This can be received in two forms, moveEvent:anEvent or moveEvent:[anEvent], with the same result.)
	 *
	 * @param nameValues  The properties needed to construct the file name.
	 * @return  The resulting file name.
	 */
	private static String buildEventName(Map nameVaules) {
		String eventName = ''
		boolean allEvents = nameVaules.allEvents
		if (!allEvents) {
			def moveEvent = nameVaules.moveEvent
			if (moveEvent instanceof List) {
				if(moveEvent.size() == 1) { // if the list has just one event, use the name of that
					eventName = safeFilename(moveEvent[0].name)
				} else if (moveEvent.size() > 1) { // return 'N_events'
						eventName = moveEvent.size() + '_events'
					}
			} else if (moveEvent instanceof MoveEvent) {
						eventName = safeFilename(moveEvent?.name)
				}
		}
		else {
			eventName = 'ALL'
		}
	}

	/**
	 * Sanitize the String with StringUtil.sanitize
	 * and then replace all white spaces with a '_'
	 */
	static String safeFilename (String str){
		if (str) {
			str = StringUtil.sanitize(str).replaceAll(/\s/, "_")
			// TM-9050 Replaces all the invalid characters for filenames on Windows and Linux with a '_'.
			str= str.replaceAll(/\#|<|\$|%|>|!|`|&|\*|“|\||\{|\?|”|}|\/|:|\\b|\\|=|@|"|\+/,'_')
		}
		return str
	}

	/**
	 * It checks if a filename is a csv or CSV filename.
	 * @param fileName
	 * @return true if fileName belongs to a csv file
	 */
	static boolean isCsvFile(String fileName){
		return FilenameUtils.isExtension(fileName?.toLowerCase(), CSV_SUFIX)
	}

	/**
	 * It checks if a filename is a json or JSON filename.
	 * @param fileName
	 * @return true if fileName belongs to a json file
	 */
	static boolean isJsonFile(String fileName){
		return FilenameUtils.isExtension(fileName?.toLowerCase(), JSON_SUFIX)
	}

}
