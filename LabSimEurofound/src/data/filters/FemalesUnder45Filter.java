package data.filters;

import org.apache.commons.collections4.Predicate;

import model.Person;
import model.enums.Gender;

public class FemalesUnder45Filter<T extends Person> implements Predicate<T> {

	@Override
	public boolean evaluate(T agent) {
		
		return ( (agent.getGender().equals(Gender.Female)) &&
//				( !agent.getActivity_status().equals(Activity_status.Student) || agent.isToLeaveSchool() ) &&	//Alignment rate for sweden includes many students who are married, so cannot filter out non-students in the alignment algorithm
				( agent.getAge() < 45 )		//Strict inequality here, to coincide with age band in the GUI chart of Cohabitiing Females
				);
				
	}


}
