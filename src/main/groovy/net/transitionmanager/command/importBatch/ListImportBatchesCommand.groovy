package net.transitionmanager.command.importBatch

import net.transitionmanager.command.CommandObject

/**
 * Command class that will be used typically for listing
 * {@link net.transitionmanager.imports.ImportBatch}.
 *
 * @see net.transitionmanager.imports.WsImportBatchController#listImportBatches() 
 */

class ListImportBatchesCommand implements CommandObject {

    String groupGuid

    static constraints = {
        groupGuid nullable: true
    }
}