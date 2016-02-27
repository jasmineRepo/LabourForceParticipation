package data.filters;

import org.apache.commons.collections.Predicate;

import data.Parameters;
import model.Person;
import model.enums.Activity_status;
import model.enums.Gender;

public class FertileFilter implements Predicate {

	@Override
	public boolean evaluate(Object object) {
		
		Person agent = (Person) object;
		int age = agent.getAge();
		
		return ( (agent.getGender().equals(Gender.Female)) &&
				( !agent.getActivity_status().equals(Activity_status.Student) || agent.isToLeaveSchool() ) &&
				( age >= Parameters.getMinAgeMaternity() ) &&
				( age <= Parameters.getMaxAgeMaternity() )
				);
	}


}
