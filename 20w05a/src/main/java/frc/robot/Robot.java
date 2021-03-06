/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

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
    // Setup smartdashboard
    m_chooser.setDefaultOption("Default Auto", kDefaultAuto);
    m_chooser.addOption("My Auto", kCustomAuto);
    SmartDashboard.putData("Auto choices", m_chooser);

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
    // m_autoSelected = SmartDashboard.getString("Auto Selector", kDefaultAuto);
    System.out.println("Auto selected: " + m_autoSelected);
  }

  /**
   * This function is called periodically during autonomous.
   */
  @Override
  public void autonomousPeriodic() {
    switch (m_autoSelected) {
      case kCustomAuto:
        // Put custom auto code here
        break;
      case kDefaultAuto:
      default:
        // Put default auto code here
        break;
    }
  }

  /**
   * This function is called periodically during operator control.
   */
  @Override
  public void teleopPeriodic() {
  }

  /**
   * This function is called periodically during test mode.
   */
  @Override
  public void testPeriodic() {
  }
}
