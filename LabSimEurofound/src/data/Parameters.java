package data;

import microsim.data.excel.ExcelAssistant;
import microsim.data.MultiKeyCoefficientMap;
import microsim.statistics.regression.MultiProbitRegression;
import microsim.statistics.regression.ProbitRegression;
import microsim.statistics.regression.RegressionColumnNames;
import microsim.statistics.regression.RegressionUtils;
import model.LabourParticipationModel;
import model.Person;
import model.enums.Country;
import model.enums.Education;
import model.enums.Gender;
import model.enums.ModelEducation;
import model.enums.Region;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import microsim.engine.SimulationEngine;

import org.apache.commons.collections.MapIterator;
import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.collections.map.MultiKeyMap;

public class Parameters {
	
	//Could define this explicitly here, however we now obtain this value from the maximum Age specified in the popProjections13eurostat.xls (which corresponds to a mortality rate of 1).  This takes place in the loadParameters() method.
	private static int MAX_AGE, MIN_AGE;		
	
	// TODO: the following parameters should be read from an xls file
	private static final int MAX_AGE_IN_EDUCATION = 30;			// Max age a person can stay in education
	private static final int AGE_THRESHOLD_EDUCATION = 21;		// Age threshold used by ModelEducation.TwoStageProbit. School leavers at the threshold or younger can only be assigned low or medium education; school leavers above the threshold can only be assigned medium or high education
	private static final int MAX_AGE_GRADUATION_MEDIUM = 21;	// Max age a person can exit education with a low degree. Used by ModelEducation.Graduation 
	private static final int MIN_AGE_GRADUATION_HIGH = 18; 		// Min age a person can exit education with a high degree. Used by ModelEducation.Graduation 
	private static final int MIN_AGE_MARRIAGE = 18;  			// Min age a person can marry
	private static final int MAX_AGE_MARRIAGE = 75;  			// Max age a person can marry
	private static final int MIN_AGE_MATERNITY = 18;  			// Min age a person can give birth
	private static final int MAX_AGE_MATERNITY = 45;  			// Max age a person can give birth
	
	public static final boolean systemOut = false;
	
	public static final ModelEducation modelEducation = ModelEducation.Multiprobit;
	public static final boolean eduReEntry = false;
	
	//Schooling alignment
	public static final boolean inSchoolAlignment = true;		//Switch for turning schooling alignment (aligning proportion of students) on/off.  
	//Education alignment
	public static final boolean educationLevelAlignment = true;		//Switch for turning education alignment on/off.  Currently only for Ireland, Sweden and Hungary
	//Education alignment
	public static final boolean unionAlignment = true;		//Switch for turning education alignment on/off.  Currently only for Ireland, Sweden and Hungary
	
	//Separate female participation regressions
	public static final boolean separateFemaleParticipationRegressions = true;
	
	//Females participation children gap alignment
	public static final boolean trickOn = false;
	public static final int ageBand = 5;			//For trickOn, the ageBand of females assessed for female participation gap
	public static final int maxAgeOfFemaleWithChild = MAX_AGE_MATERNITY + 13;
	
	//Bootstrap all the regression coefficients if true, or only the female labour participation regressions when false
	private static boolean bootstrapAll = true;
	private static boolean bootstrapEducation = false;
	
	// scenario parameters
	private static MultiKeyCoefficientMap probMinAgeStudent,
						probMinAgeActiveIfNotStudent,
						probMinAgeEmployedIfActive,
						probMinAgeEduLevelMultiMap;

	private static Education[] probMinAgeEduEvents = new Education[Education.values().length];
	private static double[] probMinAgeEduProbs = new double[Education.values().length];
	
	private static HashMap<Integer, Double> fertilityRateInYear;
	
	private static MultiKeyCoefficientMap childcare_coverage_national;
	private static MultiKeyCoefficientMap childcare_coverage_regional;
	private static MultiKeyCoefficientMap childcare_spending_national;
	private static MultiKeyCoefficientMap benefitsPerChildByAge;
	private static MultiKeyCoefficientMap childrenGapFactors;			//Factors to impose a decay in the gap between the share of active females with and without children.  Each year, a different (declining) factor is multiplied by the difference between the share of active females with and without children in the initial population. 
	private static MultiKeyCoefficientMap crisisStrengthAdjustment;
	private static MultiKeyCoefficientMap onleave_benefits;
	private static MultiKeyCoefficientMap partTimeRates;
	private static MultiKeyCoefficientMap populationProjectionsMale;
	private static MultiKeyCoefficientMap populationProjectionsFemale;
	private static MultiKeyCoefficientMap projectionsHighEdu;			//Alignment projections for High Education
	private static MultiKeyCoefficientMap projectionsMediumEdu;			//Alignment projections for Medium Education
	private static MultiKeyCoefficientMap retirementAge;
	private static MultiKeyCoefficientMap studentShareProjections;		//Alignment projections for Student share of population
	private static MultiKeyCoefficientMap unemploymentRates;
	private static MultiKeyCoefficientMap unionShareProjections;		//Alignment projections for Couple share of population

