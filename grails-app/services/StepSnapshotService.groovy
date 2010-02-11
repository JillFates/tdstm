class StepSnapshotService {

	def moveBundleService
    boolean transactional = true

    def process( def moveBundleId ) {
    	def moveBundle = MoveBundle.findById( moveBundleId )
		def MoveBundleSteps = MoveBundleStep.findAllByMoveBundle(moveBundle)
		MoveBundleSteps.each{ 
    		// statements if no predecessor
    		def actualTimes = moveBundleService.getActualTimes( moveBundleId, it.transitionId)
			it.actualStartTime = actualTimes.get("started");
    		it.actualCompletionTime = actualTimes.get("completed");
    		// need to implement the StepSnapshot functionlaity
			//def stepSnapshot = new StepSnapshot()
    	}
    }
}
