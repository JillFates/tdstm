package com.tdssrc.grails

import com.tdssrc.grails.TimeUtil

/**
 * The FilenameUtil class contains a set of file name formats that can be used to simplify the creation
 * of names for Reports, Export/Import Sheets and more, creating uniformity in file naming schemes
 * throughout the app. It can also be easily extended with new formats.
 * Created by ecantu on 12/11/2017.
 */
class FilenameUtil {

    public static final FILENAME_DEFAULT_FORMAT = 'Filename Default Format'
    public static final String FILENAME_FORMAT_1 = "Project_Client-Project_Code-Event_Name-yyyymmdd_hhmm"

    /*
     * Main method for creating a file name.
     * @param nameFormat
     *      The file name format to be used to create the name scheme. If none is chosen, the default format will be used.
     * @param nameValues
     *      The properties/values to construct the file name. This will vary depending on the nameFormat chosen.
     * @param fileExtension
     *      The extension of the resulting file name
     * @return
     *      The resulting file name for the given format.
     */
    static String buildFilename(String nameFormat=FilenameUtil.FILENAME_DEFAULT_FORMAT, Map nameValues, String fileExtension) {
        String filename =''
        switch (nameFormat) {
            case FILENAME_FORMAT_1: filename = filenameFormat1(nameValues); break
            default: filename = filenameDefaultFormat(nameValues); break
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
     * @return
     *      The resulting file name.
     */
    private static String filenameFormat1(Map nameValues) {

        String projectClient = nameValues.project?.client?.name?.replaceAll("\\s","_")
        String projectCode = nameValues.project?.projectCode?.replaceAll("\\s","_")
        String eventName = nameValues.allEvents ?  'ALL' :  nameValues.moveEvent?.name?.replaceAll("\\s","_")
        String date = TimeUtil.formatDate(TimeUtil.MIDDLE_ENDIAN, new Date(), TimeUtil.FORMAT_DATE_TIME_26)
        if(projectClient == null || projectCode == null || eventName == null) return ''
        return projectClient + '-' + projectCode + '-' + eventName + '-' + date
    }

    /*
     * This is the default format used if none is chosen.
     * @param nameValues
     *      The properties needed to construct the file name.
     * @return
     *      The resulting file name.
     */
    private static String filenameDefaultFormat(Map nameValues) {
        return filenameFormat1(nameValues)
    }
}