	// regression coefficients
	private static MultiKeyCoefficientMap coeffSchooling;
	private static MultiKeyCoefficientMap coeffSchoolingLow;	// used by ModelEducation.Graduation
	private static MultiKeyCoefficientMap coeffSchoolingMedium;	// used by ModelEducation.Graduation
	private static MultiKeyCoefficientMap coeffEducationLowHighCombined;	// used by ModelEducation.Multiprobit in order to first bootstrap the coefficients, which are then seperated and seperately fed into the MultiProbitRegression
	private static MultiKeyCoefficientMap coeffEducationLow;	// used by ModelEducation.Multiprobit
	private static MultiKeyCoefficientMap coeffEducationMedium; // used by ModelEducation.TwoStageProbit
	private static MultiKeyCoefficientMap coeffEducationHigh;	// used by both ModelEducation.Multiprobit and ModelEducation.TwoStageProbit (in different ways)
	private static MultiKeyCoefficientMap coeffGraduationMedium; // used by ModelEducation.Graduation
	private static MultiKeyCoefficientMap coeffGraduationHigh; 	// used by ModelEducation.Graduation
	private static MultiKeyCoefficientMap coeffEmployment;
	private static MultiKeyCoefficientMap coeffBirthFemales;
	private static MultiKeyCoefficientMap coeffUnionFemales;
	private static MultiKeyCoefficientMap coeffParticipationFemales;
	private static MultiKeyCoefficientMap coeffParticipationMales;
	
	private static MultiKeyCoefficientMap coeffParticipationFemalesWithChildren3orUnder;
	private static MultiKeyCoefficientMap coeffParticipationFemalesWithOnlyChildren4orOver;
	private static MultiKeyCoefficientMap coeffParticipationFemalesNoChildren;

	
	// regression objects
	private static ProbitRegression regSchooling;
	private static ProbitRegression regSchoolingLow;		// used by ModelEducation.Graduation
	private static ProbitRegression regSchoolingMedium;		// used by ModelEducation.Graduation
	private static MultiProbitRegression<Education> regEducationLevel;
	private static ProbitRegression regEducationLow;		// used by ModelEducation.Multiprobit
	private static ProbitRegression regEducationMedium;		// used by ModelEducation.TwoStageProbit
	private static ProbitRegression regEducationHigh;		// used by both ModelEducation.Multiprobit and ModelEducation.TwoStageProbit (in different ways)
	private static ProbitRegression regGraduationMedium;	// used by ModelEducation.Graduation
	private static ProbitRegression regGraduationHigh;		// used by ModelEducation.Graduation
	private static ProbitRegression regEmployment;
	private static ProbitRegression regBirthFemales;
	private static ProbitRegression regUnionFemales;
	private static ProbitRegression regParticipationFemales;
	private static ProbitRegression regParticipationMales;

	private static ProbitRegression regParticipationFemalesWithChildren3orUnder;
	private static ProbitRegression regParticipationFemalesWithOnlyChildren4orOver;
	private static ProbitRegression regParticipationFemalesNoChildren;

	private static Region[] regionsInCountryForFemales;				//Need to have two separate arrays as order of iteration may have been different
	private static double[] regionsInCountryProbabilitiesForFemales;
	private static Region[] regionsInCountryForMales;
	private static double[] regionsInCountryProbabilitiesForMales;

	private static MultiKeyMap childcare_spending_reg;

