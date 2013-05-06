package edu.umw.cpsc.humantrafficking;

import java.util.ArrayList;
import java.util.Hashtable;
import ec.util.MersenneTwisterFast;
import java.io.BufferedWriter;
import java.io.FileWriter;

/** 
 * Locations represent places where human trafficking victims can be
 * present. They may be sources (where victims are abducted), destinations
 * (where they are transported to serve as prostitutes), and/or transit
 * locations (intermediate points on a route).  A Location object keeps
 * track of the location's victims, demand and export rate. It has its own
 * function to schedule itself for the next step.
 */
public class Location {

    /**
     * The annual probability that an immigration search in this location
     * will be successful this year, (possibly) resulting in existing
     * victims being set free.
     */
    public static final double PROB_IMMIGRATION_SEARCH_SUCCESS = .01;

    private double longitude;
    private double latitude;
    private int population;
    private double povertyRate;
    private double employmentRate;
    private double genderRatio;
    private double appAgeRatio;
    private Hashtable<String, Double> religions;
    private double demand;
    private String name;    
    private double exportRate;
    private ArrayList<Victim> victims = new ArrayList<Victim>();
    private ArrayList<Victim> escapees = new ArrayList<Victim>();
    private MersenneTwisterFast generator = 
        HumanTrafficking.instance().random;
    
    
    private static final int PRIME_MERIDIAN_PIXEL = 360;
    private static final int EQUATOR_PIXEL = 335;
    private static final int TOP_MAP_PIXEL = 0;
    private static final int BOTTOM_MAP_PIXEL = 600;
    private static final int LEFT_MAP_PIXEL = 0;
    private static final int RIGHT_MAP_PIXEL = 800;

    /**
     * Constructor to instantiate new Location objects.
     * @param name the name of the location (may be multiple words; should be
     * globally unique across all Location objects).
     * @param latN the location's latitude in degrees (positive numbers
     * indicate "North", negative "South").
     * @param longE the location's longitude in degrees (positive numbers
     * indicate "East", negative "West").
     * @param population the number of <i>non</i>-victims, in individuals.
     * @param genderRatio a number from 0 to 1, indicating the fraction of
     * females in the population.
     * @param appAgeRatio a number from 0 to 1, indicating the fraction of
     * humans of "prostitution age" (12-30).
     */
    public Location(String name, double latN, double longE, int population, 
        double genderRatio, double appAgeRatio) {

        this.name = name;
        this.longitude = 
            (RIGHT_MAP_PIXEL - LEFT_MAP_PIXEL) * (longE)/360 +
            PRIME_MERIDIAN_PIXEL;
        this.latitude = (BOTTOM_MAP_PIXEL - TOP_MAP_PIXEL)*(-latN)/360 +
            EQUATOR_PIXEL;
        this.population = population;
        this.genderRatio = genderRatio;
        this.appAgeRatio = appAgeRatio;
        System.out.println("Just instantiated " + name + 
            " with a population of " + population + " and " +
            getNumVics() + " victims.");
    }

    /**
     * Add a victim to this Location's collection of victims and removes 
     * them from the current population. This function is used when 
     * instantiating new victims.
     * @param v a Victim.
     */
    public void addVictimFromLocalPopulation(Victim v){
        victims.add(v);
        population --;
    }
    
    /**
     * Return the name of this location (possibly containing spaces).
     */
    public String getName(){
        return name;
    }

    /**
     * Add a victim to this Location, and changes that victim's location. 
     * This is called when victims are moved from one location to another.
     * @param v a Victim.
     */
    public void addVictimFromExternalLocation(Victim v) {
        victims.add(v);
        v.setLocation(this);
    }
    
    /**
     * Add a victim as an "escapee" in this location.
     */
    public void addEscapee(Victim v){
        escapees.add(v);
    }
    
    /**
     * Remove a victim from this location's list of "escapees".
     */
    public void removeEscapee(Victim v){
        escapees.remove(v);
    }
    
    /**
     * Return the <i>current</i> victims at this location (<i>i.e.</i>,
     * <i>not</i> including escapees.)
     */
    public ArrayList<Victim> getVics(){
        return victims;
    }

