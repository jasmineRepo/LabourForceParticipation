package model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Transient;

import data.Parameters;
import microsim.data.db.PanelEntityKey;
import microsim.engine.SimulationEngine;
import microsim.event.EventListener;
import microsim.statistics.IDoubleSource;
import microsim.statistics.IIntSource;
import microsim.statistics.regression.RegressionUtils;
import model.enums.Activity_status;
import model.enums.Civil_status;
import model.enums.Country;
import model.enums.Education;
import model.enums.Employment_status;
import model.enums.Gender;
import model.enums.Indicator;
import model.enums.Region;

@Entity
public class Person implements EventListener, IDoubleSource, IIntSource //, Comparable<Person>
{
	
	public static long personIdCounter = 1000000;			//Could perhaps initialise this to one above the max key number in initial population, in the same way that we pull the max Age information from the input files.
	
	@Transient
	private LabourParticipationModel model;
	
	@Id
	private PanelEntityKey key;
		
	private int age;
	
	@Enumerated(EnumType.STRING)
	private Gender gender;
	
	@Enumerated(EnumType.STRING)
	private Education education;
	
	@Column(name="area")
	@Enumerated(EnumType.STRING)
	private Region region;

	@Column(name="d_union")
	@Enumerated(EnumType.ORDINAL)
	private Civil_status civil_status;
	
	@Transient
	private Civil_status civil_status_lag = null;		//Lag(1) of civil_status

	@Column(name="d_children_3under")		 
	@Enumerated(EnumType.ORDINAL)
	private Indicator d_children_3under;				//Dummy variable for whether the person has children under 3 years old.  As a string, it has values {False, True} but as ordinal this is mapped to {0, 1}.

	@Column(name="d_children_4_12")		 
	@Enumerated(EnumType.ORDINAL)
	private Indicator d_children_4_12;				//Dummy variable for whether the person has children between 4 and 12 years old.  As a string, it has values {False, True} but as ordinal this is mapped to {0, 1}.
	
	@Transient
	private Indicator d_children_3under_lag = null;				//Lag(1) of d_children_3under;
	
	@Transient
	private Indicator d_children_4_12_lag = null;				//Lag(1) of d_children_4_12;
	
	@Column(name="n_children_age0")
	private int n_children_0;
	
	@Column(name="n_children_age1")
	private int n_children_1;
	
	@Column(name="n_children_age2")
	private int n_children_2;
	
	@Column(name="n_children_age3")
	private int n_children_3;

	@Column(name="n_children_age4")
	private int n_children_4;

	@Column(name="n_children_age5")
	private int n_children_5;

	@Column(name="n_children_age6")
	private int n_children_6;

	@Column(name="n_children_age7")
	private int n_children_7;

	@Column(name="n_children_age8")
	private int n_children_8;

	@Column(name="n_children_age9")
	private int n_children_9;

	@Column(name="n_children_age10")
	private int n_children_10;

	@Column(name="n_children_age11")
	private int n_children_11;

	@Column(name="n_children_age12")
	private int n_children_12;
	
	@Column(name="n_children_age13")
	private int n_children_13;

	@Enumerated(EnumType.STRING)
	private Activity_status activity_status;

	@Transient
	private Activity_status activity_status_lag = null;		//Lag(1) of activity_status
	
	@Column(name="Employment_status")
	@Enumerated(EnumType.STRING)
	private Employment_status employment_status;

	@Transient
	private Employment_status employment_status_lag = null;	//Lag(1) of employment_status

	@Transient
	private double deviationFromMeanRetirementAge;				//Set on initialisation?
	
//	@Transient
//	private LaggedVariables laggedVariables;
	
	//For use with Resampling Alignment Algorithm to prevent inconsistent state.  If considerEmployment() and considerBirth() 
	//processes directly changed status of person, the lagged variables would be updated.  However, the alignment algorithm 
	//would work on updated lagged variables instead of the relevant previous set of lagged variables, so would not use the 
	//correct regressor values.  Also, the lagged variables would then be updated a further time by the alignment algorithm.
	@Transient
	private boolean toGiveBirth = false;
	
	@Transient
	private boolean toLeaveSchool = false;		//This is currently necessary to allow processes to occur (such as union or birth) for school leavers who have yet to determine what activity status to transition into (as Participation module comes after Household formation). 
	
	// ---------------------------------------------------------------------
	// Constructors
	// ---------------------------------------------------------------------

	//Used when loading the initial population from the input database
	public Person() {
		super();
		model = (LabourParticipationModel) SimulationEngine.getInstance().getManager(LabourParticipationModel.class.getCanonicalName());
		
	}
	
	public Person( long idNumber) {
		this();
		
		key = new PanelEntityKey(idNumber);

	}
	
	//For use with creating new people at the minimum Age who enter the simulation during population alignment. 
	public Person( long idNumber, boolean initialiseToMinimumAgeDefaults, Gender gender) {
		this(personIdCounter++);		//Sets up key

		if(initialiseToMinimumAgeDefaults) {
			setAge(Parameters.getMinAge());
			setGender(gender);
			setCivil_status(Civil_status.Single);
			setCivil_status_lag(Civil_status.Single);
			setD_children_3under(Indicator.False);
			setD_children_3under_lag(Indicator.False);
			setD_children_4_12(Indicator.False);
			setD_children_4_12_lag(Indicator.False);
			setN_children_0(0);
			setN_children_1(0);
			setN_children_2(0);
			setN_children_3(0);
			setN_children_4(0);
			setN_children_5(0);
			setN_children_6(0);
			setN_children_7(0);
			setN_children_8(0);
			setN_children_9(0);
			setN_children_10(0);
			setN_children_11(0);
			setN_children_12(0);
			setN_children_13(0);
			setActivity_status_lag(Activity_status.Student);				//Set lag activity status
			setEmployment_status_lag(null);
	
			//Set current activity Status using input parameters
			if( RegressionUtils.event( ( (Number)Parameters.getProbMinAgeStudent().getValue(model.getYear(), gender) ).doubleValue() ) ) {
				setActivity_status(Activity_status.Student);
			} else {															//Not a student
				
				//Randomly sample education level for non-students
				Education educationlevel = RegressionUtils.event(Parameters.getProbMinAgeEduEvents(), Parameters.getProbMinAgeEduProbs());
				setEducation(educationlevel);
				
				if( RegressionUtils.event( ( (Number)Parameters.getProbMinAgeActiveIfNotStudent().getValue(model.getYear(), gender) ).doubleValue() ) ) {
					setActivity_status(Activity_status.Active);
				
					if( RegressionUtils.event( ((Number)Parameters.getProbMinAgeEmployedIfActive().getValue(model.getYear(), gender) ).doubleValue() ) ) {
						setEmployment_status(Employment_status.Employed);
					} else {
						setEmployment_status(Employment_status.Unemployed);
					}
					
				} else {
					setActivity_status(Activity_status.Inactive);			//Obviously at this Age = MIN_AGE, person cannot be retired!
				}
				
			}
			
			Region region = RegressionUtils.event(Parameters.getRegionsInCountry(), Parameters.getRegionsInCountryProportions());		//Sample region with probability equal to proportions of population in each region within the country (which are assumed constant over time)
			setRegion(region);
//			initialisation();		//Problem with calling this here if we want activity_status_lag to be student, as there will be some who
									// have transition to active or inactive status.  This initialisation method configures lagged variables 
									//to their current values, so will overwrite the activity_status_lag to be non-student.  I'm not sure if
									//this would cause any problems, but probably safer not to do it here at the moment.
			setDeviationFromMeanRetirementAge();			//This would normally be done within initialisation, but the line above has been commented out for reasons given...
		}		
	}
	
