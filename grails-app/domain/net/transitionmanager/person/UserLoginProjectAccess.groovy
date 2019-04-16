package net.transitionmanager.person


import net.transitionmanager.project.Project
import net.transitionmanager.security.UserLogin

/**
 * This domain entity represents an interaction of a user login with a project.
 * A new object is created each time one of this two events happen:
 * - a user is logged in
 * - a user switches between projects
 * This serves the purpose of counting this interactions for the metrics functionality (See TM-10119)
 *
 * New records are created on a daily basis as this events occurs for different user logins.
 * Also note that this is considered unique by date and project, so if a given user logs in
 * more than once in a day this is registering it as only one interaction, creating just
 * one instance (assuming the user logs in for the same project).
 * If now a user switches to another project on that same day,
 * a new record is created.
 *
 */
class UserLoginProjectAccess {
	UserLogin userLogin
	Project   project
	Date      date

    static constraints = {
        userLogin unique: ['project', 'date']
    }

    static mapping = {
        version false
    }
}
