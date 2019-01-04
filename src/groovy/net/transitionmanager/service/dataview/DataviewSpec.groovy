package net.transitionmanager.service.dataview

import com.tdssrc.grails.JsonUtil
import net.transitionmanager.dataview.FieldSpecCache
import net.transitionmanager.command.DataviewApiFilterParam
import net.transitionmanager.command.DataviewApiParamsCommand
import net.transitionmanager.command.DataviewUserParamsCommand
import net.transitionmanager.domain.Dataview
import org.codehaus.groovy.grails.web.json.JSONObject

/**
 * DataviewSpec represents the specifications of a Dataview
 */

class DataviewSpec {

    static final String COMMON = 'common'
    static final String ASCENDING = 'a'
    static final String DECENDING = 'd'

    /*
        The following is the TypeScript definitions of the View Specification on the Angular side

        export class ViewSpec {
            domains: Array<String> = [];
            columns: Array<ViewColumn> = [];
            filters: Array<FilterColumn> = [];
            sort: ViewSort;
        }


        export class QueryColumn {
            domain: string;
            property: string;
        }
        export class ViewColumn extends QueryColumn {
            width?= 50;
            locked?= false;
            edit?= false;
            filter?= '';
            label: string;
        }
        export class FilterColumn extends QueryColumn {
            filter: string;
        }
        export class ViewSort extends QueryColumn {
            order: 'a' | 'd';
        }
        export class ViewModel {
            id: number;
            name: string;
            isOwner: boolean;
            isSystem: boolean;
            isShared: boolean;
            schema: ViewSpec;
        }
    */

    // Holds the parsed Dataview Specification after it is loaded
    private Map<String, List> spec
    private Map<String, Integer> args
    private Map<String, String> order
    private Boolean justPlanning

	DataviewSpec(DataviewApiParamsCommand apiParamsCommand, Dataview dataview, FieldSpecCache fieldSpecCache = null) {

		spec = [
			domains: [],
			columns: [:],
		]
		justPlanning = null
		args = [
			offset: apiParamsCommand.offset,
			max: apiParamsCommand.limit
		]

		JSONObject jsonDataview = JsonUtil.parseJson(dataview.reportSchema)
		order = [property: jsonDataview.sort.property, sort: jsonDataview.sort.sortOrder == ASCENDING ? 'asc' : 'desc']
		spec.domains = jsonDataview.domains.collect { it.toLowerCase() }
		spec.columns = jsonDataview.columns

		apiParamsCommand?.filterParams.each { DataviewApiFilterParam filter ->

			List matchingColumns = spec.columns.findAll{ Map columnSpec ->
				filter.matchWithDataviewColumnSpec(columnSpec)
			}

			if(matchingColumns.size() == 1){
				matchingColumns[0].filter = filter.filter
			} else if(matchingColumns.size() < 1){
				throw new RuntimeException("Column '${filter?.fieldName}' not specified in dataview.")
			} else {
				throw new RuntimeException('Non-unique field specified in filter parameter. Add domain prefix to uniquely identify field (e.g. device.custom1).')
			}
		}

		this.spec.columns = this.spec.columns.collect {
			Map map = it as Map
			map.put('fieldSpec', fieldSpecCache?.getFieldSpec(map.domain, map.property))
			map
		}
	}

	DataviewSpec(DataviewUserParamsCommand command, Dataview dataview = null, FieldSpecCache fieldSpecCache = null) {
		spec = command.filters
		justPlanning = command.justPlanning
		args = [offset: command.offset]
		if(command.limit != 0){
			args.max = command.limit
		}

		if(dataview) {
			JSONObject jsonDataview = JsonUtil.parseJson(dataview.reportSchema)
			spec.domains = ((jsonDataview.domains.collect {it.toLowerCase()} as Set) + (spec.domains as Set)) as List

			jsonDataview.columns.each { Map dataviewColumn ->
				dataviewColumn.domain = dataviewColumn.domain?.toLowerCase() // Fixing because Dataview is saving Uppercase domain
				Map specColumn = spec.columns.find { it.domain == dataviewColumn.domain && it.property == dataviewColumn.property}
				if(!specColumn){
					addColumn( dataviewColumn.domain , dataviewColumn.property, dataviewColumn.filter, fieldSpecCache)
				}
			}
		}

		order = [
			domain: command.sortDomain,
			property: command.sortProperty,
			sort: command.sortOrder == ASCENDING ? 'asc' : 'desc',
			fieldSpec: fieldSpecCache?.getFieldSpec(command.sortDomain, command.sortProperty)
		]

		this.spec.columns = this.spec.columns.collect {
			Map map = it as Map
			map.put('fieldSpec', fieldSpecCache?.getFieldSpec(map.domain, map.property))
			map
		}
	}

    void addColumn(domain, property, filter = null){
        spec.columns += [
            domain: domain,
            property: property,
            filter: (filter ?: '')
        ]
    }

    /**
     * returns a list of the domains that the dataview consists of
     * @return the list of domains
     */
    List<String> getDomains() {
        spec.domains
    }

    /**
     * Returns a list of map values that represent the columns that make up the dataview. The map contains
     * properties:
     *    domain - the domain or common if shared
     *    property - the GORM property name
     * @return the list of Map
     */
    List<Map> getColumns() {
        spec.columns
    }

    /**
     * Returns a list of map values that represent the columns that make up the dataview to be used as a filter. The map contains
     * properties:
     *    domain - the domain or common if shared
     *    property - the GORM property name
     * @return the list of Map
     */
    List<Map> getFilterColumns() {
        spec.columns.findAll {!!it.filter}
    }

    /**
     * Used determine if the spec has common domain that signifies multiple domains
     * @return true if there is common properties
     */
    boolean hasCommonDomain() {
        domains contains COMMON
    }

    /**
     * Returns a List containing all of the filters for the data view. THe map contains the properties:
     *    domain - the domain or common if shared
     *    property - the GORM property name
     *    filter - the regex like query string
     * @return the list of filters
     */
    List<Map> getFilters() {
        spec.filters
    }

    /**
     * Returns a Map containing the details on how to sort the view. The returned map contains the properties:
     *    domain - the domain or common if shared
     *    property - the GORM property name
     * @return the sort on property information as a Map
     */
    Map getOrder() {
        order
    }

    /**
     * Returns max value from args definition
     *
     * @return the max value
     */
    Integer getMax(){
        args.max
    }

    /**
     * Returns offset value from args definition
     *
     * @return the offset value
     */
    Integer getOffset(){
        args.offset
    }

    /**
     * Used to determine if the sort order should be ascending
     * @return true if view should be sorted ascending or false for descending
     */
    boolean isSortAscending() {
        (spec.sort.order == ASCENDING)
    }

    /**
     * Used to determine if the query must validate if assets are assigned to a Project with a MoveBundle
     *
     * @return null if the variable wasn't set and true if query must validate if a project assets is just planning
     */
    Boolean getJustPlanning() {
        justPlanning
    }
}
