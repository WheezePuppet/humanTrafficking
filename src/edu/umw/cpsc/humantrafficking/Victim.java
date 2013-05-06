package edu.umw.cpsc.humantrafficking;
 
import java.util.Random;
import sim.engine.*;
import sim.field.continuous.*;
import sim.util.*;
import com.vividsolutions.jts.geom.Coordinate;
import java.awt.Color;
import sim.portrayal.simple.OvalPortrayal2D;

/** 
 * A Victim represents a woman being abducted for sex trafficking. Every
 * individual woman in the simulation is represented as a Victim object,
 * even if that individual is not <i>currently</i> a victim (<i>e.g.</i>,
 * she may have escaped.)
 */

public class Victim implements Steppable{

    /** 
     * The annual probability that each victim will escape captivity.
     */
    public static final double PROB_ESCAPE = .01;
    
    /**
     * The "intercept" in a linear function mapping victim age (in years)
     * to probability of retirement.
     */ 
    public static final double RETIRE_PROB_INTERCEPT = -10.0;

    /**
     * The "slope" in a linear function mapping victim age (in years)
     * to probability of retirement.
     */ 
    public static final double RETIRE_PROB_SLOPE = 2.0;

    private double fineGrainedLong; 
    private double fineGrainedLat; 
    private int birthYear;
    private long chanceOfRetire;
    private String educationLevel;
    private String religion;
    private Location location;
    private boolean escapee; 
    private boolean invisibleNextYear;
    
    private double getRetireChance() {
        return RETIRE_PROB_INTERCEPT + getAge() * RETIRE_PROB_SLOPE;
    }

    /**
     * The maximum amount of horizontal or vertical jitter, in screen
     * pixels.
     */
    public final static double JITTER_MAX = 25.0;
    
    /**
     * Instantiate a new Victim object that has been acquired from the
     * local population at the Location passed. Schedule this new victim
     * to run on July 1st of the next year.
     */
    public Victim(Location l, SimState state){
        invisibleNextYear = false;
        HumanTraffickingWithUI.instance().myVictimPortrayal.
            setPortrayalForObject(this,
                new OvalPortrayal2D(java.awt.Color.RED,2,true));

        l.addVictimFromLocalPopulation(this);
        setLocation(l);
        int num = (int) Math.round(state.random.nextGaussian()*2.17+18.5); 
        birthYear = HumanTrafficking.instance().getYear() - num;

        makeVisibleAtRightPlace();

        // Victims will run on July 1st of each year.
        state.schedule.scheduleOnceIn(.5,this);
    }

    private void makeVisibleAtRightPlace() {
        Coordinate jittered = jitter(
            new Coordinate(fineGrainedLong, fineGrainedLat));
        
        HumanTrafficking.instance().victimsField.setObjectLocation(
            this, 
            new Double2D(jittered.x, jittered.y));
    }

    private void makeInvisible() { 
        HumanTraffickingWithUI.instance().myVictimPortrayal.
            setPortrayalForObject(this,null);
        HumanTrafficking.instance().victimsField.remove(this);
    }

    private void makeInvisibleNextYear() { 
        invisibleNextYear = true;
    }

    /**
     * Return the age of the victim (in years).
     */
    public int getAge(){
        int age = HumanTrafficking.instance().getYear() - birthYear;
        return age;
    }
    

    /**
     * Tell this Victim about her new location. This does <i>not</i> inform
     * the Location object that it has a new Victim; this is the
     * responsibility of the caller.
     */
    public void setLocation(Location l){
        location = l;
        if (location != null) {
            fineGrainedLong = location.getLong();
            fineGrainedLat = location.getLat();
            makeVisibleAtRightPlace();
        } else {
            makeInvisible();
        }
    }

    /** 
     * Take into account a victim's chance of escape and decide whether
     * they do escape this year. If so, sets their status to "escapee".
     */
    public void possiblyEscape(){
        double escape = (HumanTrafficking.instance().random.nextDouble());
        if(escape < PROB_ESCAPE){
            location.addEscapee(this);
            location.removeVictim(this);
            escapee = true;
            makeInvisibleNextYear();
        }
    }
    
    /**
     * Mark this victim "free" because of a forced immigration crackdown.
     */
    void setFree(){
        location.removeVictim(this);
        location.setPop(location.getPop() + 1);
        makeInvisibleNextYear();
    }
    
    /**
     * This function takes into account a victim's age and, if older than 30,
     * randomly determines whether they should be removed from the system.
     */
    public void possiblyRetire(){
        int age = getAge();
        double chanceOfRetire = 
            (HumanTrafficking.instance().random.nextDouble());
        if(chanceOfRetire < getRetireChance()){
            location.removeVictim(this);
            location.setPop(location.getPop() + 1);
            makeInvisibleNextYear();
        }
    }

    /**
     * Return a Coordinate object that will be slightly (and randomly) 
     *   offset from the Coordinate object passed, so as to tweak a
     *   victim's location on screen.
     */
    private Coordinate jitter(Coordinate c) {
        HumanTrafficking world = HumanTrafficking.instance();
        double radius = world.random.nextDouble() * JITTER_MAX;
        double theta = world.random.nextDouble() * 2 * 3.14159;
        Coordinate jittered = new Coordinate(
            c.x + radius * Math.cos(theta),
            c.y + radius * Math.sin(theta));
        return jittered;
    }

    /**
     * Perform actions for this Victim this year. Victims are scheduled to
     * run on July 1st of each year. Their <code>step()</code> includes the
     * following items:
     * <ol>
     * <li>Decide whether this victim escapes from forced
     * prostitution.</li>
     * <li>Decide whether this victim "retires" (<i>i.e.</i>, is determined
     * to be too old for forced prostitution, and dumped from the
     * system.</li>
     * </ol>
     * @param state the HumanTrafficking simulation.
     */
    public void step(SimState state){
        if (invisibleNextYear) {
            makeInvisible();
        }
        //TODO: factor in chance of escape on each step based on 
        //education level and prevAbducted and religion: need separate method?
        //added some get methods for education level, abduction, and education
        this.possiblyEscape();
        this.possiblyRetire();

        // Run again one calendar year from now (next July 1st)
        state.schedule.scheduleOnceIn(1, this);
   }

}
