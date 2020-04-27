package fr.irit.smac.amak;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import fr.irit.smac.amak.AgentPhase.Phase;
import fr.irit.smac.amak.Amas.ExecutionPolicy;
import fr.irit.smac.amak.tools.Log;

/**
 * This class must be overridden by all agents
 * 
 * @author Alexandre Perles
 *
 * @param <A>
 *            The kind of Amas the agent refers to
 * @param <E>
 *            The kind of Environment the agent AND the Amas refer to
 */
public abstract class Agent<A extends Amas<E>, E extends Environment> implements Runnable {
	
	/**
	 * Neighborhood of the agent (must refer to the same couple amas, environment
	 */
	protected final List<Agent<A, E>> neighborhood;

	/**
	 * Criticalities of the neighbors (and it self) as perceived at the beginning of
	 * the agent's cycle
	 */
	protected final Map<Agent<A, E>, Double> criticalities = new HashMap<>();
	/**
	 * Last calculated criticality of the agent
	 */
	protected double criticality;
	/**
	 * Amas the agent belongs to
	 */
	protected final A amas;
	
	/**
	 * TODO comment
	 */
	protected final AgentBehaviorStates<A,E> behaviorStates;
	
	/**
	 * TODO comment
	 */
	protected final AgentPhase<A,E> agentPhase;
	/**
	 * Unique index to give unique id to each agent
	 */
	private static int uniqueIndex;

	/**
	 * The id of the agent
	 */
	private final int id;
	/**
	 * The order of execution of the agent as computed by
	 * {@link Agent#_computeExecutionOrder()}
	 */
	private int executionOrder;
	/**
	 * The parameters that can be user in the initialization process
	 * {@link Agent#onInitialization()}
	 */
	protected Object[] params;

	
	private boolean synchronous = true;

	/**
	 * The constructor automatically add the agent to the corresponding amas and
	 * initialize the agent
	 * 
	 * @param amas
	 *            Amas the agent belongs to
	 * @param params
	 *            The params to initialize the agent
	 */
	public Agent(A amas, Object... params) {
		this.amas = amas;
		this.behaviorStates = new AgentBehaviorStates<>(this);
		this.agentPhase = new AgentPhase<>(this);
		this.id = uniqueIndex++;
		this.params = params;
		
		neighborhood = new ArrayList<>();
		neighborhood.add(this);
		onInitialization();
		if (!Configuration.commandLineMode)
			onRenderingInitialization();
		

		if (amas != null) {
			this.amas._addAgent(this);
		}
	}

	/**
	 * Add neighbors to the agent
	 * 
	 * @param agents
	 *            The list of agent that should be considered as neighbor
	 */
	@SafeVarargs
	public final void addNeighbor(Agent<A, E>... agents) {
		for (Agent<A, E> agent : agents) {
			if (agent != null) {
				neighborhood.add(agent);
				criticalities.put(agent, Double.NEGATIVE_INFINITY);
			}
		}
	}

	/**
	 * This method must be overridden by the agents. This method shouldn't make any
	 * calls to internal representation an agent has on its environment because
	 * these information maybe outdated.
	 * 
	 * @return the criticality at a given moment
	 */
	protected double computeCriticality() {
		return Double.NEGATIVE_INFINITY;
	}

	protected void setAsynchronous() {
		if (agentPhase.currentPhase != Phase.INITIALIZING)
			Log.defaultLog.fatal("AMAK", "Asynchronous mode must be set during the initialization");
		this.synchronous = false;
	}
	/**
	 * This method must be overriden if you need to specify an execution order layer
	 * 
	 * @return the execution order layer
	 */
	protected int computeExecutionOrderLayer() {
		return 0;
	}


	
	/**
	 * In this method the agent should expose some variables with its neighbor
	 */
	protected void onExpose() {

	}

	/**
	 * This method should be used to update the representation of the agent for
	 * example in a VUI
	 */
	public void onUpdateRender() {

	}

	/**
	 * This method is now deprecated and should be replaced by onUpdateRender
	 * 
	 * @deprecated Must be replaced by {@link #onUpdateRender()}
	 */
	@Deprecated
	protected final void onDraw() {

	}

	/**
	 * Called when all initial agents have been created and are ready to be started
	 */
	protected void onReady() {

	}

	/**
	 * Called by the framework when all initial agents have been created and are
	 * almost ready to be started
	 */
	protected final void _onBeforeReady() {
		criticality = computeCriticality();
		executionOrder = _computeExecutionOrder();
	}

	/**
	 * Called before all agents are created
	 */
	protected void onInitialization() {

	}

	/**
	 * Replaced by onInitialization
	 * 
	 * @deprecated Must be replaced by {@link #onInitialization()}
	 */
	@Deprecated
	protected final void onInitialize() {

	}

	/**
	 * Called to initialize the rendering of the agent
	 */
	protected void onRenderingInitialization() {

	}

	/**
	 * @deprecated This method is useless because the state of the agent is not
	 *             supposed to evolve before or after its cycle. Use
	 *             OnAgentCycleBegin/End instead.
	 * 
	 *             This method is final because it must not be implemented.
	 *             Implement it will have no effect.
	 */
	@Deprecated
	protected final void onSystemCycleBegin() {

	}

