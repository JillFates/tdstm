package net.transitionmanager.command

/**
 * The DataviewApiParamsCommand is used to filter API dataviews requests
 */
@grails.validation.Validateable
class DataviewApiParamsCommand implements CommandObject {

    int offset = 0
    int limit = 25

    static constraints = {
        offset min: 0
        limit min: 0, max: 100
    }
}