	public static void loadParameters(Country country) {

		//Scenario parameters		
		childcare_coverage_national = ExcelAssistant.loadCoefficientMap("input/scenario_childcare_coverage_national.xls", "Sheet1", 1, 39);
		childcare_coverage_regional = ExcelAssistant.loadCoefficientMap("input/scenario_childcare_coverage_regional.xls", ((LabourParticipationModel) SimulationEngine.getInstance().getManager(LabourParticipationModel.class.getCanonicalName())).getCountry().toString(), 1, 39);
		childcare_spending_national = ExcelAssistant.loadCoefficientMap("input/scenario_childcare_spending_national.xls", "Sheet1", 1, 39);
		benefitsPerChildByAge = ExcelAssistant.loadCoefficientMap("input/scenario_benefits_per_child.xls", ((LabourParticipationModel) SimulationEngine.getInstance().getManager(LabourParticipationModel.class.getCanonicalName())).getCountry().toString(), 1, 39);
		calculateChildcareSpendingRegional();
		crisisStrengthAdjustment = ExcelAssistant.loadCoefficientMap("input/scenario_crisis_strength_adjustment.xls", "Sheet1", 1, 43);
		onleave_benefits = ExcelAssistant.loadCoefficientMap("input/scenario_onleave_benefits.xls", "Sheet1", 1, 39);
		partTimeRates = ExcelAssistant.loadCoefficientMap("input/scenario_part_time_rate.xls", ((LabourParticipationModel) SimulationEngine.getInstance().getManager(LabourParticipationModel.class.getCanonicalName())).getCountry().toString(), 1, 39);
		retirementAge = ExcelAssistant.loadCoefficientMap("input/scenario_retirement_age.xls", ((LabourParticipationModel) SimulationEngine.getInstance().getManager(LabourParticipationModel.class.getCanonicalName())).getCountry().toString(), 1, 4);
		
		//Alignment parameters
		childrenGapFactors = ExcelAssistant.loadCoefficientMap("input/align_fem_part_children_gap_factors.xls", "Sheet1", 1, 39);
		populationProjectionsMale = ExcelAssistant.loadCoefficientMap("input/align_popProjections13eurostat.xls", ((LabourParticipationModel) SimulationEngine.getInstance().getManager(LabourParticipationModel.class.getCanonicalName())).getCountry().toString() + "_Male", 1, 68);				
		populationProjectionsFemale = ExcelAssistant.loadCoefficientMap("input/align_popProjections13eurostat.xls", ((LabourParticipationModel) SimulationEngine.getInstance().getManager(LabourParticipationModel.class.getCanonicalName())).getCountry().toString() + "_Female", 1, 68); 
		projectionsHighEdu = ExcelAssistant.loadCoefficientMap("input/align_high_edu.xls", ((LabourParticipationModel) SimulationEngine.getInstance().getManager(LabourParticipationModel.class.getCanonicalName())).getCountry().toString(), 1, 2); 
		projectionsMediumEdu = ExcelAssistant.loadCoefficientMap("input/align_medium_edu.xls", ((LabourParticipationModel) SimulationEngine.getInstance().getManager(LabourParticipationModel.class.getCanonicalName())).getCountry().toString(), 1, 2);
		studentShareProjections = ExcelAssistant.loadCoefficientMap("input/align_student_under30.xls", "Sheet1", 1, 39);
		unemploymentRates = ExcelAssistant.loadCoefficientMap("input/align_unemployment.xls", "Sheet1", 1, 39);
		unionShareProjections = ExcelAssistant.loadCoefficientMap("input/align_union_under45.xls", "Sheet1", 1, 39);
		
		probMinAgeStudent = ExcelAssistant.loadCoefficientMap("input/p17student.xls", ((LabourParticipationModel) SimulationEngine.getInstance().getManager(LabourParticipationModel.class.getCanonicalName())).getCountry().toString(), 1, 2);
		probMinAgeActiveIfNotStudent = ExcelAssistant.loadCoefficientMap("input/p17active_not_student.xls", ((LabourParticipationModel) SimulationEngine.getInstance().getManager(LabourParticipationModel.class.getCanonicalName())).getCountry().toString(), 1, 2);
		probMinAgeEmployedIfActive = ExcelAssistant.loadCoefficientMap("input/p17employ_if_active.xls", ((LabourParticipationModel) SimulationEngine.getInstance().getManager(LabourParticipationModel.class.getCanonicalName())).getCountry().toString(), 1, 2);
		probMinAgeEduLevelMultiMap = ExcelAssistant.loadCoefficientMap("input/p17edu_level.xls", ((LabourParticipationModel) SimulationEngine.getInstance().getManager(LabourParticipationModel.class.getCanonicalName())).getCountry().toString(), 1, 3);

			
		// Regressions coefficients
		switch (modelEducation) {
			case Multiprobit:
//				coeffEducationLow = ExcelAssistant.loadCoefficientMap("input/reg_education_low.xls", ((LabourParticipationModel) SimulationEngine.getInstance().getManager(LabourParticipationModel.class.getCanonicalName())).getCountry().toString(), 1, 1);
//				coeffEducationHigh = ExcelAssistant.loadCoefficientMap("input/reg_education_high.xls", ((LabourParticipationModel) SimulationEngine.getInstance().getManager(LabourParticipationModel.class.getCanonicalName())).getCountry().toString(), 1, 1);
				coeffEducationLow = ExcelAssistant.loadCoefficientMap("input/reg_education_low.xls", ((LabourParticipationModel) SimulationEngine.getInstance().getManager(LabourParticipationModel.class.getCanonicalName())).getCountry().toString(), 1, 4);		//TODO: Why do we get Null Pointer Exceptions here?  Need to debug the regressions?
				coeffEducationHigh = ExcelAssistant.loadCoefficientMap("input/reg_education_high.xls", ((LabourParticipationModel) SimulationEngine.getInstance().getManager(LabourParticipationModel.class.getCanonicalName())).getCountry().toString(), 1, 4);

				if(bootstrapAll) {
					if(bootstrapEducation) {
//						coeffEducationLow = RegressionUtils.bootstrap(coeffEducationLow);
//						coeffEducationHigh = RegressionUtils.bootstrap(coeffEducationHigh);

						coeffEducationLowHighCombined = ExcelAssistant.loadCoefficientMap("input/reg_education_lowHighCombined.xls", ((LabourParticipationModel) SimulationEngine.getInstance().getManager(LabourParticipationModel.class.getCanonicalName())).getCountry().toString(), 1, 17);
						coeffEducationLowHighCombined = RegressionUtils.bootstrap(coeffEducationLowHighCombined);
						//Need to partition coeffEducationLowHighCombined into separate Low and High MultiKeyCoefficientMaps
						String[] keyNames = new String[1];
						keyNames[0] = RegressionColumnNames.REGRESSOR.toString();
						String[] valueNames = new String[1];
						valueNames[0] = RegressionColumnNames.COEFFICIENT.toString();
						coeffEducationLow = new MultiKeyCoefficientMap(keyNames, valueNames);
						coeffEducationHigh = new MultiKeyCoefficientMap(keyNames, valueNames);				
						for (MapIterator iterator = coeffEducationLowHighCombined.mapIterator(); iterator.hasNext();) {
							iterator.next();
							
							MultiKey multiKey = (MultiKey) iterator.getKey();
							String regressor = (String) multiKey.getKey(0);		//Requires regressor names to first key entry in MultiKey
							if(regressor.startsWith("Low_")) {
								MultiKey regressorWithoutLowHigh = new MultiKey(new Object[]{regressor.substring(4)});
								coeffEducationLow.put(regressorWithoutLowHigh, coeffEducationLowHighCombined.get(multiKey));						
							}
							else if(regressor.startsWith("High_")) {
								MultiKey regressorWithoutLowHigh = new MultiKey(new Object[]{regressor.substring(5)});
								coeffEducationHigh.put(regressorWithoutLowHigh, coeffEducationLowHighCombined.get(multiKey));
							}
							else throw new IllegalArgumentException("Regressor does not contain Low or High label required to determine the appropriate MultiKeyCoefficientMap in which to put the corresponding coefficient in, during the creation of the education MultiProbitRegression.");
						}		
					}
				}
				
				
				//Now create MultiProbitRegression object:
				HashMap<Education, MultiKeyCoefficientMap> educationCoefficientMap = new HashMap<Education, MultiKeyCoefficientMap>();
				educationCoefficientMap.put(Education.Low, coeffEducationLow);
				educationCoefficientMap.put(Education.High, coeffEducationHigh);
				regEducationLevel = new MultiProbitRegression<Education>(educationCoefficientMap);	
				break;
			case TwoStageProbit:	//Regression co-efficients specific to TwoStageProbit case are not currently bootstrapped
				MultiKeyCoefficientMap coeffEducationMedium = ExcelAssistant.loadCoefficientMap("input/reg_education_medium.xls", ((LabourParticipationModel) SimulationEngine.getInstance().getManager(LabourParticipationModel.class.getCanonicalName())).getCountry().toString(), 1, 1);
				MultiKeyCoefficientMap coeffEducationHigh = ExcelAssistant.loadCoefficientMap("input/reg_education_high.xls", ((LabourParticipationModel) SimulationEngine.getInstance().getManager(LabourParticipationModel.class.getCanonicalName())).getCountry().toString(), 1, 1);
//				MultiKeyCoefficientMap coeffEducationMediumRaw = ExcelAssistant.loadCoefficientMap("input/reg_education_medium.xls", ((LabourParticipationModel) SimulationEngine.getInstance().getManager(LabourParticipationModel.class.getCanonicalName())).getCountry().toString(), 1, 3);
//				MultiKeyCoefficientMap coeffEducationHighRaw1 = ExcelAssistant.loadCoefficientMap("input/reg_education_high.xls", ((LabourParticipationModel) SimulationEngine.getInstance().getManager(LabourParticipationModel.class.getCanonicalName())).getCountry().toString(), 1, 3);
//				coeffEducationMedium = RegressionUtils.bootstrap(coeffEducationMediumRaw);
//				coeffEducationHigh = RegressionUtils.bootstrap(coeffEducationHighRaw1);
				regEducationMedium = new ProbitRegression(coeffEducationMedium);
				regEducationHigh = new ProbitRegression(coeffEducationHigh);
				break;
			case Graduation:		//Regression co-efficients specific to Graduation case are not currently bootstrapped
				MultiKeyCoefficientMap coeffSchoolingLow = ExcelAssistant.loadCoefficientMap("input/reg_schooling_low.xls", ((LabourParticipationModel) SimulationEngine.getInstance().getManager(LabourParticipationModel.class.getCanonicalName())).getCountry().toString(), 1, 1);
				MultiKeyCoefficientMap coeffSchoolingMedium = ExcelAssistant.loadCoefficientMap("input/reg_schooling_medium.xls", ((LabourParticipationModel) SimulationEngine.getInstance().getManager(LabourParticipationModel.class.getCanonicalName())).getCountry().toString(), 1, 1);
				MultiKeyCoefficientMap coeffGraduationMedium = ExcelAssistant.loadCoefficientMap("input/reg_graduation_medium.xls", ((LabourParticipationModel) SimulationEngine.getInstance().getManager(LabourParticipationModel.class.getCanonicalName())).getCountry().toString(), 1, 1);
				MultiKeyCoefficientMap coeffGraduationHigh = ExcelAssistant.loadCoefficientMap("input/reg_graduation_high.xls", ((LabourParticipationModel) SimulationEngine.getInstance().getManager(LabourParticipationModel.class.getCanonicalName())).getCountry().toString(), 1, 1);
//				MultiKeyCoefficientMap coeffSchoolingLowRaw = ExcelAssistant.loadCoefficientMap("input/reg_schooling_low.xls", ((LabourParticipationModel) SimulationEngine.getInstance().getManager(LabourParticipationModel.class.getCanonicalName())).getCountry().toString(), 1, 3);
//				MultiKeyCoefficientMap coeffSchoolingMediumRaw = ExcelAssistant.loadCoefficientMap("input/reg_schooling_medium.xls", ((LabourParticipationModel) SimulationEngine.getInstance().getManager(LabourParticipationModel.class.getCanonicalName())).getCountry().toString(), 1, 3);
//				MultiKeyCoefficientMap coeffGraduationMediumRaw = ExcelAssistant.loadCoefficientMap("input/reg_graduation_medium.xls", ((LabourParticipationModel) SimulationEngine.getInstance().getManager(LabourParticipationModel.class.getCanonicalName())).getCountry().toString(), 1, 3);
//				MultiKeyCoefficientMap coeffGraduationHighRaw = ExcelAssistant.loadCoefficientMap("input/reg_graduation_high.xls", ((LabourParticipationModel) SimulationEngine.getInstance().getManager(LabourParticipationModel.class.getCanonicalName())).getCountry().toString(), 1, 3);
//				coeffSchoolingLow = RegressionUtils.bootstrap(coeffSchoolingLowRaw);
//				coeffSchoolingMedium = RegressionUtils.bootstrap(coeffSchoolingMediumRaw);
//				coeffGraduationMedium = RegressionUtils.bootstrap(coeffGraduationMediumRaw);
//				coeffGraduationHigh = RegressionUtils.bootstrap(coeffGraduationHighRaw);
				regSchoolingLow = new ProbitRegression(coeffSchoolingLow);
				regSchoolingMedium = new ProbitRegression(coeffSchoolingMedium);
				regGraduationMedium = new ProbitRegression(coeffGraduationMedium);
				regGraduationHigh = new ProbitRegression(coeffGraduationHigh);
				break;
			default:
                System.err.println("Education model not poperly loaded.");
		}
			
		int columnsEmployment = -1;
		int columnsUnion = -1;
		int columnsParticipationMales = -1;
		int columnsSchooling = -1;
		
		if(country.equals(Country.ES)) {
			columnsEmployment = 16;
			columnsUnion = 17;
			columnsParticipationMales = 15;
			columnsSchooling = 11;			
		}
		else if(country.equals(Country.GR)) {
			columnsEmployment = 13;
			columnsUnion = 14;
			columnsParticipationMales = 12;
			columnsSchooling = 8;
		}
		else if(country.equals(Country.HU)) {
			columnsEmployment = 12;
			columnsUnion = 13;
			columnsParticipationMales = 11;
			columnsSchooling = 7;
		}
		else if(country.equals(Country.IE)) {
			columnsEmployment = 10;
			columnsUnion = 11;
			columnsParticipationMales = 9;
			columnsSchooling = 5;
		}
		else if(country.equals(Country.IT)) {
			columnsEmployment = 14;
			columnsUnion = 15;
			columnsParticipationMales = 13;
			columnsSchooling = 9;						
		}
		else if(country.equals(Country.SE)) {
			columnsEmployment = 12;
			columnsUnion = 13;
			columnsParticipationMales = 11;
			columnsSchooling = 7;
		}
		else throw new IllegalArgumentException("Country not recognised in Parameters.loadParameters()!");
		
		//The Raw maps contain the estimates and covariance matrices, from which we bootstrap at the start of each simulation
		coeffEmployment = ExcelAssistant.loadCoefficientMap("input/reg_employment.xls", ((LabourParticipationModel) SimulationEngine.getInstance().getManager(LabourParticipationModel.class.getCanonicalName())).getCountry().toString(), 1, columnsEmployment);
		coeffBirthFemales = ExcelAssistant.loadCoefficientMap("input/reg_birthFemales.xls", ((LabourParticipationModel) SimulationEngine.getInstance().getManager(LabourParticipationModel.class.getCanonicalName())).getCountry().toString(), 1, 47);
		coeffUnionFemales = ExcelAssistant.loadCoefficientMap("input/reg_unionFemales.xls", ((LabourParticipationModel) SimulationEngine.getInstance().getManager(LabourParticipationModel.class.getCanonicalName())).getCountry().toString(), 1, columnsUnion);
		coeffParticipationMales = ExcelAssistant.loadCoefficientMap("input/reg_participationMales.xls", ((LabourParticipationModel) SimulationEngine.getInstance().getManager(LabourParticipationModel.class.getCanonicalName())).getCountry().toString(), 1, columnsParticipationMales);
		coeffSchooling = ExcelAssistant.loadCoefficientMap("input/reg_schooling.xls", ((LabourParticipationModel) SimulationEngine.getInstance().getManager(LabourParticipationModel.class.getCanonicalName())).getCountry().toString(), 1, columnsSchooling);

		coeffParticipationFemales = ExcelAssistant.loadCoefficientMap("input/reg_participationFemales.xls", ((LabourParticipationModel) SimulationEngine.getInstance().getManager(LabourParticipationModel.class.getCanonicalName())).getCountry().toString(), 1, 1);	//Not bootstrapped, but bootstrapping is used in the separate female participation regressions (for females with no children, only children under 3, or only children between 4 and 12)
		coeffParticipationFemalesWithChildren3orUnder = ExcelAssistant.loadCoefficientMap("input/reg_participationFemalesWithChildren3orUnder.xls", ((LabourParticipationModel) SimulationEngine.getInstance().getManager(LabourParticipationModel.class.getCanonicalName())).getCountry().toString(), 1, 40);
		coeffParticipationFemalesWithOnlyChildren4orOver = ExcelAssistant.loadCoefficientMap("input/reg_participationFemalesWithOnlyChildren4orOver.xls", ((LabourParticipationModel) SimulationEngine.getInstance().getManager(LabourParticipationModel.class.getCanonicalName())).getCountry().toString(), 1, 43);
		coeffParticipationFemalesNoChildren = ExcelAssistant.loadCoefficientMap("input/reg_participationFemalesNoChildren.xls", ((LabourParticipationModel) SimulationEngine.getInstance().getManager(LabourParticipationModel.class.getCanonicalName())).getCountry().toString(), 1, 52);
		
		//Bootstrap the coefficients
		if(bootstrapAll) {
			coeffEmployment = RegressionUtils.bootstrap(coeffEmployment);		
			coeffBirthFemales = RegressionUtils.bootstrap(coeffBirthFemales);
			coeffUnionFemales = RegressionUtils.bootstrap(coeffUnionFemales);
			coeffParticipationMales = RegressionUtils.bootstrap(coeffParticipationMales);
			coeffSchooling = RegressionUtils.bootstrap(coeffSchooling);
		}
		
//		coeffParticipationFemales = RegressionUtils.bootstrap(coeffParticipationFemalesRaw);	//Not bootstrapped, but bootstrapping is used in the separate female participation regressions (for females with no children, only children under 3, or only children between 4 and 12)

		coeffParticipationFemalesWithChildren3orUnder = RegressionUtils.bootstrap(coeffParticipationFemalesWithChildren3orUnder);
		coeffParticipationFemalesWithOnlyChildren4orOver = RegressionUtils.bootstrap(coeffParticipationFemalesWithOnlyChildren4orOver);
		coeffParticipationFemalesNoChildren = RegressionUtils.bootstrap(coeffParticipationFemalesNoChildren);
			
		regEmployment = new ProbitRegression(coeffEmployment);
		regBirthFemales = new ProbitRegression(coeffBirthFemales);
		regUnionFemales = new ProbitRegression(coeffUnionFemales);
		regParticipationMales = new ProbitRegression(coeffParticipationMales);
		regSchooling = new ProbitRegression(coeffSchooling);		
		
		regParticipationFemales = new ProbitRegression(coeffParticipationFemales);
		regParticipationFemalesWithChildren3orUnder = new ProbitRegression(coeffParticipationFemalesWithChildren3orUnder);
		regParticipationFemalesWithOnlyChildren4orOver = new ProbitRegression(coeffParticipationFemalesWithOnlyChildren4orOver);
		regParticipationFemalesNoChildren = new ProbitRegression(coeffParticipationFemalesNoChildren);

		//Max Age now obtained from initial population
//		//Obtain the maximum Age modelled, from the ages used in the Eurostat population projections 
//		try {
//			for(Object multiKey : populationProjectionsFemale.keySet()) {		//This provides a way of obtaining the maximum possible Age from the mortality_rates.xls input file.  Beware that if the maximum Age is greater than the ages in the other input files, there will be NullPointer exceptions when, for example, getting values from the Age-income profile for Sims whose ages are not within the range in the age_income_profile.xls! 
//				final Object key = ((MultiKey) multiKey).getKey(0);
//				final Integer Age = Integer.parseInt(key.toString());
//				if(Age > MAX_AGE) {
//					MAX_AGE = Age;		
//				}
//			} 		
//		} catch (NumberFormatException nfe) {
////			System.out.println("NumberFormatException: " + nfe.getMessage());
//		}	
		
		calculateFertilityRatesFromProjections();

	}