	//Copy constructor used to create 'clones' of existing population in population alignment module
	public Person(Person person) {
		this(personIdCounter++);		//Sets up key
		
		//Copied from person
		this.age = person.age;
		this.gender = person.gender;
		this.region = person.region;
		this.civil_status = person.civil_status;
		this.civil_status_lag = person.civil_status_lag;
		this.d_children_3under = person.d_children_3under;			
		this.d_children_3under_lag = person.d_children_3under_lag;
		this.d_children_4_12 = person.d_children_4_12;			
		this.d_children_4_12_lag = person.d_children_4_12_lag;
		this.n_children_0 = person.n_children_0;
		this.n_children_1 = person.n_children_1;
		this.n_children_2 = person.n_children_2;
		this.n_children_3 = person.n_children_3;
		this.n_children_4 = person.n_children_4;
		this.n_children_5 = person.n_children_5;
		this.n_children_6 = person.n_children_6;
		this.n_children_7 = person.n_children_7;
		this.n_children_8 = person.n_children_8;
		this.n_children_9 = person.n_children_9;
		this.n_children_10 = person.n_children_10;
		this.n_children_11 = person.n_children_11;
		this.n_children_12 = person.n_children_12;
		this.n_children_13 = person.n_children_13;
		this.activity_status = person.activity_status;
		this.activity_status_lag = person.activity_status_lag;
		this.education = person.education;			
		this.employment_status = person.employment_status;
		this.employment_status_lag = person.employment_status_lag;
		this.deviationFromMeanRetirementAge = person.deviationFromMeanRetirementAge;	
//		this.laggedVariables = person.laggedVariables;									
		this.toGiveBirth = person.toGiveBirth;
//		this.toWork = person.toWork;
		this.toLeaveSchool = person.toLeaveSchool;
		
	}
	

	// ---------------------------------------------------------------------
	// Event Listener
	// ---------------------------------------------------------------------


	public enum Processes {
		Ageing,
		ConsiderBirth,
		ConsiderEmployment,
		ConsiderRetirement,
		HouseholdFormation,
		InSchool,
		Participation, 
		SetEducationLevel,
//		UpdateEmploymentStatus,
		UpdateMaternityStatus, 
	}
		
	@Override
	public void onEvent(Enum<?> type) {
		switch ((Processes) type) {
		case Ageing:
//			System.out.println("Ageing for person " + this.getId().getId());
			ageing();		
			break;
		case ConsiderRetirement:
//			System.out.println("Consider Retirement for person " + this.getId().getId());
			considerRetirement();
			break;
		case HouseholdFormation:
//			System.out.println("Household Formation for person " + this.getId().getId());
			householdFormation();
			break;
		case ConsiderBirth:
			considerBirth();
			break;
		case UpdateMaternityStatus:
//			System.out.println("Update Fertility Status for person " + this.getId().getId());
			updateMaternityStatus();
			break;
		case InSchool:
//			System.out.println("In Education for person " + this.getId().getId());
			inSchool();
			break;
		case SetEducationLevel:
//			System.out.println("Education level for person " + this.getId().getId());
			setEducationLevel();
			break;
		case Participation:
//			System.out.println("Participation for person " + this.getId().getId() + " with age " + age + " with activity_status " + activity_status + " and activity_status_lag " + activity_status_lag + " and toLeaveSchool " + toLeaveSchool + " with education " + education);
			participation();
			break;
		case ConsiderEmployment:
//			System.out.println("Consider employment for person " + this.getId().getId() + " with age " + age + " with activity_status " + activity_status + " and activity_status_lag " + activity_status_lag + " and toLeaveSchool " + toLeaveSchool + " with education " + education + " employment status " + employment_status + " employment status lag " + employment_status_lag);
			considerEmployment();
			break;
		}
	}
	
	//-----------------------------------------------------------------------------------
	// IIntSource implementation for the CrossSection.Integer objects in the collector
	//-----------------------------------------------------------------------------------
	
	public enum Variables {
		isActive,			//For cross section of Collector
		isEmployed,
	}
	
	public int getIntValue(Enum<?> variableID) {
		
		switch ((Variables) variableID) {
		
		case isActive:
			if (activity_status.equals(Activity_status.Active)) return 1;
			else return 0;
		case isEmployed:
			if (employment_status == null) return 0;		//For inactive people, who don't participate in the labour market
			else if (employment_status.equals(Employment_status.Employed)) return 1;
			else return 0;		//For unemployed case

		
		default:
			throw new IllegalArgumentException("Unsupported variable " + variableID.name() + " in Person#getIntValue");
		}
	}
	
	
	// ---------------------------------------------------------------------
	// implements IDoubleSource for use with Regression classes
	// ---------------------------------------------------------------------	
	
	public enum Regressors {
		
		//Regressors:
		Age,
		AgeSquared,
		Age23to25,						// Indicator for whether the person is in the Age category (see below for definition)
		Age26to30,						// Indicator for whether the person is in the Age category (see below for definition)
		Age21to27,						// Indicator for whether the person is in the Age category (see below for definition)
		Age28to30,						// Indicator for whether the person is in the Age category (see below for definition)
		ChildcareSpendingRegional,
		ChildcareTotal,
		Cohort,
		Constant, 						// For the constant (intercept) term of the regression
		Dcrisis,
		EduHigh,
		EduMedium,
		FertilityRate,
		Female,
		Ld_children_3under,
		Ld_children_4_12,
		Lemployed,
		Lactive,
		Lretired,
		Lstudent,
		Lunion,
		OnleaveBenefits,
		PartTimeRate,
		PartTime_AND_Ld_children_3under,			//Interaction term conditional on if the person had a child under 3 at the previous time-step
		UnemploymentRate,				

		//New enums to handle the covariance matrices
		Ld_children_3underES,
		Ld_children_3underGR,
		Ld_children_3underHU,
		Ld_children_3underIE,
		Ld_children_3underSE,
		Ld_children_3underIT,
		Ld_children_4_12ES,
		Ld_children_4_12GR,
		Ld_children_4_12HU,
		Ld_children_4_12IE,
		Ld_children_4_12SE,
		Ld_children_4_12IT,
		LactiveES,
		LactiveGR,
		LactiveHU,
		LactiveIE,
		LactiveSE,
		LactiveIT,
		EduHighES,
		EduHighGR,
		EduHighHU,
		EduHighIE,
		EduHighSE,
		EduHighIT,
		LunionES,
		LunionGR,
		LunionHU,
		LunionIE,
		LunionSE,
		LunionIT,
		EduMediumES,
		EduMediumGR,
		EduMediumHU,
		EduMediumIE,
		EduMediumSE,
		EduMediumIT,

		
		
		
		//Regional enums
		ES1,				//Spain
		ES2,
		ES3,
		ES4,
		ES5,
		ES6,
		ES7,
		GR1,				//Greece
		GR2,
		GR3,
		GR4,
		HU1,				//Hungary
		HU2,
		HU3,
		IE0,				//Ireland
		ITC,				//Italy
		ITF,
		ITG,
		ITH,
		ITI,
		SE1,				//Sweden
		SE2,
		SE3,   
	}
	
