package com.tdsops.common.query
/**
 *
 * Filter object with the Structure that is necessary
 * for making generic queries over domain classes.
 *
 *
 */
class FilterParams {

    String domain
    String property

    enum SortOrder {

        ASC('ASC'),
        DES('DESC')

        final String id

        SortOrder(String id) {
            switch (id) {
                case ['a', 'A', 'ASC', 'asc']:
                    this.id = ASC
                    break
                case ['d', 'D', 'DESC', 'desc']:
                    this.id = DES
                    break
                default:
                    this.id = ASC
                    break
            }
        }

        static SortOrder byId(String id) {
            values().find { it.id == id }
        }

        static SortOrder lookup(String id) {
            for (SortOrder type : values()) {
                if (type.toString().equalsIgnoreCase(id)) return type
            }
            return null
        }
    }

}


