package net.transitionmanager.command.assetentity

import net.transitionmanager.command.CommandObject

/**
 *
 * A Command Object used for bulk-deleting dependencies.
 * The purpose of having a command object for this was that this functionality could be invoked from
 * an html form (legacy) or from angular code using json, so the command object solves this as the
 * mapping is handled automatically.
 *
 * @param dependencies  The list of dependency ids to delete.
 */
class BulkDeleteDependenciesCommand implements CommandObject {
    List dependencies

    static constraints = {
        nullable: false
    }
}