	/**
	 * @deprecated This method is useless because the state of the agent is not
	 *             supposed to evolve before or after its cycle. Use
	 *             OnAgentCycleBegin/End instead.
	 * 
	 *             This method is final because it must not be implemented.
	 *             Implement it will have no effect.
	 */
	@Deprecated
	protected final void onSystemCycleEnd() {

	}

	/**
	 * This method is called automatically and corresponds to a full cycle of an
	 * agent
	 */
	@Override
	public void run() {
		ExecutionPolicy executionPolicy = amas.getExecutionPolicy();
		if (executionPolicy == ExecutionPolicy.TWO_PHASES) {

			agentPhase.currentPhase = nextPhase();
			switch (agentPhase.currentPhase) {
			case PERCEPTION:
				phase1();
				amas.informThatAgentPerceptionIsFinished();
				break;
			case DECISION_AND_ACTION:
				phase2();
				amas.informThatAgentDecisionAndActionAreFinished();
				break;
			default:
				Log.defaultLog.fatal("AMAK", "An agent is being run in an invalid phase (%s)", agentPhase.currentPhase);
			}
		} else if (executionPolicy == ExecutionPolicy.ONE_PHASE) {
			onePhaseCycle();
			amas.informThatAgentPerceptionIsFinished();
			amas.informThatAgentDecisionAndActionAreFinished();
		}
	}
	
	/******************************************************************************************/
	
	public void onePhaseCycle() {
		agentPhase.currentPhase = Phase.PERCEPTION;
		phase1();
		agentPhase.currentPhase = Phase.DECISION_AND_ACTION;
		phase2();
	}
	/**
	 * This method represents the perception phase of the agent
	 */
	protected final void phase1() {
		agentPhase.onAgentCycleBegin();
		behaviorStates.perceive();
		agentPhase.currentPhase = Phase.PERCEPTION_DONE;
	}

	/**
	 * This method represents the decisionAndAction phase of the agent
	 */
	protected final void phase2() {
		behaviorStates.decideAndAct();
		executionOrder = _computeExecutionOrder();
		onExpose();
		if (!Configuration.commandLineMode)
			onUpdateRender();
		agentPhase.onAgentCycleEnd();
		agentPhase.currentPhase = Phase.DECISION_AND_ACTION_DONE;
	}
	
	/********************************************************************************************/

	/**
	 * Determine which phase comes after another
	 * 
	 * @return the next phase
	 */
	private Phase nextPhase() {
		switch (agentPhase.currentPhase) {
		case PERCEPTION_DONE:
			return Phase.DECISION_AND_ACTION;
		case INITIALIZING:
		case DECISION_AND_ACTION_DONE:
		default:
			return Phase.PERCEPTION;
		}
	}

	/**
	 * Compute the execution order from the layer and a random value. This method
	 * shouldn't be overridden.
	 * 
	 * @return A number used by amak to determine which agent executes first
	 */
	protected int _computeExecutionOrder() {
		return computeExecutionOrderLayer() * 10000 + amas.getEnvironment().getRandom().nextInt(10000);
	}

	

	/**
	 * Convenient method giving the most critical neighbor at a given moment
	 * 
	 * @param includingMe
	 *            Should the agent also consider its own criticality
	 * @return the most critical agent
	 */
	protected final Agent<A, E> getMostCriticalNeighbor(boolean includingMe) {
		List<Agent<A, E>> criticalest = new ArrayList<>();
		double maxCriticality = Double.NEGATIVE_INFINITY;

		if (includingMe) {
			criticalest.add(this);
			maxCriticality = criticalities.getOrDefault(criticalest, Double.NEGATIVE_INFINITY);
		}
		for (Entry<Agent<A, E>, Double> e : criticalities.entrySet()) {
			if (e.getValue() > maxCriticality) {
				criticalest.clear();
				maxCriticality = e.getValue();
				criticalest.add(e.getKey());
			} else if (e.getValue() == maxCriticality) {
				criticalest.add(e.getKey());
			}
		}
		if (criticalest.isEmpty())
			return null;
		return criticalest.get(getEnvironment().getRandom().nextInt(criticalest.size()));
	}

	/**
	 * Get the latest computed execution order
	 * 
	 * @return the execution order
	 */
	public int getExecutionOrder() {
		return executionOrder;
	}

	/**
	 * Getter for the AMAS
	 * 
	 * @return the amas
	 */
	public A getAmas() {
		return amas;
	}

	/**
	 * Remove the agent from the system
	 */
	public void destroy() {
		getAmas()._removeAgent(this);
	}

	/**
	 * Agent toString
	 */
	@Override
	public String toString() {
		return String.format("Agent #%d", id);
	}

	/**
	 * Return the id of the agent
	 * 
	 * @return the id of the agent
	 */
	public int getId() {
		return id;
	}

	/**
	 * Getter for the environment
	 * 
	 * @return the environment
	 */
	public E getEnvironment() {
		return getAmas().getEnvironment();
	}

	public boolean isSynchronous() {
		return synchronous ;
	}
	
	/**
	 * Getter for the criticality
	 * 
	 * @return the criticality
	 */
	
	public double getCriticality() {
		return criticality;
	}
	
	/**
	 * Getter for the neighborhood
	 * 
	 * @return the neighborhood
	 */

	public List<Agent<A, E>> getNeighborhood() {
		return neighborhood;
	}
	
	/**
	 * Getter for the criticalities
	 * 
	 * @return the criticalities
	 */

	public Map<Agent<A, E>, Double> getCriticalities() {
		return criticalities;
	}
}
