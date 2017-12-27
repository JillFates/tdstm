package specs.common

class TestUtils {

    static randStr(Integer len) {
        def charset = (('A'..'Z') + ('a'..'z') + ('0'..'9')).join()
        def randomStr = (1..len).inject("") { a, b -> a += charset[new Random().nextFloat() * 62 as int] }.capitalize()
        return randomStr
    }

}