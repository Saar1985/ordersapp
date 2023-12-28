package com.orders.managers;

import com.orders.pojos.LoginResponse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.OpenOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystem;

public class LoginManager {

  private static final String PERMISSIONS_FILE_PATH = "/resources/authentication/permissions.csv";
  private static final String SESSIONS_FILE_PATH = "/resources/authentication/sessions.csv";
  private static final Logger logger = LogManager.getLogger(LoginManager.class);

  private FileSystem fileSystem;

  public LoginManager(FileSystem fileSystem) {
    this.fileSystem = fileSystem;
    validateFilesExist();
  }

  public boolean validateLogin(String username, String password) {
    try {
      validateFilesExist();
      String content = fileSystem.readFileBlocking(PERMISSIONS_FILE_PATH).toString();
      return content.lines().anyMatch(line -> line.equals(username + "," + password));
    } catch (Exception e) {
      e.printStackTrace(); // Handle the exception appropriately
      return false;
    }
  }

  public Optional<String> validateSession(String sessionId)
  {
    return getUsernameBySessionId(sessionId);
  }

  public  LoginResponse loginUser(String username, String password)
  {
    boolean validUser = validateLogin(username, password);

    if(!validUser)
    {
      return new LoginResponse(false, null, "User has no login permission");
    }
    if(getSessionByUsername(username).isPresent())
    {
      return new LoginResponse(false, null, "User already logged in");
    }

    String sessionId = generateSessionId();
    saveSessionToFile(username, sessionId);
    return new LoginResponse(true, sessionId, "Success");
  }

  public boolean logoutUser(String sessionId) {
    Optional<String> username = getUsernameBySessionId(sessionId);

    if(username.isPresent())
    {
      removeSessionFromFile(username.get());
      return true;
    }

    return false;
  }

  private static String generateSessionId() {
    return UUID.randomUUID().toString();
  }

  private void saveSessionToFile(String username, String sessionId) {
    try {
      String line = String.format("%s,%s%n", username, sessionId);

      fileSystem.open(SESSIONS_FILE_PATH, new OpenOptions().setAppend(true), ar -> {
        if (ar.succeeded()) {
          AsyncFile ws = ar.result();
          ws.write(Buffer.buffer(line));
        } else {
          logger.error("Could not open file: " + SESSIONS_FILE_PATH);
        }
      });
    } catch (Exception e) {
      e.printStackTrace();
      logger.error(e.getMessage());
    }
  }

  private Optional<String> getSessionByUsername(String username) {
    try {
      String content = fileSystem.readFileBlocking(SESSIONS_FILE_PATH).toString();

      return Arrays.stream(content.lines().toArray(String[]::new))
        .filter(line -> line.startsWith(username + ","))
        .map(line -> line.split(",")[1])
        .findFirst();
    } catch (Exception e) {
      e.printStackTrace();
      logger.error(e.getMessage());
      return Optional.empty();
    }
  }

  private Optional<String> getUsernameBySessionId(String sessionId) {
    try {
      String content = fileSystem.readFileBlocking(SESSIONS_FILE_PATH).toString();

      return Arrays.stream(content.lines().toArray(String[]::new))
        .filter(line -> line.endsWith(sessionId))
        .map(line -> line.split(",")[0])
        .findFirst();
    } catch (Exception e) {
      e.printStackTrace();
      logger.error(e.getMessage());
      return Optional.empty();
    }
  }

  private void removeSessionFromFile(String username) {
    try {
      String content = fileSystem.readFileBlocking(SESSIONS_FILE_PATH).toString();

      List<String> lines = Arrays.stream(content.lines().toArray(String[]::new))
        .filter(line -> !line.startsWith(username + ","))
        .collect(Collectors.toList());

      fileSystem.writeFileBlocking(SESSIONS_FILE_PATH, Buffer.buffer(String.join("\n", lines)));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void validateFilesExist()
  {
    if (!fileSystem.existsBlocking(SESSIONS_FILE_PATH)) {

      fileSystem.createFile(SESSIONS_FILE_PATH, handler -> {
        if (handler.succeeded()) {
          logger.info("Successfully created file: " + SESSIONS_FILE_PATH);
        } else {
          logger.info("Failure in creating file: " + SESSIONS_FILE_PATH);
        }
      });
    }

    if (!fileSystem.existsBlocking(PERMISSIONS_FILE_PATH)) {
      // Create the file
      fileSystem.createFile(PERMISSIONS_FILE_PATH, createHandler -> {
        if (createHandler.succeeded()) {
          logger.info("Successfully created file: " + PERMISSIONS_FILE_PATH);

          String content = fileSystem.readFileBlocking("/app/default_permissions.csv").toString();
          List<String> lines = content.lines().toList();;
          fileSystem.writeFileBlocking(PERMISSIONS_FILE_PATH, Buffer.buffer(String.join("\n", lines)));

        } else {
          logger.error("Failure in creating file: " + PERMISSIONS_FILE_PATH, createHandler.cause());
        }
      });
    }
  }
}
