package data.filters;

import microsim.statistics.ICollectionFilter;
import model.Person;
import model.enums.Activity_status;
import model.enums.Education;
import model.enums.Gender;

public class FemalesAgeGroupEducationCSfilter implements ICollectionFilter{
	
	private int ageFrom;
	private int ageTo;
	private Education edu;
	
	public FemalesAgeGroupEducationCSfilter(int ageFrom, int ageTo, Education edu) {
		super();
		this.ageFrom = ageFrom;
		this.ageTo = ageTo;
		this.edu = edu;
	}
	
	public boolean isFiltered(Object object) {
		Person person = (Person) object;
		if( person.getEducation() == null ) return false;		//Better just to check on Education being null, rather than assuming anything about the Students.  In future models, it may be possible to go back to education after already working.
//		if(person.getActivity_status().equals(Activity_status.Student)) {		//Need to check they are not still students, otherwise education level is not defined
//			return false;
//		}
		else return ( person.getGender().equals(Gender.Female) && 
				(person.getAge() >= ageFrom) && (person.getAge() <= ageTo) && 
				( person.getEducation().equals(edu) )
				);
	}
	
}

