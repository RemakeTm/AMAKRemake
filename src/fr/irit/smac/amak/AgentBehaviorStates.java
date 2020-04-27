package fr.irit.smac.amak;

public class AgentBehaviorStates<A extends Amas<E>, E extends Environment> {
	
	private Agent<A,E> agent;
	
	public AgentBehaviorStates(Agent<A,E> agent) {
		this.agent = agent;
	}
	
	/**
	 * This method corresponds to the perception phase of the agents and must be
	 * overridden
	 */
	protected void onPerceive() {

	}

	/**
	 * This method corresponds to the decision phase of the agents and must be
	 * overridden
	 */
	protected void onDecide() {

	}

	/**
	 * This method corresponds to the action phase of the agents and must be
	 * overridden
	 */
	protected void onAct() {

	}
	
	/**
	 * Perceive, decide and act
	 */
	void perceive() {
		for (Agent<A, E> neighbor : agent.neighborhood) {
			agent.criticalities.put(agent, agent.getCriticality());
		}
		onPerceive();
		// Criticality of agent should be updated after perception AND after action
		agent.criticality = agent.computeCriticality();
		agent.criticalities.put(agent, agent.criticality);
	}

	/**
	 * A combination of decision and action as called by the framework
	 */
	protected final void decideAndAct() {
		onDecideAndAct();

		agent.criticality = agent.computeCriticality();
	}

	/**
	 * Decide and act These two phases can often be grouped
	 */
	protected void onDecideAndAct() {
		onDecide();
		onAct();
	}


}
