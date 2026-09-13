package com.bypass.ai.engine

/**
 * Agent state machine states.
 */
enum class AgentState {
    IDLE,
    THINKING,
    PLANNING,
    INSPECTING,
    EXECUTING,
    BUILDING,
    ANALYZING_ERROR,
    REPAIRING,
    PREVIEWING,
    SUCCESS,
    FAILED,
    CANCELLED
}

/**
 * Project context for agent execution.
 */
data class ProjectContext(
    val projectPath: String,
    val projectType: String? = null,
    val relevantFiles: List<String> = emptyList(),
    val recentActions: List<String> = emptyList(),
    val recentResults: List<String> = emptyList(),
    val lastError: String? = null,
    val currentTask: String? = null
)

/**
 * Result of agent execution.
 */
data class AgentResult(
    val success: Boolean,
    val state: AgentState,
    val message: String,
    val actionsExecuted: Int = 0,
    val failedAction: String? = null,
    val repairAttempts: Int = 0,
    val duration: Long = 0L,
    val error: String? = null
)

/**
 * Internal agent event for logging/debugging.
 */
sealed class AgentEvent {
    abstract val timestamp: Long

    data class Started(override val timestamp: Long = System.currentTimeMillis()) : AgentEvent()
    data class PlanningStarted(override val timestamp: Long = System.currentTimeMillis()) : AgentEvent()
    data class GeminiRequestStarted(override val timestamp: Long = System.currentTimeMillis()) : AgentEvent()
    data class GeminiResponseReceived(
        val text: String,
        override val timestamp: Long = System.currentTimeMillis()
    ) : AgentEvent()

    data class ActionParsed(
        val type: String,
        val count: Int,
        override val timestamp: Long = System.currentTimeMillis()
    ) : AgentEvent()

    data class ActionStarted(
        val actionType: String,
        override val timestamp: Long = System.currentTimeMillis()
    ) : AgentEvent()

    data class ActionCompleted(
        val actionType: String,
        val output: String? = null,
        override val timestamp: Long = System.currentTimeMillis()
    ) : AgentEvent()

    data class ActionFailed(
        val actionType: String,
        val error: String,
        override val timestamp: Long = System.currentTimeMillis()
    ) : AgentEvent()

    data class RepairStarted(
        val failedAction: String,
        val attempt: Int,
        override val timestamp: Long = System.currentTimeMillis()
    ) : AgentEvent()

    data class RepairCompleted(
        val attempt: Int,
        val success: Boolean,
        override val timestamp: Long = System.currentTimeMillis()
    ) : AgentEvent()

    data class BuildStarted(override val timestamp: Long = System.currentTimeMillis()) : AgentEvent()
    data class BuildCompleted(
        val success: Boolean,
        val output: String? = null,
        override val timestamp: Long = System.currentTimeMillis()
    ) : AgentEvent()

    data class Completed(
        val success: Boolean,
        override val timestamp: Long = System.currentTimeMillis()
    ) : AgentEvent()

    data class Failed(
        val error: String,
        override val timestamp: Long = System.currentTimeMillis()
    ) : AgentEvent()

    data class Cancelled(override val timestamp: Long = System.currentTimeMillis()) : AgentEvent()
}

/**
 * Listener for agent state changes and events.
 */
interface AgentEventListener {
    fun onEvent(event: AgentEvent)
    fun onStateChanged(oldState: AgentState, newState: AgentState)
}

/**
 * Agent state machine for managing execution flow.
 * Ensures only valid state transitions and prevents infinite loops.
 */
class AgentStateMachine {
    private var currentState: AgentState = AgentState.IDLE
    private val listeners = mutableListOf<AgentEventListener>()
    private val events = mutableListOf<AgentEvent>()

    fun getCurrentState(): AgentState = currentState

    fun getEventHistory(): List<AgentEvent> = events.toList()

    fun addListener(listener: AgentEventListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: AgentEventListener) {
        listeners.remove(listener)
    }

    /**
     * Transition to a new state.
     * Validates the transition and notifies listeners.
     */
    fun transitionTo(newState: AgentState): Boolean {
        if (!isValidTransition(currentState, newState)) {
            return false
        }

        val oldState = currentState
        currentState = newState
        notifyStateChanged(oldState, newState)
        return true
    }

    /**
     * Emit an event.
     */
    fun emitEvent(event: AgentEvent) {
        events.add(event)
        notifyEvent(event)
    }

    /**
     * Check if a transition is valid.
     */
    private fun isValidTransition(from: AgentState, to: AgentState): Boolean {
        return when (from) {
            AgentState.IDLE -> to in setOf(
                AgentState.THINKING,
                AgentState.IDLE
            )
            AgentState.THINKING -> to in setOf(
                AgentState.PLANNING,
                AgentState.FAILED,
                AgentState.CANCELLED
            )
            AgentState.PLANNING -> to in setOf(
                AgentState.INSPECTING,
                AgentState.EXECUTING,
                AgentState.FAILED,
                AgentState.CANCELLED
            )
            AgentState.INSPECTING -> to in setOf(
                AgentState.EXECUTING,
                AgentState.PLANNING,
                AgentState.FAILED,
                AgentState.CANCELLED
            )
            AgentState.EXECUTING -> to in setOf(
                AgentState.BUILDING,
                AgentState.ANALYZING_ERROR,
                AgentState.PREVIEWING,
                AgentState.SUCCESS,
                AgentState.FAILED,
                AgentState.CANCELLED
            )
            AgentState.BUILDING -> to in setOf(
                AgentState.ANALYZING_ERROR,
                AgentState.PREVIEWING,
                AgentState.SUCCESS,
                AgentState.FAILED,
                AgentState.CANCELLED
            )
            AgentState.ANALYZING_ERROR -> to in setOf(
                AgentState.REPAIRING,
                AgentState.FAILED,
                AgentState.CANCELLED
            )
            AgentState.REPAIRING -> to in setOf(
                AgentState.EXECUTING,
                AgentState.ANALYZING_ERROR,
                AgentState.FAILED,
                AgentState.CANCELLED
            )
            AgentState.PREVIEWING -> to in setOf(
                AgentState.SUCCESS,
                AgentState.FAILED,
                AgentState.CANCELLED
            )
            AgentState.SUCCESS -> to in setOf(
                AgentState.IDLE,
                AgentState.CANCELLED
            )
            AgentState.FAILED -> to in setOf(
                AgentState.IDLE,
                AgentState.CANCELLED
            )
            AgentState.CANCELLED -> to in setOf(
                AgentState.IDLE
            )
        }
    }

    private fun notifyStateChanged(oldState: AgentState, newState: AgentState) {
        for (listener in listeners) {
            listener.onStateChanged(oldState, newState)
        }
    }

    private fun notifyEvent(event: AgentEvent) {
        for (listener in listeners) {
            listener.onEvent(event)
        }
    }
}