	//For parameters that change every year
	public static void updateParameters(int year) {
		//Update educational level probabilities for people of minimum Age (as they are dependent on the simulation year)
		int count = 0;
		for(Education eduLevel : Education.values()) {			//This loop ensures events are properly aligned with probs
			probMinAgeEduEvents[count] = eduLevel;
			probMinAgeEduProbs[count] = ((Number) probMinAgeEduLevelMultiMap.getValue(year, eduLevel)).doubleValue();
			count++;
		}		
		
	}

	private static void calculateChildcareSpendingRegional() {
		String country = ((LabourParticipationModel) SimulationEngine.getInstance().getManager(LabourParticipationModel.class.getCanonicalName())).getCountry().toString();
		Integer startYear = ((LabourParticipationModel) SimulationEngine.getInstance().getManager(LabourParticipationModel.class.getCanonicalName())).getStartYear();
		Integer endYear = ((LabourParticipationModel) SimulationEngine.getInstance().getManager(LabourParticipationModel.class.getCanonicalName())).getEndYear();
		
//		childcare_spending_reg = MultiKeyMap.decorate(new LinkedMap());
		childcare_spending_reg = new MultiKeyMap();
		
		for(Integer year_ = startYear; year_ <= endYear; year_++) {
			for(Region region : Region.values()) {	
				if(region.toString().startsWith(country)) {			//Only assess the relevant regions for the country
					
					Double regionalSpending = ((Number) childcare_spending_national.getValue(country, year_)).doubleValue() *
							( ((Number) childcare_coverage_regional.getValue(region.toString(), year_)).doubleValue() /
							  ((Number) childcare_coverage_national.getValue(country, year_)).doubleValue()  );
					regionalSpending /= 1000.;									//Units are in thousands of PPP USD
					
					childcare_spending_reg.put(region.toString(), year_, regionalSpending);					
				}
			}	
		}
	}
		
