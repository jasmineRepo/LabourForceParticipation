package data.filters;

import org.apache.commons.collections4.Predicate;

import model.Person;
import model.enums.Activity_status;

public class ActiveFilter<T extends Person> implements Predicate<T> {

	@Override
	public boolean evaluate(T agent) {
		
		return agent.getActivity_status().equals(Activity_status.Active);
		
	}

}
