package experiment;

import microsim.annotation.ModelParameter;
import microsim.engine.AbstractSimulationObserverManager;
import microsim.engine.SimulationCollectorManager;
import microsim.engine.SimulationEngine;
import microsim.engine.SimulationManager;
import microsim.event.CommonEventType;
import microsim.event.EventGroup;
import microsim.event.EventListener;
import microsim.event.SingleTargetEvent;
import microsim.gui.GuiUtils;
import microsim.gui.plot.TimeSeriesSimulationPlotter;
import microsim.statistics.CrossSection;
import microsim.statistics.functions.MeanArrayFunction;
import model.LabourParticipationModel;
import model.Person;
import data.filters.AgeGroupCSfilter;
import data.filters.FemaleAgeGroupCSfilter;
import data.filters.FemalesWithChildrenAgeGroupCSfilter;
import data.filters.MaleAgeGroupCSfilter;

public class LabourParticipationObserver extends AbstractSimulationObserverManager implements EventListener {

	@ModelParameter(description="Toggle to turn charts on/off")
	private Boolean showCharts = true;
	
	@ModelParameter
	private Double displayFrequency = 1.;
		
	private CrossSection.Integer lowEducationCS;
	private CrossSection.Integer midEducationCS;
	private CrossSection.Integer highEducationCS;
	
	private CrossSection.Integer studentYoungCS;
	private CrossSection.Integer studentMediumCS;
	private CrossSection.Integer studentOldCS;
	
	private CrossSection.Integer femalesCohabiting20_29CS;
	private CrossSection.Integer femalesCohabiting30_44CS;
	private CrossSection.Integer femalesCohabiting45_59CS;
	private CrossSection.Integer femalesCohabiting60_74CS;
	
	private CrossSection.Integer femaleParticipationCS;
//	private CrossSection.Integer femaleParticipation20_44CS;
//	private CrossSection.Integer femaleParticipation20_44WithChildrenCS;
	private CrossSection.Integer maleParticipationCS;

	private CrossSection.Integer femaleParticipation20_24CS;
	private CrossSection.Integer femaleParticipation20_24WithChildrenCS;
	private CrossSection.Integer femaleParticipation25_29CS;
	private CrossSection.Integer femaleParticipation25_29WithChildrenCS;
	private CrossSection.Integer femaleParticipation30_34CS;
	private CrossSection.Integer femaleParticipation30_34WithChildrenCS;
	private CrossSection.Integer femaleParticipation35_39CS;
	private CrossSection.Integer femaleParticipation35_39WithChildrenCS;
	private CrossSection.Integer femaleParticipation40_44CS;
	private CrossSection.Integer femaleParticipation40_44WithChildrenCS;
	private CrossSection.Integer femaleParticipation45_49CS;
	private CrossSection.Integer femaleParticipation45_49WithChildrenCS;

	private CrossSection.Integer malesEmployed17_74CS;
	private CrossSection.Integer femalesEmployed17_74CS;
	private CrossSection.Integer femalesEmployed20_44CS;
	private CrossSection.Integer femalesEmployed20_44WithChildrenCS;

	private CrossSection.Integer femaleEmployed20_24CS;
	private CrossSection.Integer femaleEmployed20_24WithChildrenCS;
	private CrossSection.Integer femaleEmployed25_29CS;
	private CrossSection.Integer femaleEmployed25_29WithChildrenCS;
	private CrossSection.Integer femaleEmployed30_34CS;
	private CrossSection.Integer femaleEmployed30_34WithChildrenCS;
	private CrossSection.Integer femaleEmployed35_39CS;
	private CrossSection.Integer femaleEmployed35_39WithChildrenCS;
	private CrossSection.Integer femaleEmployed40_44CS;
	private CrossSection.Integer femaleEmployed40_44WithChildrenCS;
	private CrossSection.Integer femaleEmployed45_49CS;
	private CrossSection.Integer femaleEmployed45_49WithChildrenCS;
	
	private TimeSeriesSimulationPlotter eduPlotter;
	private TimeSeriesSimulationPlotter studentPlotter;
	private TimeSeriesSimulationPlotter femalesCohabitingPlotter;
	private TimeSeriesSimulationPlotter activePlotter;
	private TimeSeriesSimulationPlotter active20sPlotter;
	private TimeSeriesSimulationPlotter active30sPlotter;
	private TimeSeriesSimulationPlotter active40sPlotter;
	private TimeSeriesSimulationPlotter employedPlotter;
	private TimeSeriesSimulationPlotter employed20sPlotter;
	private TimeSeriesSimulationPlotter employed30sPlotter;
	private TimeSeriesSimulationPlotter employed40sPlotter;

