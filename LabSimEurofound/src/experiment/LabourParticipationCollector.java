package experiment;

import data.filters.FemaleAgeGroupCSfilter;
import data.filters.FemalesAgeGroupEducationCSfilter;
import data.filters.FemalesWithChildren3underAgeGroupCSfilter;
import data.filters.FemalesWithChildrenAgeGroupCSfilter;
import data.filters.FemalesWithChildrenAgeGroupEducationCSfilter;
import data.filters.FemalesWithoutChildrenAgeGroupCSfilter;
import data.filters.MaleAgeGroupCSfilter;
import model.LabourParticipationModel;
import model.Person;
import model.Statistics;
import model.enums.Education;
import microsim.annotation.ModelParameter;
import microsim.data.DataExport;
import microsim.engine.AbstractSimulationCollectorManager;
import microsim.engine.SimulationEngine;
import microsim.engine.SimulationManager;
import microsim.event.EventListener;
import microsim.event.SingleTargetEvent;
import microsim.statistics.CrossSection;
import microsim.statistics.IDoubleSource;
import microsim.statistics.functions.MeanArrayFunction;

import org.apache.log4j.Logger;

public class LabourParticipationCollector extends AbstractSimulationCollectorManager implements EventListener {

	private static Logger log = Logger.getLogger(LabourParticipationCollector.class);
	
	@ModelParameter(description="Toggle to turn database persistence on/off")
	private boolean exportToDatabase = false;
	
	@ModelParameter(description="Toggle to turn export to .csv files on/off")
	private boolean exportToCSV = true;
	
	@ModelParameter(description="Toggle to turn persistence of persons on/off")
	private boolean persistPersons = true;

	@ModelParameter(description="First time-step to dump data to database")
	private Long dataDumpStartTime = 0L;

	@ModelParameter(description="Number of time-steps in between database dumps")
	private Double dataDumpTimePeriod = 1.;
	
	private Statistics stats;

	//Cross sections for summary statistics to be persisted to database
	private CrossSection.Integer maleParticipation15_64CS;
	private CrossSection.Integer maleParticipation20_64CS;

	private CrossSection.Integer femaleParticipation15_64CS;
	private CrossSection.Integer femaleParticipation20_64CS;
	
	private CrossSection.Integer femaleParticipation20_44CS;
	private CrossSection.Integer femaleParticipation20_44WithChildrenCS;
	private CrossSection.Integer femaleParticipation20_44WithoutChildrenCS;
	
	private CrossSection.Integer femaleParticipation20_44WithChildrenLowEducationCS;
	private CrossSection.Integer femaleParticipation20_44WithChildrenHighEducationCS;
	
	
	private CrossSection.Integer femaleParticipation20_44WithChildren3underCS;
	private CrossSection.Integer femaleParticipation20_44LowEducationCS;
	private CrossSection.Integer femaleParticipation20_44HighEducationCS;
	
	private CrossSection.Integer maleEmployment20_64CS;
	private CrossSection.Integer femaleEmployment20_64CS;

	
	private MeanArrayFunction fAvgPartRateMales15_64;
	private MeanArrayFunction fAvgPartRateMales20_64;

	private MeanArrayFunction fAvgPartRateFemales15_64;
	private MeanArrayFunction fAvgPartRateFemales20_64;

	private MeanArrayFunction fAvgPartRateFemales20_44;
	private MeanArrayFunction fAvgPartRateFemales20_44WithChildren;
	private MeanArrayFunction fAvgPartRateFemales20_44WithoutChildren;
	
	private MeanArrayFunction fAvgPartRateFemales20_44WithChildrenLowEducation;
	private MeanArrayFunction fAvgPartRateFemales20_44WithChildrenHighEducation;
	
	
	private MeanArrayFunction fAvgPartRateFemales20_44WithChildren3under;
	private MeanArrayFunction fAvgPartRateFemales20_44LowEducation;
	private MeanArrayFunction fAvgPartRateFemales20_44HighEducation;
	
	private MeanArrayFunction fAvgEmplRateMales20_64;
	private MeanArrayFunction fAvgEmplRateFemales20_64;
	
