package experiment;

import microsim.engine.ExperimentBuilder;
import microsim.engine.SimulationEngine;
import microsim.gui.shell.MicrosimShell;
import model.LabourParticipationModel;

public class LabourParticipationStart implements ExperimentBuilder {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		boolean showGui = true;
		
		final SimulationEngine engine = SimulationEngine.getInstance();
		MicrosimShell gui = null;
		if (showGui) {
			gui = new MicrosimShell(engine);		
			gui.setVisible(true);
		}
		
		LabourParticipationStart experimentBuilder = new LabourParticipationStart();
		engine.setExperimentBuilder(experimentBuilder);
		
		engine.setup();				
		
	}

	@Override
	public void buildExperiment(SimulationEngine engine) {
		LabourParticipationModel model = new LabourParticipationModel();
		LabourParticipationCollector collector = new LabourParticipationCollector(model);
		LabourParticipationObserver observer = new LabourParticipationObserver(model, collector);
				
		engine.addSimulationManager(model);
		engine.addSimulationManager(collector);
		engine.addSimulationManager(observer);	
	}

}
