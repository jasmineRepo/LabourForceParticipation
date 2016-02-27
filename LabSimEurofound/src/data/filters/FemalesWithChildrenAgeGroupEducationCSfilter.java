package data.filters;

import microsim.statistics.ICollectionFilter;
import model.Person;
import model.enums.Education;
import model.enums.Gender;
import model.enums.Indicator;

public class FemalesWithChildrenAgeGroupEducationCSfilter implements ICollectionFilter{
	
	private int ageFrom;
	private int ageTo;
	private Education edu;
	
	public FemalesWithChildrenAgeGroupEducationCSfilter(int ageFrom, int ageTo, Education edu) {
		super();
		this.ageFrom = ageFrom;
		this.ageTo = ageTo;
		this.edu = edu;
	}
	
	public boolean isFiltered(Object object) {
		Person person = (Person) object;
		return ( person.getGender().equals(Gender.Female) && 
				(person.getAge() >= ageFrom) && (person.getAge() <= ageTo) && 
				( person.getD_children_3under().equals(Indicator.True) || person.getD_children_4_12().equals(Indicator.True) ) &&
				( person.getEducation().equals(edu))
				);
	}
	
}