	public static void calculateMinAgeRegionProbabilities(Gender gender, List<Person> persons) {
		 
		String country = ((LabourParticipationModel) SimulationEngine.getInstance().getManager(LabourParticipationModel.class.getCanonicalName())).getCountry().toString();
		
		//If numWithMinAgeAndGenderInRegion is not a LinkedHashMap but a HashMap instead, the simulation becomes non-deterministic because of the order of iteration by it's keySet() in the for loops in this method below. 
		LinkedHashMap<Region, Integer> numWithMinAgeAndGenderInRegion = new LinkedHashMap<Region, Integer>();
		int numWithMinAgeAndGender = 0;
		
		for(Region region : Region.values()) {	
			if(region.toString().startsWith(country)) {			//Only add the relevant regions for the country
				numWithMinAgeAndGenderInRegion.put(region, 0);		//Initialise to 0
			}
		}
		for(Person person : persons) {
			if( (person.getAge() == MIN_AGE) && person.getGender().equals(gender)) {		//Check that min Age and Gender are consistent
				numWithMinAgeAndGenderInRegion.put(person.getRegion(), numWithMinAgeAndGenderInRegion.get(person.getRegion())+1);		//Increment regionally
				
				numWithMinAgeAndGender++;			//Increment nationally
			} else {
				throw new IllegalArgumentException("Gender and Age not consistent in Parameters#calculateRegionProbabilities!");
			}
		}
		
		for(Region region : numWithMinAgeAndGenderInRegion.keySet()) {
			if(numWithMinAgeAndGenderInRegion.get(region).equals(0)) {
				throw new RuntimeException("WARNING: Region " + region + " does not have any " + gender.toString() + "s with the minimum Age of " + MIN_AGE + ".  There needs to be a positive number, in order for the simulation to create new " + gender.toString() + "s with the minimum Age!");
			}
		}
		
		if(gender.equals(Gender.Female)) {
			regionsInCountryForFemales = new Region[Region.values().length];
			regionsInCountryProbabilitiesForFemales = new double[Region.values().length];

			int count = 0;
			for(Region region : numWithMinAgeAndGenderInRegion.keySet()) {		//This loop ensures events are properly aligned with probs
				if(region.toString().startsWith(country)) {			//Only add the relevant regions for the country. The first 2 letters of each region correspond to the country enum 
					regionsInCountryForFemales[count] = region;
					regionsInCountryProbabilitiesForFemales[count] = (double)numWithMinAgeAndGenderInRegion.get(region) / (double)numWithMinAgeAndGender;			//Probabilities assumed constant over time (not year dependent)
//					System.out.println("region " + region.toString() + " has prob for females " + regionsInCountryProbabilitiesForFemales[count]);
					count++;

				}
			}	
		} else if(gender.equals(Gender.Male)){
			regionsInCountryForMales = new Region[Region.values().length];
			regionsInCountryProbabilitiesForMales = new double[Region.values().length];

			int count = 0;
			for(Region region : numWithMinAgeAndGenderInRegion.keySet()) {		//This loop ensures events are properly aligned with probs
				if(region.toString().startsWith(country)) {			//Only add the relevant regions for the country. The first 2 letters of each region correspond to the country enum 
					regionsInCountryForMales[count] = region;
					regionsInCountryProbabilitiesForMales[count] = (double)numWithMinAgeAndGenderInRegion.get(region) / (double)numWithMinAgeAndGender;			//Probabilities assumed constant over time (not year dependent)
//					System.out.println("region " + region.toString() + " has prob for males " + regionsInCountryProbabilitiesForMales[count]);
					count++;
				}
			}	
		} else {
			throw new IllegalArgumentException("Gender argument is not Female or Male in Parameters#calculateMinAgeRegionProbabilities!");
		}
		
	}
	
