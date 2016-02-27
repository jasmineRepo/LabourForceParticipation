package data.filters;

import microsim.statistics.ICollectionFilter;
import model.Person;

public class AgeGroupCSfilter implements ICollectionFilter{
	
	private int ageFrom;
	private int ageTo;
	
	public AgeGroupCSfilter(int ageFrom, int ageTo) {
		super();
		this.ageFrom = ageFrom;
		this.ageTo = ageTo;
	}
	
	public boolean isFiltered(Object object) {
		Person person = (Person) object;
		return ( (person.getAge() >= ageFrom) && (person.getAge() <= ageTo) );
	}
	
}