	// ---------------------------------------------------------------------
	// Constructor
	// ---------------------------------------------------------------------
	
	public LabourParticipationObserver(SimulationManager manager, SimulationCollectorManager simulationCollectionManager) {
		super(manager, simulationCollectionManager);		
	}

	
	// ---------------------------------------------------------------------
	// Manager
	// ---------------------------------------------------------------------

	@Override
	public void buildObjects() {
		if(showCharts) {
			
			final LabourParticipationModel model = (LabourParticipationModel) getManager();

			//Cross sections
			
			//For Education Levels Chart
			AgeGroupCSfilter over29yoFilter = new AgeGroupCSfilter(30,75);
			lowEducationCS = new CrossSection.Integer(model.getPersons(), Person.class, "getLowEducation", true);
			lowEducationCS.setFilter(over29yoFilter);
			midEducationCS = new CrossSection.Integer(model.getPersons(), Person.class, "getMidEducation", true);
			midEducationCS.setFilter(over29yoFilter);
			highEducationCS = new CrossSection.Integer(model.getPersons(), Person.class, "getHighEducation", true);
			highEducationCS.setFilter(over29yoFilter);

			//For Student Chart
			AgeGroupCSfilter age17_20Filter = new AgeGroupCSfilter(17,20);
			AgeGroupCSfilter age21_24Filter = new AgeGroupCSfilter(21,24);
			AgeGroupCSfilter age25_29Filter = new AgeGroupCSfilter(25,29);
			studentYoungCS = new CrossSection.Integer(model.getPersons(), Person.class, "getStudent", true);
			studentYoungCS.setFilter(age17_20Filter);
			studentMediumCS = new CrossSection.Integer(model.getPersons(), Person.class, "getStudent", true);
			studentMediumCS.setFilter(age21_24Filter);
			studentOldCS = new CrossSection.Integer(model.getPersons(), Person.class, "getStudent", true);
			studentOldCS.setFilter(age25_29Filter);
					
			//For Cohabiting (Coupled) Chart
			FemaleAgeGroupCSfilter female20_29Filter = new FemaleAgeGroupCSfilter(20, 29);
			femalesCohabiting20_29CS = new CrossSection.Integer(model.getPersons(), Person.class, "getCohabiting", true);
			femalesCohabiting20_29CS.setFilter(female20_29Filter);
			
			FemaleAgeGroupCSfilter female30_44Filter = new FemaleAgeGroupCSfilter(30, 44);
			femalesCohabiting30_44CS = new CrossSection.Integer(model.getPersons(), Person.class, "getCohabiting", true);
			femalesCohabiting30_44CS.setFilter(female30_44Filter);
			
			FemaleAgeGroupCSfilter female45_59Filter = new FemaleAgeGroupCSfilter(45, 59);
			femalesCohabiting45_59CS = new CrossSection.Integer(model.getPersons(), Person.class, "getCohabiting", true);
			femalesCohabiting45_59CS.setFilter(female45_59Filter);
			
			FemaleAgeGroupCSfilter female60_74Filter = new FemaleAgeGroupCSfilter(60, 74);
			femalesCohabiting60_74CS = new CrossSection.Integer(model.getPersons(), Person.class, "getCohabiting", true);
			femalesCohabiting60_74CS.setFilter(female60_74Filter);
			
			//Filters for both participation and employment charts
			FemaleAgeGroupCSfilter female17_74Filter = new FemaleAgeGroupCSfilter(17, 74);
			FemaleAgeGroupCSfilter female20_44Filter = new FemaleAgeGroupCSfilter(20, 44);
			FemalesWithChildrenAgeGroupCSfilter fem20_44withChildrenFilter = new FemalesWithChildrenAgeGroupCSfilter(20, 44);
			MaleAgeGroupCSfilter male17_74Filter = new MaleAgeGroupCSfilter(17, 74);
			
			FemaleAgeGroupCSfilter female20_24Filter = new FemaleAgeGroupCSfilter(20, 24);
			FemalesWithChildrenAgeGroupCSfilter fem20_24withChildrenFilter = new FemalesWithChildrenAgeGroupCSfilter(20, 24);
			FemaleAgeGroupCSfilter female25_29Filter = new FemaleAgeGroupCSfilter(25, 29);
			FemalesWithChildrenAgeGroupCSfilter fem25_29withChildrenFilter = new FemalesWithChildrenAgeGroupCSfilter(25, 29);
			FemaleAgeGroupCSfilter female30_34Filter = new FemaleAgeGroupCSfilter(30, 34);
			FemalesWithChildrenAgeGroupCSfilter fem30_34withChildrenFilter = new FemalesWithChildrenAgeGroupCSfilter(30, 34);
			FemaleAgeGroupCSfilter female35_39Filter = new FemaleAgeGroupCSfilter(35, 39);
			FemalesWithChildrenAgeGroupCSfilter fem35_39withChildrenFilter = new FemalesWithChildrenAgeGroupCSfilter(35, 39);
			FemaleAgeGroupCSfilter female40_44Filter = new FemaleAgeGroupCSfilter(40, 44);
			FemalesWithChildrenAgeGroupCSfilter fem40_44withChildrenFilter = new FemalesWithChildrenAgeGroupCSfilter(40, 44);		
			FemaleAgeGroupCSfilter female45_49Filter = new FemaleAgeGroupCSfilter(45, 49);
			FemalesWithChildrenAgeGroupCSfilter fem45_49withChildrenFilter = new FemalesWithChildrenAgeGroupCSfilter(45, 49);
			
			
			//For Participation (Active) Charts
			femaleParticipationCS = new CrossSection.Integer(model.getPersons(), Person.class, "getActive", true);
			femaleParticipationCS.setFilter(female17_74Filter);
			
//			femaleParticipation20_44CS = new CrossSection.Integer(model.getPersons(), Person.class, "getActive", true);
//			femaleParticipation20_44CS.setFilter(female20_44Filter);
//			
//			femaleParticipation20_44WithChildrenCS = new CrossSection.Integer(model.getPersons(), Person.class, "getActive", true);
//			femaleParticipation20_44WithChildrenCS.setFilter(fem20_44withChildrenFilter);
			
			maleParticipationCS = new CrossSection.Integer(model.getPersons(), Person.class, "getActive", true);
			maleParticipationCS.setFilter(male17_74Filter);
			
			femaleParticipation20_24CS = new CrossSection.Integer(model.getPersons(), Person.class, "getActive", true);
			femaleParticipation20_24WithChildrenCS = new CrossSection.Integer(model.getPersons(), Person.class, "getActive", true);
			femaleParticipation25_29CS = new CrossSection.Integer(model.getPersons(), Person.class, "getActive", true);
			femaleParticipation25_29WithChildrenCS = new CrossSection.Integer(model.getPersons(), Person.class, "getActive", true);
			femaleParticipation30_34CS = new CrossSection.Integer(model.getPersons(), Person.class, "getActive", true);
			femaleParticipation30_34WithChildrenCS = new CrossSection.Integer(model.getPersons(), Person.class, "getActive", true);
			femaleParticipation35_39CS = new CrossSection.Integer(model.getPersons(), Person.class, "getActive", true);
			femaleParticipation35_39WithChildrenCS = new CrossSection.Integer(model.getPersons(), Person.class, "getActive", true);
			femaleParticipation40_44CS = new CrossSection.Integer(model.getPersons(), Person.class, "getActive", true);
			femaleParticipation40_44WithChildrenCS = new CrossSection.Integer(model.getPersons(), Person.class, "getActive", true);
			femaleParticipation45_49CS = new CrossSection.Integer(model.getPersons(), Person.class, "getActive", true);
			femaleParticipation45_49WithChildrenCS = new CrossSection.Integer(model.getPersons(), Person.class, "getActive", true);

			femaleParticipation20_24CS.setFilter(female20_24Filter);
			femaleParticipation20_24WithChildrenCS.setFilter(fem20_24withChildrenFilter);
			femaleParticipation25_29CS.setFilter(female25_29Filter);
			femaleParticipation25_29WithChildrenCS.setFilter(fem25_29withChildrenFilter);
			femaleParticipation30_34CS.setFilter(female30_34Filter);
			femaleParticipation30_34WithChildrenCS.setFilter(fem30_34withChildrenFilter);
			femaleParticipation35_39CS.setFilter(female35_39Filter);
			femaleParticipation35_39WithChildrenCS.setFilter(fem35_39withChildrenFilter);
			femaleParticipation40_44CS.setFilter(female40_44Filter);
			femaleParticipation40_44WithChildrenCS.setFilter(fem40_44withChildrenFilter);
			femaleParticipation45_49CS.setFilter(female45_49Filter);
			femaleParticipation45_49WithChildrenCS.setFilter(fem45_49withChildrenFilter);

			//For Employment Chart
//			ActiveMalesAgeGroupCSfilter activeMales17_74Filter = new ActiveMalesAgeGroupCSfilter(17, 74);
//			ActiveFemalesAgeGroupCSfilter activeFemales17_74Filter = new ActiveFemalesAgeGroupCSfilter(17, 74);
//			ActiveFemalesAgeGroupCSfilter activeFemales20_44Filter = new ActiveFemalesAgeGroupCSfilter(20, 44);
//			ActiveFemalesWithKids3underAgeGroupCSfilter activeFemales20_44WithChildren3under = new ActiveFemalesWithKids3underAgeGroupCSfilter(20, 44);
			
			malesEmployed17_74CS = new CrossSection.Integer(model.getPersons(), Person.class, "getEmployed", true);
			femalesEmployed17_74CS = new CrossSection.Integer(model.getPersons(), Person.class, "getEmployed", true);
			femalesEmployed20_44CS = new CrossSection.Integer(model.getPersons(), Person.class, "getEmployed", true);
			femalesEmployed20_44WithChildrenCS = new CrossSection.Integer(model.getPersons(), Person.class, "getEmployed", true);
			
			malesEmployed17_74CS.setFilter(male17_74Filter);
			femalesEmployed17_74CS.setFilter(female17_74Filter);
			femalesEmployed20_44CS.setFilter(female20_44Filter);
			femalesEmployed20_44WithChildrenCS.setFilter(fem20_44withChildrenFilter);

			femaleEmployed20_24CS = new CrossSection.Integer(model.getPersons(), Person.class, "getEmployed", true);
			femaleEmployed20_24WithChildrenCS = new CrossSection.Integer(model.getPersons(), Person.class, "getEmployed", true);
			femaleEmployed25_29CS = new CrossSection.Integer(model.getPersons(), Person.class, "getEmployed", true);
			femaleEmployed25_29WithChildrenCS = new CrossSection.Integer(model.getPersons(), Person.class, "getEmployed", true);
			femaleEmployed30_34CS = new CrossSection.Integer(model.getPersons(), Person.class, "getEmployed", true);
			femaleEmployed30_34WithChildrenCS = new CrossSection.Integer(model.getPersons(), Person.class, "getEmployed", true);
			femaleEmployed35_39CS = new CrossSection.Integer(model.getPersons(), Person.class, "getEmployed", true);
			femaleEmployed35_39WithChildrenCS = new CrossSection.Integer(model.getPersons(), Person.class, "getEmployed", true);
			femaleEmployed40_44CS = new CrossSection.Integer(model.getPersons(), Person.class, "getEmployed", true);
			femaleEmployed40_44WithChildrenCS = new CrossSection.Integer(model.getPersons(), Person.class, "getEmployed", true);
			femaleEmployed45_49CS = new CrossSection.Integer(model.getPersons(), Person.class, "getEmployed", true);
			femaleEmployed45_49WithChildrenCS = new CrossSection.Integer(model.getPersons(), Person.class, "getEmployed", true);

			femaleEmployed20_24CS.setFilter(female20_24Filter);
			femaleEmployed20_24WithChildrenCS.setFilter(fem20_24withChildrenFilter);
			femaleEmployed25_29CS.setFilter(female25_29Filter);
			femaleEmployed25_29WithChildrenCS.setFilter(fem25_29withChildrenFilter);
			femaleEmployed30_34CS.setFilter(female30_34Filter);
			femaleEmployed30_34WithChildrenCS.setFilter(fem30_34withChildrenFilter);
			femaleEmployed35_39CS.setFilter(female35_39Filter);
			femaleEmployed35_39WithChildrenCS.setFilter(fem35_39withChildrenFilter);
			femaleEmployed40_44CS.setFilter(female40_44Filter);
			femaleEmployed40_44WithChildrenCS.setFilter(fem40_44withChildrenFilter);
			femaleEmployed45_49CS.setFilter(female45_49Filter);
			femaleEmployed45_49WithChildrenCS.setFilter(fem45_49withChildrenFilter);
			
			//Plotters			
			
		    studentPlotter = new TimeSeriesSimulationPlotter("Proportion of students", "");
		    studentPlotter.addSeries("17-20 yo", new MeanArrayFunction(studentYoungCS));		//'yo' means "years old"
		    studentPlotter.addSeries("21-24 yo", new MeanArrayFunction(studentMediumCS));
		    studentPlotter.addSeries("25-29 yo", new MeanArrayFunction(studentOldCS));
		    GuiUtils.addWindow(studentPlotter, 250, 0, 400, 300);	
	
		    eduPlotter = new TimeSeriesSimulationPlotter("Education level of over-29 yo's", "");		//'yo' means "years old"
		    eduPlotter.addSeries("low", new MeanArrayFunction(lowEducationCS));
		    eduPlotter.addSeries("mid", new MeanArrayFunction(midEducationCS));
		    eduPlotter.addSeries("high", new MeanArrayFunction(highEducationCS));
		    GuiUtils.addWindow(eduPlotter, 650, 0, 400, 300);
		    
			femalesCohabitingPlotter = new TimeSeriesSimulationPlotter("Proportion of cohabiting females", "");
			femalesCohabitingPlotter.addSeries("20-29 yo", new MeanArrayFunction(femalesCohabiting20_29CS));		//'yo' means "years old"
			femalesCohabitingPlotter.addSeries("30-44 yo", new MeanArrayFunction(femalesCohabiting30_44CS));
			femalesCohabitingPlotter.addSeries("45-59 yo", new MeanArrayFunction(femalesCohabiting45_59CS));
			femalesCohabitingPlotter.addSeries("60-74 yo", new MeanArrayFunction(femalesCohabiting60_74CS));
			GuiUtils.addWindow(femalesCohabitingPlotter, 1050, 0, 400, 300);
			
			activePlotter = new TimeSeriesSimulationPlotter("Participation Rates", "");
			activePlotter.addSeries("females", new MeanArrayFunction(femaleParticipationCS));
	//		activePlotter.addSeries("females 20-44 yo", new MeanArrayFunction(collector.getFemaleParticipation20_44CS()));		//'yo' means "years old"
	//		activePlotter.addSeries("females 20-44 yo with children", new MeanArrayFunction(collector.getFemaleParticipation20_44WithChildrenCS()));
			activePlotter.addSeries("males", new MeanArrayFunction(maleParticipationCS));
			GuiUtils.addWindow(activePlotter, 250, 300, 400, 300);
			
			active20sPlotter = new TimeSeriesSimulationPlotter("Female Participation Rates I", "");
			active20sPlotter.addSeries("females 20-24 yo", new MeanArrayFunction(femaleParticipation20_24CS));		//'yo' means "years old"
			active20sPlotter.addSeries("females 20-24 yo with children", new MeanArrayFunction(femaleParticipation20_24WithChildrenCS));
			active20sPlotter.addSeries("females 25-29 yo", new MeanArrayFunction(femaleParticipation25_29CS));		//'yo' means "years old"
			active20sPlotter.addSeries("females 25-29 yo with children", new MeanArrayFunction(femaleParticipation25_29WithChildrenCS));
			GuiUtils.addWindow(active20sPlotter, 650, 300, 400, 300);
	
			active30sPlotter = new TimeSeriesSimulationPlotter("Female Participation Rates II", "");
			active30sPlotter.addSeries("females 30-34 yo", new MeanArrayFunction(femaleParticipation30_34CS));		//'yo' means "years old"
			active30sPlotter.addSeries("females 30-34 yo with children", new MeanArrayFunction(femaleParticipation30_34WithChildrenCS));
			active30sPlotter.addSeries("females 35-39 yo", new MeanArrayFunction(femaleParticipation35_39CS));		//'yo' means "years old"
			active30sPlotter.addSeries("females 35-39 yo with children", new MeanArrayFunction(femaleParticipation35_39WithChildrenCS));
			GuiUtils.addWindow(active30sPlotter, 1050, 300, 400, 300);
			
			active40sPlotter = new TimeSeriesSimulationPlotter("Female Participation Rates III", "");
			active40sPlotter.addSeries("females 40-44 yo", new MeanArrayFunction(femaleParticipation40_44CS));		//'yo' means "years old"
			active40sPlotter.addSeries("females 40-44 yo with children", new MeanArrayFunction(femaleParticipation40_44WithChildrenCS));
			active40sPlotter.addSeries("females 45-49 yo", new MeanArrayFunction(femaleParticipation45_49CS));		//'yo' means "years old"
			active40sPlotter.addSeries("females 45-49 yo with children", new MeanArrayFunction(femaleParticipation45_49WithChildrenCS));
			GuiUtils.addWindow(active40sPlotter, 1450, 300, 400, 300);
			
			employedPlotter = new TimeSeriesSimulationPlotter("Employment Rates", "");
			employedPlotter.addSeries("females", new MeanArrayFunction(femalesEmployed17_74CS));
			employedPlotter.addSeries("females 20-44 yo", new MeanArrayFunction(femalesEmployed20_44CS));		//'yo' means "years old"
			employedPlotter.addSeries("females 20-44 yo with children", new MeanArrayFunction(femalesEmployed20_44WithChildrenCS));
			employedPlotter.addSeries("males", new MeanArrayFunction(malesEmployed17_74CS));		
			GuiUtils.addWindow(employedPlotter, 250, 600, 400, 300);
			
			employed20sPlotter = new TimeSeriesSimulationPlotter("Female Employment Rates I", "");
			employed20sPlotter.addSeries("females 20-24 yo", new MeanArrayFunction(femaleEmployed20_24CS));		//'yo' means "years old"
			employed20sPlotter.addSeries("females 20-24 yo with children", new MeanArrayFunction(femaleEmployed20_24WithChildrenCS));
			employed20sPlotter.addSeries("females 25-29 yo", new MeanArrayFunction(femaleEmployed25_29CS));		//'yo' means "years old"
			employed20sPlotter.addSeries("females 25-29 yo with children", new MeanArrayFunction(femaleEmployed25_29WithChildrenCS));
			GuiUtils.addWindow(employed20sPlotter, 650, 600, 400, 300);
	
			employed30sPlotter = new TimeSeriesSimulationPlotter("Female Employment Rates II", "");
			employed30sPlotter.addSeries("females 30-34 yo", new MeanArrayFunction(femaleEmployed30_34CS));		//'yo' means "years old"
			employed30sPlotter.addSeries("females 30-34 yo with children", new MeanArrayFunction(femaleEmployed30_34WithChildrenCS));
			employed30sPlotter.addSeries("females 35-39 yo", new MeanArrayFunction(femaleEmployed35_39CS));		//'yo' means "years old"
			employed30sPlotter.addSeries("females 35-39 yo with children", new MeanArrayFunction(femaleEmployed35_39WithChildrenCS));
			GuiUtils.addWindow(employed30sPlotter, 1050, 600, 400, 300);
			
			employed40sPlotter = new TimeSeriesSimulationPlotter("Female Employment Rates III", "");
			employed40sPlotter.addSeries("females 40-44 yo", new MeanArrayFunction(femaleEmployed40_44CS));		//'yo' means "years old"
			employed40sPlotter.addSeries("females 40-44 yo with children", new MeanArrayFunction(femaleEmployed40_44WithChildrenCS));
			employed40sPlotter.addSeries("females 45-49 yo", new MeanArrayFunction(femaleEmployed45_49CS));		//'yo' means "years old"
			employed40sPlotter.addSeries("females 45-49 yo with children", new MeanArrayFunction(femaleEmployed45_49WithChildrenCS));
			GuiUtils.addWindow(employed40sPlotter, 1450, 600, 400, 300);
		}
							
	}
	
