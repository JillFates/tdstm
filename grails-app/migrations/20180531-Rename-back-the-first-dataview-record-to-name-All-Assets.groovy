/**
 * @author ecantu
 * TM-10933 Investigate and document cause of multiple "All Asset" view entries in a recent 4.4.2 install
 * This migration script is related to 20180404-Dataview-add-unique-constraint-trim-tabs-and-spaces.groovy
 * and 20180302-Dataview-add-unique-constraint.groovy
 * Those scripts fixed a problem when views with the same needed to be renamed, but after this is possible
 * that the "All Assets" view got renamed as well to something else.
 * This should not happen as the "All Assets" view is a special view (id=1), so just in case make sure this
 * view name remains as "All Assets".
 */
databaseChangeLog = {
	changeSet(author: "ecantu", id: "TM-10933-1") {
		comment("Rename back the first dataview to name 'All Assets'")
				sql ("update dataview set name = 'All Assets' where id = 1;")
	}
}
