package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import microsim.alignment.outcome.ResamplingAlignment;
import microsim.alignment.outcome.AlignmentOutcomeClosure;
import microsim.annotation.ModelParameter;
import microsim.data.db.DatabaseUtils;
import microsim.engine.AbstractSimulationManager;
import microsim.engine.SimulationEngine;
import microsim.event.EventGroup;
import microsim.event.EventListener;
import microsim.event.SystemEvent;
import microsim.event.SystemEventType;
import model.enums.Activity_status;
import model.enums.Civil_status;
import model.enums.Country;
import model.enums.Education;
import model.enums.Employment_status;
import model.enums.Gender;
import model.enums.Indicator;

import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.log4j.Logger;

import data.Parameters;
import data.filters.ActiveFilter;
import data.filters.FemaleWithChildrenByAgeBandFilter;
import data.filters.FemalesUnder45Filter;
import data.filters.FertileFilter;

public class LabourParticipationModel extends AbstractSimulationManager implements
		EventListener {

	private static Logger log = Logger.getLogger(LabourParticipationModel.class);

	@ModelParameter(description = "Country to be simulated")
	private Country country = Country.IT;
	
	@ModelParameter(description = "Simulated population size (base year) [max = 100,000]")
	private Integer popSize = 100000;

	@ModelParameter(description = "Simulation first year [valid range 2013-2049]")
	private Integer startYear = 2013;

	@ModelParameter(description = "Simulation ends at year [valid range 2014-2050]")
	private Integer endYear = 2050;
	
	@ModelParameter(description = "Cohort effect stopped for those born after")
	private Integer cohortEffectEndTrend = 1995;
	
	@ModelParameter(description="Minimum age for males to retire")
	private Integer minRetireAgeMales = 45;

	@ModelParameter(description="Maximum age for males to retire")
	private Integer maxRetireAgeMales = 75;

	@ModelParameter(description="Minimum age for females to retire")
	private Integer minRetireAgeFemales = 45;

	@ModelParameter(description="Maximum age for females to retire")
	private Integer maxRetireAgeFemales = 75;

	@ModelParameter(description = "Fix random seed?")
	private Boolean fixRandomSeed = true;

	@ModelParameter(description = "If random seed is fixed, set to this number")
	private Long randomSeedIfFixed = 1L;

	private List<Person> persons;
	
//	private LinkedHashMap<Gender, ArrayList<Person>> personsLeavingEducation = new LinkedHashMap<Gender, ArrayList<Person>>();

//	MultiKeyMap personsByGenderAndAge = MultiKeyMap.decorate(new LinkedMap());
	MultiKeyMap personsByGenderAndAge = new MultiKeyMap();

	private long elapsedTime;
	private int moduleId = 1;

	private int year;
	
	private double scalingFactor;

//	//Alignment fields
//	private double proportionInitialPopWithMediumEdu;		//For education alignment

//	private double[] initialChildrenGap;								//For female participation children gap alignment
//	private MultiKeyMap initialChildrenGap = MultiKeyMap.decorate(new LinkedMap());			//For female participation children gap alignment
	private MultiKeyMap initialChildrenGap = new MultiKeyMap();			//For female participation children gap alignment
	
//	public LabourParticipationModel() {			//Should be automatically created
//		super();		
//	}
	

	// ---------------------------------------------------------------------
	// Manager methods
	// ---------------------------------------------------------------------


	@SuppressWarnings("unchecked")
//	@Override
	public void buildObjects() {
		
        // Copy initial population for selected country into PERSON table
        try {
            Class.forName("org.h2.Driver");
            Connection conn = DriverManager.getConnection("jdbc:h2:"+DatabaseUtils.databaseInputUrl, "sa", "");
            Statement stat = conn.createStatement();
            stat.execute("DROP TABLE IF EXISTS PERSON");
            stat.execute("CREATE TABLE PERSON AS SELECT * FROM PERSON_"+country.toString());
            stat.close();
            conn.close();
        } catch(ClassNotFoundException|SQLException e){
            System.out.println( "ERROR: Class not found: " + e.getMessage() + "\nCheck that the input.h2.db "
            		+ "exists in the input folder.  If not, unzip the input.h2.zip file and store the resulting "
            		+ "input.h2.db in the input folder!\n");
        }
        
		if(fixRandomSeed) {
			SimulationEngine.getRnd().setSeed(randomSeedIfFixed);
//			SimulationEngine.getInstance().setRandomSeed(randomSeedIfFixed);
		}
		Parameters.loadParameters(country);
        System.out.println("Parameters loaded.");        

        List<Person> initialPopulation = (List<Person>) DatabaseUtils.loadTable(Person.class);        //RHS returns an ArrayList, not a LinkedList, so need to copy to new LinkedList below
        Collections.shuffle(initialPopulation, SimulationEngine.getRnd());
        System.out.println("Initial population loaded from input database.");
        persons = new LinkedList<Person>(); 
        if (popSize > initialPopulation.size()) {
        	popSize = initialPopulation.size();
        	System.out.println("Required sample size reduced to "+initialPopulation.size()+".");
        }
        
        // create LinkedList type to allow faster removal of randomly scattered entries (in population alignment module)
        for (int i=0; i<popSize; i++) persons.add(initialPopulation.get(i));						  // copy desired sample size to LinkedList
        initialPopulation = null;            														  //Allow to be reclaimed by garbage collector as population is now stored in persons.
		
		elapsedTime = System.currentTimeMillis();
		
		year = startYear;		//Now incremented at end of year...
		
		int minAgeInInitialPopulation = Integer.MAX_VALUE;
		int maxAgeInInitialPopulation = Integer.MIN_VALUE;
		
		//Alignment measures
//		//School and Education Alignment
//		for(Gender gender : Gender.values()) {
//			personsLeavingEducation.put(gender, new ArrayList<Person>());
//		}
//		int numInitialPopWithMediumEducation = 0;		//For education alignment benchmark target for medium education level
//		//To align number of participating females with children compared to participating females without children

		//Children Gap Alignment of Female Labour Participation
//		int numBands = ((Parameters.maxAgeOfFemaleWithChild - Parameters.getMinAgeMaternity()) / Parameters.ageBand) + 1;
		int[] numActiveFemalesWithChildren = new int[101];
		int[] numActiveFemalesNoChildren = new int[101];
		int[] numFemalesWithChildren = new int[101];
		int[] numFemalesNoChildren = new int[101];
//		initialChildrenGap = new double[101];
		for(int age = 0; age <= 100; age++) {
			numActiveFemalesWithChildren[age] = 0;
			numActiveFemalesNoChildren[age] = 0;
			numFemalesWithChildren[age] = 0;
			numFemalesNoChildren[age] = 0;
//			initialChildrenGap[age] = 0.;
		}
		
		
		for (Person person : persons) {
			
			int age = person.getAge();
			if(age < minAgeInInitialPopulation) {					//TODO: Could we do this on some other input data, as this requires checking every person in initial population - some tens of thousands of checks!
				minAgeInInitialPopulation = age;
			} 
			if(age > maxAgeInInitialPopulation) {
				maxAgeInInitialPopulation = age;
			}
			
			//Clean the input population for consistency
			if(person.getActivity_status().equals(Activity_status.Student) && ! Parameters.eduReEntry) {
				 person.setEducation(null);		//This is to ensure no information can be obtained from education level before it has been set.  Initial population contains this information for people even if they are still students, which is inconsistent with the simulation.
				 person.setCivil_status(Civil_status.Single);
					person.setD_children_3under(Indicator.False);
					person.setD_children_4_12(Indicator.False);
					person.setN_children_0(0);
					person.setN_children_1(0);
					person.setN_children_2(0);
					person.setN_children_3(0);
					person.setN_children_4(0);
					person.setN_children_5(0);
					person.setN_children_6(0);
					person.setN_children_7(0);
					person.setN_children_8(0);
					person.setN_children_9(0);
					person.setN_children_10(0);
					person.setN_children_11(0);
					person.setN_children_12(0);
					person.setN_children_13(0);
			} else if(person.getActivity_status().equals(Activity_status.Retired)) {
				person.setEmployment_status(null);
			} else if(person.getActivity_status().equals(Activity_status.Inactive)) {
				person.setEmployment_status(null);
			}
			
			if(person.getAge() > Parameters.getMaxAgeMaternity() + 13) {	//Clear children related fields.  Was previously testing for age > maxAgeMaturnity + 3, however now we are expanding the model to allow for effects of having children less than 14 years old 
				
				if(person.getGender().equals(Gender.Female) && ! Parameters.eduReEntry) {

					person.setD_children_3under(Indicator.False);
					person.setD_children_4_12(Indicator.False);
					person.setN_children_0(0);
					person.setN_children_1(0);
					person.setN_children_2(0);
					person.setN_children_3(0);
					person.setN_children_4(0);
					person.setN_children_5(0);
					person.setN_children_6(0);
					person.setN_children_7(0);
					person.setN_children_8(0);
					person.setN_children_9(0);
					person.setN_children_10(0);
					person.setN_children_11(0);
					person.setN_children_12(0);
					person.setN_children_13(0);
				}				 
				if(person.getAge() > getMaxRetireAge(person.getGender())) {
					person.setActivity_status(Activity_status.Retired);
					person.setEmployment_status(null);
				}
				
			}
			
			if(person.getGender().equals(Gender.Male)) {		//Not modelling these fields in males
				person.setCivil_status(null);
				person.setD_children_3under(null);
				person.setD_children_4_12(null);
				person.setN_children_0(0);
				person.setN_children_1(0);
				person.setN_children_2(0);
				person.setN_children_3(0);
				person.setN_children_4(0);
				person.setN_children_5(0);
				person.setN_children_6(0);
				person.setN_children_7(0);
				person.setN_children_8(0);
				person.setN_children_9(0);
				person.setN_children_10(0);
				person.setN_children_11(0);
				person.setN_children_12(0);
				person.setN_children_13(0);
			}
			
			person.setAdditionalFieldsInInitialPopulation();		//Those not in database
			
//			//Create statistic for aligning medium education levels
//			if(person.getEducation() != null) {
//				if(person.getEducation().equals(Education.Medium)) {		//TODO: Should we also check if the person is not a student?  I.e. if eduReEntry is true so that the person can go back to school, is it correct to include the medium education levels of mature students?  Is the education level of mature students set to null in this case anyway???? 
//					numInitialPopWithMediumEducation++;
//				}
//			}
			
			//Create statistics for aligning female participation rate gaps
			if(person.getGender().equals(Gender.Female)) {
				if(person.getD_children_3under().equals(Indicator.True) || person.getD_children_4_12().equals(Indicator.True)) {
					if(person.getActivity_status().equals(Activity_status.Active)) {
						numActiveFemalesWithChildren[person.getAge()]++;
					}
					numFemalesWithChildren[person.getAge()]++;
				}
				else {			//No children
					if(person.getActivity_status().equals(Activity_status.Active)) {
						numActiveFemalesNoChildren[person.getAge()]++;
					}
					numFemalesNoChildren[person.getAge()]++;
				}
			}
			
		}
//		proportionInitialPopWithMediumEdu = ((double)numInitialPopWithMediumEducation / (double)persons.size());

		int ageFrom = Parameters.getMinAgeMaternity();
		while(ageFrom < Parameters.maxAgeOfFemaleWithChild) {
			int ageTo = ageFrom + Parameters.ageBand;
			
			int numActiveFemalesNoChildrenAgeBand = 0, numFemalesNoChildrenAgeBand = 0, numActiveFemalesWithChildrenAgeBand = 0, numFemalesWithChildrenAgeBand = 0; 
			for(int age = ageFrom; age < ageTo; age++) {		//Aggregate over ages in ageBand
				numActiveFemalesNoChildrenAgeBand += numActiveFemalesNoChildren[age];
				numFemalesNoChildrenAgeBand += numFemalesNoChildren[age];
				numActiveFemalesWithChildrenAgeBand += numActiveFemalesWithChildren[age];
				numFemalesWithChildrenAgeBand += numFemalesWithChildren[age];
			}
			if( (numFemalesNoChildrenAgeBand > 0) && (numFemalesWithChildrenAgeBand > 0) ) {
				initialChildrenGap.put(ageFrom, ageTo, ((double)numActiveFemalesNoChildrenAgeBand / (double)numFemalesNoChildrenAgeBand) - ((double)numActiveFemalesWithChildrenAgeBand / (double)numFemalesWithChildrenAgeBand));
			}	
			
			if(Parameters.trickOn) {
				System.out.println("AgeFrom ," + ageFrom + ", ageTo ," + ageTo + ", active females no children ," + numActiveFemalesNoChildrenAgeBand + ", females no children ," + numFemalesNoChildrenAgeBand + ", active females with children ," + numActiveFemalesWithChildrenAgeBand + ", females with children ," + numFemalesWithChildrenAgeBand + ", Initial children gap is ," + initialChildrenGap.get(ageFrom, ageTo));
			}
			ageFrom += Parameters.ageBand;
		}		

		
		Parameters.setMinAge(minAgeInInitialPopulation);
		Parameters.setMaxAge(maxAgeInInitialPopulation);
		System.out.println("Min Age is " + Parameters.getMinAge() + ", Max Age is " + Parameters.getMaxAge());
		
		// Partition population by Gender and Age
		for(Gender gender : Gender.values()) {
			for(int age = Parameters.getMinAge(); age <= Parameters.getMaxAge(); age++) {
				personsByGenderAndAge.put(gender, age, new LinkedList<Person>());		//Only creates MAX_AGE + 1 - MIN_AGE entries, will not keep track of persons that become older than MAX_AGE
			}
		}
		
		for (Person person : persons) {
			Gender gender = person.getGender();
			int age = person.getAge();
			if(age==Parameters.getMinAge()) {
				((List<Person>) personsByGenderAndAge.get(gender, age)).add(person);	//Need these values for calculating probability of being in a specific region for people of minimum age 
			}
		}
		
		for(Gender gender : Gender.values()) {
			Parameters.calculateMinAgeRegionProbabilities(gender, ((List<Person>) personsByGenderAndAge.get(gender, Parameters.getMinAge())));
		}
		
//		double popSizeBaseYear = ((Number) Parameters.getPopulationProjectionsFemale().getValue("Total", startYear.toString())).doubleValue() + ((Number) Parameters.getPopulationProjectionsMale().getValue("Total", startYear.toString())).doubleValue();
		int popSizeBaseYear = 0;
		for(int age = minAgeInInitialPopulation; age <= maxAgeInInitialPopulation; age++) {
			popSizeBaseYear += ((Number) Parameters.getPopulationProjectionsFemale().getValue(age, startYear.toString())).intValue();		//Add females 
			popSizeBaseYear += ((Number) Parameters.getPopulationProjectionsMale().getValue(age, startYear.toString())).intValue();			//Add males
		}
		
		scalingFactor = (double)popSizeBaseYear / (double)persons.size();		//TODO:  Need to adjust denominator to an variable population size specified as a model parameter in GUI 
		System.out.println("popSizeBaseYear " + popSizeBaseYear + ", input population size " + persons.size() + ", scaling factor " + scalingFactor);
	}

	@Override
	public void buildSchedule() {

		EventGroup yearlySchedule = new EventGroup();

		yearlySchedule.addEvent(this, Processes.UpdateParameters);
		
		//1 - DEMOGRAPHIC MODULE
		// A: Ageing 
		yearlySchedule.addCollectionEvent(persons, Person.Processes.Ageing, false);		//Read only mode as agents are removed when they become older than Parameters.getMAX_AGE();		

		// B: Population Alignment - adjust population to projections by Gender and Age, and creates new population for minimum age
		yearlySchedule.addEvent(this, Processes.PopulationAlignment);
		
		//2 - EDUCATION MODULE
		// A: Check In School - check whether still in education
		yearlySchedule.addCollectionEvent(persons, Person.Processes.InSchool);

		// B: In School alignment - in countries (Ireland and Sweden) where unusual decay in proportion of students is observed
//		if( Parameters.inSchoolAlignment ) { // &&  (country.equals(Country.IE) || country.equals(Country.SE) ) ) {
			yearlySchedule.addEvent(this, Processes.InSchoolAlignment);			// Only use if unusual distributions emerge, such as in Ireland and Sweden.
			System.out.println("Proportion of students will be aligned.");
//		}
		
		// C: Set Education Level - If leaving school, check what education level was attained.
		yearlySchedule.addCollectionEvent(persons, Person.Processes.SetEducationLevel);

		// D: Education level alignment - in countries (Ireland and Sweden) where unusual education distributions emerge (i.e. proportion of high education decreases over time)
//		if( Parameters.educationLevelAlignment && (country.equals(Country.IE) || country.equals(Country.SE)  ) ) {
			yearlySchedule.addEvent(this, Processes.EducationLevelAlignment);			//Only use if unusual distributions emerge, such as in Ireland and Sweden.
			System.out.println("Education levels will be aligned.");
//		}
//		yearlySchedule.addEvent(this, Processes.Timer);

		//3 - HOUSEHOLD COMPOSITION MODULE
		// A: For females, check whether in consensual union (cohabiting) and whether to give birth.
		yearlySchedule.addCollectionEvent(persons, Person.Processes.HouseholdFormation);
		if( Parameters.unionAlignment && (country.equals(Country.IE) || country.equals(Country.SE) ) ) {
			yearlySchedule.addEvent(this, Processes.UnionAlignment);
			System.out.println("Proportion of cohabiting females will be aligned.");
		}
		yearlySchedule.addCollectionEvent(persons, Person.Processes.ConsiderBirth);
		yearlySchedule.addEvent(this, Processes.FertilityAlignment);		//Align to fertility rates implied by projected population statistics.
		yearlySchedule.addCollectionEvent(persons, Person.Processes.UpdateMaternityStatus);
//		yearlySchedule.addEvent(this, Processes.Timer);

		//4 - LABOUR MARKET MODULE
		// A: Check whether persons have reached retirement Age
		yearlySchedule.addCollectionEvent(persons, Person.Processes.ConsiderRetirement);
//		yearlySchedule.addEvent(this, Processes.Timer);

		// B1: Check whether participating in the Labour market
		yearlySchedule.addCollectionEvent(persons, Person.Processes.Participation);
		if(Parameters.separateFemaleParticipationRegressions) {
			System.out.println("Separate Female Labour Participation regression co-efficients used depending on whether there are children aged 3 or under, only children over 3 years old, or no children.");
		}
		
		// B2: Female Participation Gap Alignment - to align the gap between the share of active females with children and without children
		if(Parameters.trickOn) {
			yearlySchedule.addEvent(this, Processes.FemaleParticipationChildrenGapAlignment);
			System.out.println("Trick on - female participation gap will be aligned using age bands of " + Parameters.ageBand + " years");
		}
		
		// C: Check whether employed and align to external employment forecasts
		yearlySchedule.addCollectionEvent(persons, Person.Processes.ConsiderEmployment);
		yearlySchedule.addEvent(this, Processes.EmploymentAlignment);
//		yearlySchedule.addCollectionEvent(persons, Person.Processes.UpdateEmploymentStatus);
//		yearlySchedule.addEvent(this, Processes.Timer);
		
		
		yearlySchedule.addEvent(this, Processes.UpdateYear);

		int order = 0;
		getEngine().getEventList().scheduleRepeat(yearlySchedule, startYear, order, 1.);
		
		SystemEvent e = new SystemEvent(SimulationEngine.getInstance(), SystemEventType.End);
		int orderEarlier = -1;			//Set less than order so that this is called before the yearlySchedule in the endYear.
		getEngine().getEventList().scheduleRepeat(e, endYear, orderEarlier, 0.);
	}

	// ---------------------------------------------------------------------
	// EventListener
	// ---------------------------------------------------------------------

	public enum Processes {

		//Alignment Processes
		EducationLevelAlignment,
		EmploymentAlignment,
		FemaleParticipationChildrenGapAlignment,
		FertilityAlignment, 
		InSchoolAlignment,
		PopulationAlignment,
		UnionAlignment,
		

		//Other processes
		Stop, 
		Timer, 
		UpdateParameters,
		UpdateYear, 
		
	}

	@Override
	public void onEvent(Enum<?> type) {
		switch ((Processes) type) {

		case PopulationAlignment:
//			System.out.println("Beginning Population Alignment");
			populationAlignment();
//			System.out.println("Population Alignment Complete");
			break;
		case InSchoolAlignment:			//Used for Ireland and Sweden where unusual distributions emerge. Hungary added.
			inSchoolAlignment();			//For share of under 30s who are still students
			break;
		case EducationLevelAlignment:			//Used for Ireland and Sweden where unusual distributions emerge.
			educationLevelAlignment();			//For proportion of education levels within population
			break;
		case UnionAlignment:
			unionAlignment();
			break;
		case FertilityAlignment:
			fertilityAlignment();
			break;
		case FemaleParticipationChildrenGapAlignment:
			femaleParticipationChildrenGapAlignment();
			break;
		case EmploymentAlignment:
//			System.out.println("Beginning Employment Alignment");
			employmentAlignment();
//			System.out.println("Employment Alignment Complete");
			break;
			
		case Stop:
			log.info("Simulation completed");
			System.out.println("Simulation completed");
			getEngine().pause();
			break;
		case Timer:
			printElapsedTime(); 
			break;
		case UpdateParameters:
			updateParameters();
			break;
		case UpdateYear:
//			System.out.println("it's the New Year's Eve of " + year);
			year++;
			moduleId = 1;		//Reset moduleId
			break;
			
		default:
			break;
		}
	}

	// ---------------------------------------------------------------------
	// Processes methods
	// ---------------------------------------------------------------------

	private void updateParameters() {
//		System.out.println("endYear " + endYear);
		//For debugging of population alignment
//		for(int age = Parameters.getMIN_AGE(); age <= Parameters.getMAX_AGE(); age++) {
//			int popNumByAge = 0;
//			for(Gender gender : Gender.values()) {
//				popNumByAge += ((List<Person>) personsByGenderAndAge.get(gender, age)).size();
//			}
//			System.out.print(popNumByAge + ", ");
//		}
//		System.out.println(" ");
		
		
//		System.out.println("Year " + year);
		Parameters.updateParameters(year);
	}
	
	
//	TODO: Need to allow cells with no people to sample from nearby Age / sex in demographic alignment (or at least throw a warning).  This is likely to be an issue with people at the very oldest ages which represent a small size of the real population, and then when scaled down by the scaling factor have a value < 1, which is then rounded down…  Could perhaps always round up, to ensure at least 1 person for each Female / Age?
	@SuppressWarnings("unchecked")
	private void populationAlignment() { // Align national population by Age and
											// Female using
											// Demographer.BruteForceAdjustment()
											// algorithm from LaborSim model

		//Update Age index of personsByGenderAndAge
		for(Gender gender : Gender.values()) {
//			for (int age = Parameters.getMAX_AGE(); age > Parameters.getMIN_AGE(); age--) {			
//				((List<Person>) personsByGenderAndAge.get(gender, age)).clear();				
//				((List<Person>) personsByGenderAndAge.get(gender, age)).addAll(((List<Person>) personsByGenderAndAge.get(gender, age-1)));		//Update the Age labels of the list of persons
//			}
//			((List<Person>) personsByGenderAndAge.get(gender, Parameters.getMIN_AGE())).clear();
			
			for(int age = Parameters.getMinAge(); age <= Parameters.getMaxAge(); age++) {
				((List<Person>) personsByGenderAndAge.get(gender, age)).clear();
			}
			
		}
		
		for(Person person : persons) {
			((List<Person>) personsByGenderAndAge.get(person.getGender(), person.getAge())).add(person);		//Update
		}
		
		//Create persons with minimum Age (17 year old) here
		int numFemalesWithMinAgeToCreate = (int)(((Number) Parameters
				.getPopulationProjectionsFemale().getValue(Parameters.getMinAge(), year)).doubleValue() 
				/ scalingFactor);					//Need to divide by scalingFactor to get appropriate size
		if(numFemalesWithMinAgeToCreate == 0) {
			throw new IllegalArgumentException("Number of females of minimum Age to be created is zero!");
//			numFemalesWithMinAgeToCreate++;			//Ensure there is at least one person in each cell / Age
		}
		
		int changeInNumAgentsOfThisGenderAndAge = createPopulationWithMinimumAge(Gender.Female, numFemalesWithMinAgeToCreate);
//		System.out.println("num Female Agents of min age to create is,,,,,, ," + numFemalesWithMinAgeToCreate + ", num created is ," + changeInNumAgentsOfThisGenderAndAge);
		
		int numMalesWithMinAgeToCreate = (int)(((Number) Parameters
				.getPopulationProjectionsMale().getValue(Parameters.getMinAge(), year)).doubleValue() 
				/ scalingFactor);					//Need to divide by scalingFactor to get appropriate size
		if(numMalesWithMinAgeToCreate == 0) {
			throw new IllegalArgumentException("Number of males of minimum Age to be created is zero!");
//			numMalesWithMinAgeToCreate++;			//Ensure there is at least one person in each cell / Age
		}
		
		changeInNumAgentsOfThisGenderAndAge = createPopulationWithMinimumAge(Gender.Male, numMalesWithMinAgeToCreate);				
//		System.out.println("num Male Agents of min age to create is,,,,,, ," + numMalesWithMinAgeToCreate + ", num created is ," + changeInNumAgentsOfThisGenderAndAge);
		
		//Brute Force Adjustment of population for all other ages (not MIN_AGE) and genders
		for (int age = Parameters.getMinAge()+1; age <= Parameters.getMaxAge(); age++) {		//Population with MIN_AGE will already have been aligned
			
			HashMap<Gender, Integer> targetNumByGenderForThisAge = new HashMap<Gender, Integer>(Gender.values().length);
			targetNumByGenderForThisAge.put(Gender.Male, (int)(((Number) Parameters.getPopulationProjectionsMale().getValue(age, year)).doubleValue() / scalingFactor));
			targetNumByGenderForThisAge.put(Gender.Female, (int)(((Number) Parameters.getPopulationProjectionsFemale().getValue(age, year)).doubleValue() / scalingFactor));	//Need to divide by scalingFactor to get appropriate size

			for(Gender gender : Gender.values()) {
//				System.out.println("original num of ,,," + gender + "s of age " + age + " is ," + ((LinkedList<Person>) personsByGenderAndAge.get(gender, age)).size() + ", and target is ," + targetNumByGenderForThisAge.get(gender));
				changeInNumAgentsOfThisGenderAndAge = createOrRemovePersons(gender, age, ((LinkedList<Person>) personsByGenderAndAge.get(gender, age)).size(), targetNumByGenderForThisAge.get(gender));
//				System.out.println(",change in num ,,," + gender + " agents of age " + age + " is ," + changeInNumAgentsOfThisGenderAndAge + ", and final number of agents is now ," + ((LinkedList<Person>) personsByGenderAndAge.get(gender, age)).size());
			}
			
		}

//		System.out.println("Population aligned.");
	}
	
	@SuppressWarnings("unchecked")
	protected int createOrRemovePersons(Gender gender, int age, int numPersonsInSimOfThisGenderAndAge, int targetNum) {
		if(numPersonsInSimOfThisGenderAndAge <= 0) {
			throw new IllegalArgumentException("Number of " + gender.toString() + "s of Age " + age + " in simulation is not positive!  Impossible to clone to create more persons of same Age and Female!");
		} else if (targetNum <= 0) {
//			targetNum = 1;			//Is this good enough?  Only one person would not provide good coverage of fields, i.e. if there is only one female aged MAX_AGE, and they lived in one Region, any cloning to increase the population of this demographic class to the target number would mean that they would all live in the same region.  Is this acceptable, or should we sample more widely, i.e. first to other Female of same Age, then to of +/-n from same Age, with n running from 1 to N such that there are at least a specific number of agents to sample from?  How would we decide what a good enough sample size is?  
			throw new IllegalArgumentException("Target number of " + gender.toString() + "s of Age " + age + " is not positive!  This would lead to lack of population for this Age and Female, making it impossible to clone from this demography class in future!");
		}
		
		int changeInAgentsOfThisGenderAndAge = 0;		//Change in the number of agents of this gender and age (negative if agents removed, positive if agents created)
		int numPersonsInSimMinusTarget = numPersonsInSimOfThisGenderAndAge - targetNum;
	
		if(numPersonsInSimMinusTarget > 0)	{					//Too many persons in sim, need to remove some
			
			TreeSet<Integer> randomIndices = new TreeSet<Integer>();				//Need to check we don't have same random numbers by adding them to a set (which cannot hold more than one of the same elements)
			int count = 0;
			boolean test;
			while(count < numPersonsInSimMinusTarget) {
				do {
					test = randomIndices.add(SimulationEngine.getRnd().nextInt(numPersonsInSimOfThisGenderAndAge));
					if(test) {
						count++;
					}
				} while(!test);
			}
//			System.out.println("randomIndices.size() " + randomIndices.size() + " " + Female + "InSimMinusProjection " + numPersonsInSimMinusTarget);
			
			for(Integer randomIndex : randomIndices) {		//Iterated in ascending order
				
				Person person = ((List<Person>) personsByGenderAndAge.get(gender, age)).get(numPersonsInSimOfThisGenderAndAge - 1 - randomIndex);		//As randomIndex is in ascending order, the index here is descending, meaning that we can remove person within the loop and still maintain a consistent labelling of the indices (if we removed indices in ascending order, we could potentially call an index no longer in existence after we removed some persons, e.g. if the largest index was called after a person was removed).  We have a '-1' to prevent ArrayOutOfBounds exceptions whenever randomIndex is 0.
				if(removePerson(person)) {
					changeInAgentsOfThisGenderAndAge--;
				}
//				persons.remove(person);			
//				((List<Person>) personsByGenderAndAge.get(gender, age)).remove(person);
//				System.out.println("Removed person? " + persons.remove(person) );			
//				System.out.println("Removed " + gender + " with age " + age + " from personsByGenderAndAge? " + ((List<Person>) personsByGenderAndAge.get(gender, age)).remove(person) );	

				
			}
			
		} else if(numPersonsInSimMinusTarget < 0) {												//Too few persons in sim, need to create some more
			
			ArrayList<Integer> randomIndices = new ArrayList<Integer>(((int)-numPersonsInSimMinusTarget)+1);			//+1 to allow rounding up for initial capacity
			int count = 0;
			while(count < -numPersonsInSimMinusTarget) {
				randomIndices.add(SimulationEngine.getRnd().nextInt(numPersonsInSimOfThisGenderAndAge));
				count++;
			}
//			System.out.println("randomIndices.size() " + randomIndices.size() + " " + Female + "InSimMinusProjection " + numPersonsInSimMinusTarget);
			
			for(Integer randomIndex : randomIndices) {
				
				Person person = ((List<Person>) personsByGenderAndAge.get(gender, age)).get(randomIndex); 
				Person newPerson = new Person(person);					//Copy constructor creates a new person with exactly the same fields
				boolean success = ((List<Person>) personsByGenderAndAge.get(gender, age)).add(newPerson);
				if(success && persons.add(newPerson)) {
					changeInAgentsOfThisGenderAndAge++;
				}
//				System.out.println("Add to personsByGenderAndAge for " + gender + " and age " + age + "? " + ((List<Person>) personsByGenderAndAge.get(gender, age)).add(newPerson) );
//				System.out.println("Add to persons " + persons.add(newPerson));
			}
		
		}
		
		return changeInAgentsOfThisGenderAndAge;
	
	}
	
	@SuppressWarnings("unchecked")
	protected int createPopulationWithMinimumAge(Gender gender, int numPersonsToCreate) {
		int count = 0;
		for(int i = 0; i < numPersonsToCreate; i++) {
			Person newPerson = new Person(Person.personIdCounter++, true, gender);		//Second argument is a flag on whether to initialise to minimum Age defaults
			boolean success = false;
			//Add to collections
			success = ((List<Person>) personsByGenderAndAge.get(gender, Parameters.getMinAge())).add(newPerson);
			if(success && persons.add(newPerson)) {
				count++;
			}
		}
//		System.out.println(numPersonsToCreate + " " + gender.toString() + "s created with minimum age.");
		return count;		//return number of agents created
	}
	

	private void inSchoolAlignment() {
		//Check alignment is being used for the correct case
		if(Parameters.systemOut) {
			System.out.println("year " + year + ", country " + country);
			if ( !( country.equals(Country.IE) || country.equals(Country.SE) ) ) {
				System.out.println("WARNING: Schooling is being aligned. Check whether this is intentional.");
			}
		}
		
		int numStudents = 0;		//Measure also include those who would be leaving school in this year, so the actual share of students is:- Num of students - Num who are leaving school
		int numUnder30 = 0;
		ArrayList<Person> personsLeavingSchool = new ArrayList<Person>();
		for(Person person : persons) {
			if(person.getActivity_status().equals(Activity_status.Student) ) {
				numStudents++;
			}
			if(person.isToLeaveSchool()) {
				personsLeavingSchool.add(person);
			}
			if(person.getAge() < 30) {
				numUnder30++;
			}
			
		}		
		
		int targetNumberOfPeopleLeavingSchool = numStudents - (int)( (double)numUnder30 * ((Number) Parameters.getStudentShareProjections()
												.getValue(country.toString(), year)).doubleValue() );
		
//		System.out.println("numStudents, " + numStudents + ", numLeaving, " + personsLeavingSchool.size() + ", numUnder30, " + numUnder30 + ", target, " + targetNumberOfPeopleLeavingSchool);

		if(targetNumberOfPeopleLeavingSchool <= 0) {		// This means the projected alignment rate is too high to reach (i.e. there are not enough students)
			for(Person person : personsLeavingSchool) {
				person.setToLeaveSchool(false);					//Best case scenario is to prevent anyone from leaving school in this year as the target share of students is higher than the number of students.  Although we cannot match the target, this is the nearest we can get to it.
				if(Parameters.systemOut) {
					System.out.println("target number of school leavers is not positive.  Force all school leavers to stay at school.");
				}
			}
		}
		else if(targetNumberOfPeopleLeavingSchool < personsLeavingSchool.size()) {			//If more people are leaving school than the target, perform resampling alignment on the school leavers to reduce the number. 
			if(Parameters.systemOut) {
				System.out.println("Schooling alignment: target number of students is " + targetNumberOfPeopleLeavingSchool);
			}
			new ResamplingAlignment<Person>().align(personsLeavingSchool
					, null, new AlignmentOutcomeClosure<Person>() {
	
						@Override
						public boolean getOutcome(Person agent) {
							return agent.isToLeaveSchool();
						}
	
						@Override
						public void resample(Person agent) {
							agent.setToLeaveSchool(!Parameters.getRegSchooling().event(agent, Person.Regressors.class));	//Those whose probit regression is true remain at school, hence toLeaveSchool must be false, and vice versa...
						}
	
					}, targetNumberOfPeopleLeavingSchool);

			if(Parameters.systemOut) {
				int numPostAlign = 0;
				for(Person person : persons) {
					if(person.isToLeaveSchool()) {
						numPostAlign++;
					}
				}
				
				System.out.println("Schooling alignment: aligned number of students is " + numPostAlign);
			}
		}
		
	}


	private void educationLevelAlignment() {
		
		//Check alignment is being used for the correct case
		if(Parameters.systemOut) {
			System.out.println("year " + year + ", country " + country);
			if(!(country.equals(Country.IE) || country.equals(Country.SE) )) {
				System.out.println("WARNING: Education is being aligned. Check whether this is intentional.");
			}
		}
		
		HashMap<Gender, ArrayList<Person>> personsLeavingEducation = new HashMap<Gender, ArrayList<Person>>();
		for(Gender gender : Gender.values()) {
			personsLeavingEducation.put(gender, new ArrayList<Person>());
		}
		
		for(Person person : persons) {
			if(person.isToLeaveSchool()) {
				personsLeavingEducation.get(person.getGender()).add(person);
			}
		}
		
		for(Gender gender : Gender.values()) {
		
			//Check pre-aligned population for education level statistics
			int numPersonsOfThisGenderWithMediumEduPreAlignment = 0, numPersonsOfThisGenderWithHighEduPreAlignment = 0, numPersonsOfThisGender = 0;
			for(Person person : persons) {
				if( person.getGender().equals(gender) && (person.getAge() <= 65) ) {		//Alignment projections are based only on persons younger than 66 years old
					if(person.getEducation() != null) {
						if(person.getEducation().equals(Education.Medium)) {
							numPersonsOfThisGenderWithMediumEduPreAlignment++; 
						}
						else if(person.getEducation().equals(Education.High)) {
							numPersonsOfThisGenderWithHighEduPreAlignment++; 
						}
						numPersonsOfThisGender++;
					}
				}
			}
			
			//Calculate alignment targets
			//High Education
			double highEducationRateTarget = ((Number)Parameters.getHighEducationRateInYear().getValue(year, gender.toString())).doubleValue();
			int numPersonsWithHighEduAlignmentTarget = (int) (highEducationRateTarget * (double)numPersonsOfThisGender);
			//Medium Education
			double mediumEducationRateTarget = ((Number)Parameters.getMediumEducationRateInYear().getValue(year, gender.toString())).doubleValue();
//			int numPersonsWithMediumEduAlignmentTarget = (int) (proportionInitialPopWithMediumEdu * numPersonsOfThisGender);		//Based on initial population - this ensures that proportion of medium educated people can never decrease below initial values
			int numPersonsWithMediumEduAlignmentTarget = (int) (mediumEducationRateTarget * (double)numPersonsOfThisGender);
			if(Parameters.systemOut) {
				System.out.println("Gender " + gender + ", highEduRateTarget, " + highEducationRateTarget + ", mediumEduRateTarget, " + mediumEducationRateTarget);
			}
			//Sort the list of school leavers by age
			Collections.shuffle(personsLeavingEducation.get(gender), SimulationEngine.getRnd());		//To remove any source of bias in borderline cases because the first subset of school leavers of same age are assigned a higher education level.  (I.e. if education level is deemed to be associated with age, so that higher ages are assigned higher education levels, then if the boundary between high and medium education levels is e.g. at the people aged 27, the first few people aged 27 will be assigned a high education level and the rest will have medium (or low) education levels.  To avoid any sort of regularity in the iteration order of school leavers, we shuffle here.  
			Collections.sort(personsLeavingEducation.get(gender), 
					new Comparator<Person>(){
						@Override
						public int compare(Person arg0, Person arg1) {
							return arg1.getAge() - arg0.getAge();	//Sort school leavers by descending order in age
						}
											});
			
			//Perform alignment
			int countHigh = 0, countMedium = 0;
			for(Person schoolLeaver : personsLeavingEducation.get(gender)) {		//This tries to maintain the naturally generated number of school-leavers with medium education, so that an increase in the number of school-leavers with high education is achieved through a reduction in the number of school-leavers with low education.  However, in the event that the number of school-leavers with either high or medium education are more than the total number of school leavers (in this year), we end up having no school leavers with low education and we have to reduce the number of school leavers with medium education
				
				if(numPersonsOfThisGenderWithHighEduPreAlignment + countHigh < numPersonsWithHighEduAlignmentTarget) {				//Only align if number of people in population with high education is too low.
					if(schoolLeaver.getEducation().equals(Education.Medium)) {
						schoolLeaver.setEducation(Education.High);			//As the personsLeavingEducation list is sorted by descending age, the oldest people leaving education are assigned to have high education levels
						countHigh++;
						countMedium--;		//Need to keep track of the number of people we reassign away from medium education, in order to properly align medium education levels once high education levels are aligned. 
					}
					else if(schoolLeaver.getEducation().equals(Education.Low)) {
						schoolLeaver.setEducation(Education.High);			//As the personsLeavingEducation list is sorted by descending age, the oldest people leaving education are assigned to have high education levels
						countHigh++;
					}
					
				}
				else if(numPersonsOfThisGenderWithMediumEduPreAlignment + countMedium < numPersonsWithMediumEduAlignmentTarget) {
					if(schoolLeaver.getEducation().equals(Education.Low)) {		//Now only re-assign those with Low education levels (we should not re-assign those with high education levels, as this would mess up the high education alignment!!!)
						schoolLeaver.setEducation(Education.Medium);		//When the number of high education level people have been assigned, the next oldest people are assigned to have medium education levels
						countMedium++;
					}
				}
				
//				System.out.println(schoolLeaver.getAge() + ", " + schoolLeaver.getEducation().toString());		//Test
			}
			personsLeavingEducation.get(gender).clear();	//Clear for re-use in the next year
		
			if(Parameters.systemOut) {
				//Check result of alignment
				int countHighEdPeople = 0, countMediumEdPeople = 0;
				for(Person person : persons) {
					if( person.getGender().equals(gender) && (person.getAge() <= 65) ) {		//Alignment projections are based only on persons younger than 66 years old
						if(person.getEducation() != null) {
							if(person.getEducation().equals(Education.High)) {
								countHighEdPeople++;
							}
							else if(person.getEducation().equals(Education.Medium)) {
								countMediumEdPeople++;
							}
						}
					}
				}
				System.out.println("Gender " + gender + ", Proportions of High Edu " + ((double)countHighEdPeople/(double)numPersonsOfThisGender) + ", Medium Edu " + ((double)countMediumEdPeople/(double)numPersonsOfThisGender));
			}			
		}		
	}


	private void unionAlignment() {
		
		if(Parameters.systemOut) {
			//Check alignment is being used for the correct case
			System.out.println("year " + year + ", country " + country);
			if ( !(country.equals(Country.IE) || country.equals(Country.SE) ) ) {
				System.out.println("WARNING: Union is being aligned. Check whether this is intentional.");
			}
		}
		
		double cohabitingFemalesUnder45rateTarget = ((Number)Parameters.getFemaleUnionUnder45ShareProjections().getValue(country.toString(), year)).doubleValue();
		
		if(Parameters.systemOut) {
			System.out.println("Union alignment target rate, " + cohabitingFemalesUnder45rateTarget);
		}
		new ResamplingAlignment<Person>().align(getPersons(),
				new FemalesUnder45Filter(), new AlignmentOutcomeClosure<Person>() {

					@Override
					public boolean getOutcome(Person agent) {
						return agent.getCivil_status().equals(Civil_status.Couple);
					}

					@Override
					public void resample(Person agent) {
						agent.householdFormation();
					}

				}, cohabitingFemalesUnder45rateTarget);
		
		if(Parameters.systemOut) {
			int countTotal = 0, countCohabiting = 0;
			for(Person person : persons) {
				if(person.getGender().equals(Gender.Female)) {
					if(person.getAge() < 45) {
						if(person.getCivil_status().equals(Civil_status.Couple)) {
							countCohabiting++;
						}
						countTotal++;
					}
				}
			}
			System.out.println("Cohabiting share after alignment, " + ( (double)countCohabiting / (double)countTotal ) );		
		}		
	}

	
	private void fertilityAlignment() {
		double fertilityRate = Parameters.getFertilityRateInYear().get(year);

		new ResamplingAlignment<Person>().align(getPersons(),
				new FertileFilter(), new AlignmentOutcomeClosure<Person>() {

					@Override
					public boolean getOutcome(Person agent) {
						return agent.isToGiveBirth();
					}

					@Override
					public void resample(Person agent) {
						agent.considerBirth();
					}

				}, fertilityRate);
		
		
//	//		System.out.println("Fertility Aligned.");
//			int countNumPuerperas = 0;
//			int countNumToGiveBirth = 0;
//			int countNumNotGivingBirth = 0;
//			for(Person person : persons) {
//				int age = person.getAge();
//				if( (person.getGender().equals(Gender.Female)) &&
//						( !person.getActivity_status().equals(Activity_status.Student) || person.isToLeaveSchool() ) &&
//						( age >= Parameters.getMinAgeMaternity() ) &&
//						( age <= Parameters.getMaxAgeMaternity() )) {
//					countNumPuerperas++;
//					if(person.isToGiveBirth()) {
//						countNumToGiveBirth++;
//					} else {
//						countNumNotGivingBirth++;
//					}
//				}
//				
//			}
//	//		System.out.println("countNumFertile - countNumToGiveBirth - countNumNotGivingBirth ,,,,,,,,,,,,," + (countNumFertile - countNumToGiveBirth - countNumNotGivingBirth));
//	//		System.out.println("fertilityRate ," + fertilityRate + ", and actual value is ," + (double)countNumToGiveBirth/(double)countNumFertile);
			
	}

	private void femaleParticipationChildrenGapAlignment() {

		int[] numActiveFemalesNoChildren = new int[101];
		int[] numFemalesNoChildren = new int[101];
		for(Person person : persons) {
			if(person.getGender().equals(Gender.Female)) {
				if(person.getD_children_3under().equals(Indicator.False) && person.getD_children_4_12().equals(Indicator.False)) {
					
					if(person.getActivity_status().equals(Activity_status.Active)) {
						numActiveFemalesNoChildren[person.getAge()]++;	
					}
					numFemalesNoChildren[person.getAge()]++;
				}
			}
		}
				
//		System.out.println("Year " + year);
		double shareActiveFemalesNoChildren = 0;
		int ageFrom = Parameters.getMinAgeMaternity();
		while(ageFrom < Parameters.maxAgeOfFemaleWithChild) {	
			int ageTo = ageFrom + Parameters.ageBand;
			
			int numActiveFemalesNoChildrenAgeBand = 0, numFemalesNoChildrenAgeBand = 0; 
			for(int age = ageFrom; age < ageTo; age++) {		//Aggregate over all ages in ageBand
				numActiveFemalesNoChildrenAgeBand += numActiveFemalesNoChildren[age];
				numFemalesNoChildrenAgeBand += numFemalesNoChildren[age];
			}
			if( numFemalesNoChildrenAgeBand > 0) {
				shareActiveFemalesNoChildren = (double)numActiveFemalesNoChildrenAgeBand / (double)numFemalesNoChildrenAgeBand;  
			}				
			
			double gapFactor = ((Number)Parameters.getChildrenGapFactors().getValue(country.toString(), year)).doubleValue();
			if( (shareActiveFemalesNoChildren > 0) && (((Number)initialChildrenGap.get(ageFrom, ageTo)).doubleValue() != 0)) {			//Only perform alignment for cases where there is at least one person in the sample (so that there are no divide by zero errors when analysing small populations).  TODO: In fact, it may be better to specify at least XX persons in the sample, where XX could be e.g. 10, so that the statistics are more reliable.
					//Note, there will be a null pointer exception from the line above if there is no initialChildrenGap defined for the age band, because ((numFemalesNoChildrenAgeBand > 0) && (numFemalesWithChildrenAgeBand > 0)) is not true in the creation of initialChildrenGap (i.e. the sample size is zero, so the statistic cannot be calculated).

				//Need to ensure target share is between 0% and 100% to be valid target value
				double targetProportionOfFemalesWithChildrenWhoAreActive = Math.min(1., 
						shareActiveFemalesNoChildren - ( ((Number)initialChildrenGap.get(ageFrom, ageTo)).doubleValue() * gapFactor) );
				targetProportionOfFemalesWithChildrenWhoAreActive = Math.max(0., targetProportionOfFemalesWithChildrenWhoAreActive);
				
//				System.out.println("ageFrom ," + ageFrom + ", ageTo ," + ageTo + ", target is ," + targetProportionOfFemalesWithChildrenWhoAreActive);
				
				new ResamplingAlignment<Person>().align(getPersons(),
						new FemaleWithChildrenByAgeBandFilter(ageFrom, ageTo), new AlignmentOutcomeClosure<Person>() {
		
							@Override
							public boolean getOutcome(Person agent) {
								return agent.getActivity_status().equals(Activity_status.Active);
							}
		
							@Override
							public void resample(Person agent) {
								agent.participation();
							}
		
						}, targetProportionOfFemalesWithChildrenWhoAreActive);
				
//				int countActiveFemalesWithKidsAgeBand = 0, countFemalesWithKidsAgeBand = 0;
//				for(Person person : persons) {
//					if(person.getGender().equals(Gender.Female)) {
//						if( (person.getAge() >= ageFrom) && (person.getAge() < ageTo) ) {
//							if(person.getD_children_3under().equals(Indicator.True) || person.getD_children_4_12().equals(Indicator.True)) {
//								if(person.getActivity_status().equals(Activity_status.Active)) {
//									countActiveFemalesWithKidsAgeBand++;
//								}
//								countFemalesWithKidsAgeBand++;
//							}
//						}
//					}	
//				}
//				double postAlignmentShare = (double)countActiveFemalesWithKidsAgeBand/(double)countFemalesWithKidsAgeBand;
//				System.out.println("Actual share after alignment is ," + postAlignmentShare);
//				System.out.println("ageFrom ," + ageFrom + ", ageTo ," + ageTo + " Relative Alignment error is ," + ((postAlignmentShare - targetProportionOfFemalesWithChildrenWhoAreActive)/targetProportionOfFemalesWithChildrenWhoAreActive) + " numFemalesWithKidsAgeBand (sample size) ," + countFemalesWithKidsAgeBand);

			}
//			else {
//				System.out.println("ageFrom ," + ageFrom + ", ageTo ," + ageTo + ", shareActiveFemalesNoChildren ," + shareActiveFemalesNoChildren + ", initialChildrenGap ," + ((Number)initialChildrenGap.get(ageFrom, ageTo)).doubleValue());
//			}
						
			ageFrom += Parameters.ageBand;		//Increase ageFrom for next loop
		}		
		
	}
	
	private void employmentAlignment() {

		double inWorkTarget = 1. - ((Number) Parameters
				.getUnemploymentRates().getValue(country.toString(),
				year)).doubleValue(); 
//		System.out.println("in work target" + inWorkTarget);
		
		// Align
		new ResamplingAlignment<Person>().align(getPersons(),
				new ActiveFilter(), new AlignmentOutcomeClosure<Person>() {

					@Override
					public boolean getOutcome(Person agent) {							
//						return agent.getEmployment_status().equals(Employment_status.Employed);
						if(agent.getEmployment_status().equals(Employment_status.Employed)) {
							return true;
						} else if(agent.getEmployment_status().equals(Employment_status.Unemployed)) {
							return false;
						} else {
							throw new IllegalArgumentException("Employment_status not Employed or Unemployed!  Check that only active people are being assessed, as employment_status should be null for people whose activity_status is not active!");
						}
					}

					@Override
					public void resample(Person agent) {					//Cannot just call agent agent.employed() again as this would mess up lagged variable for employment_status
						agent.considerEmployment();
					}
				}, inWorkTarget);
								
//		System.out.println("employment aligned.");
//		int countNumActive = 0;
//		int countNumEmployed = 0;
//		int countNumUnemployed = 0;
//		for(Person person : persons) {
//			if(person.getActivity_status().equals(Activity_status.Active)) {
//				countNumActive++;
//				if(person.getEmployment_status().equals(Employment_status.Employed)) {
//					countNumEmployed++;
//				} else if(person.getEmployment_status().equals(Employment_status.Unemployed)) {
//					countNumUnemployed++;
//				}
//			}
//		}
//		System.out.println("Year " + year);
//		System.out.println("active - employed - unemployed ,,,,,,,,,,,,," + (countNumActive - countNumEmployed - countNumUnemployed));
//		System.out.println("inWorkTarget ," + inWorkTarget + ", and actual value is ," + (double)countNumEmployed/(double)countNumActive);
//		double unemplTarget = ((Number) Parameters
//				.getUnemploymentRates().getValue(country.toString(),
//				year)).doubleValue();
//		double unemplActual = (double)countNumUnemployed/(double)countNumActive;
//		System.out.println("unemployment target ," + unemplTarget + ", and actual value is ," + unemplActual + ", actual - target = ," + (unemplActual - unemplTarget));
		
	}
	

	private void printElapsedTime() {

//		long timeDiff = System.currentTimeMillis() - elapsedTime;
//		System.out.println("Year " + year + " Module " + moduleId
//				+ " completed in " + timeDiff + "ms.");
		elapsedTime = System.currentTimeMillis();
		moduleId++;
	}
	
	@SuppressWarnings("unchecked")
	public boolean removePerson(Person person) {
		
		if(person.getAge() <= Parameters.getMaxAge()) {		//personsByGenderAndAge does not maintain population with age greater than MAX_AGE (there is no alignment at such ages).  The person will automatically fall out of PersonsByGenderAndAge, with no need for explicity removal!
			boolean removeSuccessful = ((List<Person>) personsByGenderAndAge.get(person.getGender(), person.getAge())).remove(person);
			return removeSuccessful && persons.remove(person);
		} else {
			return persons.remove(person);
		}
	}

	@SuppressWarnings("unchecked")
	public boolean addPerson(Person person) {
		if( (person.getAge() >= Parameters.getMinAge()) && (person.getAge() <= Parameters.getMaxAge()) ) {
			boolean addSuccessful = ((List<Person>) personsByGenderAndAge.get(person.getGender(), person.getAge())).add(person);
			return addSuccessful && persons.add(person);
		} else {
			return persons.add(person);
		}
	}

	// ---------------------------------------------------------------------
	// Access methods
	// ---------------------------------------------------------------------

	public Integer getStartYear() {
		return startYear;
	}

	 public List<Person> getPersons() {
		 return persons;
	 }

	public Integer getEndYear() {
		return endYear;
	}

	public Person getPerson(Long id) {

		for (Person person : persons) {
			if ((person.getId() != null) && (person.getId().getId().equals(id)))
				return person;
		}
		throw new IllegalArgumentException("Person with id " + id
				+ " is not present!");
	}

	public Country getCountry() {
		return country;
	}

	public void setCountry(Country country) {
		this.country = country;
	}
	
	public Integer getPopSize() {
		return popSize;
	}
	
	public void setPopSize(Integer popSize) {
		this.popSize = popSize;
	}

	public int getYear() {
		return year;
	}
	
	public Integer getMinRetireAgeMales() {
		return minRetireAgeMales;
	}

	public Integer getMinRetireAgeFemales() {
		return minRetireAgeFemales;
	}

	public void setMinRetireAgeFemales(Integer minRetireAgeFemales) {
		this.minRetireAgeFemales = minRetireAgeFemales;
	}

	public void setMinRetireAgeMales(Integer minRetireAgeMales) {
		this.minRetireAgeMales = minRetireAgeMales;
	}

	public int getMinRetireAge(Gender gender) {
		if(gender.equals(Gender.Female)) {
			return minRetireAgeFemales;
		} else {
			return minRetireAgeMales;
		}
	}

	public Integer getMaxRetireAgeMales() {
		return maxRetireAgeMales;
	}

	public void setMaxRetireAgeMales(Integer maxRetireAgeMales) {
		this.maxRetireAgeMales = maxRetireAgeMales;
	}

	public Integer getMaxRetireAgeFemales() {
		return maxRetireAgeFemales;
	}

	public void setMaxRetireAgeFemales(Integer maxRetireAgeFemales) {
		this.maxRetireAgeFemales = maxRetireAgeFemales;
	}

	public int getMaxRetireAge(Gender gender) {
		if(gender.equals(Gender.Female)) {
			return maxRetireAgeFemales;
		} else {
			return maxRetireAgeMales;
		}
	}

	public Integer getCohortEffectEndTrend() {
		return cohortEffectEndTrend;
	}

	public void setCohortEffectEndTrend(Integer cohortEffectEndTrend) {
		this.cohortEffectEndTrend = cohortEffectEndTrend;
	}

	public void setStartYear(Integer startYear) {
		this.startYear = startYear;
	}

	public void setEndYear(Integer endYear) {
		this.endYear = endYear;
	}

	public Boolean getFixRandomSeed() {
		return fixRandomSeed;
	}

	public void setFixRandomSeed(Boolean fixRandomSeed) {
		this.fixRandomSeed = fixRandomSeed;
	}

	public Long getRandomSeedIfFixed() {
		return randomSeedIfFixed;
	}

	public void setRandomSeedIfFixed(Long randomSeedIfFixed) {
		this.randomSeedIfFixed = randomSeedIfFixed;
	}	

}
