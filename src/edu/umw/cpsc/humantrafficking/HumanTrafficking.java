package edu.umw.cpsc.humantrafficking;

import sim.engine.*;
import sim.field.continuous.*;
import java.util.Hashtable;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.Enumeration;

/**
 * A simulation of global human trafficking; specifically forced
 * prostitution.
 * Notes on Scheduling:
 * <ul>
 *   <li>The time for this simulation is in <i>years</i>. (<i>i.e.</i>, if 
 *     the current time step is 0.5, that represents July 1st, 2013.)</li>
 *   <li>This object will run once per year, on Dec. 31st, incrementing the
 *     value of getYear() so that objects scheduled to run immediately
 *     hereafter will see the new year.</li>
 *   <li>For other scheduling information, see {@link #start}.</li>
 * </ul>
 */
public class HumanTrafficking extends SimState implements Steppable {

    private static HumanTrafficking theInstance;

    Continuous2D victimsField = new Continuous2D(.1, 800, 600);

    private int year = 2013;
    private static final String ROUTE_FILE = "routes.txt";

    private Hashtable<String,Location> locations = 
        new Hashtable<String, Location> ();
    private ArrayList<Route> routes = new ArrayList<Route> ();

    /**
     * Singleton pattern.
     */
    public static synchronized HumanTrafficking instance(){
        if (theInstance == null) {
            theInstance = new HumanTrafficking(0);
        }
        return theInstance;
    }

    private void incrementYear() { 
        year++; 
    }

    private HumanTrafficking(long seed){
        super(seed);
    }

    /**
     * Begin the simulation, including everything necessary on the
     * schedule. In particular:
     * <ul>
     * <li>Each {@link Route} will run on Jan 1st of every year (including
     * <i>this</i> year, immediately after this method concludes).
     *  <ul>
     *  <li>When routes run, they will acquire {@link Victim}s, and those
     *  victims will run on July 1st of every year.</li> 
     *  </ul></li>
     * <li>The {@link StatsPrinter} will run on Dec. 30th of every year.</li>
     * <li>This object itself will run on Dec. 31st of every year.</li>
     * </ul>
     */
    public void start(){
        super.start();

        System.out.println("Loading route data...");
        try {
            loadRouteData();
        } catch (java.io.IOException e) {
            e.printStackTrace();
            System.out.println("Could not load route file " + ROUTE_FILE +
                    "?");
            System.exit(1);
        }
        System.out.println("----------------------------------------------");

        System.out.println("Performing initial scheduling...");
        // Schedule every route to run immediately when the simulation
        //   begins (i.e., January 1st of 2013.)
        scheduleRoutes();

        // Schedule this object to run on Dec. 31st of 2013.
        scheduleSelf();


        // Schedule the StatsPrinter to run on Dec. 30th of 2013.
        scheduleStatsPrinter();
        System.out.println("==============================================");
        System.out.println("It's now " + year + ".");
    }

    private void sceduleStatsPrinter(){
        schedule.scheduleOnce(364.0/365.0, StatsPrinter.instance());
        schedule.scheduleOnceIn(1, StatsPrinter.instance());
    }

    /**
     * This function schedules all routes. They are first scheduled beginning 
     * January 1st, 2013. Every route is then scheduled each succeeding year 
     * after that. 
     **/
    private void scheduleRoutes() {
        // All routes are scheduled to step at the beginning of Jan. 1st,
        //   2013. They will schedule themselves each succeeding year from 
        //   there.
        for (int i=0; i<routes.size(); i++) {
            schedule.scheduleOnce(0,routes.get(i));
        }
    }    

    /**
     * Run this simulation "headless"; <i>i.e.</i>, without a GUI.
     */
    public static void main(String[] args) throws Exception {
        doLoop(new MakesSimState() {
            public SimState newInstance(long seed, String[] args) {
                // you should actually be passing a seed here.
                return HumanTrafficking.instance(); 
            }

            public Class simulationClass() {
                return HumanTrafficking.class;
            }
        }, args);

        System.exit(0);
    }

    // For debug only.
    private void printRouteData() {
        for (int i=0; i<routes.size(); i++) {
            System.out.println(routes.get(i));
        }
    }

