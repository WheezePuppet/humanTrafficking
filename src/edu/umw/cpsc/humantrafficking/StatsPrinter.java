package edu.umw.cpsc.humantrafficking;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import sim.engine.*;
import sim.util.*;

/**
 * Utility class to periodically dump statistical output from simulation to
 * flat files.
 */
public class StatsPrinter implements Steppable{

    private static StatsPrinter theInstance;    

    /**
     * The name of the output directory into which statistical files will
     * be written.
     */
    public static final String OUTPUT_DIRECTORY = "output";

    /**
     * Acquire the singleton StatsPrinter object, and <b>delete</b> the
     * existing contents of {@link #OUTPUT_DIRECTORY}.
     */
    public static synchronized StatsPrinter instance(){
        if (theInstance == null) {
            theInstance = new StatsPrinter();
        }
        return theInstance;
    }

    private StatsPrinter() {    
        File dir = new File(OUTPUT_DIRECTORY);
        dir.mkdir();
        File files[] = dir.listFiles();
        for (File file : files) {
            file.delete();
        }
    }

    private void printCurrentState() {
        String countryName = null;
        int numVics;
        int age;
        int year;
        Hashtable<String,Location> ht;
        ArrayList<Victim> vics;
        ht = HumanTrafficking.instance().getLocations(); 

        Enumeration<String> keys = ht.keys();
        while (keys.hasMoreElements()) {
            String nextKey = keys.nextElement();
            year = HumanTrafficking.instance().getYear();
            countryName = ht.get(nextKey).getName();
            countryName = countryName.replace(" ","");
            vics = ht.get(nextKey).getVics();
            numVics = ht.get(nextKey).getNumVics();
            try{
                File file = new File(OUTPUT_DIRECTORY + "/Ages" + 
                    countryName + year + ".data");
                FileWriter fstream = new FileWriter(file);
                PrintWriter out = new PrintWriter(fstream);
                for(int i=0; i<numVics; i++){
                    age = vics.get(i).getAge();
                    out.println(age);   
                }
                out.close();
            }catch (Exception e){//Catch exception if any
                System.err.println("Error: " + e.getMessage());
                System.exit(1);
            }
        }

    }

    /**
     * Dump a snapshot of statistical information to flat files.<br/>
     * The directory {@link #OUTPUT_DIRECTORY} will appear, if it doesn't
     * already exist, and its contents cleared. Then, in this directory will 
     * be written the following files:
     *
     * <ul>
     * <li>One "Ages" file for each country (for this year). The name of the 
     * file will be <code>Ages</code> followed by the country's name (with 
     * no spaces), the current year, and a "<code>.data</code>" suffix. (For 
     * instance, "<code>AgesSouthKorea2028.data</code>".) The file will 
     * contain one line for each victim present in that location. Each such 
     * line consists of a single numerical field: the victim's current 
     * age.</li>
     * </ul>
     *
     * This dump is scheduled to occur once per year, and when 
     * <code>step()</code> is invoked, it will schedule itself for the next 
     * year.
     * @param state the HumanTrafficking simulation.
     */
    public void step(SimState state){
        printCurrentState();
        state.schedule.scheduleOnceIn(1, this);
    }
}

