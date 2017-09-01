package net.transitionmanager.command

/**
 * The DataviewUserParamsCommand represents the various parameters that make up the properties
 * that the user can override in the view.
 */
@grails.validation.Validateable
class DataviewUserParamsCommand implements CommandObject {

    List<Map> filters
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
    String sortDomain
    String sortProperty
    String sortOrder = 'a'
    
    static constraints = {
        sortDomain blank:false, inList: ['common', 'application', 'database', 'device', 'storage']
        sortProperty blank:false
        sortOrder blank:false, inList: ['a', 'd']         
    }
}