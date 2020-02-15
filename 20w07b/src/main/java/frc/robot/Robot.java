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

// Color sensor
import edu.wpi.first.wpilibj.util.Color;
import edu.wpi.first.wpilibj.I2C;

import com.revrobotics.ColorSensorV3;
import com.revrobotics.ColorMatchResult;
import com.revrobotics.ColorMatch;

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

  
  // 2020 robot mortors
    // Mortors - left
    WPI_VictorSPX leftDrive = new WPI_VictorSPX(1);
    WPI_VictorSPX leftSlave = new WPI_VictorSPX(2);
    
    // Motors - right
    WPI_VictorSPX rightDrive = new WPI_VictorSPX(13);
    WPI_VictorSPX rightSlave = new WPI_VictorSPX(14);

    // Motor - Elevator
    WPI_VictorSPX elevator = new WPI_VictorSPX(12);


    // Joystick
    Joystick joy = new Joystick(0);

    double joystickX = 0;
    double joystickY = 0;

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

    Boolean getRecordButton() {
      return joy.getRawButtonPressed(11);
    }
  
  // Color Sensor
    // I2C port for the sensor
    private final I2C.Port i2cPort = I2C.Port.kOnboard;

    // Color sensor takes the I2C port
    private final ColorSensorV3 m_colorSensor = new ColorSensorV3(i2cPort);

    // The color matcher detects the color (gives closest match)
    private final ColorMatch m_colorMatcher = new ColorMatch();

    // Sample calabration, might need to be changed
    private final Color blueTarget = ColorMatch.makeColor(0.143, 0.427, 0.429);
    private final Color greenTarget = ColorMatch.makeColor(0.197, 0.561, 0.240);
    private final Color redTarget = ColorMatch.makeColor(0.561, 0.232, 0.114);
    private final Color yellowTarget = ColorMatch.makeColor(0.361, 0.524, 0.113);

  /**
   * This function is run when the robot is first started up and should be
   * used for any initialization code.
   */
  @Override
  public void robotInit() {
    m_chooser.setDefaultOption("Default Auto", kDefaultAuto);
    m_chooser.addOption("My Auto", kCustomAuto);
    SmartDashboard.putData("Auto choices", m_chooser);

    // Set inverted for both side and make the drive train.
    boolean leftInverted = false;
    leftDrive.setInverted(leftInverted);
    leftSlave.setInverted(leftInverted);

    rightDrive.setInverted(!leftInverted);
    rightSlave.setInverted(!leftInverted);

    // Setup color matcher
    m_colorMatcher.addColorMatch(blueTarget);
    m_colorMatcher.addColorMatch(greenTarget);
    m_colorMatcher.addColorMatch(redTarget);
    m_colorMatcher.addColorMatch(yellowTarget); 
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
    // Auto handeling
    if(player!= null && !isAutonomous() && hasStarted) {
			player.end(this);
    }
    
    // getColor returns the color as values between 0 and 1 (normalized),
    // In order to get the raw color values GetRawColor should be used,
    // THe farther from the object, the less accurate it is.
    Color detectedColor = m_colorSensor.getColor();

    // Run the color match algorithm on our detected color
    String colorString;
    ColorMatchResult match = m_colorMatcher.matchClosestColor(detectedColor);

    if (match.color == blueTarget) {
      colorString = "Blue";
    } else if (match.color == redTarget) {
      colorString = "Red";
    } else if (match.color == greenTarget) {
      colorString = "Green";
    } else if (match.color == yellowTarget) {
      colorString = "Yellow";
    } else {
      colorString = "Unknown";
    }

    // Open Smart Dashboard or Shuffleboard to see the color detected by the sensor.
    SmartDashboard.putNumber("Red", detectedColor.red);
    SmartDashboard.putNumber("Green", detectedColor.green);
    SmartDashboard.putNumber("Blue", detectedColor.blue);
    SmartDashboard.putNumber("Confidence", match.confidence);
    SmartDashboard.putString("Detected Color", colorString);
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

    // Potential to change the macro
    switch (m_autoSelected) {
      case kCustomAuto:
        // Put custom auto code here
        break;
      case kDefaultAuto:
      default:
        // Put default auto code here
        break;
    }

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
    // Play the macro
    if (player != null) {
      player.play(this);
    }
  }

  // @Override
  // public void teleopInit() {
    
  // }

  /**
   * This function is called periodically during operator control.
   */
  // spin color wheel, then more lemons, then set color.
  @Override
  public void teleopPeriodic() {
    setDrive(joy.getX(), -joy.getY());

    // Elevator control -temp-
    double elevatorSpeed = 0;
    //gets the hat input, -1 for no input, 0 for up, 180 for down
    if (joy.getPOV() == 0) {
      elevatorSpeed = 0.4;
    } else if (joy.getPOV() == 180) {
      elevatorSpeed = -0.8;
    }
    elevator.set(elevatorSpeed);
  }

  @Override
  public void testInit() {
    try {
			recorder = new BTMacroRecord(autoFile);
		} catch (IOException e) {
			e.printStackTrace();
    }
  }

  /**
   * This function is called periodically during test mode.
   */
  @Override
  public void testPeriodic() {
    //Save input
    //String potterville = "Hola";
    joystickX = joy.getX();
    joystickY = -joy.getY();
    
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
  }

  public void setDrive(double joyX, double joyY) {
    double y = joyY * 0.8;
    double x = joyX * 0.8;

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
