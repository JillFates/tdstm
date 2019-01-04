import grails.test.mixin.TestFor
import test.AbstractUnitSpec

/**
 * Created by BairesDev on 5/18/2017.
 */
@TestFor(AssetEntityController)
class AssetEntityControllerSpec extends AbstractUnitSpec {

    void '01. Test tardy factor'() {
        expect: 'Test the resulting tardy factor for different durations.'
                'For durations between 0 and 5, tardy factor should be 5. '
                'For durations equal or bigger than 300, tardy factor should be 30.'
                'Any duration in between should give the 10 percentage integer value of the duration for the tardy factor.'
        controller.computeTardyFactor(duration) == tardyFactor
        where:
        duration	| tardyFactor
            1 		| 5
            10   	| 5
            30      | 5
            50 	    | 5
            55 	    | 5
            60 	    | 6
            63 	    | 6
            69 	    | 6
            70 	    | 7
            80 	    | 8
            90 	    | 9
            100 	| 10
            200     | 20
            250     | 25
            300     | 30
            310     | 30
            350     | 30
            800     | 30
            2500    | 30
    }
}
