package data.filters;

import microsim.statistics.ICollectionFilter;
import model.Person;
import model.enums.Gender;

public class FemaleCSfilter implements ICollectionFilter{
	
	public boolean isFiltered(Object object) {
		return ( ((Person) object).getGender().equals(Gender.Female) );
	}
	
}
