# LabourForceParticipation

by Matteo Richiardi and Ross Richardson (2015)

NOTE: BEFORE RUNNING THE SIMULATIONS - the input database is stored in the input folder in zipped form as input.h2.zip as the unzipped file is over 100MB and cannot be stored on GitHub.  Please unzip this file and store the resulting input.h2.db file in the same input folder.  The simulation will not run until you do this!

-------------------------------------------------------------------------------

A microsimulation model developed for the project "Anticipating the future trend of female labour market participation and its impact on economic growth", prepared for the European Foundation for the Improvement of Living and Working Conditions as Lot 3 of "The gender and employment gap: Challenges and Solutions", awarded to LABORatorio Revelli Centre for Employment Studies, Moncalieri, Italy.

The microsimulation model aims at providing medium- and long-term projections of participation and employment rates for a selected number of Member States: Italy, Spain, Ireland, Hungary and Greece. These case studies have been selected because they are among the most problematic in terms of female participation and employment rates, and gender (in)equality. Sweden has also been included as a high participation benchmark.

The model receives as an input a representative sample of the population in each country, drawn from the 2012 wave of EU-SILC (2011 for Ireland, updated to 2012) – the last available wave at the moment when the model was implemented – plus the estimated coefficients and tables for the scenario parameters.

Individuals effectively enter the simulation at age 17, the first age observed in EU-SILC data. The initial population is then evolved forward in time from 2013 to 2050 according to the estimated coefficients and the scenario parameters. Time is discrete, with one period corresponding to one year: correspondingly, all models are discrete choice models (either probit or multinomial probit) with the outcome variable being the probability of occurrence of a given event/transition.

The microsimulation is composed of four different modules: (i) Demography, (ii) Education, (iii) Household composition, and (iv) Labour market. Each module is in turn composed of different processes, or sub-modules (more details on the specification of each module are given in Appendix B). In each period, agents first go through the Demographic module, which deals with evolving the population structure by age, gender and area, based on Eurostat official demographic projections. Then, individuals above an individual-specific age threshold retire. Retired individuals remain in the simulation until they die but nothing else happens to them. Students enter the Education module. If they remain in education, nothing else happens to them until the next period. If they exit education, they join the ranks of potentially active individuals. Females enter the Household composition module, where it is determined whether they form or remain in a union and whether they give birth to a child. Then, they join males in the Labour market module, where participation and employment are finally determined. 

The model simulates the following state variables of the individuals: age, gender, region, educational attainment, labour market status (student, employed, unemployed, retired or other inactive), cohabitation status (for females only) and number and age of children (for females only). 

For more details, see http://www.jas-mine.net/demo/labour-force-participation
