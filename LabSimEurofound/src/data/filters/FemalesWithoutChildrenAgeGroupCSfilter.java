package data.filters;

import microsim.statistics.ICollectionFilter;
import model.Person;
import model.enums.Gender;
import model.enums.Indicator;

public class FemalesWithoutChildrenAgeGroupCSfilter implements ICollectionFilter{
	
	private int ageFrom;
	private int ageTo;
	
	public FemalesWithoutChildrenAgeGroupCSfilter(int ageFrom, int ageTo) {
		super();
		this.ageFrom = ageFrom;
		this.ageTo = ageTo;
	}
	
	public boolean isFiltered(Object object) {
		Person person = (Person) object;
		return ( person.getGender().equals(Gender.Female) && 
				(person.getAge() >= ageFrom) && (person.getAge() <= ageTo) && 
				!( person.getD_children_3under().equals(Indicator.True) || person.getD_children_4_12().equals(Indicator.True) )
				);
	}
	
}