	public double getDoubleValue(Enum<?> variableID) {
		
		switch ((Regressors) variableID) {
				
		case Age:
//			System.out.println("age");
			return (double) age;
		case AgeSquared:
//			System.out.println("age sq");
			return (double) age * age;
		case Age23to25:
//			System.out.println("age 23 to 25");
			if(age >= 23 && age <= 25) {
				 return 1.;
			 }
			 else return 0.;
		case Age26to30:
//			System.out.println("age 26 to 30");
			 if(age >= 26 && age <= 30) {
				 return 1.;
			 }
			 else return 0.;
		case Age21to27: 
//			System.out.println("age 21 to 27");
			 if(age >= 21 && age <= 27) {
				 return 1.;
			 }
			 else return 0.;
		case Age28to30: 
//			System.out.println("age 28 to 30");
			 if(age >= 28 && age <= 30) {
				 return 1.;
			 }
			 else return 0.;
		case ChildcareSpendingRegional:
//			System.out.println("childcare spending regional");
			double spending_reg = ((Number) Parameters.getChildcare_spending_reg().get(region.toString(), model.getYear())).doubleValue(); 
			return spending_reg;
		case ChildcareTotal:
			double childcare_tot = ( n_children_0 * ((Number) Parameters.getBenefitsPerChildByAge().getValue(0, model.getYear())).doubleValue() ) +
									( n_children_1 * ((Number) Parameters.getBenefitsPerChildByAge().getValue(1, model.getYear())).doubleValue() ) +
									( n_children_2 * ((Number) Parameters.getBenefitsPerChildByAge().getValue(2, model.getYear())).doubleValue() ) +
									( n_children_3 * ((Number) Parameters.getBenefitsPerChildByAge().getValue(3, model.getYear())).doubleValue() );			
			return childcare_tot;
		case Cohort:
//			System.out.println("cohort");
			return  Math.min(model.getYear() - age, model.getCohortEffectEndTrend());
//			if(model.getYear() < model.getCohortEffectEndTrend()) {
//				return (double)(model.getYear() - age);
//			} else {
//				return (double)(model.getCohortEffectEndTrend() - age);
//			}
		case Constant:	
//			System.out.println("constant");
			return 1.;
		case Dcrisis:
//			System.out.println("Dcrisis");
//			return (model.getYear() >= 2009)? 1. : 0.;				//Dummy variable to capture effect of the Global Financial Crisis
			return ( (Number) Parameters.getCrisisStrengthAdjustment().getValue(model.getCountry().toString(), model.getYear() )).doubleValue();		//Adjustment factor for effect of Global Financial Crisis
		case EduHigh:
//			System.out.println("edu high");
			return education.equals(Education.High)? 1. : 0.;			//Returns 1 if true (education level is High), 0 otherwise
		case EduMedium:
//			System.out.println("edu medium");
			return education.equals(Education.Medium)? 1. : 0.;			//Returns 1 if true (education level is Medium), 0 otherwise
		case FertilityRate:
//			System.out.println("fertility");
			return Parameters.getFertilityRateInYear().get(model.getYear());			
		case Female:
//			System.out.println("female");
//			return (double) Female.ordinal();		//A bit less robust than method below as if we change the order of the enums in Gender class, it messes up the program without any warning!!!
			return gender.equals(Gender.Female)? 1. : 0.;
		case Ld_children_3under:
//			System.out.println("Ld child 3 under");
//			return (double) d_children_3under_lag.ordinal();
			return (d_children_3under_lag.equals(Indicator.True)) ? 1. : 0.;
		case Ld_children_4_12:
//			System.out.println("Ld child 4-12");
			return (d_children_4_12_lag.equals(Indicator.True)) ? 1. : 0.;
		case Lemployed:
//			System.out.println("Lemployed");
			if(employment_status_lag != null) {		//Problem will null pointer exceptions for those who are inactive and then become active as their lagged employment status is null!
				return employment_status_lag.equals(Employment_status.Employed)? 1. : 0.;
			} else {
				return 0.;			//A person who was not active but has become active in this year should have an employment_status_lag == null.  In this case, we assume this means 0 for the Employment regression, where Lemployed is used.
			}
		case Lactive:
//			System.out.println("Lactive");
			return activity_status_lag.equals(Activity_status.Active)? 1. : 0.;
		case Lretired:
//			System.out.println("Lretired");
			return activity_status_lag.equals(Activity_status.Retired)? 1. : 0.;
		case Lstudent:
//			System.out.println("Lstudent");
			return activity_status_lag.equals(Activity_status.Student)? 1. : 0.;
		case Lunion:
//			System.out.println("Lunion");
			return civil_status_lag.equals(Civil_status.Couple)? 1. : 0.;
		case OnleaveBenefits:
//			System.out.println("onleave benefits");
			return ((Number) Parameters.getOnleave_benefits().getValue(model.getCountry().toString(), model.getYear())).doubleValue();
		case PartTimeRate:
//			System.out.println("part time rate");
			return ((Number) Parameters.getPartTimeRates().getValue(region.toString(), model.getYear())).doubleValue();
		case PartTime_AND_Ld_children_3under:
//			System.out.println("part time and ld child 3 under");
			if( d_children_3under_lag.equals(Indicator.True) ) {
				return ((Number) Parameters.getPartTimeRates().getValue(region.toString(), model.getYear())).doubleValue();			//return the same as in PartTimeRate
			}
			else return 0.;
		case UnemploymentRate:
			return ((Number) Parameters.getUnemploymentRates().getValue(region.toString().substring(0, 2), model.getYear())).doubleValue();		//region.toString().substring(0, 2) gets the first two letters of the region code, which equals the country code (e.g. ITC is transformed to IT for Italy).  In this way, it is general if ever people can migrate across countries.
//			return ((Number) Parameters.getUnemploymentRates().getValue( model.getCountry().toString(), model.getYear())).doubleValue();
			
			//New enums to handle covariance matrices that are aggregated
		case Ld_children_3underES:
//			return (d_children_3under_lag.equals(Indicator.True) && (region.toString().startsWith(Country.ES.toString()))) ? 1. : 0.;
			double b = (d_children_3under_lag.equals(Indicator.True) && (region.toString().startsWith(Country.ES.toString()))) ? 1. : 0.;
//			System.out.println("d_children_3under_lag " + d_children_3under_lag.toString() + " region " + region + " return " + b);
			return b;
		case Ld_children_3underGR:
			return (d_children_3under_lag.equals(Indicator.True) && (region.toString().startsWith(Country.GR.toString()))) ? 1. : 0.;
		case Ld_children_3underHU:
			return (d_children_3under_lag.equals(Indicator.True) && (region.toString().startsWith(Country.HU.toString()))) ? 1. : 0.;
		case Ld_children_3underIE:
			return (d_children_3under_lag.equals(Indicator.True) && (region.toString().startsWith(Country.IE.toString()))) ? 1. : 0.;
		case Ld_children_3underSE:
			return (d_children_3under_lag.equals(Indicator.True) && (region.toString().startsWith(Country.SE.toString()))) ? 1. : 0.;
		case Ld_children_3underIT:
			double b10 = (d_children_3under_lag.equals(Indicator.True) && (region.toString().startsWith(Country.IT.toString()))) ? 1. : 0.;
//			System.out.println("d_children_3under_lag " + d_children_3under_lag.toString() + " region " + region + " return " + b10);
			return b10;			
		case Ld_children_4_12ES:
			return (d_children_4_12_lag.equals(Indicator.True) && (region.toString().startsWith(Country.ES.toString()))) ? 1. : 0.;
		case Ld_children_4_12GR:
			return (d_children_4_12_lag.equals(Indicator.True) && (region.toString().startsWith(Country.GR.toString()))) ? 1. : 0.;
		case Ld_children_4_12HU:
			return (d_children_4_12_lag.equals(Indicator.True) && (region.toString().startsWith(Country.HU.toString()))) ? 1. : 0.;
		case Ld_children_4_12IE:
			return (d_children_4_12_lag.equals(Indicator.True) && (region.toString().startsWith(Country.IE.toString()))) ? 1. : 0.;
		case Ld_children_4_12SE:
			return (d_children_4_12_lag.equals(Indicator.True) && (region.toString().startsWith(Country.SE.toString()))) ? 1. : 0.;
		case Ld_children_4_12IT:
			return (d_children_4_12_lag.equals(Indicator.True) && (region.toString().startsWith(Country.IT.toString()))) ? 1. : 0.;
			
		case LactiveES:
//			return (activity_status_lag.equals(Activity_status.Active) && (region.toString().startsWith(Country.ES.toString())))? 1. : 0.;
			double b2 = (activity_status_lag.equals(Activity_status.Active) && (region.toString().startsWith(Country.ES.toString())))? 1. : 0.;
//			System.out.println("activity_status_lag " + activity_status_lag.toString() + " region " + region + " return " + b2);			
			return b2;
		case LactiveGR:
			return (activity_status_lag.equals(Activity_status.Active) && (region.toString().startsWith(Country.GR.toString())))? 1. : 0.;
		case LactiveHU:
			return (activity_status_lag.equals(Activity_status.Active) && (region.toString().startsWith(Country.HU.toString())))? 1. : 0.;
		case LactiveIE:
			return (activity_status_lag.equals(Activity_status.Active) && (region.toString().startsWith(Country.IE.toString())))? 1. : 0.;
		case LactiveSE:
			return (activity_status_lag.equals(Activity_status.Active) && (region.toString().startsWith(Country.SE.toString())))? 1. : 0.;
		case LactiveIT:
			return (activity_status_lag.equals(Activity_status.Active) && (region.toString().startsWith(Country.IT.toString())))? 1. : 0.;

		case LunionES:
//			return (civil_status_lag.equals(Civil_status.Couple) && (region.toString().startsWith(Country.ES.toString())))? 1. : 0.;
			double b4 = (civil_status_lag.equals(Civil_status.Couple) && (region.toString().startsWith(Country.ES.toString())))? 1. : 0.;
//			System.out.println("civil_status_lag " + civil_status_lag.toString() + " region " + region + " return " + b4);
			return b4;
		case LunionGR:
			return (civil_status_lag.equals(Civil_status.Couple) && (region.toString().startsWith(Country.GR.toString())))? 1. : 0.;
		case LunionHU:
			return (civil_status_lag.equals(Civil_status.Couple) && (region.toString().startsWith(Country.HU.toString())))? 1. : 0.;
		case LunionIE:
			return (civil_status_lag.equals(Civil_status.Couple) && (region.toString().startsWith(Country.IE.toString())))? 1. : 0.;
		case LunionSE:
			return (civil_status_lag.equals(Civil_status.Couple) && (region.toString().startsWith(Country.SE.toString())))? 1. : 0.;
		case LunionIT:
			return (civil_status_lag.equals(Civil_status.Couple) && (region.toString().startsWith(Country.IT.toString())))? 1. : 0.;

		case EduMediumES:
			double b5 = (education.equals(Education.Medium) && (region.toString().startsWith(Country.ES.toString())))? 1. : 0.;
//			System.out.println("education " + education.toString() + " region " + region + " return " + b5);
			return b5;
		case EduMediumGR:
			return (education.equals(Education.Medium) && (region.toString().startsWith(Country.GR.toString())))? 1. : 0.;
		case EduMediumHU:
			return (education.equals(Education.Medium) && (region.toString().startsWith(Country.HU.toString())))? 1. : 0.;
		case EduMediumIE:
			return (education.equals(Education.Medium) && (region.toString().startsWith(Country.IE.toString())))? 1. : 0.;
		case EduMediumSE:
			return (education.equals(Education.Medium) && (region.toString().startsWith(Country.SE.toString())))? 1. : 0.;
		case EduMediumIT:
			return (education.equals(Education.Medium) && (region.toString().startsWith(Country.IT.toString())))? 1. : 0.;

			
		case EduHighES:
//			return (education.equals(Education.High) && (region.toString().startsWith(Country.ES.toString())))? 1. : 0.;
			double b3 = (education.equals(Education.High) && (region.toString().startsWith(Country.ES.toString())))? 1. : 0.;
//			System.out.println("education " + education.toString() + " region " + region + " return " + b3);
			return b3;
		case EduHighGR:
			return (education.equals(Education.High) && (region.toString().startsWith(Country.GR.toString())))? 1. : 0.;
		case EduHighHU:
			return (education.equals(Education.High) && (region.toString().startsWith(Country.HU.toString())))? 1. : 0.;
		case EduHighIE:
			return (education.equals(Education.High) && (region.toString().startsWith(Country.IE.toString())))? 1. : 0.;
		case EduHighSE:
			return (education.equals(Education.High) && (region.toString().startsWith(Country.SE.toString())))? 1. : 0.;
		case EduHighIT:
			return (education.equals(Education.High) && (region.toString().startsWith(Country.IT.toString())))? 1. : 0.;


			
		//Regional indicators (dummy variables)
		case ES1:				//Spain
			return (region.equals(Region.ES1)) ? 1. : 0.;
		case ES2:
			return (region.equals(Region.ES2)) ? 1. : 0.;
		case ES3:
			return (region.equals(Region.ES3)) ? 1. : 0.;
		case ES4:
			return (region.equals(Region.ES4)) ? 1. : 0.;
		case ES5:
			return (region.equals(Region.ES5)) ? 1. : 0.;
		case ES6:
			return (region.equals(Region.ES6)) ? 1. : 0.;
		case ES7:
			return (region.equals(Region.ES7)) ? 1. : 0.;
		case GR1:				//Greece
			return (region.equals(Region.GR1)) ? 1. : 0.;
		case GR2:
			return (region.equals(Region.GR2)) ? 1. : 0.;
		case GR3:
			return (region.equals(Region.GR3)) ? 1. : 0.;
		case GR4:
			return (region.equals(Region.GR4)) ? 1. : 0.;
		case HU1:				//Hungary
			return (region.equals(Region.HU1)) ? 1. : 0.;
		case HU2:
			return (region.equals(Region.HU2)) ? 1. : 0.;
		case HU3:
			return (region.equals(Region.HU3)) ? 1. : 0.;
		case IE0:				//Ireland
			return (region.equals(Region.IE0)) ? 1. : 0.;
		case ITC:				//Italy
			return (region.equals(Region.ITC)) ? 1. : 0.;
		case ITF:
			return (region.equals(Region.ITF)) ? 1. : 0.;
		case ITG:
			return (region.equals(Region.ITG)) ? 1. : 0.;
		case ITH:
			return (region.equals(Region.ITH)) ? 1. : 0.;
		case ITI:
			return (region.equals(Region.ITI)) ? 1. : 0.;
		case SE1:				//Sweden
			return (region.equals(Region.SE1)) ? 1. : 0.;
		case SE2:
			return (region.equals(Region.SE2)) ? 1. : 0.;
		case SE3:
			return (region.equals(Region.SE3)) ? 1. : 0.;
						
		default:
			throw new IllegalArgumentException("Unsupported regressor " + variableID.name() + " in Person#getDoubleValue");
		}
	}
	
	
//	private static enum LaggedVariableNames {
//
//		civil_status,
//		d_children_3under,
//		activity_status,
//		employment_status,
//
//	}
	
