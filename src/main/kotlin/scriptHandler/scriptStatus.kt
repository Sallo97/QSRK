package scriptHandler

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

data class ScriptStatus(val statusType: StatusType = StatusType.WAITING) {
    val icon = toIcon(statusType)

    /**
     * The possible status of a script:
     * SUCCESS -> The script has terminated with success (i.e. exitStatus = 0).
     * FAIL -> The script has terminated with failure (i.e. exitStatus != 0).
     * RUNNING -> The script has been started and has yet to finish.
     * ABORTED -> The script has terminated abruptly (i.e. exitStatus = 137/130).
     * WAITING -> No script has been started (this status happens only at the start of the program).
     */
    enum class StatusType {
        SUCCESS, FAIL, RUNNING, WAITING, ABORTED;
    }

    companion object {
        /**
         * Given the [exitStatus] of a process, returns the associated StatusT.
         */
        fun fromExitStatus(exitStatus: Int): ScriptStatus {
            val statusType =
                when (exitStatus) {
                    0 -> StatusType.SUCCESS
                    137 -> StatusType.ABORTED
                    130 -> StatusType.ABORTED
                    else -> StatusType.FAIL
                }
            return ScriptStatus(statusType)
        }

        /**
         * Given a [statusType] returns the associated icon.
         */
        private fun toIcon(statusType: StatusType): ImageVector =
            when (statusType) {
                StatusType.SUCCESS -> Icons.Filled.Check
                StatusType.FAIL -> Icons.Filled.Warning
                StatusType.RUNNING -> Icons.Filled.Refresh
                StatusType.WAITING -> Icons.Filled.Check
                StatusType.ABORTED -> Icons.Filled.Warning
            }
    }
}