	private static void calculateFertilityRatesFromProjections() {
		
		int endYear = ((LabourParticipationModel) SimulationEngine.getInstance().getManager(LabourParticipationModel.class.getCanonicalName())).getEndYear();
		int startYear = ((LabourParticipationModel) SimulationEngine.getInstance().getManager(LabourParticipationModel.class.getCanonicalName())).getStartYear();

		
		fertilityRateInYear = new HashMap<Integer, Double>(endYear - startYear + 1);
		for(int year = startYear; year <= endYear; year++) {
			
			int projectedNumPuerperas = 0;
			for(int age = MIN_AGE_MATERNITY; age <= MAX_AGE_MATERNITY; age++) {
				projectedNumPuerperas += ((Number)populationProjectionsFemale.getValue(age, year)).intValue();
			}

			int numNewBorn = ((Number)populationProjectionsFemale.getValue(0, year)).intValue() + ((Number)populationProjectionsMale.getValue(0, year)).intValue();		//Number of people aged 0 in projected years
			
			if(projectedNumPuerperas <= 0) {
				throw new IllegalArgumentException("Projected Number of Females of Fertile Age is not positive!");
			} else {
				fertilityRateInYear.put(year, (double)numNewBorn / (double)projectedNumPuerperas);	
			}
		}
		
	}
	
	
	//-----------------------------------------------------------------------------------------------------
	// Access methods
	//-----------------------------------------------------------------------------------------------------
	

