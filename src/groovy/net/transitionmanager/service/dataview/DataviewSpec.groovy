package net.transitionmanager.service.dataview

// import net.transitionmanager.domain.Dataview
import com.tdssrc.grails.JsonUtil
import groovy.transform.CompileStatic
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
    private JSONObject spec

    // Constructor
    DataviewSpec(Dataview dataview) {
        spec = JsonUtil.parseJson(dataview.reportSchema)
    }

    DataviewSpec(JSONObject jsonObject) {
       spec = jsonObject 
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
    Map getSortOn() {
        [ domain: spec.sort.domain, property: spec.sort.property ]
    }

    /**
     * Used to determine if the sort order should be ascending
     * @return true if view should be sorted ascending or false for descending
     */
    boolean isSortAscending() {
        (spec.sort.order == ASCENDING)
    }
}