    /**
     * Removes a victim from this location. The victim is <i>not</i> added
     * back to the local population.
     * @param v a Victim to be removed. If the Victim is not currently in
     * this Location, this method has no effect.
     */
    public void removeVictim(Victim v){
        victims.remove(v);
    }
    
    /**
     * Return the longitude of this location, in degrees (positive numbers
     * mean "East", negative "West".)
     */
    public double getLong(){
        return longitude;
    }

    /**
     * Return the latitude of this location, in degrees (positive numbers
     * mean "North", negative "South".)
     */
    public double getLat(){
        return latitude;
    }

    /**
     * Returns the number of Victims currently at this location (and in
     *   captivity.)
     */
    public int getNumVics(){
        return victims.size();
    }
    
    /**
     * Returns the number of Victims currently having escaped from this
     * location (and vulnerable to being reacquired from here.)
     */
    public int getNumEscapees(){
        return escapees.size();
    }

    /**
     * Take a random Victim at this location, remove her from this
     * location, and return her to the caller. The Victim's location will
     * be (temporarily) set to null as a result.
     */
    public Victim extractRandomVic() {

        int vicNum = HumanTrafficking.instance().random.nextInt(
            getNumVics());
        Victim extractedVic = victims.get(vicNum);

        // temporarily set the location to null, to indicate she's in the
        // process of moving locations.
        extractedVic.setLocation(null);
        victims.remove(extractedVic);
        return extractedVic;
    }

    /**
     * Take a random Escapee at this location, remove her from this
     * location, and return her to the caller. The Victim's location will
     * be (temporarily) set to null as a result.
     */
    public Victim extractRandomEscapee() {

        int escNum = HumanTrafficking.instance().random.nextInt(
            getNumEscapees());
        Victim extractedEscapee = escapees.get(escNum);

        // temporarily set the location to null, to indicate she's in the
        // process of moving locations.
        extractedEscapee.setLocation(null);
        escapees.remove(extractedEscapee);
        return extractedEscapee;
    }
    
    
    /**
     * Returns the number of non-Victims, but "eligible" potential Victims,
     *   currently at this location (not in captivity.)
     */
    public int getPop(){
        return population;   
    }
    
    void setPop(int population){
        this.population = population;
    }

    
    /**
     * Determine whether an immigration search in this location is
     * successful this year, and if so, set a random number of its victims 
     * free.
     */
    public void performImmigrationSearch(){
        double imm = (HumanTrafficking.instance().random.nextDouble());
        if(imm < PROB_IMMIGRATION_SEARCH_SUCCESS){
            if(getNumVics() != 0){
                int vicNum = HumanTrafficking.instance().random.nextInt(
                    getNumVics());
                Victim extractedVic = victims.get(vicNum);

                extractedVic.setFree();
            }
        }
    }

    /**
     * TODO
     * Generates demand based off of factors in the location.
     * Returns a number in the range [0.9,1.1] indicating a multiplicative
     * factor for victims on this route.
     * @return a multiplicative factor for victims on this route. Numbers
     * greater than 1 indicate a higher-than-usual demand on the
     * destination side, while numbers less than 1 indicate
     * lower-than-usual demand.
     */
    public double getDemand(){
        //calculate demand based off of external factors. Ex) military
        //or sporting events like the super bowl
        
        // For now, generate a random number (uniform) from .9 to 1.1 and
        // return that.
        // TODO


        demand = generator.nextDouble();
                
        return demand;
    }

    /**
     * Computes eligible victims based off of the properties at the current 
     * location.
     * @return the number of eligible victims at this location.
     */
    public int getEligibleVics(){
        int value = 0; 
        //actually compute eligible victims based off of properties of the 
        //current location.
        value = (int)
            (this.population * (this.genderRatio) * (this.appAgeRatio));
        return value;
    }

    /**
     * Return the name of this location.
     */
    public String toString() {
        return name;
    }

    /**
     * Print statistical information about this route to standard out.
     */
    public void printNumbers() {
        System.out.println("    (" + this + " now has " + 
            getNumVics() + (getNumVics() == 1 ? " victim, " : " victims, ") + 
            getNumEscapees() + 
                (getNumEscapees() == 1 ? " escapee" : " escapees") +
            " and population " + getPop() + ")");
    }
}