	@Override
	public void buildSchedule() {
		
		if(showCharts) {
			
			LabourParticipationModel model = (LabourParticipationModel) SimulationEngine.getInstance().getManager(LabourParticipationModel.class.getCanonicalName());
			EventGroup chartingEvents = new EventGroup();
			chartingEvents.addEvent(new SingleTargetEvent(this, Processes.Update));
			chartingEvents.addEvent(new SingleTargetEvent(studentPlotter, CommonEventType.Update));		
			chartingEvents.addEvent(new SingleTargetEvent(eduPlotter, CommonEventType.Update));
			chartingEvents.addEvent(new SingleTargetEvent(femalesCohabitingPlotter, CommonEventType.Update));
			chartingEvents.addEvent(new SingleTargetEvent(activePlotter, CommonEventType.Update));
			chartingEvents.addEvent(new SingleTargetEvent(active20sPlotter, CommonEventType.Update));
			chartingEvents.addEvent(new SingleTargetEvent(active30sPlotter, CommonEventType.Update));
			chartingEvents.addEvent(new SingleTargetEvent(active40sPlotter, CommonEventType.Update));
			chartingEvents.addEvent(new SingleTargetEvent(employedPlotter, CommonEventType.Update));
			chartingEvents.addEvent(new SingleTargetEvent(employed20sPlotter, CommonEventType.Update));
			chartingEvents.addEvent(new SingleTargetEvent(employed30sPlotter, CommonEventType.Update));
			chartingEvents.addEvent(new SingleTargetEvent(employed40sPlotter, CommonEventType.Update));
			
			int ordering = 2;		//Schedule at the same time as the model and collector events, but with an higher order, so will be fired after the model and collector have updated. 
			getEngine().getEventList().scheduleRepeat(chartingEvents, model.getStartYear(), ordering, displayFrequency);
		
		}
							
	}
	