	// ---------------------------------------------------------------------
	// Initialisation methods
	// ---------------------------------------------------------------------
	
	public void setAdditionalFieldsInInitialPopulation() {
		setDeviationFromMeanRetirementAge();			
		configureLaggedVariables();
		
		//Sample deviationFromMeanRetirementAge until person in initial population is not immediately retired off 
		//(don't want to change their activity status immediately).  This is because we need to sample from the conditional 
		//distribution of retirement age given that the person has not yet retired (which is equivalent to a truncated normal 
		//distribution with the same mean and variance as the unconditional distribution).
		if(!activity_status.equals(Activity_status.Retired)) {		 
			if(age >= model.getMinRetireAge(gender)) {		//No need to check whether student as the deviationFromMeanRetirementAge is truncated so that it is always above min retirement Age, which is 45.
				while(age >= deviationFromMeanRetirementAge + ((Number)Parameters.getRetirementAge().getValue(model.getYear(), gender.toString() + "_Mean")).doubleValue()) {
					setDeviationFromMeanRetirementAge();			
//					System.out.println("age " + age + " retirement age " + (deviationFromMeanRetirementAge + ((Number)Parameters.getRetirementAge().getValue(model.getYear(), gender.toString() + "_Mean")).doubleValue()));
				}
			}
		}
		
	}
	
