package net.transitionmanager.command

/**
 * The PaginationCommand class represents the parameters that are used for paginating any datagrid
 */
@grails.validation.Validateable
class PaginationCommand implements CommandObject {

    int offset = 0
    int limit = 25

    static constraints = {
        offset min: 0
        limit min: 25, inList: [25, 50, 100, 500, 1000]
    }
}