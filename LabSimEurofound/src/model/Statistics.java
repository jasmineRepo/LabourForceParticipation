package model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import microsim.data.db.PanelEntityKey;

@Entity
public class Statistics {

	@Id
	private PanelEntityKey key = new PanelEntityKey(1L);

	@Column(name="avg_participation_of_males_aged_15_to_64")
	private double avgPartRateMales15_64;
	
	@Column(name="avg_participation_of_males_aged_20_to_64")
	private double avgPartRateMales20_64;
	
	@Column(name="avg_participation_of_females_aged_15_to_64")
	private double avgPartRateFemales15_64;
	
	@Column(name="avg_participation_of_females_aged_20_to_64")
	private double avgPartRateFemales20_64;
	
	@Column(name="avg_participation_of_females_aged_20_to_44")
	private double avgPartRateFemales20_44;
	
	@Column(name="avg_participation_of_females_aged_20_to_44_with_children_aged_3_or_below")
	private double avgPartRateFemales20_44withChildren3under;
	
	@Column(name="avg_participation_of_females_aged_20_to_44_with_children")
	private double avgPartRateFemales20_44withChildren;
	
	@Column(name="avg_participation_of_females_aged_20_to_44_without_children")
	private double avgPartRateFemales20_44withoutChildren;

	@Column(name="avg_participation_of_females_aged_20_to_44_with_low_education")
	private double avgPartRateFemales20_44lowEducation;

	@Column(name="avg_participation_of_females_aged_20_to_44_with_high_education")
	private double avgPartRateFemales20_44highEducation;

	@Column(name="avg_participation_of_females_aged_20_to_44_with_children_and_low_education")
	private double avgPartRateFemales20_44withChildrenLowEducation;

	@Column(name="avg_participation_of_females_aged_20_to_44_with_children_and_high_education")
	private double avgPartRateFemales20_44withChildrenHighEducation;

	@Column(name="avg_employment_rate_of_males_aged_20_to_64")
	private double avgEmplRateMales20_64;
	
	@Column(name="avg_employment_rate_of_females_aged_20_to_64")
	private double avgEmplRateFemales20_64;
	
	
	public double getAvgPartRateMales15_64() {
		return avgPartRateMales15_64;
	}

	public void setAvgPartRateMales15_64(double avgPartRateMales15_64) {
		this.avgPartRateMales15_64 = avgPartRateMales15_64;
	}

	
	public double getAvgPartRateMales20_64() {
		return avgPartRateMales20_64;
	}

	public void setAvgPartRateMales20_64(double avgPartRateMales20_64) {
		this.avgPartRateMales20_64 = avgPartRateMales20_64;
	}

	public double getAvgPartRateFemales15_64() {
		return avgPartRateFemales15_64;
	}

	public void setAvgPartRateFemales15_64(double avgPartRateFemales15_64) {
		this.avgPartRateFemales15_64 = avgPartRateFemales15_64;
	}




	public double getAvgPartRateFemales20_64() {
		return avgPartRateFemales20_64;
	}




	public void setAvgPartRateFemales20_64(double avgPartRateFemales20_64) {
		this.avgPartRateFemales20_64 = avgPartRateFemales20_64;
	}




	public double getAvgPartRateFemales20_44() {
		return avgPartRateFemales20_44;
	}




	public void setAvgPartRateFemales20_44(double avgPartRateFemales20_44) {
		this.avgPartRateFemales20_44 = avgPartRateFemales20_44;
	}




	public double getAvgPartRateFemales20_44withChildren() {
		return avgPartRateFemales20_44withChildren;
	}




	public void setAvgPartRateFemales20_44withChildren(
			double avgPartRateFemales20_44withChildren) {
		this.avgPartRateFemales20_44withChildren = avgPartRateFemales20_44withChildren;
	}




	public double getAvgPartRateFemales20_44withoutChildren() {
		return avgPartRateFemales20_44withoutChildren;
	}




	public void setAvgPartRateFemales20_44withoutChildren(
			double avgPartRateFemales20_44withoutChildren) {
		this.avgPartRateFemales20_44withoutChildren = avgPartRateFemales20_44withoutChildren;
	}




	public double getAvgPartRateFemales20_44withChildrenLowEducation() {
		return avgPartRateFemales20_44withChildrenLowEducation;
	}




	public void setAvgPartRateFemales20_44withChildrenLowEducation(
			double avgPartRateFemales20_44withChildrenLowEducation) {
		this.avgPartRateFemales20_44withChildrenLowEducation = avgPartRateFemales20_44withChildrenLowEducation;
	}




	public double getAvgPartRateFemales20_44withChildrenHighEducation() {
		return avgPartRateFemales20_44withChildrenHighEducation;
	}




	public void setAvgPartRateFemales20_44withChildrenHighEducation(
			double avgPartRateFemales20_44withChildrenHighEducation) {
		this.avgPartRateFemales20_44withChildrenHighEducation = avgPartRateFemales20_44withChildrenHighEducation;
	}

	public double getAvgPartRateFemales20_44withChildren3under() {
		return avgPartRateFemales20_44withChildren3under;
	}

	public void setAvgPartRateFemales20_44withChildren3under(
			double avgPartRateFemales20_44withChildren3under) {
		this.avgPartRateFemales20_44withChildren3under = avgPartRateFemales20_44withChildren3under;
	}

	public double getAvgPartRateFemales20_44lowEducation() {
		return avgPartRateFemales20_44lowEducation;
	}

	public void setAvgPartRateFemales20_44lowEducation(
			double avgPartRateFemales20_44lowEducation) {
		this.avgPartRateFemales20_44lowEducation = avgPartRateFemales20_44lowEducation;
	}

	public double getAvgPartRateFemales20_44highEducation() {
		return avgPartRateFemales20_44highEducation;
	}

	public void setAvgPartRateFemales20_44highEducation(
			double avgPartRateFemales20_44highEducation) {
		this.avgPartRateFemales20_44highEducation = avgPartRateFemales20_44highEducation;
	}

	public double getAvgEmplRateMales20_64() {
		return avgEmplRateMales20_64;
	}

	public void setAvgEmplRateMales20_64(double avgEmplRateMales20_64) {
		this.avgEmplRateMales20_64 = avgEmplRateMales20_64;
	}

	public double getAvgEmplRateFemales20_64() {
		return avgEmplRateFemales20_64;
	}

	public void setAvgEmplRateFemales20_64(double avgEmplRateFemales20_64) {
		this.avgEmplRateFemales20_64 = avgEmplRateFemales20_64;
	}

}
