/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

// Robot
import edu.wpi.first.wpilibj.TimedRobot;
// Interface
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
// Other Imports
import edu.wpi.first.wpilibj.Joystick;

// Used for recording auton
import java.io.FileNotFoundException;
import java.io.IOException;

//import com.ctre.phoenix.motorcontrol.InvertType;
import com.ctre.phoenix.motorcontrol.can.*;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the TimedRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {
  private static final String kDefaultAuto = "Default";
  private static final String kCustomAuto = "My Auto";
  private String m_autoSelected;
  private final SendableChooser<String> m_chooser = new SendableChooser<>();
  
  // Mortors - left
  WPI_VictorSPX leftDrive = new WPI_VictorSPX(14);
  WPI_VictorSPX leftSlave = new WPI_VictorSPX(15);
  
  // Motors - right
  WPI_VictorSPX rightDrive = new WPI_VictorSPX(1);
  WPI_VictorSPX rightSlave = new WPI_VictorSPX(0);
  

  // Recording auton
  boolean isRecording = false;
  boolean canRecord = true;
  boolean hasStarted = false;
	//autoNumber defines an easy way to change the file you are recording to/playing from, in case you want to make a
	//few different auto programs
	static final int autoNumber = 1;
	//autoFile is a global constant that keeps you from recording into a different file than the one you play from
  static final String autoFile = new String("/home/lvuser/recordedAuto" + autoNumber + ".csv");
  
  //Play and record file
  BTMacroPlay player = null;
  BTMacroRecord recorder = null;

  // Joystick
  Joystick joy = new Joystick(0);

  double joystickX = 0;
  double joystickY = 0;

  Boolean getRecordButton() {
    return joy.getRawButtonPressed(11);
  }

  double getTurn(){
    return joy.getRawAxis(0);
  }

  double getFoward(){
    return -joy.getRawAxis(5);
  }

  // Insert cam here:

  /**
   * This function is run when the robot is first started up and should be
   * used for any initialization code.
   */
  @Override
  public void robotInit() {
    m_chooser.setDefaultOption("Default Auto", kDefaultAuto);
    m_chooser.addOption("My Auto", kCustomAuto);
    SmartDashboard.putData("Auto choices", m_chooser);

    // Robot init
     // Set inverted for both side and make the drive train.
     leftDrive.setInverted(false);
     leftSlave.setInverted(false);
 
     rightDrive.setInverted(true);
     rightSlave.setInverted(true);

    
  }

  /**
   * This function is called every robot packet, no matter the mode. Use
   * this for items like diagnostics that you want ran during disabled,
   * autonomous, teleoperated and test.
   *
   * <p>This runs after the mode specific periodic functions, but before
   * LiveWindow and SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic() {
    if(player!= null && !isAutonomous() && hasStarted) {
			player.end(this);
		}
  }

  /**
   * This autonomous (along with the chooser code above) shows how to select
   * between different autonomous modes using the dashboard. The sendable
   * chooser code works with the Java SmartDashboard. If you prefer the
   * LabVIEW Dashboard, remove all of the chooser code and uncomment the
   * getString line to get the auto name from the text box below the Gyro
   *
   * <p>You can add additional auto modes by adding additional comparisons to
   * the switch structure below with additional strings. If using the
   * SendableChooser make sure to add them to the chooser code above as well.
   */
  @Override
  public void autonomousInit() {
    m_autoSelected = m_chooser.getSelected();
    hasStarted = true;
    // m_autoSelected = SmartDashboard.getString("Auto Selector", kDefaultAuto);
    System.out.println("Auto selected: " + m_autoSelected);
    try {
      player = new BTMacroPlay(autoFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
  }

  /**
   * This function is called periodically during autonomous.
   */
  @Override
  public void autonomousPeriodic() {
    // switch (m_autoSelected) {
    //   case kCustomAuto:
    //     // Put custom auto code here
    //     System.out.println("custom auto");
        
    //     break;
    //   case kDefaultAuto:
    //   default:
    //     // Put default auto code here
    //     break;
    // }
    if (player != null) {
      player.play(this);
    }
  }

  @Override
  public void teleopInit() {
    try {
			recorder = new BTMacroRecord(autoFile);
		} catch (IOException e) {
			e.printStackTrace();
    }
  }

  /**
   * This function is called periodically during operator control.
   */
  @Override
  public void teleopPeriodic() {
    //Save input
    //String potterville = "Hola";

    // get raw axis depends on the controler:
    // For the flight stick, axis 0 is X, 1 is Y, and 2 is Z,
    // For xbox, axis 0 is LX, 1 is LY, 4 is RX, 5 is RY,
    joystickX = getTurn();
    joystickY = getFoward();
    
    // Drive
    setDrive(joystickX , joystickY);
    //setDrive(joy.getX(), -joy.getY());
    
    // Toggle recording
    if (this.getRecordButton()) {
      isRecording = !isRecording;
      if (isRecording) {
        // Set the recording start time
        System.out.println("Recording!");
        recorder.startRecording();

      } else {
        //once we're done recording, the last thing we'll do is clean up the recording using the end
        //function. more info on the end function is in the record class
        System.out.println("Ending Recording!");
        isRecording = false;
        canRecord = false;
        try {
          if(recorder != null) {
            recorder.end();
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }  

    // Record if needed
    //if our record button has been pressed, lets start recording!
    if (isRecording && canRecord) {
        try {
        //if we succesfully have made the recorder object, lets start recording stuff
        //2220 uses a storage object that we can get motors values, etc. from.
        //if you don't need to pass an object like that in, modify the methods/classes
        if(recorder != null) {
          recorder.record(this);
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    if (joy.getRawButtonPressed(12)) {
      
    }
  }

  /**
   * This function is called periodically during test mode.
   */
  @Override
  public void testPeriodic() {
    setDrive(getTurn(), getFoward());
  }

  // Method to set the drive train baised on joyX and Y, converts joystick to tank drive
  public void setDrive(double joyX, double joyY) {
    double y = joyY * 0.6;
    double x = joyX * 0.4;

    //System.out.println("X: " + x + " Y: " + y);
  
    // Deadband - within 10% joystick, make it zero
    if (Math.abs(y) < 0.10) {
      y = 0;
    }
    if (Math.abs(x) < 0.10) {
      x = 0;
    }
    
    double v = (1 - Math.abs(x)) * (y) + y;
    double w = (1 - Math.abs(y)) * (x) + x;
  
    double l = (v+w)/2;
    double r = (v-w)/2;
  
    this.leftDrive.set(l);
    this.leftSlave.set(l);
  
    this.rightDrive.set(r);
    this.rightSlave.set(r);
  }
}