	public static MultiKeyCoefficientMap getPartTimeRates() {
		return partTimeRates;
	}

	public static MultiKeyCoefficientMap getRetirementAge() {
		return retirementAge;
	}

	public static MultiKeyCoefficientMap getUnemploymentRates() {
		return unemploymentRates;
	}

	public static MultiKeyCoefficientMap getPopulationProjectionsMale() {
		return populationProjectionsMale;
	}

	public static MultiKeyCoefficientMap getPopulationProjectionsFemale() {
		return populationProjectionsFemale;
	}

	public static MultiProbitRegression<Education> getRegEducationLevel() {
		return regEducationLevel;
	}

	public static ProbitRegression getRegSchooling() {
		return regSchooling;
	}
	
	public static ProbitRegression getRegSchoolingLow() {
		return regSchoolingLow;
	}

	public static ProbitRegression getRegSchoolingMedium() {
		return regSchoolingMedium;
	}

	public static ProbitRegression getRegEducationMedium() {
		return regEducationMedium;
	}

	public static ProbitRegression getRegEducationHigh() {
		return regEducationHigh;
	}

	public static ProbitRegression getRegGraduationMedium() {
		return regGraduationMedium;
	}
	
	public static ProbitRegression getRegGraduationHigh() {
		return regGraduationHigh;
	}
	
