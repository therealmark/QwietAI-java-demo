package io.shiftleft.controller;

import io.shiftleft.data.DataLoader;
import io.shiftleft.model.Patient;
import io.shiftleft.repository.PatientRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin checks login
 */

@RestController
public class PatientController {

  private static Logger log = LoggerFactory.getLogger(PatientController.class);

  @Autowired
  private PatientRepository patientRepository;

  /**
   * Gets all customers.
   *
   * @return the customers
/**
 * Utility class for sanitizing log inputs to prevent log injection attacks
 */
public class LogSanitizer {
    /**
     * Sanitizes input strings to prevent log injection attacks
     * @param input The string to sanitize
     * @return Sanitized string safe for logging
     */
    public static String sanitize(String input) {
        if (input == null) return null;
        // Remove CRLF characters to prevent log forging
        String sanitized = input.replaceAll("[r
]", "");
        return StringEscapeUtils.escapeJava(sanitized);
    }
}

    return patientRepository.findAll();
  }

}
