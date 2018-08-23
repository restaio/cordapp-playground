package restaio.estates.flow

import net.corda.core.utilities.ProgressTracker

interface FlowCompanion {

    fun tracker(): ProgressTracker
}