	private void setDeviationFromMeanRetirementAge() {		//Modelled as Gender but not time-dependent
		
//		deviationFromMeanRetirementAge = SimulationEngine.getInstance().getRandom().nextGaussian() 
//				* ((Number) Parameters.getRetirementAge().getValue(model.getYear(), gender.toString() + "_StandardError")).doubleValue();
		deviationFromMeanRetirementAge = SimulationEngine.getRnd().nextGaussian() 
				* ((Number) Parameters.getRetirementAge().getValue(model.getYear(), gender.toString() + "_StandardError")).doubleValue();
		
	}
	
	protected void configureLaggedVariables() {		//Initialise lag variables to current variables (only for initial population)
		
		activity_status_lag = activity_status;
		civil_status_lag = civil_status;
		d_children_3under_lag = d_children_3under;
		d_children_4_12_lag = d_children_4_12;
		employment_status_lag = employment_status;
		
//		for(LaggedVariableNames name : LaggedVariableNames.values()) {
//			laggedVariables.configure(name.toString(), 2);				//All lagged variables only have lag(0) and lag(1), so the length is 2.
//		}
	}

	// ---------------------------------------------------------------------
	// Processes
	// ---------------------------------------------------------------------

	private void ageing() {
		age++;
		if(age > Parameters.getMaxAge()) {
			model.getPersons().remove(this);		//No need to remove person from PersonsByGenderAndAge as person will 'age' out of it automatically.
		}
			
		//Update lagged variables
		activity_status_lag = activity_status;
		civil_status_lag = civil_status;
		d_children_3under_lag = d_children_3under;
		d_children_4_12_lag = d_children_4_12;
		employment_status_lag = employment_status;

		// Update household composition
		n_children_13 = n_children_12;
		n_children_12 = n_children_11;
		n_children_11 = n_children_10;
		n_children_10 = n_children_9;
		n_children_9 = n_children_8;
		n_children_8 = n_children_7;
		n_children_7 = n_children_6;
		n_children_6 = n_children_5;
		n_children_5 = n_children_4;
		n_children_4 = n_children_3;
		n_children_3 = n_children_2;
		n_children_2 = n_children_1;
		n_children_1 = n_children_0;
		n_children_0 = 0;					//Initialise to zero and increment if a birth happens in updateFertilityStatus()
		
		if(n_children_1 > 0 || n_children_2 > 0 || n_children_3 > 0) 
			d_children_3under = Indicator.True;
		else d_children_3under = Indicator.False; // This will be updated if a birth occurs.
		
		if ( n_children_4 > 0 || n_children_5 > 0 || n_children_6 > 0 || n_children_7 > 0  || n_children_8 > 0 || n_children_9 > 0 || n_children_10 > 0 || n_children_11 > 0 || n_children_12 > 0 ) 
			d_children_4_12 = Indicator.True;
		else d_children_4_12 = Indicator.False;
		
	}
		