	private DataExport exportPersons;
	private DataExport exportStatistics;
	
	public LabourParticipationCollector(SimulationManager manager) {
		super(manager);		
	}
	
	// ---------------------------------------------------------------------
	// Event Listener
	// ---------------------------------------------------------------------
	
	public enum Processes {
		DumpPersons,
		DumpStatistics,
		
	}
	
	@Override
	public void onEvent(Enum<?> type) {
		switch ((Processes) type) {
	
		case DumpPersons:
			try {
				exportPersons.export();
//				DatabaseUtils.snap(((LabSimEurofoundModel) getManager()).getPersons());
			} catch (Exception e) {
				log.error(e.getMessage());
			}
			break;	
		case DumpStatistics:
			updateStatistics();
			try {
				exportStatistics.export();
//				DatabaseUtils.snap(stats);
//				 DatabaseUtils.snap(DatabaseUtils.getOutEntityManger(), 
//							(long) SimulationEngine.getInstance().getCurrentRunNumber(), 
//							getEngine().getTime(), 
//							stats);
			} catch (Exception e) {
				log.error(e.getMessage());
			}
			break;
		}
	}
	
	// ---------------------------------------------------------------------
	// Manager
	// ---------------------------------------------------------------------
	
	@Override
	public void buildObjects() {
		
		final LabourParticipationModel model = (LabourParticipationModel) getManager();
		
		stats = new Statistics();
		
		//For export to database or .csv files.
		exportPersons = new DataExport(((LabourParticipationModel) getManager()).getPersons(), exportToDatabase, exportToCSV);
		exportStatistics = new DataExport(stats, exportToDatabase, exportToCSV);
		
		//Create CS but still need to set filters (see afterwards)
		//Uses getDoubleValue of Person class implementation of IDoubleSource interface in order to get variable representing whether the person is active (i.e. participates in the labour market).  Should be quicker than reflection method below. 
		maleParticipation15_64CS = new CrossSection.Integer(model.getPersons(), Person.Variables.isActive);
		maleParticipation20_64CS = new CrossSection.Integer(model.getPersons(), Person.Variables.isActive);
		femaleParticipation15_64CS = new CrossSection.Integer(model.getPersons(), Person.Variables.isActive);
		femaleParticipation20_64CS = new CrossSection.Integer(model.getPersons(), Person.Variables.isActive);		
		femaleParticipation20_44CS = new CrossSection.Integer(model.getPersons(), Person.Variables.isActive);
		femaleParticipation20_44WithChildrenCS = new CrossSection.Integer(model.getPersons(), Person.Variables.isActive);
		femaleParticipation20_44WithoutChildrenCS = new CrossSection.Integer(model.getPersons(), Person.Variables.isActive);
		femaleParticipation20_44WithChildrenLowEducationCS = new CrossSection.Integer(model.getPersons(), Person.Variables.isActive);
		femaleParticipation20_44WithChildrenHighEducationCS = new CrossSection.Integer(model.getPersons(), Person.Variables.isActive);

		femaleParticipation20_44WithChildren3underCS = new CrossSection.Integer(model.getPersons(), Person.Variables.isActive);
		femaleParticipation20_44LowEducationCS = new CrossSection.Integer(model.getPersons(), Person.Variables.isActive);
		femaleParticipation20_44HighEducationCS = new CrossSection.Integer(model.getPersons(), Person.Variables.isActive);
		
		maleEmployment20_64CS = new CrossSection.Integer(model.getPersons(), Person.Variables.isEmployed);
		femaleEmployment20_64CS = new CrossSection.Integer(model.getPersons(), Person.Variables.isEmployed);

		
		//Uses reflection (therefore slow)
//		maleParticipation15_64CS = new CrossSection.Integer(model.getPersons(), Person.class, "getActive", true);
//		maleParticipation20_64CS = new CrossSection.Integer(model.getPersons(), Person.class, "getActive", true);
//		femaleParticipation15_64CS = new CrossSection.Integer(model.getPersons(), Person.class, "getActive", true);
//		femaleParticipation20_64CS = new CrossSection.Integer(model.getPersons(), Person.class, "getActive", true);		
//		femaleParticipation20_44CS = new CrossSection.Integer(model.getPersons(), Person.class, "getActive", true);
//		femaleParticipation20_44WithChildrenCS = new CrossSection.Integer(model.getPersons(), Person.class, "getActive", true);
//		femaleParticipation20_44WithoutChildrenCS = new CrossSection.Integer(model.getPersons(), Person.class, "getActive", true);
//		femaleParticipation20_44WithChildrenLowEducationCS = new CrossSection.Integer(model.getPersons(), Person.class, "getActive", true);
//		femaleParticipation20_44WithChildrenHighEducationCS = new CrossSection.Integer(model.getPersons(), Person.class, "getActive", true);

		//Create filters
		MaleAgeGroupCSfilter male15_64Filter = new MaleAgeGroupCSfilter(15, 64);
		MaleAgeGroupCSfilter male20_64Filter = new MaleAgeGroupCSfilter(20, 64);
		FemaleAgeGroupCSfilter female15_64Filter = new FemaleAgeGroupCSfilter(15, 64);
		FemaleAgeGroupCSfilter female20_64Filter = new FemaleAgeGroupCSfilter(20, 64);		
		FemaleAgeGroupCSfilter female20_44Filter = new FemaleAgeGroupCSfilter(20, 44);
		FemalesWithChildrenAgeGroupCSfilter fem20_44withChildrenFilter = new FemalesWithChildrenAgeGroupCSfilter(20, 44);
		FemalesWithoutChildrenAgeGroupCSfilter fem20_44withoutChildrenFilter = new FemalesWithoutChildrenAgeGroupCSfilter(20, 44);
		FemalesWithChildrenAgeGroupEducationCSfilter fem20_44withChildrenLowEducationFilter = new FemalesWithChildrenAgeGroupEducationCSfilter(20, 44, Education.Low);
		FemalesWithChildrenAgeGroupEducationCSfilter fem20_44withChildrenHighEducationFilter = new FemalesWithChildrenAgeGroupEducationCSfilter(20, 44, Education.High);
		
		FemalesWithChildren3underAgeGroupCSfilter fem20_44withChildren3underFilter = new FemalesWithChildren3underAgeGroupCSfilter(20, 44);
		FemalesAgeGroupEducationCSfilter fem20_44lowEducationFilter = new FemalesAgeGroupEducationCSfilter(20, 44, Education.Low);
		FemalesAgeGroupEducationCSfilter fem20_44highEducationFilter = new FemalesAgeGroupEducationCSfilter(20, 44, Education.High);
		
		//Set filters
		maleParticipation15_64CS.setFilter(male15_64Filter);
		maleParticipation20_64CS.setFilter(male20_64Filter);
		femaleParticipation15_64CS.setFilter(female15_64Filter);
		femaleParticipation20_64CS.setFilter(female20_64Filter);
		femaleParticipation20_44CS.setFilter(female20_44Filter);
		femaleParticipation20_44WithChildrenCS.setFilter(fem20_44withChildrenFilter);
		femaleParticipation20_44WithoutChildrenCS.setFilter(fem20_44withoutChildrenFilter);
		femaleParticipation20_44WithChildrenLowEducationCS.setFilter(fem20_44withChildrenLowEducationFilter);
		femaleParticipation20_44WithChildrenHighEducationCS.setFilter(fem20_44withChildrenHighEducationFilter);
		
		femaleParticipation20_44WithChildren3underCS.setFilter(fem20_44withChildren3underFilter);
		femaleParticipation20_44LowEducationCS.setFilter(fem20_44lowEducationFilter);
		femaleParticipation20_44HighEducationCS.setFilter(fem20_44highEducationFilter);
		maleEmployment20_64CS.setFilter(male20_64Filter);
		femaleEmployment20_64CS.setFilter(female20_64Filter);

		//Create MeanArrayFunctions
		fAvgPartRateMales15_64 = new MeanArrayFunction(maleParticipation15_64CS);
		fAvgPartRateMales20_64 = new MeanArrayFunction(maleParticipation20_64CS);
		fAvgPartRateFemales15_64 = new MeanArrayFunction(femaleParticipation15_64CS);
		fAvgPartRateFemales20_64 = new MeanArrayFunction(femaleParticipation20_64CS);
		fAvgPartRateFemales20_44 = new MeanArrayFunction(femaleParticipation20_44CS);
		fAvgPartRateFemales20_44WithChildren = new MeanArrayFunction(femaleParticipation20_44WithChildrenCS);
		fAvgPartRateFemales20_44WithoutChildren = new MeanArrayFunction(femaleParticipation20_44WithoutChildrenCS);
		fAvgPartRateFemales20_44WithChildrenLowEducation = new MeanArrayFunction(femaleParticipation20_44WithChildrenLowEducationCS);
		fAvgPartRateFemales20_44WithChildrenHighEducation = new MeanArrayFunction(femaleParticipation20_44WithChildrenHighEducationCS);
		
		fAvgPartRateFemales20_44WithChildren3under = new MeanArrayFunction(femaleParticipation20_44WithChildren3underCS);
		fAvgPartRateFemales20_44LowEducation = new MeanArrayFunction(femaleParticipation20_44LowEducationCS);
		fAvgPartRateFemales20_44HighEducation = new MeanArrayFunction(femaleParticipation20_44HighEducationCS);
		fAvgEmplRateMales20_64 = new MeanArrayFunction(maleEmployment20_64CS);
		fAvgEmplRateFemales20_64 = new MeanArrayFunction(femaleEmployment20_64CS);
	}
	
