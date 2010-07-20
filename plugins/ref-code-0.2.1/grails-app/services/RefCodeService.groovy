class RefCodeService {

    boolean transactional = false

    def get(String domain) {
        RefCode.withCriteria() {
            cacheable(true)
            eq("domain", domain)
            order("sortOrder", "asc") 
            order("value", "asc") 
        }
    }    
    
    def get(String domain, String value) {
        RefCode.withCriteria(uniqueResult:true) {
            cacheable(true)
            eq("domain", domain)
            eq("value", value)
        }
    }
    
    def validate(String domain, String value) {
        def rc = get(domain, value)
        if (!rc) {
            return false
        }
        return true
    }
}
