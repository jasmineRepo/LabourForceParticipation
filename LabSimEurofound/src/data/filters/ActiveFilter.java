package data.filters;

import org.apache.commons.collections.Predicate;

import model.Person;
import model.enums.Activity_status;

public class ActiveFilter implements Predicate {

	@Override
	public boolean evaluate(Object object) {
		
		Person agent = (Person) object;
		return agent.getActivity_status().equals(Activity_status.Active);
		
	}

}
