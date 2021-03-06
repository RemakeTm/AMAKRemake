package fr.irit.smac.amak.tests;

import fr.irit.smac.amak.Agent;
import fr.irit.smac.amak.Amas;
import fr.irit.smac.amak.Configuration;
import fr.irit.smac.amak.Environment;
import fr.irit.smac.amak.Scheduling;
import fr.irit.smac.amak.tools.Log;
import fr.irit.smac.amak.tools.Profiler;

public class AsyncTest {

	public static void main(String[] args) {
		new AsyncTest();
	}

	public AsyncTest() {
		Configuration.allowedSimultaneousAgentsExecution = 10;

		Log.defaultMinLevel = Log.Level.DEBUG;
		runAmas();
	}

	private void runAmas() {
		MyAMAS amas = new MyAMAS(new MyEnvironment(), Scheduling.DEFAULT);
		amas.getScheduler().setOnStop(s -> {
			Log.defaultLog.inform("time", "Threads used: %d", Configuration.allowedSimultaneousAgentsExecution);
			Log.defaultLog.inform("time", "Elapsed time: %s", Profiler.endHR("amas"));
		});
		Profiler.start("amas");

		amas.start();
	}

	public class MyAMAS extends Amas<MyEnvironment> {

		public MyAMAS(MyEnvironment environment, Scheduling scheduling, Object... params) {
			super(environment, scheduling);
		}

		@Override
		protected void onInitialAgentsCreation() {
			for (int i = 0; i < 2; i++) {
				new MyAgent(this);
			}
		}
	}

	public class MyAgent extends Agent<MyAMAS, MyEnvironment> {
		public int myCycle = 0;

		public MyAgent(MyAMAS amas, Object... params) {
			super(amas, params);
		}

		@Override
		public void onInitialization() {
			setAsynchronous();
		}

		@Override
		protected void onPerceive() {
		}

		@Override
		protected void onAct() {
			myCycle++;
			// Sleep the thread to simulate a behavior
			try {
				Thread.sleep(getId() % 2 == 0 ? 0 : 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		@Override
		protected void onAgentCycleEnd() {
			Log.defaultLog.debug("AsyncTest", "Agent #%d cycle %d", getId(), myCycle);
		}
	}

	public class MyEnvironment extends Environment {

		public MyEnvironment(Object... params) {
			super(Scheduling.DEFAULT, params);
		}
	}
}
