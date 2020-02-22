package frc.robot;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/*Code outline to implement playing back a macro recorded in BTMacroRecord
*Be sure to read out of the same file created in BTMacroRecord
*BEWARE OF: setting your motors in a different order than in BTMacroRecord and changing motor values before
*time is up. Both issues are dealt with and explained below. Also only read/write from/to the motors 
*you have fully coded for, otherwise your code will cut out for no reason. 
*In main, the try/catch structure catches any IOExceptions or FileNotFoundExceptions. Necessary to play back
*the recorded routine during autonomous
*Dennis Melamed and Melanie (sorta, she slept)
*https://github.com/DennisMelamed/FRC-Play-Record-Macro
*March 22nd, 2015
*Modified by Dustin Kloepfer
*/


public class BTMacroPlay {
	Scanner scanner;
	long startTime;

	boolean onTime = true;
	double nextDouble;
	

	public BTMacroPlay(String autoFile) throws FileNotFoundException {
		//create a scanner to read the file created during BTMacroRecord
		//scanner is able to read out the doubles recorded into recordedAuto.csv (as of 2015)
		scanner = new Scanner(new File(autoFile));
		
		//let scanner know that the numbers are separated by a comma or a newline, as it is a .csv file
		scanner.useDelimiter(",|\\n");
		
		//lets set start time to the current time you begin autonomous
		startTime = System.currentTimeMillis();	
	}
	
	public void play(Robot robot) {
		//if recordedAuto.csv has a double to read next, then read it
		if ((scanner != null) && (scanner.hasNextDouble())) {
			// double deltaTime;
			
			nextDouble = scanner.nextDouble();
			
			// Time recorded for values minus how far into replaying it we are--> if not zero, hold up
			// deltaTime = nextDouble - (System.currentTimeMillis()-startTime);
			
			// For our 2020 robot, we have a setDrive method to convert joystick X and Y to tank drive.
			// We save it in the order of joyX then joyY so we can call scanner.nextDouble() right as the perameters.
			robot.setDrive(scanner.nextDouble(), scanner.nextDouble());
			
			//go to next double
			onTime = true;
		}
		//end play, there are no more values to find
		else {
			this.end(robot);
			if (scanner != null) {
				scanner.close();
				scanner = null;
			}
		}
		
	}
	
	//stop motors and end playing the recorded file
	public void end(Robot robot) {
		robot.setDrive(scanner.nextDouble(), scanner.nextDouble());

		// robot.leftDrive.set(0);
        // robot.leftSlave.set(0);
		// robot.rightDrive.set(0);
		// robot.rightSlave.set(0);
		
		if (scanner != null) {
			scanner.close();
		}
	}
}