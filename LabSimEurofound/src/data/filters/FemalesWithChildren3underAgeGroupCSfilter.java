package data.filters;

import microsim.statistics.ICollectionFilter;
import model.Person;
import model.enums.Gender;
import model.enums.Indicator;

public class FemalesWithChildren3underAgeGroupCSfilter implements ICollectionFilter{
	
	private int ageFrom;
	private int ageTo;
	
	public FemalesWithChildren3underAgeGroupCSfilter(int ageFrom, int ageTo) {
		super();
		this.ageFrom = ageFrom;
		this.ageTo = ageTo;
	}
	
	public boolean isFiltered(Object object) {
		Person person = (Person) object;
		return ( person.getGender().equals(Gender.Female) && 
				(person.getAge() >= ageFrom) && (person.getAge() <= ageTo) && 
				person.getD_children_3under().equals(Indicator.True)
				);
	}
	
}

