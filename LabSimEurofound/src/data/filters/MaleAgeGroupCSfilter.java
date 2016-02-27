package data.filters;

import microsim.statistics.ICollectionFilter;
import model.Person;
import model.enums.Gender;

public class MaleAgeGroupCSfilter implements ICollectionFilter{
	
	private int ageFrom;
	private int ageTo;
	
	public MaleAgeGroupCSfilter(int ageFrom, int ageTo) {
		super();
		this.ageFrom = ageFrom;
		this.ageTo = ageTo;
	}
	
	public boolean isFiltered(Object object) {
		Person person = (Person) object;
		return ( person.getGender().equals(Gender.Male) && (person.getAge() >= ageFrom) && (person.getAge() <= ageTo) );
	}
	
}

