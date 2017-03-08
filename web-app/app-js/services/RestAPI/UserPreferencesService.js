/**
 * Created by Jorge Morayta on 3/7/2017.
 */

'use strict';

export default class UserPreferencesService {

    constructor($log, restServiceHandler) {
        this.log = $log;
        this.restService = restServiceHandler;
        this.log.debug('UserPreferencesService Instanced');

        this.timeZoneConfiguration = {
            preferences: {}
        }
    }

    getTimeZoneConfiguration(onSuccess) {
        this.restService.commonServiceHandler().getTimeZoneConfiguration((data) => {
            this.timeZoneConfiguration = data.data;
            return onSuccess();
        });
    }

    getConvertedDateIntoTimeZone(dateString) {
        var timeString = dateString;
        var userDTFormat = this.timeZoneConfiguration.preferences.CURR_DT_FORMAT;
        var timeZone = this.timeZoneConfiguration.preferences.CURR_TZ;

        if(dateString && moment(dateString).isValid()){
            if (timeZone === null) {
                timeZone = 'GMT';
            }
            var format = 'MM/DD/YYYY';
            if (userDTFormat === 'DD/MM/YYYY') {
                format = 'DD/MM/YYYY';
            }
            timeString = moment(dateString).tz(timeZone).format(format)
        }

        return timeString !== 'null'? timeString: '';
    }

    getConvertedDateTimeIntoTimeZone(dateString) {
        var timeString = dateString;
        var userDTFormat = this.timeZoneConfiguration.preferences.CURR_DT_FORMAT;
        var timeZone = this.timeZoneConfiguration.preferences.CURR_TZ;

        if(dateString && moment(dateString).isValid()){
            if (timeZone === null) {
                timeZone = 'GMT';
            }
            var format = 'MM/DD/YYYY hh:mm a'
            if (userDTFormat === 'DD/MM/YYYY') {
                format = 'DD/MM/YYYY hh:mm a'
            }
            timeString = moment(dateString).tz(timeZone).format(format)
        }

        return timeString !== 'null'? timeString: '';
    }

}