	protected void inSchool() {		//Have now removed setting education levels to setEducationLevels() method.  This is because we need to allow the possibility of performing alignment of the share of students to decide whether they are leaving school or not, before any education levels are assigned (or, that was the idea compatible with the original model).  This seems difficult to do properly in the 'Graduation' case, so this method has been kept whole, but executed within the setEducationLevel() method, instead of the inSchool() method.

		if ( ! Parameters.eduReEntry && ! activity_status.equals(Activity_status.Student) )
			return;  // The model only applies to students (activityStatus == Student and toLeaveSchool == false)
		

		switch (Parameters.modelEducation) {
		case Multiprobit:

			// 1) Determine if continuing education (conditional on age)
			if( (age >= Parameters.getMaxAgeInEducation()) || !(Parameters.getRegSchooling().event(this, Person.Regressors.class)) ) {
				toLeaveSchool = true;				//Cannot update activity_status yet, as have not determined what to transition to.  Will update in participation()
			}
			
//			// 2) Assign education level to school leavers using MultiProbitRegression
//			if(toLeaveSchool) {	
//				//TODO:  Need to see if it is possible to change MultiProbitRegression, so that we don't have to pass the Education class below, as it could 
//				// potentially cause problems if this variable would ever be set as a different class to the type that MultiProbitRegression has been 
//				// initialised with (i.e. the T type in the classes).
//				education = Parameters.getRegEducationLevel().eventType(this, Person.Regressors.class, Education.class);
//			}
			break;

		case TwoStageProbit:
			
			// 1) Determine if continuing education
			if( (age >= Parameters.getMaxAgeInEducation()) || !(Parameters.getRegSchooling().event(this, Person.Regressors.class)) ) 
				toLeaveSchool = true;				//Cannot update activity_status yet, as have not determined what to transition to.  Will update in participation()

//			// 2) Assign education level to school leavers using separate probit models for different age ranges (low vs. medium at younger ages, medium vs. high at older ages)
//			if (age <= Parameters.getAgeThresholdEducation()) {
//				if (Parameters.getRegEducationMedium().event(this, Person.Regressors.class))
//					education = Education.Medium;
//				else education = Education.Low;
//			}
//			else {
//				if (Parameters.getRegEducationHigh().event(this, Person.Regressors.class))
//					education = Education.High;
//				else education = Education.Medium;
//			}
			break;
		
		case Graduation:		//Ross - I'm unsure how to split this between querying whether the person leaves school (after which we perform alignment) and then, if the person leaves school, setting their education levels...

			if(Parameters.inSchoolAlignment) {
				System.out.println("WARNING!  InSchoolAlignment will not work for the Graduation case of the education module as it currently stands!!!");
			}
//			
//			// 0) Assign education = Low to those with unassigned education
//			if (education == null) setEducation(Education.Low);
//			
//			// 1) Dropouts
//			if ( (education.equals(Education.Low) && age > Parameters.getMaxAgeGraduationMedium() ) ||
//					 (age > Parameters.getMaxAgeInEducation() ) ) {
//				toLeaveSchool = true;
//				return;
//			}
//			
//			// 2) Determine if graduation
//			if ( education.equals(Education.Low) && Parameters.getRegGraduationMedium().event(this, Person.Regressors.class) ) 	// not necessary to check that age <= Parameters.getMaxAgeGraduationMedium() as these people have already been thrown away.
//					setEducation(Education.Medium);
//
//			else if ( education.equals(Education.Medium) && age >= Parameters.getMinAgeGraduationHigh() && Parameters.getRegGraduationHigh().event(this, Person.Regressors.class) )
//					setEducation(Education.High);
//			
//			// 3) Determine if continuing education (conditional on age and education level)
//			if ( (education.equals(Education.Low)) && !(Parameters.getRegSchoolingLow().event(this, Person.Regressors.class)) ) 
//					toLeaveSchool = true;				//Cannot update activity_status yet, as have not determined what to transition to.  Will update in participation()
//			else if ( (education.equals(Education.Medium)) &&  !(Parameters.getRegSchoolingMedium().event(this, Person.Regressors.class)) ) 
//					toLeaveSchool = true;				//Cannot update activity_status yet, as have not determined what to transition to.  Will update in participation()
			break;
			
		default:
            System.err.println("In School model not properly loaded.");
				
		}
		
	}	
	
	private void setEducationLevel() {
		if ( ! Parameters.eduReEntry && ! activity_status.equals(Activity_status.Student) )
			return;  // The model only applies to students (activityStatus == Student and toLeaveSchool == false)

		switch (Parameters.modelEducation) {
		case Multiprobit:

//			// 1) Determine if continuing education (conditional on age)
//			if( (age >= Parameters.getMaxAgeInEducation()) || !(Parameters.getRegSchooling().event(this, Person.Regressors.class)) ) {
//				toLeaveSchool = true;				//Cannot update activity_status yet, as have not determined what to transition to.  Will update in participation()
//			}
			
			// 2) Assign education level to school leavers using MultiProbitRegression
			if(toLeaveSchool) {	
				//TODO:  Need to see if it is possible to change MultiProbitRegression, so that we don't have to pass the Education class below, as it could 
				// potentially cause problems if this variable would ever be set as a different class to the type that MultiProbitRegression has been 
				// initialised with (i.e. the T type in the classes).
				education = Parameters.getRegEducationLevel().eventType(this, Person.Regressors.class, Education.class);
			}
			break;

		case TwoStageProbit:
			
//			// 1) Determine if continuing education
//			if( (age >= Parameters.getMaxAgeInEducation()) || !(Parameters.getRegSchooling().event(this, Person.Regressors.class)) ) 
//				toLeaveSchool = true;				//Cannot update activity_status yet, as have not determined what to transition to.  Will update in participation()

			// 2) Assign education level to school leavers using separate probit models for different age ranges (low vs. medium at younger ages, medium vs. high at older ages)
			if(toLeaveSchool) {		//TODO:  Check whether this is a correct addition to Matteo's model (Ross).  Maintains convention that you don't assign education until person leaves school.
				if (age <= Parameters.getAgeThresholdEducation()) {
					if (Parameters.getRegEducationMedium().event(this, Person.Regressors.class))
						education = Education.Medium;
					else education = Education.Low;
				}
				else {
					if (Parameters.getRegEducationHigh().event(this, Person.Regressors.class))
						education = Education.High;
					else education = Education.Medium;
				}
				break;
			}
		
		case Graduation:		//Ross - I'm unsure how to split this between querying whether the person leaves school (after which we perform alignment) and then, if the person leaves school, setting their education levels...  
			
			// 0) Assign education = Low to those with unassigned education
			if (education == null) setEducation(Education.Low);
			
			// 1) Dropouts
			if ( (education.equals(Education.Low) && age > Parameters.getMaxAgeGraduationMedium() ) ||
					 (age > Parameters.getMaxAgeInEducation() ) ) {
				toLeaveSchool = true;
				return;
			}
			
			// 2) Determine if graduation
			if ( education.equals(Education.Low) && Parameters.getRegGraduationMedium().event(this, Person.Regressors.class) ) 	// not necessary to check that age <= Parameters.getMaxAgeGraduationMedium() as these people have already been thrown away.
					setEducation(Education.Medium);

			else if ( education.equals(Education.Medium) && age >= Parameters.getMinAgeGraduationHigh() && Parameters.getRegGraduationHigh().event(this, Person.Regressors.class) )
					setEducation(Education.High);
			
			// 3) Determine if continuing education (conditional on age and education level)
			if ( (education.equals(Education.Low)) && !(Parameters.getRegSchoolingLow().event(this, Person.Regressors.class)) ) 
					toLeaveSchool = true;				//Cannot update activity_status yet, as have not determined what to transition to.  Will update in participation()
			else if ( (education.equals(Education.Medium)) &&  !(Parameters.getRegSchoolingMedium().event(this, Person.Regressors.class)) ) 
					toLeaveSchool = true;				//Cannot update activity_status yet, as have not determined what to transition to.  Will update in participation()
			break;
			
		default:
            System.err.println("Education levels model not properly loaded.");
				
		}		
		
	}

	protected void householdFormation() {
		
		if(gender.equals(Gender.Female)) {							//Same filters for both Union and Birth components, so placed in the same method for computational efficiency
			if(!activity_status.equals(Activity_status.Student) || toLeaveSchool) { 	//Apply only to non-students or those who have just finished school (whose activity_status has not been updated yet as the state to transition into has not yet been evaluated until the participation module)  
		
				//Consensual Union: Only a change in civil_status for those who satisfy all the criteria.  Otherwise, they remain with the same civil_status
				if((age >= Parameters.getMinAgeMarriage()) && (age <= Parameters.getMaxAgeMarriage())) {	 
	
					if( Parameters.getRegFemalesUnion().event(this, Person.Regressors.class) ) {
						civil_status = Civil_status.Couple;
//						System.out.println("Updated civil status is " + civil_status.toString());
					} else {
						civil_status = Civil_status.Single;
//						System.out.println("Updated civil status is " + civil_status.toString());
					}
				}
				
//				//Consider Birth			//Has to be moved to separate process to allow for union alignment in Ireland and Sweden 
//				if( (age >= Parameters.getMinAgeMaternity()) && (age <= Parameters.getMaxAgeMaternity()))	{
//					considerBirth();
//				}
			}
		}	
	}
	
