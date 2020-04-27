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
	
	
	/**
	 * Getter for the current phase of the agent
	 * 
	 * @return the current phase
	 */
	
	public Phase getCurrentPhase() {
		return currentPhase;
	}
	

}
