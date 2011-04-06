/**
 * Project: scapi.
 * Package: edu.biu.scapi.generals.
 * File: LoggerExtendedLevel.java.
 * Creation date Mar 13, 2011
 * Create by LabTest
 *
 *
 * This file TODO
 */
package edu.biu.scapi.generals;

import java.util.logging.Level;

/**
 * @author LabTest
 * 
 * This class extends the Level class of the logger. The aim is to generate additional levels that will
 * suite the scapi project. For example, we create a TIMING level who's aim is to log only timing records
 * and thus must have a higher level than severe. 
 *
 */
public class LoggerExtendedLevel extends Level {

	// Create the new level
    public static final Level TIMING = new LoggerExtendedLevel("TIMING", Level.SEVERE.intValue()+10);

    /**
     * 
     * @param name - the name of the level
     * @param value - the int value of the level.
     */
    public LoggerExtendedLevel(String name, int value) {
        super(name, value);
    }


}
