package data.filters;

import microsim.statistics.ICollectionFilter;
import model.Person;
import model.enums.Gender;

public class FemaleAgeGroupCSfilter implements ICollectionFilter{
	
	private int ageFrom;
	private int ageTo;
	
	public FemaleAgeGroupCSfilter(int ageFrom, int ageTo) {
		super();
		this.ageFrom = ageFrom;
		this.ageTo = ageTo;
	}
	
	public boolean isFiltered(Object object) {
		Person person = (Person) object;
		return ( person.getGender().equals(Gender.Female) && (person.getAge() >= ageFrom) && (person.getAge() <= ageTo) );
	}
	
}
