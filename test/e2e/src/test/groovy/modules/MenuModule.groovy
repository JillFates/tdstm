package modules

import geb.Module

class MenuModule extends Module {

    static content = {

        pageHeader      { $( "div", class:"container menu-top-container menu-top-container-full-menu")}
        menuBar         { pageHeader.find("div#navbar-collapse") }

        adminMenu       { menuBar.find("li.menu-parent-admin").find("a", class:"dropdown-toggle") }
        adminStaffItem  { menuBar.find("li.menu-list-staff").find("a") }
        adminUsersItem  { menuBar.find("li.menu-list-users").find("a") }

// TODO Add remains menu items here

    }
}
