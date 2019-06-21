package net.transitionmanager.command.reports

import net.transitionmanager.command.CommandObject

class ActivityMetricsCommand implements CommandObject{

    /**
     * Project ID.
     */
    List<String> projectIds

    /**
     * Start Date.
     */
    String startDate

    /**
     * End Date.
     */
    String endDate

    /**
     * Include Non Planning.
     */
    Boolean includeNonPlanning
}
