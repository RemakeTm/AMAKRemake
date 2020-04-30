package fr.irit.smac.amak;


public class AgentPhase<A extends Amas<E>, E extends Environment> {
	
	private Agent<A,E> agent;
		
	/**
	 * These phases are used to synchronize agents on phase
	 * 
	 * @see fr.irit.smac.amak.Amas.ExecutionPolicy
	 * @author perles
	 *
	 */
	public enum Phase {
		/**
		 * Agent is perceiving
		 */
		PERCEPTION,
		/**
		 * Agent is deciding and acting
		 */
		DECISION_AND_ACTION,
		/**
		 * Agent haven't started to perceive, decide or act
		 */
		INITIALIZING,
		/**
		 * Agent is ready to decide
		 */
		PERCEPTION_DONE,
		/**
		 * Agent is ready to perceive or die
		 */
		DECISION_AND_ACTION_DONE
	}

	/**
	 * The current phase of the agent {@link Phase}
	 */
	protected Phase currentPhase = Phase.INITIALIZING;
	
	public AgentPhase(Agent<A,E> agent) {
		this.agent = agent;
	}
	
	/**
	 * This method is called at the beginning of an agent's cycle
	 */
	protected void onAgentCycleBegin() {

	}

	/**
	 * This method is called at the end of an agent's cycle
	 */
	protected void onAgentCycleEnd() {

	}
	
	public void onePhaseCycle() {
		currentPhase = Phase.PERCEPTION;
		phase1();
		currentPhase = Phase.DECISION_AND_ACTION;
		phase2();
	}
	/**
	 * This method represents the perception phase of the agent
	 */
	protected final void phase1() {
		onAgentCycleBegin();
		agent.behaviorStates.perceive();
		agent.agentPhase.currentPhase = Phase.PERCEPTION_DONE;
	}

	/**
	 * This method represents the decisionAndAction phase of the agent
	 */
	protected final void phase2() {
		agent.behaviorStates.decideAndAct();
		agent.executionOrder = agent._computeExecutionOrder();
		agent.onExpose();
		if (!Configuration.commandLineMode)
			agent.onUpdateRender();
		onAgentCycleEnd();
		currentPhase = Phase.DECISION_AND_ACTION_DONE;
	}
	
	/**
	 * Determine which phase comes after another
	 * 
	 * @return the next phase
	 */
	protected Phase nextPhase() {
		switch (currentPhase) {
		case PERCEPTION_DONE:
			return Phase.DECISION_AND_ACTION;
		case INITIALIZING:
		case DECISION_AND_ACTION_DONE:
		default:
			return Phase.PERCEPTION;
		}
	}
	
	/**
	 * Getter for the current phase of the agent
	 * 
	 * @return the current phase
	 */
	
	public Phase getCurrentPhase() {
		return currentPhase;
	}
	

}
