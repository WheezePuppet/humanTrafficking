package edu.umw.cpsc.humantrafficking;

import ec.util.MersenneTwisterFast;
import java.lang.Object;
import java.util.ArrayList;
import sim.engine.*;
import sim.field.continuous.*;
import sim.util.*;
import sim.portrayal.simple.OvalPortrayal2D;

/**
 * A Route represents a known pathway of human trafficking victims
 * exploited by a trafficking network. Each Route has a source and
 * destination location, plus zero or more intermediate transit locations.
 * Routes know how to acquire victims, reacquire escapees, and move victims
 * from one location to another.
 */
public class Route implements Steppable{

    /** 
     * For each escaped victim, the annual probability that that victim
     * will be reacquired by the system.
     */
    public static final double PROB_REACQUIRE_VICTIM = .02;
    
    private long interference;
    private Location source;
    private Location destination;
    private Location transit;
    private MersenneTwisterFast generator = 
        HumanTrafficking.instance().random;

    // Estimated number of victims trafficked along this route annually.
    // The data is extremely messy and at times contradictory here. Note
    // that this may be 0, in which case we have no estimate.
    private int sourceVol;
    private int transitVol;
    private int destVol;
    
    /**
     * This constructor initializes a route with no transit locations.
     * @param source the source Location.
     * @param destination the destination Location.
     * @param sourceVol an estimate of the number of newly abducted victims 
     * per year this source location is likely to produce.
     * @param destVol an estimate of the number of victims per year this 
     * destination location is likely to acquire. This will be combined 
     * with <code>sourceVol</code> to determine an annual number of victims
     * along this route.
     */
    public Route(Location source, Location destination, 
        int sourceVol, int destVol){

        this.source = source;
        this.destination = destination;
        this.transit = null;
        this.sourceVol = sourceVol;
        this.destVol = destVol;
    }

    /**
     * This constructor initializes a route with one transit location.
     * (Currently the simulation doesn't support multiple transit locations
     * per route.)
     * @param source the source Location.
     * @param transit the (one) transit Location.
     * @param destination the destination Location.
     * @param sourceVol an estimate of the number of newly abducted victims 
     * per year this source location is likely to produce.
     * @param transitVol an estimate of the number of victims per year likely 
     * to be transported through this transit location. This will be
     * combined with <code>sourceVol</code> and <code>destVol</code> to
     * determine an annual number of victims along this route.
     * @param destVol an estimate of the number of victims per year this 
     * destination location is likely to acquire. This will be combined 
     * with <code>transitVol</code> and <code>sourceVol</code> to determine 
     * an annual number of victims.
     */
    public Route(Location source, Location transit, 
        Location destination, int sourceVol, int transitVol,
        int destVol){
        this.source = source;
        this.destination = destination;
        this.transit = transit;
        this.sourceVol = sourceVol;
        this.transitVol = transitVol;
        this.destVol = destVol;
    }

    private void acquireVictims(SimState state){

        HumanTrafficking world = (HumanTrafficking) state;
        
        int eligibleVics = source.getEligibleVics();
        double estYearlyVol = .01;
        if(transitVol == 0){
            estYearlyVol = ((sourceVol + destVol)/2);
        } else {
            estYearlyVol = ((sourceVol + destVol + transitVol)/3);
        }
        double outflow = (eligibleVics * (estYearlyVol/100));
        double randVicEst = (generator.nextGaussian()*.1+outflow);
        double demandedVics = destination.getDemand(); //edit demand (.9-1.1?)
        int thisYearVics = (int)(randVicEst * demandedVics);
        


        // TODO: Totally non-trivial calculation to determine how many 
        //   victims are abducted this year, based on the supply chain idea
        //   Stephen got at WSC 2012.
        int numAbductedVics = (int) 
            Math.round(thisYearVics/100);
        System.out.println("  Abducting " + numAbductedVics + " new " + 
            (numAbductedVics == 1 ? "victim" : "victims") + " from "
            + source + "...");

                
        // Instantiate this number of new Victims. (The Victim object is
        //   responsible for positioning itself on screen, adding itself to
        //   the source location, and scheduling itself to run.)
        for(int i=0; i<numAbductedVics; i++){
            Victim v = new Victim(source, state);
            if (source.getName().equals("Sri Lanka")) {
            HumanTraffickingWithUI.instance().myVictimPortrayal.
                setPortrayalForObject(v,
                    new OvalPortrayal2D(java.awt.Color.ORANGE,2,true));
            }
            if (source.getName().equals("Bangladesh")) {
            HumanTraffickingWithUI.instance().myVictimPortrayal.
                setPortrayalForObject(v,
                    new OvalPortrayal2D(java.awt.Color.BLUE,2,true));
            }
        }

        source.printNumbers();
    }

