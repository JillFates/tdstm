package com.tdssrc.grails

import com.tdsops.tm.enums.FilenameFormat
import groovy.util.logging.Slf4j
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.MoveEvent

/**
 * The FilenameUtil class contains a set of file name formats that can be used to simplify the creation
 * of names for Reports, Export/Import Sheets and more, creating uniformity in file naming schemes
 * throughout the app. It can also be easily extended with new formats.
 * Created by ecantu on 12/11/2017.
 */
@Slf4j(value='logger')
class FilenameUtil {

    /**
     * Main method for creating a file name.
     *
     * @param nameFormat  The file name format to be used to create the name scheme.
     * If none is chosen, the default format will be used.
     * @param nameValues  The properties/values to construct the file name.
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
     * The complete resulting format is as follows: "Project_Client-Project_Code-Event_Name-yyyymmdd_hhmm"
     * For more information see TM-8124.
     * For tests on this see TM-8125, and refer to FilenameUtilTests class.
     *
     * @param nameValues  A Map with the properties needed to construct the file name.
     *
     * project:  Mandatory
     * The project entity to use.
     * The client name and ProjectCode will be extracted to write in the resulting file name.
     * <p>
     * moveEvent:  Mandatory (unless some optional params are present, see the details below).
     * The Event entity to use. The event name will be used in the file name.(moveEvent:anEvent)
     * You can also send a list with just one event, for the same result. (moveEvent:anEvent or moveEvent:[anEvent])
     * If more than one event is sent in the list, the event names will be concatenated in the resulting filename.
     * (eg: for moveEvent:[eventOne, eventTwo] will be 'eventOne-eventTwo').
     * <p>
     * allEvents:  Optional.
     * If true, the event name part will be 'ALL' and not bundle name ([allEvents:true]).
     * In this case the mandatory property 'moveEvent' is not required, and it will be ignored if present.
     * <p>
     * If any values or properties are missing, an empty String is returned.
     * @param date  The date to be used in the file name.
     * @return  The resulting file name.
     */
   private static String clientProjectEventDateFormatter(Map nameValues, Date date) {

      String projectClient = nameValues.project?.client?.name?.replaceAll("\\s","_")
      String projectCode = nameValues.project?.projectCode?.replaceAll("\\s","_")
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
    * The client name and ProjectCode will be extracted to write in the resulting file name.
    * <p>
    * moveBundle:  Mandatory (unless some optional params are present, see the details below).
    * The moveBundle entity to use. The bundle name will be used in the file name.(moveBundle:aBundle)
    * You can also send a list with just one bundle, for the same result. (moveBundle:aBundle or moveBundle:[aBundle])
    * If more than one bundle is sent in the list, the bundle names will be concatenated in the resulting filename.
    * (eg: for moveBundle:[bundleOne, bundleTwo] will be 'bundleOne-bundleTwo').
    * <p>
    * allBundles:  Optional.
    * If true, the bundle name part will be 'ALL' and not bundle name ([allBundles:true]).
    * In this case the mandatory property 'moveBundle' is not required.
    * <p>
    * useForPlanning:  Optional.
    * If true the bundle name part will be 'PLANNING' and not the bundle name ([useForPlanning:true]).
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

      String projectClient = nameValues.project?.client?.name?.replaceAll("\\s","_")
      String projectCode = nameValues.project?.projectCode?.replaceAll("\\s","_")
      String bundleName = buildBundleName(nameValues)
      String useForPlanning = nameValues.useForPlanning ? 'PLANNING' : ''
      String exportedEntities = nameValues.exportedEntities
      String formattedDate = TimeUtil.formatDate(TimeUtil.BIG_ENDIAN, date, TimeUtil.FORMAT_DATE_TIME_26)
      if(projectClient == null || projectCode == null || !(useForPlanning || bundleName)) {     // either useForPlanning or bundleName has to have a value
         logger.error 'FilenameUtil: Error while creating file name - Some required properties are missing'
         return ''
      }
      return projectClient + '-' + projectCode + '-' + (useForPlanning ?: bundleName) + '-' + (exportedEntities ? exportedEntities + '-' : '' ) + formattedDate
   }

    /**
     * This is the default format used if none is chosen.
     *
     * @param nameValues  The properties needed to construct the file name.
     * @return  The resulting file name.
     */
    private static String filenameDefaultFormat(Map nameValues, Date date) {
        return clientProjectEventDateFormatter(nameValues, date)
    }

    /**
     * This returns the bundleName, depending on the nameValues present.
     * If the 'allBundles' param is present and true, it will return ALL_BUNDLES.
     * If not and multiple bundles are present in the moveBundle param, it will return the bundle names
     * concatenated by (-) (eg: for moveBundle:[bundleOne, bundleTwo] will be 'bundleOne-bundleTwo').
     * Finally if it's just one bundle it will return the bundle name.
     * (This can be received in two forms, moveBundle:aBundle or moveBundle:[aBundle])
     *
     * @param nameValues  The properties needed to construct the file name.
     * @return  The resulting file name.
     */
    private static String buildBundleName(Map nameVaules) {
       String bundleName = ''
       boolean allBundles = nameVaules.allBundles
       if (!allBundles) {
          def moveBundle = nameVaules.moveBundle
          if (moveBundle instanceof List){
             if(moveBundle.size() == 1) { // if the list has just one bundle, use the name of that
                bundleName = moveBundle[0].name?.replaceAll("\\s","_")
             } else if (moveBundle.size() > 1) { // concatenate names
                def bundles = moveBundle.collect {it.name?.replaceAll("\\s","_")}
                bundleName = bundles.join('-')
             }
          } else if (moveBundle instanceof MoveBundle) {
             bundleName = moveBundle?.name?.replaceAll("\\s","_")
          }
       }
       else {
          bundleName = 'ALL_BUNDLES'
       }
    }

   /**
    * This returns the eventName, depending on the nameValues present.
    * If the 'allEvents' param is present and true, it will return ALL.
    * If not and multiple events are present in the moveEvent param, it will return the event names
    * concatenated by (-) (eg: for moveEvent:[eventOne, eventTwo] will be 'eventOne-eventTwo').
    * Finally if it's just one event it will return the event name.
    * (This can be received in two forms, moveEvent:anEvent or moveEvent:[anEvent])
    *
    * @param nameValues  The properties needed to construct the file name.
    * @return  The resulting file name.
    */
   private static String buildEventName(Map nameVaules) {
      String eventName = ''
      boolean allEvents = nameVaules.allEvents
      if (!allEvents) {
         def moveEvent = nameVaules.moveEvent
         if (moveEvent instanceof List){
            if(moveEvent.size() == 1) { // if the list has just one event, use the name of that
               eventName = moveEvent[0].name?.replaceAll("\\s","_")
            } else if (moveEvent.size() > 1) { // concatenate names
               def events = moveEvent.collect {it.name?.replaceAll("\\s","_")}
               eventName = events.join('-')
            }
         } else if (moveEvent instanceof MoveEvent) {
            eventName = moveEvent?.name?.replaceAll("\\s","_")
         }
      }
      else {
         eventName = 'ALL'
      }
   }
}
