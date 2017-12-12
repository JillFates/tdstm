package com.tdssrc.grails

import com.tdsops.tm.enums.FilenameFormat
import groovy.util.logging.Slf4j

/**
 * The FilenameUtil class contains a set of file name formats that can be used to simplify the creation
 * of names for Reports, Export/Import Sheets and more, creating uniformity in file naming schemes
 * throughout the app. It can also be easily extended with new formats.
 * Created by ecantu on 12/11/2017.
 */
@Slf4j(value='logger')
class FilenameUtil {

    /*
     * Main method for creating a file name.
     * @param nameFormat
     *      The file name format to be used to create the name scheme. If none is chosen, the default format will be used.
     * @param nameValues
     *      The properties/values to construct the file name. This will vary depending on the nameFormat chosen.
     * @param fileExtension
     *      The extension of the resulting file name
     * @param date
     *      The date to be used in the file name. This parameter is optional, if no date is provided one will be created.
     * @return
     *      The resulting file name for the given format.
     */
    static String buildFilename(FilenameFormat nameFormat=FilenameFormat.DEFAULT, Map nameValues, String fileExtension, Date date = new Date()) {
        String filename =''
        switch (nameFormat) {
            case FilenameFormat.CLIENT_PROJECT_EVENT_DATE:
                filename = clientProjectEventDateFormatter(nameValues, date)
                break
            default:
                filename = filenameDefaultFormat(nameValues, date)
                break
        }
        return filename? filename << '.' + fileExtension: filename
    }

    /*
     * This format consist of a set of property values separated by '-'.
     * If the properties have spaces in between, those will be replaced with underscores (_)
     * (ej: 'Sample Project' will be 'Sample_Project').
     * The complete resulting format is as follows: "Project_Client-Project_Code-Event_Name-yyyymmdd_hhmm"
     * For more information see TM-8124.
     *
     * @param nameValues
     *      The properties needed to construct the file name. For this format the properties
     *      expected will be 'project' and 'moveEvent'.
     *      From this, the Project_Client, Project_Code and Event_Name will be extracted.
     *      There is an optional argument 'allEvents'. If it's present, the event name part will be 'ALL'.
     *      If is not present, the event name will be used in the normal form,
     *      replacing with (_) any spaces found if the event name has any (eg: 'Test Event' will be 'Test_Event').
     *      If any values or properties are missing, an empty String is returned.
     * @param date
     *      The date to be used in the file name.
     * @return
     *      The resulting file name.
     */
    private static String clientProjectEventDateFormatter(Map nameValues, Date date) {

        String projectClient = nameValues.project?.client?.name?.replaceAll("\\s","_")
        String projectCode = nameValues.project?.projectCode?.replaceAll("\\s","_")
        String eventName = nameValues.allEvents ?  'ALL' :  nameValues.moveEvent?.name?.replaceAll("\\s","_")
        String formattedDate = TimeUtil.formatDate(TimeUtil.MIDDLE_ENDIAN, date, TimeUtil.FORMAT_DATE_TIME_26)
        if(projectClient == null || projectCode == null || eventName == null) {
            logger.error 'FilenameUtil: Error while creating file name - Some required properties are missing'
            return ''
        }
        return projectClient + '-' + projectCode + '-' + eventName + '-' + formattedDate
    }

    /*
     * This is the default format used if none is chosen.
     * @param nameValues
     *      The properties needed to construct the file name.
     * @return
     *      The resulting file name.
     */
    private static String filenameDefaultFormat(Map nameValues, Date date) {
        return clientProjectEventDateFormatter(nameValues, date)
    }
}
