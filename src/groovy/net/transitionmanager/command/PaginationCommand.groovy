package net.transitionmanager.command

/**
 * The PaginationCommand class represents the parameters that are used for paginating any datagrid
 */
@grails.validation.Validateable
class PaginationCommand implements CommandObject {

    int offset = 0
    int limit = 1

    static constraints = {
        offset min: 0
        limit min: 1
    }
}