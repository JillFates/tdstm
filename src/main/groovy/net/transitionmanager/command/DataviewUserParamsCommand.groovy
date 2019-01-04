package net.transitionmanager.command

/**
 * The DataviewUserParamsCommand represents the various parameters that make up the properties
 * that the user can override in the view.
 */
@grails.validation.Validateable
class DataviewUserParamsCommand implements CommandObject {

    String sortDomain
    String sortProperty
    String sortOrder = 'a'
    Boolean justPlanning
    Boolean forExport

    int offset = 0
    int limit = 25

    Map<String, List> filters
    /* 
        [
              [
                domain: 'application',
                property: 'sme',
                filter: 'joe'
            ],
            [
                domain: 'common',
                property: 'environment',
                filter: 'dev'
            ]
        ]
    */

    static constraints = {
        sortDomain blank: false, inList: ['common', 'application', 'database', 'device', 'storage']
        sortProperty blank: false
        sortOrder blank: false, inList: ['a', 'd']
        offset min: 0
        limit min: 0
        justPlanning nullable: true
        forExport nullable: true
    }
}
