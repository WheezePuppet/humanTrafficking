package edu.umw.cpsc.humantrafficking;

import javax.swing.*;
import java.awt.*;
import sim.util.*;

import com.vividsolutions.jts.io.ParseException;
import java.awt.Image;
import javax.swing.ImageIcon;
import sim.engine.*;
import sim.portrayal.*;
import java.util.Hashtable;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import javax.swing.JFrame;
import sim.display.Console;
import sim.display.Controller;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.portrayal.simple.ImagePortrayal2D;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.field.continuous.Continuous2D;
import sim.portrayal.simple.OvalPortrayal2D;

/**
 * The class controlling MASON-oriented visual display for the simulation.
 */
public class HumanTraffickingWithUI extends GUIState{
        
    JFrame displayFrame;
    private static HumanTraffickingWithUI theInstance;

    FieldPortrayal2D myVictimPortrayal = new ContinuousPortrayal2D();
    FieldPortrayal2D myMapPortrayal = new ContinuousPortrayal2D();
    Display2D myDisplay = new Display2D(800, 600, this, 1);
    
    /**
     * Singleton pattern.
     */
    public static synchronized HumanTraffickingWithUI instance() {
        if (theInstance == null) {
            try {
                theInstance = new HumanTraffickingWithUI();
            } catch (ParseException e) {
                e.printStackTrace();
                System.out.println("Zai jian!");
                System.exit(1);
            }
        }
        return theInstance;
    }

    private HumanTraffickingWithUI() throws ParseException {
        super(HumanTrafficking.instance());
    }
    
    /**
     * Start the simulation with graphical display.
     */
    public static void main(String[] args){
        HumanTraffickingWithUI worldGUI = null;
        
        worldGUI = HumanTraffickingWithUI.instance();
        
        Console c = new Console(worldGUI);
        c.setVisible(true);
    }

    /**
     * Before actually starting the simulation, set up the background along 
     * with the portrayals for the victims on the map.
     */
    public void start(){
        super.start();
        setupPortrayals();
	}

    private void setupPortrayals(){

        HumanTrafficking world = (HumanTrafficking) state;
        
        JFrame jf = myDisplay.createFrame();
        jf.setTitle("Human Trafficking simulation -- East Asia");
        //jf.setContentPane(jp);
        jf.setSize(900,700);
        jf.setVisible(true);


        Continuous2D mapField = new Continuous2D(1, 1000, 1000);
        mapField.setObjectLocation(new Object(), new Double2D(500,500));
        myMapPortrayal.setField(mapField);
        ImageIcon myImageIcon = new ImageIcon("map.jpg");
        Image image = myImageIcon.getImage();
        myMapPortrayal.setPortrayalForAll(new ImagePortrayal2D(image, 1000));
        myVictimPortrayal.setField(world.victimsField);
                
//myVictimPortrayal.setPortrayalForAll(new OvalPortrayal2D(java.awt.Color.GREEN,1));
        myDisplay.attach(myMapPortrayal, "Map");
        myDisplay.attach(myVictimPortrayal, "Victims");
        

        // setup the portrayals for the OTHER portrayal and OTHER field
        // which will hold many many many many objects, one for each Victim
        // object, and display them not as maps, but as little blue circles
        // or whatever.
    }

}
