import org.jsecurity.SecurityUtils
class SecurityFilters {
    def filters = {
        // Creating, modifying, or deleting a Party,person, project,partyGroup requires the ADMIN role.
        partyCrud(controller: "(party|person|project|partyGroup)", action: "(create|edit|save|update|delete)") {
            before = {
                accessControl {
                    role("Administrator")
                }
            }
        }
        // for modify and delete a userLogin require ADMIN role 
        userCrud(controller: "userLogin", action: "(edit|update|delete)") {
            before = {
                accessControl {
                    role("Administrator")
                }
            }
        }
        // for delete require ADMIN role 
        crud(controller: "*", action: "delete") {
            before = {
                accessControl {
                    role("Administrator")
                }
            }
        }
    } 
}