	//Consider whether to give birth
	protected void considerBirth() {
		if(gender.equals(Gender.Female)) {							//Same filters for both Union and Birth components
			if(!activity_status.equals(Activity_status.Student) || toLeaveSchool) { 	//Apply only to non-students or those who have just finished school (whose activity_status has not been updated yet as the state to transition into has not yet been evaluated until the participation module)  
				if( (age >= Parameters.getMinAgeMaternity()) && (age <= Parameters.getMaxAgeMaternity()))	{
					toGiveBirth = Parameters.getRegFemalesBirth().event(this, Person.Regressors.class);
				}
			}
		}
		
//		if(gender.equals(Gender.Female)) {						//Same filters for both Union and Birth components, so placed in the same method for computational efficiency
//			if(!activity_status.equals(Activity_status.Student) || toLeaveSchool) { 	//Apply only to non-students or those who have just finished school (whose activity_status has not been updated yet as the state to transition into has not yet been evaluated until the participation module)
//				
//				if( (age >= model.getMinFertileAge()) && (age <= model.getMaxFertileAge()))	{			//Now in householdFormation() and Fertile filter
//					
//					if(Parameters.getRegFemalesBirth().event(this, Person.Regressors.class) ) {		// Only if Age <= maxFertileAge can person give birth
//						toGiveBirth = true;
//					} else {
//						toGiveBirth = false;
//					}
//				}
//			}
//		}
	}
	
	private void updateMaternityStatus() {				//To be called once per year after fertility alignment

		if(toGiveBirth) {			
			n_children_0++;
			d_children_3under = Indicator.True;
			toGiveBirth = false;						//Reset boolean for next year
		} 	
	}
	
	private void considerRetirement() {					
		
		//Check whether to retire if not already retired and in the relevant Age domain
		if(!activity_status.equals(Activity_status.Retired)) {		
			boolean toRetire = false;
			if(age >= model.getMaxRetireAge(gender)) {
				toRetire = true;
			} else if(age >= model.getMinRetireAge(gender)) {		//No need to check whether student as the deviationFromMeanRetirementAge is truncated so that it is always above min retirement Age, which is 45.
				if(age >= deviationFromMeanRetirementAge + ((Number)Parameters.getRetirementAge().getValue(model.getYear(), gender.toString() + "_Mean")).doubleValue()) {			//deviationFromMeanRetirementAge to have been set on initialisation???
					toRetire = true;
				}
			}
			if(toRetire) {			//Update status
				activity_status = Activity_status.Retired;
				employment_status = null;
			}
		}
	}

	protected void participation() {
		if( (!activity_status.equals(Activity_status.Student) || toLeaveSchool) && !activity_status.equals(Activity_status.Retired) ) {
			boolean active;			//Cannot update activity status prior to regressions as will overwrite lag activity status, which may be a regressor!
			if(gender.equals(Gender.Male)) {
				active = Parameters.getRegParticipationMales().event(this, Person.Regressors.class);
			} 
			else if(Parameters.separateFemaleParticipationRegressions) {		//Use different regression co-efficients depending on whether person has children, and if so, whether children are all over 3.
				if(d_children_3under.equals(Indicator.True)) {
					active = Parameters.getRegParticipationFemalesWithChildren3orUnder().event(this, Person.Regressors.class);
				} 
				else if(d_children_4_12.equals(Indicator.True)) {		//else statement ensure no children aged 3 or under here
					active = Parameters.getRegParticipationFemalesWithOnlyChildren4orOver().event(this, Person.Regressors.class);
				}
				else {													//Case where female has no children
					active = Parameters.getRegParticipationFemalesNoChildren().event(this, Person.Regressors.class);
				}
			}
			else {			//Use conventional Female Participation Regression, where co-efficients are same for all females and presence of children is handled as an extra dummy variable
				active = Parameters.getRegParticipationFemales().event(this, Person.Regressors.class);
			}
						
			if(active) {
				activity_status = Activity_status.Active;
			} else {
				activity_status = Activity_status.Inactive;
				employment_status = null;
			}
			
			if(toLeaveSchool) {
				toLeaveSchool = false;				//Reset boolean, as may be used in alignment to search for school leavers
			}
		}
	}
	
	protected void considerEmployment() {		//Can be called more than once due to resampling alignment algorithm 
		if(activity_status.equals(Activity_status.Active)) {
			boolean toWork = Parameters.getRegEmployment().event(this, Person.Regressors.class);
			if(toWork) {
				employment_status = Employment_status.Employed;
			} else {
				employment_status = Employment_status.Unemployed;
			}
		}
	}
	
	
//	// ---------------------------------------------------------------------
//	// Comparator	//TODO: Do we still need this????
//	// ---------------------------------------------------------------------
//	// Person comparators are defined "on the fly" with closures in Model.marriageMatching(), 
//	// but a compareTo method has to be defined as the class implements the Comparable interface. 
//	
//	@Override
//	public int compareTo(Person p) {
//
//			return -1;
//	}
	
	
	// ---------------------------------------------------------------------
	// Access methods
	// ---------------------------------------------------------------------

	
	public PanelEntityKey getKey() {
		return key;
	}

	public void setKey(PanelEntityKey key) {
		this.key = key;
	}

	public int getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	public Gender getGender() {
		return gender;
	}

	public void setGender(Gender gender) {
		this.gender = gender;
	}

	public Activity_status getActivity_status() {
		return activity_status;
	}
	
	public int getActive()
	{
		if (activity_status.equals(Activity_status.Active)) return 1;
		else return 0;
	}
	
	public int getInactive()
	{
		if (activity_status.equals(Activity_status.Inactive)) return 1;
		else return 0;
	}
	
	public int getRetired()
	{
		if (activity_status.equals(Activity_status.Retired)) return 1;
		else return 0;
	}
	
	public int getStudent()
	{
		if (activity_status.equals(Activity_status.Student)) return 1;
		else return 0;
	}

//	public LaggedVariables getLagged() {
//		return lagged;
//	}

	public void setActivity_status(Activity_status activity_status) {
		this.activity_status = activity_status;
	}

	public Civil_status getCivil_status() {
		return civil_status;
	}

	public void setCivil_status(Civil_status civil_status) {
		this.civil_status = civil_status;
	}
	
	public int getCohabiting() {
		if(civil_status != null) {
			return civil_status.equals(Civil_status.Couple)? 1 : 0;
		}
		else {
			return 0;
		}
	}

//	public Integer getAgeGroupWork() {
//		return ageGroupWork;
//	}
//
//	public void setAgeGroupWork(Integer ageGroupWork) {
//		this.ageGroupWork = ageGroupWork;
//	}
//
//	public Integer getAgeGroupCivilState() {
//		return ageGroupCivilState;
//	}
//
//	public void setAgeGroupCivilState(Integer ageGroupCivilState) {
//		this.ageGroupCivilState = ageGroupCivilState;
//	}