    private void moveVictims(SimState state){
        
        // TODO: Totally non-trivial calculation to determine how many 
        //   victims are moved this year, based on the supply chain idea
        //   Stephen got at WSC 2012.
        if(this.transit != null){

            // Step 1: Move victims from transit location (if any) to
            // destination location.
            double demand = destination.getDemand();
            int vics = transit.getNumVics();
            int numMoved = 
              (int) Math.round(destination.getDemand()*transit.getNumVics());
            double hold = demand*vics;
            
            
            if (transit.getNumVics() < numMoved) {
                int move = (int)Math.round(transit.getNumVics() * 0.9);   
                numMoved = move;
            }
            
            System.out.println("  Moving " + numMoved + 
                (numMoved == 1 ? " victim" : " victims") + " from " + 
                transit + " to " + destination + "... ... (dem=" + 
                destination.getDemand() + 
                ", src=" + transit.getNumVics() + ")");

            for(int i = 0; i < numMoved; i++){
                // Choose a random victim to move.
                Victim movedVic = transit.extractRandomVic();

                // Add her to the destination location. This will automatically
                // inform the Victim object of its new Location.
                destination.addVictimFromExternalLocation(movedVic);
                if (destination.getName().equals("Pakistan")) {
                HumanTraffickingWithUI.instance().myVictimPortrayal.
                    setPortrayalForObject(movedVic,
                        new OvalPortrayal2D(java.awt.Color.BLACK,2,true));
                }
                if (destination.getName().equals("Thailand")) {
                HumanTraffickingWithUI.instance().myVictimPortrayal.
                    setPortrayalForObject(movedVic,
                        new OvalPortrayal2D(java.awt.Color.MAGENTA,2,true));
                }
            }       

            transit.printNumbers();
            destination.printNumbers();

            // Step 2: Move victims from source location to transit
            // location.
            numMoved = 
               (int) Math.round(destination.getDemand()*source.getNumVics());

            if (source.getNumVics() < numMoved) {
                int move = (int)Math.round(source.getNumVics() * 0.9);
                numMoved = move;
            }
            
            System.out.println("  Moving " + numMoved + 
                (numMoved == 1 ? " victim" : " victims") + " from " + 
                source + " to " + transit + "...");

            for(int i = 0; i < numMoved; i++){
                // Choose a random victim to move.
                Victim movedVic = source.extractRandomVic();

                // Add her to the destination location. This will automatically
                // inform the Victim object of its new Location.
                transit.addVictimFromExternalLocation(movedVic);
                if (transit.getName().equals("India")) {
                HumanTraffickingWithUI.instance().myVictimPortrayal.
                    setPortrayalForObject(movedVic,
                        new OvalPortrayal2D(java.awt.Color.PINK,2,true));
                }
                if (transit.getName().equals("Myanmar")) {
                HumanTraffickingWithUI.instance().myVictimPortrayal.
                    setPortrayalForObject(movedVic,
                        new OvalPortrayal2D(java.awt.Color.CYAN,2,true));
                }
            }       
            
            source.printNumbers();
            transit.printNumbers();
            
        }else{
            int numMoved = 
                  (int) Math.round(destination.getDemand()*source.getNumVics());

            System.out.println("  Moving " + numMoved + 
                (numMoved == 1 ? " victim" : " victims") + " from " + 
                source + " to " + destination + "...");

            if (source.getNumVics() < numMoved) {
                numMoved = source.getNumVics();
            }

            for(int i = 0; i < numMoved; i++){
                // Choose a random victim to move.
                Victim movedVic = source.extractRandomVic();

                // Add her to the destination location. This will automatically
                // inform the Victim object of its new Location.
                destination.addVictimFromExternalLocation(movedVic);
                if (destination.getName().equals("South Korea")) {
                HumanTraffickingWithUI.instance().myVictimPortrayal.
                    setPortrayalForObject(movedVic,
                        new OvalPortrayal2D(java.awt.Color.BLACK,2,false));
                }
            }       

            source.printNumbers();
            destination.printNumbers();
        }
    }
    
    private void reacquireVictims(SimState state, Location l){
       //Much like acquireVictims, but takes into account the prevAbducted 
       //variable as a factor
        HumanTrafficking world = (HumanTrafficking) state;
        
        int numEscapees = l.getNumEscapees();
        int numReacquires = 0;
        
        for(int i=0; i<numEscapees; i++){
            if(HumanTrafficking.instance().random.nextDouble() < 
                    PROB_REACQUIRE_VICTIM){
                Victim v = l.extractRandomEscapee();
                l.addVictimFromExternalLocation(v);
                numReacquires++;
            }
        }
        System.out.println("  Reacquiring " + numReacquires +  
            (numEscapees == 1 ? " victim" : " victims") + " from "
            + l + "...");
        
        l.printNumbers();
    }
    
    /**
     * Perform actions for this Route this year. Routes are scheduled to be
     * run on Jan. 1st of each year. This includes the following main steps:
     * <ol>
     * <li>Acquire victims from the source location, based (mostly) on its 
     * current population and (somewhat) on the strength of the demand on 
     * the destination side.</il>
     * <li>Move victims along the route. If this route has a transit
     * location, no victim can move from source to destination immediately
     * in one year; the transit location will consume at least one year's 
     * time.</li>
     * <li>Possibly reacquire escaped victims from all locations that are
     * part of this route.</li>
     * </ol>
     * @param state the HumanTrafficking simulation.
     */
    public void step(SimState state){        

        System.out.println("Route::step(simtime=" +     
            ((HumanTrafficking)state).getSimTime() + "): " + this);
        this.acquireVictims(state);
        this.moveVictims(state);
        this.reacquireVictims(state, source);
        if(this.transit != null){
            this.reacquireVictims(state, transit);
        }
        this.reacquireVictims(state, destination);
        if(transit != null){
            // (Bethy's original demo: only in India)
            transit.performImmigrationSearch();
        }
        // (Bethy's original demo: only in Thailand)
        destination.performImmigrationSearch();
        
        // Schedule this route to run again in one calendar year.
        state.schedule.scheduleOnceIn(1,this);
    }

    /**
     * Return a human-readable string representing this Route.
     */
    public String toString() {
        if (transit == null) {
            return "from " + source + " to " + destination;
        } else {
            return "from " + source + " through " + transit +
                " to " + destination;
        }
    }
}
