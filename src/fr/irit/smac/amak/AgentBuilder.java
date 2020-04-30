package fr.irit.smac.amak;

public abstract class AgentBuilder<A extends Amas<E>, E extends Environment> {
	
	protected Agent<A,E> agent;
	
	public Agent<A,E> getAgent(){
		return agent;
	}
	
	public void createNewAgent(A amas, Object... params) {
		agent = new Agent<A,E>(amas, params);
	}
	
	public abstract void buildBehaviorStates(Agent<A,E> agent);
	
	public abstract void buildAgentPhase(Agent<A,E> agent);

}
