package data.filters;

import org.apache.commons.collections4.Predicate;

import data.Parameters;
import model.Person;
import model.enums.Activity_status;
import model.enums.Gender;

public class FertileFilter<T extends Person> implements Predicate<T> {

	@Override
	public boolean evaluate(T agent) {
		
		int age = agent.getAge();
		
		return ( (agent.getGender().equals(Gender.Female)) &&
				( !agent.getActivity_status().equals(Activity_status.Student) || agent.isToLeaveSchool() ) &&
				( age >= Parameters.getMinAgeMaternity() ) &&
				( age <= Parameters.getMaxAgeMaternity() )
				);
	}


}
