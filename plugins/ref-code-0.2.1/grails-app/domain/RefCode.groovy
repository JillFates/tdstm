class RefCode {

    static mapping = {
        cache usage:'nonstrict-read-write'
    }
    
    String domain
    String value
    String abbreviation
    String meaning
    Integer sortOrder

    static constraints = {
        domain(blank:false, nullable:false, maxSize:100)
        value(blank:false, nullable:false, maxSize:240, unique:'domain')
        abbreviation(blank:true, nullable:true, maxSize:240)
        meaning(blank:true, nullable:true, maxSize:240)
        sortOrder(nullable:false, range:0..99)
    }
    
    def getAbbreviationOrValue() { abbreviation ? abbreviation : value }
    
    def getMeaningOrValue() { meaning ? meaning : value }
    
    String toString() { value }  
}