	@Override
	public void buildSchedule() {	

		LabourParticipationModel model = (LabourParticipationModel) SimulationEngine.getInstance().getManager(LabourParticipationModel.class.getCanonicalName());
		
		int ordering = 1;
		getEngine().getEventList().scheduleRepeat(new SingleTargetEvent(this, Processes.DumpStatistics), model.getStartYear() + dataDumpStartTime, ordering, dataDumpTimePeriod);
		
		if (persistPersons) {
			getEngine().getEventList().scheduleRepeat(new SingleTargetEvent(this, Processes.DumpPersons), model.getStartYear() + dataDumpStartTime, ordering, dataDumpTimePeriod);
			getEngine().getEventList().scheduleRepeat(new SingleTargetEvent(this, Processes.DumpPersons), model.getEndYear(), -2, 0.);		//Ensures the database is persisted on the last time-step
		}
		
	}
	
	// ---------------------------------------------------------------------
	// methods
	// ---------------------------------------------------------------------
	
	private void updateStatistics() {			//Called just before database dump of statistics entity

		//Are the cross sections automatically updated when the MeanArrayFunction is updated?
		maleParticipation15_64CS.updateSource();
		maleParticipation20_64CS.updateSource();
		femaleParticipation15_64CS.updateSource();
		femaleParticipation20_64CS.updateSource();		
		femaleParticipation20_44CS.updateSource();
		femaleParticipation20_44WithChildrenCS.updateSource();
		femaleParticipation20_44WithoutChildrenCS.updateSource();
		femaleParticipation20_44WithChildrenLowEducationCS.updateSource();
		femaleParticipation20_44WithChildrenHighEducationCS.updateSource();
		
		femaleParticipation20_44WithChildren3underCS.updateSource();
		femaleParticipation20_44LowEducationCS.updateSource();
		femaleParticipation20_44HighEducationCS.updateSource();
		maleEmployment20_64CS.updateSource();
		femaleEmployment20_64CS.updateSource();
		
		//Update functions
		fAvgPartRateMales15_64.updateSource();
		stats.setAvgPartRateMales15_64(fAvgPartRateMales15_64.getDoubleValue(IDoubleSource.Variables.Default));
		
		fAvgPartRateMales20_64.updateSource();
		stats.setAvgPartRateMales20_64(fAvgPartRateMales20_64.getDoubleValue(IDoubleSource.Variables.Default));

		fAvgPartRateFemales15_64.updateSource();
		stats.setAvgPartRateFemales15_64(fAvgPartRateFemales15_64.getDoubleValue(IDoubleSource.Variables.Default));
		
		fAvgPartRateFemales20_64.updateSource();
		stats.setAvgPartRateFemales20_64(fAvgPartRateFemales20_64.getDoubleValue(IDoubleSource.Variables.Default));

		fAvgPartRateFemales20_44.updateSource();
		stats.setAvgPartRateFemales20_44(fAvgPartRateFemales20_44.getDoubleValue(IDoubleSource.Variables.Default));

		fAvgPartRateFemales20_44WithChildren.updateSource();
		stats.setAvgPartRateFemales20_44withChildren(fAvgPartRateFemales20_44WithChildren.getDoubleValue(IDoubleSource.Variables.Default));

		fAvgPartRateFemales20_44WithoutChildren.updateSource();
		stats.setAvgPartRateFemales20_44withoutChildren(fAvgPartRateFemales20_44WithoutChildren.getDoubleValue(IDoubleSource.Variables.Default));
		
		fAvgPartRateFemales20_44WithChildrenLowEducation.updateSource();
		stats.setAvgPartRateFemales20_44withChildrenLowEducation(fAvgPartRateFemales20_44WithChildrenLowEducation.getDoubleValue(IDoubleSource.Variables.Default));
		
		fAvgPartRateFemales20_44WithChildrenHighEducation.updateSource();
		stats.setAvgPartRateFemales20_44withChildrenHighEducation(fAvgPartRateFemales20_44WithChildrenHighEducation.getDoubleValue(IDoubleSource.Variables.Default));
		
		fAvgPartRateFemales20_44WithChildren3under.updateSource();
		stats.setAvgPartRateFemales20_44withChildren3under(fAvgPartRateFemales20_44WithChildren3under.getDoubleValue(IDoubleSource.Variables.Default));
		
		fAvgPartRateFemales20_44LowEducation.updateSource();
		stats.setAvgPartRateFemales20_44lowEducation(fAvgPartRateFemales20_44LowEducation.getDoubleValue(IDoubleSource.Variables.Default));
		
		fAvgPartRateFemales20_44HighEducation.updateSource();
		stats.setAvgPartRateFemales20_44highEducation(fAvgPartRateFemales20_44HighEducation.getDoubleValue(IDoubleSource.Variables.Default));
		
		fAvgEmplRateMales20_64.updateSource();
		stats.setAvgEmplRateMales20_64(fAvgEmplRateMales20_64.getDoubleValue(IDoubleSource.Variables.Default));
		
		fAvgEmplRateFemales20_64.updateSource();
		stats.setAvgEmplRateFemales20_64(fAvgEmplRateFemales20_64.getDoubleValue(IDoubleSource.Variables.Default));
		
	}


	// ---------------------------------------------------------------------
	// getters and setters
	// ---------------------------------------------------------------------

	
	public boolean isPersistPersons() {
		return persistPersons;
	}

	public void setPersistPersons(boolean persistPersons) {
		this.persistPersons = persistPersons;
	}

	public Long getDataDumpStartTime() {
		return dataDumpStartTime;
	}

	public void setDataDumpStartTime(Long dataDumpStartTime) {
		this.dataDumpStartTime = dataDumpStartTime;
	}

	public Double getDataDumpTimePeriod() {
		return dataDumpTimePeriod;
	}

	public void setDataDumpTimePeriod(Double dataDumpTimePeriod) {
		this.dataDumpTimePeriod = dataDumpTimePeriod;
	}

	public Statistics getStats() {
		return stats;
	}

	public void setStats(Statistics stats) {
		this.stats = stats;
	}

	public boolean isExportToDatabase() {
		return exportToDatabase;
	}

	public void setExportToDatabase(boolean exportToDatabase) {
		this.exportToDatabase = exportToDatabase;
	}

	public boolean isExportToCSV() {
		return exportToCSV;
	}

	public void setExportToCSV(boolean exportToCSV) {
		this.exportToCSV = exportToCSV;
	}

}