    /**
     * Return a hashtable of locations that embody this simulation. The
     * keys of the hashtable are location names (Strings), and the values
     * are corresponding {@link Location} objects.
     */
    public Hashtable<String,Location> getLocations(){
        return locations;

    }

    /** 
     * This function reads from a text file at location {@link ROUTE_FILE}. The
     * file contains the following information:
     *
     * <ol>
     * <li>The string <code>Locations:</code> on a line by itself.</li>
     * <li>A line for each location, containing comma-separated fields for
     * location name, lat, long, population, fraction of females, and fraction 
     * of individuals in the age range 12-30. </li>
     * <li>The string <code>Routes:</code> on a line by itself.</li>
     * <li>A line for each route, in the following format: source (-&gt;
     * transit) -&gt; destination. All locations (source, transit, destination)
     * in a route must have appeared previously in the "Locations:" section of
     * the file.</li>
     * </ol>
     **/

    private void loadRouteData() throws java.io.IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(
                    new FileInputStream(ROUTE_FILE)));

        locations = new Hashtable<String,Location>();
        routes = new ArrayList<Route>();

        String line = br.readLine();  // throw away "Locations:"
        line = br.readLine();
        while (!line.equals("Routes:")) {
            Scanner s = new Scanner(line);
            s.useDelimiter(",");
            String name = s.next().trim();
            double latitude = Double.valueOf(s.next());
            double longitude = Double.valueOf(s.next());
            int pop = Integer.valueOf(s.next().trim());
            double gender = Double.valueOf(s.next());
            double ageGroup = Double.valueOf(s.next());
            locations.put(name,new Location(name, latitude, longitude,
                        pop, gender, ageGroup)); 
            line = br.readLine();
        }
        line = br.readLine();
        while (line != null) {
            Scanner s = new Scanner(line);
            s.useDelimiter(",");
            String locs = s.next();
            int sourceVol = Integer.parseInt(s.next().trim());
            int transitVol = Integer.parseInt(s.next().trim());
            int destVol = 0;
            try {
                destVol = Integer.parseInt(s.next().trim());
            } catch (java.util.NoSuchElementException e){
                destVol = 0;
            }
            s = new Scanner(locs);
            s.useDelimiter("->");
            String source = s.next().trim();
            String transit = s.next().trim();
            try {
                String dest = s.next().trim();
                routes.add(new Route(locations.get(source),
                            locations.get(transit),
                            locations.get(dest),sourceVol, transitVol, destVol));
            } catch (java.util.NoSuchElementException e) {
                routes.add(new Route(locations.get(source),
                            locations.get(transit), sourceVol, transitVol));
            }
            line = br.readLine();
        }

    }

    /**
     * Schedules the HumanTrafficking object one time. The step method will 
     * then schedule it each successive year.
     **/
    private void scheduleSelf() {
        // The HumanTrafficking object will run at the end of each year
        //   (Dec. 31st). We schedule it once here, for Dec. 31st, 2013,
        //   and in its step() method it will schedule itself for the next
        //   year.
        schedule.scheduleOnce(364.0/365.0, this);
    }

    /**
     * Schedules the StatsPrinter object one time. It will then schedule 
     * itself each successive year.
     **/
    private void scheduleStatsPrinter() {
        // The StatsPrinter object will run near the end of each year
        //   (Dec. 30th). We schedule it once here, for Dec. 30th, 2013,
        //   and in its step() method it will schedule itself for the next
        //   year.
        schedule.scheduleOnce(363.0/365.0, StatsPrinter.instance());
    }

    /**
     * Maintains necessary simulation state (<i>e.g.</i>, incrementing the
     * year), and schedules the HumanTrafficking object for next year).
     * @param state the HumanTrafficking simulation.
     **/
    public void step(SimState state) {
        incrementYear();
        HumanTrafficking ht = (HumanTrafficking) state;
        // Schedule myself to run again one calendar year from now.
        schedule.scheduleOnceIn(1, this);
        System.out.println("----------------------------------------------");
        System.out.println("Happy new year! It's now " + year + 
                ". (simtime=" + getSimTime() + ")");
    }

    /** 
     * Get the current simulation time, as a formatted string.
     */
    public String getSimTime() {
        return String.format("%.3f",schedule.getTime());
    }

    /**
     * Return the current year of the simulation.
     */
    public int getYear() {
        return year;
    }

}
