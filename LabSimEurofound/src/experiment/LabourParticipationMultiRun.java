package experiment;

import microsim.engine.MultiRun;
import microsim.engine.SimulationEngine;
import microsim.gui.shell.MultiRunFrame;
import model.LabourParticipationModel;
import model.enums.Country;

public class LabourParticipationMultiRun extends MultiRun {

	public static boolean executeWithGui = true;

	private static int maxNumberOfRuns = 12;

	private static String countryString;

	private Long counter = 0L;
	
	private Long randomSeed = 600L;
	
	public static void main(String[] args) {
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-n")){
				
				try {
					maxNumberOfRuns = Integer.parseInt(args[i + 1]);
			    } catch (NumberFormatException e) {
			        System.err.println("Argument " + args[i + 1] + " must be an integer reflecting the maximum number of runs.");
			        System.exit(1);
			    }
				
				i++;
			}
			else if (args[i].equals("-g")){
				executeWithGui = Boolean.parseBoolean(args[i + 1]);
				i++;
			}
			else if (args[i].equals("-c")){				//Set country by arguments here
				countryString = args[i+1];				
				i++;
			}
			
		}
		
		SimulationEngine engine = SimulationEngine.getInstance();
		
		LabourParticipationMultiRun experimentBuilder = new LabourParticipationMultiRun();
//		engine.setBuilderClass(LabourParticipationMultiRun.class);			//This works but is deprecated
		engine.setExperimentBuilder(experimentBuilder);					//This replaces the above line... but does it work?
		engine.setup();													//Do we need this?  Worked fine without it...

		if (executeWithGui)
			new MultiRunFrame(experimentBuilder, "LabSimEurofound MultiRun", maxNumberOfRuns);
		else
			experimentBuilder.start();
	}

	@Override
	public void buildExperiment(SimulationEngine engine) {
		LabourParticipationModel model = new LabourParticipationModel();
		setCountry(model);								//Set country based on input arguments
		model.setRandomSeedIfFixed(randomSeed);
		engine.addSimulationManager(model);
		
		LabourParticipationCollector collector = new LabourParticipationCollector(model);
		engine.addSimulationManager(collector);
		
//		LabourParticipationObserver observer = new LabourParticipationObserver(model, collector);		//Not needed for MultiRun?
//		engine.addSimulationManager(observer);

		
	}

	private void setCountry(LabourParticipationModel model) {
		if(countryString.equalsIgnoreCase("ES")) {
			model.setCountry(Country.ES);
		}
		else if(countryString.equalsIgnoreCase("GR")) {
			model.setCountry(Country.GR);
		}
		else if(countryString.equalsIgnoreCase("HU")) {
			model.setCountry(Country.HU);
		}	
		else if(countryString.equalsIgnoreCase("IE")) {
			model.setCountry(Country.IE);
		}
		else if(countryString.equalsIgnoreCase("IT")) {
			model.setCountry(Country.IT);
		}
		else if(countryString.equalsIgnoreCase("SE")) {
			model.setCountry(Country.SE);
		}
		else throw new RuntimeException("countryString is not set to an appropriate string!");
//		if((counter%6)==0) {
//			model.setCountry(Country.ES);
//		}
//		else if((counter%6)==1) {
//			model.setCountry(Country.GR);
//		}
//		else if((counter%6)==2) {
//			model.setCountry(Country.HU);
//		}	
//		else if((counter%6)==3) {
//			model.setCountry(Country.IE);
//		}
//		else if((counter%6)==4) {
//			model.setCountry(Country.IT);
//		}
//		else if((counter%6)==5) {
//			model.setCountry(Country.SE);
//		}
//		else throw new RuntimeException("country not set properly!");

	}
	
	@Override
	public boolean nextModel() {
		randomSeed++;
		
		counter++;

//		if((counter%6)==0) {
//			model.setCountry(Country.ES);
//		}
//		else if((counter%6)==1) {
//			model.setCountry(Country.GR);
//		}
//		else if((counter%6)==2) {
//			model.setCountry(Country.HU);
//		}
//		else if((counter%6)==3) {
//			model.setCountry(Country.IE);
//		}	
//		else if((counter%6)==4) {
//			model.setCountry(Country.IT);
//		}
//		else if((counter%6)==5) {
//			model.setCountry(Country.SE);
//		}
//		else throw new RuntimeException("country not set properly!");
//		
//		if(counter < maxNumberOfRuns) {
//			return true;
//		}
//		else return false;
//
		
		if(counter < maxNumberOfRuns) {
			return true;
		}
//		else if(!(countryString.equalsIgnoreCase("SE"))) {
//			counter = 0L;
//			if(countryString.equalsIgnoreCase("ES")) {
//				countryString = "GR";
//			}
//			else if(countryString.equalsIgnoreCase("GR")) {
//				countryString = "HU";
//			}
//			else if(countryString.equalsIgnoreCase("HU")) {
//				countryString = "IE";
//			}	
//			else if(countryString.equalsIgnoreCase("IE")) {
//				countryString = "IT";
//			}
//			else if(countryString.equalsIgnoreCase("IT")) {
//				countryString = "SE";
//			}	
//			return true;
//		}
		else return false;
	}

	@Override
	public String setupRunLabel() {
		return randomSeed.toString();
	}

}