	//--------------------------------------------------------------------------
	//	Event Listener implementation 
	//--------------------------------------------------------------------------
	
	
	public enum Processes {
		Update,
	}
	
	@Override
	public void onEvent(Enum<?> type) {
		switch ((Processes) type) {
		case Update:

			//Do these really need to be updated manually like this or are they updated automatically?
			lowEducationCS.updateSource();
			midEducationCS.updateSource();
			highEducationCS.updateSource();
			
			studentYoungCS.updateSource();
			studentMediumCS.updateSource();
			studentOldCS.updateSource();
			
			femalesCohabiting20_29CS.updateSource();
			femalesCohabiting30_44CS.updateSource();
			femalesCohabiting45_59CS.updateSource();
			femalesCohabiting60_74CS.updateSource();
	
			femaleParticipationCS.updateSource();
	//		femaleParticipation20_44CS.updateSource();
	//		femaleParticipation20_44WithChildrenCS.updateSource();
			maleParticipationCS.updateSource();
	
			femaleParticipation20_24CS.updateSource();
			femaleParticipation20_24WithChildrenCS.updateSource();
			femaleParticipation25_29CS.updateSource();
			femaleParticipation25_29WithChildrenCS.updateSource();
			femaleParticipation30_34CS.updateSource();
			femaleParticipation30_34WithChildrenCS.updateSource();
			femaleParticipation35_39CS.updateSource();
			femaleParticipation35_39WithChildrenCS.updateSource();
			femaleParticipation40_44CS.updateSource();
			femaleParticipation40_44WithChildrenCS.updateSource();
			femaleParticipation45_49CS.updateSource();
			femaleParticipation45_49WithChildrenCS.updateSource();
	
			malesEmployed17_74CS.updateSource();
			femalesEmployed17_74CS.updateSource();
			femalesEmployed20_44CS.updateSource();
			femalesEmployed20_44WithChildrenCS.updateSource();
			
			femaleEmployed20_24CS.updateSource();
			femaleEmployed20_24WithChildrenCS.updateSource();
			femaleEmployed25_29CS.updateSource();
			femaleEmployed25_29WithChildrenCS.updateSource();
			femaleEmployed30_34CS.updateSource();
			femaleEmployed30_34WithChildrenCS.updateSource();
			femaleEmployed35_39CS.updateSource();
			femaleEmployed35_39WithChildrenCS.updateSource();
			femaleEmployed40_44CS.updateSource();
			femaleEmployed40_44WithChildrenCS.updateSource();
			femaleEmployed45_49CS.updateSource();
			femaleEmployed45_49WithChildrenCS.updateSource();
	
			break;
		}
		
	}
	
	//--------------------------------------------------------------------------
	// Access methods
	//--------------------------------------------------------------------------
	
	public Double getDisplayFrequency() {
		return displayFrequency;
	}

	public void setDisplayFrequency(Double displayFrequency) {
		this.displayFrequency = displayFrequency;
	}
	
	public Boolean getShowCharts() {
		return showCharts;
	}

	public void setShowCharts(Boolean showCharts) {
		this.showCharts = showCharts;
	}


}