	public Education getEducation() {
		return education;
	}
	
	public int getLowEducation() {
		if(education != null) {
			if (education.equals(Education.Low)) return 1;
			else return 0;
		}
		else {
			return 0;
		}
	}
	
	public int getMidEducation() {
		if(education != null) {
			if (education.equals(Education.Medium)) return 1;
			else return 0;
		}
		else {
			return 0;
		}
	}
	
	public int getHighEducation() {
		if(education != null) {
			if (education.equals(Education.High)) return 1;
			else return 0;
		}
		else {
			return 0;
		}
	}
	
//	public int getStudentYoung() {
//		if( activity_status.equals(Activity_status.Student) && (age >= 17) && (age < 21)) {
//			return 1;
//		}
//		else return 0;
//	}
//
//	public int getStudentMedium() {
//		if( activity_status.equals(Activity_status.Student) && (age >= 21) && (age < 25)) {
//			return 1;
//		}
//		else return 0;
//	}
//
//	public int getStudentOld() {
//		if( activity_status.equals(Activity_status.Student) && (age >= 25) && (age < 30)) {
//			return 1;
//		}
//		else return 0;
//	}

//	public int getCohabiting20_29() {		//Currently only females can have Civil_status.Couple
//		if(civil_status != null) {
//			if( civil_status.equals(Civil_status.Couple) && (age >= 20) && (age < 30)) {
//				return 1;
//			}
//			else return 0;
//			}
//		else {
//			System.out.println("Gender " + gender + ", age " + age);
//			return 0;
//		}
//	}
//	
//	public int getCohabiting30_44() {		//Currently only females can have Civil_status.Couple
//		if(civil_status != null) {
//			if( civil_status.equals(Civil_status.Couple) && (age >= 30) && (age < 45)) {
//				return 1;
//			}
//			else return 0;
//			}
//		else {
//			System.out.println("Gender " + gender + ", age " + age);
//			return 0;
//		}
//	}
//	
//	public int getCohabiting45_59() {		//Currently only females can have Civil_status.Couple
//		if(civil_status != null) {
//			if( civil_status.equals(Civil_status.Couple) && (age >= 45) && (age < 60)) {
//				return 1;
//			}
//			else return 0;
//			}
//		else {
//			System.out.println("Gender " + gender + ", age " + age);
//			return 0;
//		}
//	}
//	
//	public int getCohabiting60_74() {		//Currently only females can have Civil_status.Couple
//		if(civil_status != null) {
//			if( civil_status.equals(Civil_status.Couple) && (age >= 60) && (age < 75)) {
//				return 1;
//			}
//			else return 0;
//			}
//		else {
//			System.out.println("Gender " + gender + ", age " + age);
//			return 0;
//		}
//	}

	public void setEducation(Education educationlevel) {
		this.education = educationlevel;
	}

	public Employment_status getEmployment_status() {
		return employment_status;
	}

	public void setEmployment_status(Employment_status employment_status) {
		this.employment_status = employment_status;
	}
	
	public int getEmployed() {
		if(employment_status != null) {
			if(employment_status.equals(Employment_status.Employed) ) {
				return 1;
			}
			else return 0;
			
		} else {
			return 0;
		}
		
	}

	public Region getRegion() {
		return region;
	}

	public void setRegion(Region region) {
		this.region = region;
	}

	public Indicator getD_children_3under() {
		return d_children_3under;
	}

	public void setD_children_3under(Indicator d_children_3under) {
		this.d_children_3under = d_children_3under;
	}
	
	public Indicator getD_children_4_12() {
		return d_children_4_12;
	}

	public void setD_children_4_12(Indicator d_children_4_12) {
		this.d_children_4_12 = d_children_4_12;
	}

	public Civil_status getCivil_status_lag() {
		return civil_status_lag;
	}

	public void setCivil_status_lag(Civil_status civil_status_lag) {
		this.civil_status_lag = civil_status_lag;
	}

	public Activity_status getActivity_status_lag() {
		return activity_status_lag;
	}

	public void setActivity_status_lag(Activity_status activity_status_lag) {
		this.activity_status_lag = activity_status_lag;
	}

	public Employment_status getEmployment_status_lag() {
		return employment_status_lag;
	}

	public void setEmployment_status_lag(Employment_status employment_status_lag) {
		this.employment_status_lag = employment_status_lag;
	}

	public Indicator getD_children_3under_lag() {
		return d_children_3under_lag;
	}

	public void setD_children_3under_lag(Indicator d_children_3under_lag) {
		this.d_children_3under_lag = d_children_3under_lag;
	}

	public Indicator getD_children_4_12_lag() {
		return d_children_4_12_lag;
	}

	public void setD_children_4_12_lag(Indicator d_children_4_12_lag) {
		this.d_children_4_12_lag = d_children_4_12_lag;
	}

	public double getDeviationFromMeanRetirementAge() {
		return deviationFromMeanRetirementAge;
	}

	public int getN_children_0() {
		return n_children_0;
	}

	public void setN_children_0(int n_children_0) {
		this.n_children_0 = n_children_0;
	}

	public void setN_children_1(int n_children_1) {
		this.n_children_1 = n_children_1;
	}

	public void setN_children_2(int n_children_2) {
		this.n_children_2 = n_children_2;
	}

	public void setN_children_3(int n_children_3) {
		this.n_children_3 = n_children_3;
	}
	
	public void setN_children_4(int n_children_4) {
		this.n_children_4 = n_children_4;
	}

	public void setN_children_5(int n_children_5) {
		this.n_children_5 = n_children_5;
	}

	public void setN_children_6(int n_children_6) {
		this.n_children_6 = n_children_6;
	}

	public void setN_children_7(int n_children_7) {
		this.n_children_7 = n_children_7;
	}

	public void setN_children_8(int n_children_8) {
		this.n_children_8 = n_children_8;
	}

	public void setN_children_9(int n_children_9) {
		this.n_children_9 = n_children_9;
	}

	public void setN_children_10(int n_children_10) {
		this.n_children_10 = n_children_10;
	}

	public void setN_children_11(int n_children_11) {
		this.n_children_11 = n_children_11;
	}

	public void setN_children_12(int n_children_12) {
		this.n_children_12 = n_children_12;
	}

	public void setN_children_13(int n_children_13) {
		this.n_children_13 = n_children_13;
	}
	
	
//
//	public LaggedVariables getLaggedVariables() {
//		return laggedVariables;
//	}
//
//	public void setLaggedVariables(LaggedVariables laggedVariables) {
//		this.laggedVariables = laggedVariables;
//	}


	public boolean isToGiveBirth() {
		return toGiveBirth;
	}

//	public boolean isToWork() {
//		return toWork;
//	}

	public boolean isToLeaveSchool() {
		return toLeaveSchool;
	}

	public void setToLeaveSchool(boolean toLeaveSchool) {
		this.toLeaveSchool = toLeaveSchool;
	}


	
}