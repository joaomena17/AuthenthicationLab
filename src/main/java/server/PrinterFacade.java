package server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;

/**
 *  This class provides the printer server endpoints that client can use.
 */
public class PrinterFacade extends UnicastRemoteObject implements IPrinterFacade {

    private final IPasswordService passwordService;
    private final IPrinterService printerService;
    private final IUserService userService;
    private final IUserOperation verifyAccess;
    public PrinterFacade(IPasswordService passwordService, IPrinterService printer, IUserService userService, IUserOperation verifyAccess) throws RemoteException {
        super();
        this.passwordService = passwordService;
        this.printerService = printer;
        this.userService = userService;
        this.verifyAccess = verifyAccess;
    }

    @Override
    public void createUser(String username, char[] password) throws RemoteException {
        try {
            passwordService.saveUser(username, password);
            System.out.println("New user with username: " + username + " is created");
        } catch (SQLException e) {
            System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
        }
    }

    @Override
    public boolean verifyUser(String username, char[] password) throws AuthenticationFailedException, RemoteException {
        try {
            if (passwordService.verifyUser(username, password)) {
                userService.addAuthenticatedUser(username);
                System.out.println("User with username " + username + " is successfully verified and can use the printer");
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new AuthenticationFailedException("Authentication of user with username: " + username + " failed. User cannot use the printer");
        }
        System.out.println("User with username " + username + " is successfully verified and can use the printer");
        throw new AuthenticationFailedException("Authentication of user with username: " + username + " failed. User cannot use the printer");
    }


    @Override
    public String print(String username, String filename, String printer) throws AuthenticationFailedException {
        verifyAccess.controlAccess(username, Operations.print.ordinal());
        userService.verifyUser(username);
        return printerService.print(filename, printer);
    }

    @Override
    public String queue(String username, String printer) throws AuthenticationFailedException {
        verifyAccess.controlAccess(username, Operations.queue.ordinal());
        userService.verifyUser(username);
        return printerService.queue(printer);
    }

    @Override
    public void topQueue(String username, String printer, Integer job) throws AuthenticationFailedException {
        verifyAccess.controlAccess(username, Operations.topQueue.ordinal());
        userService.verifyUser(username);
        printerService.topQueue(printer, job);
    }

    @Override
    public void start(String username) throws AuthenticationFailedException {
        verifyAccess.controlAccess(username, Operations.start.ordinal());
        userService.verifyUser(username);
         printerService.start();
    }

    @Override
    public void stop(String username) throws AuthenticationFailedException {
        verifyAccess.controlAccess(username, Operations.stop.ordinal());
        userService.verifyUser(username);
        printerService.stop();

    }

    @Override
    public void restart(String username) throws AuthenticationFailedException {
        verifyAccess.controlAccess(username, Operations.restart.ordinal());
        userService.verifyUser(username);
        printerService.restart();
    }

    @Override
    public String status(String username) throws AuthenticationFailedException {
        verifyAccess.controlAccess(username, Operations.status.ordinal());
        userService.verifyUser(username);
        return printerService.status();
    }

    @Override
    public String readConfig(String username, String parameter) throws AuthenticationFailedException {
        verifyAccess.controlAccess(username, Operations.readConfig.ordinal());
        userService.verifyUser(username);
        return printerService.readConfig(parameter);
    }

    @Override
    public void setConfig(String username, String parameter, String value) {
        try {
            verifyAccess.controlAccess(username, Operations.setConfig.ordinal());
            userService.verifyUser(username);
            printerService.setConfig(parameter, value);
        } catch (AuthenticationFailedException e) {
            System.out.println(e.getMessage());
        }
    }
}
