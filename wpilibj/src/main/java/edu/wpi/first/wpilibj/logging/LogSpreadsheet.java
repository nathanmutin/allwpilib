/*----------------------------------------------------------------------------*/
/* Copyright (c) 2019 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package edu.wpi.first.wpilibj.logging;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import edu.wpi.first.wpilibj.RobotController;

/**
 * A LogSpreadsheet writes to a file the contents of each of its registered
 * {@link LogCell}'s.
 *
 * <p>For the LogSpreadsheet to write log informations, you must call
 * {@link #periodic()} periodically.
 */
public class LogSpreadsheet {
  private final String m_name;
  private final List<LogCell> m_cells;
  private final LogCell m_timestampCell;
  private OutputStream m_logFile;
  private boolean m_active;

  /**
   * Instantiate a LogSpreadsheet passing in the name of the table.
   *
   * @param name The name of the table.
   */
  public LogSpreadsheet(String name) {
    m_name = name;
    m_cells = new ArrayList<LogCell>();
    m_timestampCell = new LogCell("Timestamp (ms)");
    m_active = false;
    registerCell(m_timestampCell);
  }

  /**
   * Instantiate a LogSpreadsheet.
   */
  public LogSpreadsheet() {
    this("");
  }

  /**
   * Register a new column to be logged. You will figure this value out
   * in {@link LogCell#getContent()}.
   *
   * <p>A LogCell can only be registered if the spreadsheet is inactive:
   * see {@link #start()} and {@link #stop()} to activate or inactivate it.
   *
   * @param cell The LogCell to register.
   */
  public void registerCell(LogCell cell) {
    if (m_active) {
      System.out.println("You can't add a new cell when the spreadsheet is active: "
          + cell.getName());
    } else {
      m_cells.add(cell);
    }
  }

  /**
   * Start the table: open a new file and write the column headers.
   *
   * <p>It causes the spreadsheet to be "active".
   */
  public void start() {
    if (m_active) {
      System.out.println("This table has already been initialized");
      return;
    }

    String fileName = "log-" + m_name + "-" + RobotController.getFPGATime() + ".txt";
    try {
      m_logFile = Files.newOutputStream(Paths.get(fileName));
    } catch (IOException ex) {
      System.out.println(ex.getMessage());
    }

    for (LogCell cell : m_cells) {
      writeFile("\"" + cell.getName() + "\",");
    }
    writeFile("\n");

    m_active = true;
  }

  /**
   * Release the table and close the file.
   *
   * <p>It causes the spreadsheet to be "inactive".
   * Previously registered LogCell's remain unchanged.
   */
  public void stop() {
    if (m_active) {
      m_active = false;
    }
  }

  /**
   * Call it regularly. If the table is active, write a row of the table.
   */
  public void periodic() {
    if (m_active) {
      m_timestampCell.log(System.currentTimeMillis());
      writeRow();
    }
  }

  private void writeRow() {
    for (LogCell cell : m_cells) {
      cell.acquireLock();
      writeFile("\"" + cell.getContent() + "\",");
      cell.releaseLock();
    }
    writeFile("\n");
  }

  private void writeFile(String text) {
    try {
      m_logFile.write(text.getBytes());
    } catch (IOException ex) {
      System.out.println(ex.getMessage());
    }
  }
}