	public static ProbitRegression getRegFemalesBirth() {
		return regBirthFemales;
	}

	public static ProbitRegression getRegFemalesUnion() {
		return regUnionFemales;
	}

	public static ProbitRegression getRegParticipationFemales() {
		return regParticipationFemales;
	}

	public static ProbitRegression getRegParticipationMales() {
		return regParticipationMales;
	}
	
	public static ProbitRegression getRegEmployment() {
		return regEmployment;
	}


	public static int getMaxAge() {
		return MAX_AGE;
	}
	

	public static int getMinAge() {
		return MIN_AGE;
	}
	
	public static void setMaxAge(int maxAge) {
		MAX_AGE = maxAge;
		
	}

	public static void setMinAge(int minAge) {
		MIN_AGE = minAge;
	}
	
	public static int getMaxAgeInEducation() {
		return MAX_AGE_IN_EDUCATION;
	}
	
	public static int getAgeThresholdEducation() {
		return AGE_THRESHOLD_EDUCATION;
	}

	public static int getMaxAgeGraduationMedium() {
		return MAX_AGE_GRADUATION_MEDIUM;
	}

	public static int getMinAgeGraduationHigh() {
		return MIN_AGE_GRADUATION_HIGH;
	}
	
	public static int getMinAgeMarriage() {
		return MIN_AGE_MARRIAGE;
	}

	public static int getMaxAgeMarriage() {
		return MAX_AGE_MARRIAGE;
	}

	public static int getMinAgeMaternity() {
		return MIN_AGE_MATERNITY;
	}

	public static int getMaxAgeMaternity() {
		return MAX_AGE_MATERNITY;
	}

	public static MultiKeyCoefficientMap getProbMinAgeActiveIfNotStudent() {
		return probMinAgeActiveIfNotStudent;
	}

	public static MultiKeyCoefficientMap getProbMinAgeEmployedIfActive() {
		return probMinAgeEmployedIfActive;
	}

	public static MultiKeyCoefficientMap getProb17eduLevel() {
		return probMinAgeEduLevelMultiMap;
	}


	public static Education[] getProbMinAgeEduEvents() {
		return probMinAgeEduEvents;
	}

	public static double[] getProbMinAgeEduProbs() {
		return probMinAgeEduProbs;
	}

	public static Region[] getRegionsInCountry() {
		return regionsInCountryForFemales;
	}

	public static double[] getRegionsInCountryProportions() {
		return regionsInCountryProbabilitiesForFemales;
	}

	public static MultiKeyCoefficientMap getProbMinAgeStudent() {
		return probMinAgeStudent;
	}

	public static MultiKeyCoefficientMap getOnleave_benefits() {
		return onleave_benefits;
	}

//	public static MultiKeyCoefficientMap getProbRegions() {
//		return probRegions;
//	}

	public static Region[] getRegionsInCountryForFemales() {
		return regionsInCountryForFemales;
	}

	public static double[] getRegionsInCountryProbabilitiesForFemales() {
		return regionsInCountryProbabilitiesForFemales;
	}

	public static Region[] getRegionsInCountryForMales() {
		return regionsInCountryForMales;
	}

	public static double[] getRegionsInCountryProbabilitiesForMales() {
		return regionsInCountryProbabilitiesForMales;
	}

	public static HashMap<Integer, Double> getFertilityRateInYear() {
		return fertilityRateInYear;
	}

	public static MultiKeyMap getChildcare_spending_reg() {
		return childcare_spending_reg;
	}

	public static MultiKeyCoefficientMap getCrisisStrengthAdjustment() {
		return crisisStrengthAdjustment;
	}

	public static MultiKeyCoefficientMap getHighEducationRateInYear() {
		return projectionsHighEdu;
	}

	public static MultiKeyCoefficientMap getMediumEducationRateInYear() {
		return projectionsMediumEdu;
	}
	
	public static MultiKeyCoefficientMap getBenefitsPerChildByAge() {
		return benefitsPerChildByAge;
	}
	
	public static MultiKeyCoefficientMap getChildrenGapFactors() {
		return childrenGapFactors;
	}

	public static ProbitRegression getRegParticipationFemalesWithChildren3orUnder() {
		return regParticipationFemalesWithChildren3orUnder;
	}

	public static ProbitRegression getRegParticipationFemalesWithOnlyChildren4orOver() {
		return regParticipationFemalesWithOnlyChildren4orOver;
	}

	public static ProbitRegression getRegParticipationFemalesNoChildren() {
		return regParticipationFemalesNoChildren;
	}

	public static MultiKeyCoefficientMap getStudentShareProjections() {
		return studentShareProjections;
	}

	public static MultiKeyCoefficientMap getFemaleUnionUnder45ShareProjections() {
		return unionShareProjections;
	}


}
