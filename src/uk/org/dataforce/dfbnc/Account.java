/*
 * Copyright (c) 2006-2007 Shane Mc Cormack
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * SVN: $Id$
 */

package uk.org.dataforce.dfbnc;

import com.dmdirc.util.InvalidConfigFileException;
import java.io.File;
import java.io.IOException;
import uk.org.dataforce.dfbnc.commands.CommandManager;
import uk.org.dataforce.dfbnc.servers.ServerType;
import uk.org.dataforce.dfbnc.servers.ServerTypeNotFound;
import java.util.List;
import java.util.ArrayList;

/**
 * Functions related to Accounts
 */
public final class Account implements UserSocketWatcher {

    /** Salt used when generating passwords */
    private static final String salt =
            "a5S5l1N4u4O2y9Z4l6W7t1A9b9L8a1X5a7F4s5E8";
    /** Are passwords case sensitive? */
    private static final boolean caseSensitivePasswords = false;
    /** This account name */
    private final String myName;
    /** Is this account an admin */
    private boolean isAdmin;
    /** Deletecode for this account. This is not saved between sessions */
    private String deleteCode = "";
    /** CommandManager for this account */
    private CommandManager myCommandManager = new CommandManager();
    /** ConnectionHandler for this account */
    private ConnectionHandler myConnectionHandler = null;
    /** List of all sockets that are part of this account. */
    private List<UserSocket> myUserSockets = new ArrayList<UserSocket>();
    /** Account config file. */
    private Config config;

    /**
     * Create an Account object.
     * This will load all the settings for the account from the config file.
     *
     * @param username Name of this account
     *
     * @throws IOException Error loading config
     * @throws InvalidConfigFileException  Error loading config
     */
    public Account(final String username) throws IOException,
            InvalidConfigFileException {
        myName = username;

        final File confDir = new File(DFBnc.getConfigDirName(), username);
        if (!confDir.exists()) {
            confDir.mkdirs();
        }
        config = new Config(new File(confDir, username + ".conf"));

        // Enable global commands.
        myCommandManager.addSubCommandManager(DFBnc.getUserCommandManager());
        if (isAdmin) {
            myCommandManager.addSubCommandManager(DFBnc.getAdminCommandManager());
        }

        final ServerType myServerType = getServerType();
        if (myServerType != null) {
            myServerType.activate(this);
        }
    }

    /**
     * Called when a new UserSocket is opened on an account that this class is
     * linked to.
     *
     * @param user UserSocket for user
     */
    @Override
    public void userConnected(final UserSocket user) {
        myUserSockets.add(user);
        if (myConnectionHandler != null &&
                myConnectionHandler instanceof UserSocketWatcher) {
            ((UserSocketWatcher) myConnectionHandler).userConnected(user);
        }
        for (UserSocket socket : myUserSockets) {
            if (user != socket) {
                socket.sendBotMessage("Another client has connected (" + user.
                        getIP() + ")");
            }
        }
    }

    /**
     * Called when a UserSocket is closed on an account that this class is
     * linked to.
     *
     * @param user UserSocket for user
     */
    @Override
    public void userDisconnected(final UserSocket user) {
        myUserSockets.remove(user);
        if (myConnectionHandler != null &&
                myConnectionHandler instanceof UserSocketWatcher) {
            ((UserSocketWatcher) myConnectionHandler).userDisconnected(user);
        }
        for (UserSocket socket : myUserSockets) {
            if (user != socket) {
                socket.sendBotMessage("Client has Disconnected (" + user.getIP() +
                        ")");
            }
        }
    }

    /**
     * Get a List of all UserSockets that are part of this account
     *
     * @return a List of all UserSockets that are part of this account
     */
    public List<UserSocket> getUserSockets() {
        return myUserSockets;
    }

    /**
     * Get the ServerType for this account
     *
     * @return The ServerType for this account (or null if not defined, or invalid)
     */
    public ServerType getServerType() {
        final String currentType = config.getOption("server", "servertype", "");
        if (!currentType.isEmpty()) {
            try {
                return DFBnc.getServerTypeManager().getServerType(currentType);
            } catch (ServerTypeNotFound stnf) {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Get the CommandManager for this account
     *
     * @return The CommandManager for this account
     */
    public CommandManager getCommandManager() {
        return myCommandManager;
    }

    /**
     * Get the ConnectionHandler for this account
     *
     * @return The ConnectionHandler for this account
     */
    public ConnectionHandler getConnectionHandler() {
        return myConnectionHandler;
    }

    /**
     * Set the ConnectionHandler for this account
     *
     * @param handler The New ConnectionHandler for this account
     */
    public void setConnectionHandler(final ConnectionHandler handler) {
        myConnectionHandler = handler;
    }

    /**
     * Save the account settings for this account to the config file
     */
    public void save() {
        config.save();
    }

    /**
     * Get the name of this account
     *
     * @return Name of this account
     */
    public String getName() {
        return myName;
    }

    /**
     * Check if a password matches this account password.
     *
     * @param password Password to check
     * @return true/false depending on successful match
     */
    public boolean checkPassword(final String password) {
        StringBuffer hashedPassword = new StringBuffer(myName.toLowerCase());
        if (caseSensitivePasswords) {
            hashedPassword.append(password);
        } else {
            hashedPassword.append(password.toLowerCase());
        }
        hashedPassword.append(salt);

        return Functions.md5(hashedPassword.toString()).equals(config.getOption(
                "user", "password", "..."));
    }

    /**
     * Change the password of this account
     *
     * @param password New password
     */
    public void setPassword(final String password) {
        StringBuffer hashedPassword = new StringBuffer(myName.toLowerCase());
        if (caseSensitivePasswords) {
            hashedPassword.append(password);
        } else {
            hashedPassword.append(password.toLowerCase());
        }
        hashedPassword.append(salt);

        config.setOption("user", "password", Functions.md5(hashedPassword.
                toString()));
        config.save();
    }

    /**
     * Get the DeleteCode for this account
     *
     * @return The DeleteCode for this account
     */
    public String getDeleteCode() {
        return deleteCode;
    }

    /**
     * Set the Delete Code for this account
     *
     * @param deleteCode The New DeleteCode for this account
     */
    public void setDeleteCode(final String deleteCode) {
        this.deleteCode = deleteCode;
    }

    /**
     * Delete this account
     */
    public void delete() {
        for (UserSocket socket : myUserSockets) {
            socket.sendLine(
                    ":%s NOTICE :Connection terminating (Account Deleted)",
                    Functions.getServerName(socket.getAccount()));
            socket.close();
        }
        myConnectionHandler.shutdown("Account Deleted");
    }

    /**
     * Change the suspended setting for this account
     *
     * @param value true/false for new value of isSuspended
     * @param reason Reason for account suspension
     */
    public void setSuspended(final boolean value, final String reason) {
        config.setBoolOption("user", "suspended", value);
        if (value) {
            final String suspendReason = (reason != null && !reason.isEmpty()) ? reason
                    : "No reason specified";
            config.setOption("user", "suspendReason", suspendReason);

            for (UserSocket socket : myUserSockets) {
                socket.sendLine(
                        ":%s NOTICE :Connection terminating - Account Suspended (%s)",
                        Functions.getServerName(socket.getAccount()),
                        suspendReason);
                socket.close();
            }
            myConnectionHandler.shutdown("Account Suspended");
        }
    }

    /**
     * Is the account suspended?
     *
     * @return Is the account suspended?
     */
    public boolean isSuspended() {
        return config.getBoolOption("user", "suspended", false);
    }

    /**
     * Why is the account suspended?
     *
     * @return Reason why the account is suspended
     */
    public String getSuspendReason() {
        if (isSuspended()) {
            return config.getOption("user", "suspendReason", "");
        } else {
            return "";
        }
    }

    /**
     * Change the admin setting for this account
     *
     * @param value true/false for new value of isAdmin
     */
    public void setAdmin(final boolean value) {
        if (value != isAdmin) {
            // Change command manager to reflect new setting
            if (value) {
                myCommandManager.addSubCommandManager(DFBnc.
                        getAdminCommandManager());
            } else {
                myCommandManager.delSubCommandManager(DFBnc.
                        getAdminCommandManager());
            }
        }
        config.setBoolOption("user", "admin", value);
    }

    /**
     * Is the account an admin
     *
     * @return Is the account an admin?
     */
    public boolean isAdmin() {
        return config.getBoolOption("user", "admin", false);
    }

    /**
     * Change the first-time setting for this account
     *
     * @param value true/false for new value of isFirst
     */
    public void setFirst(final boolean value) {
        config.setBoolOption("user", "first", value);
    }

    /**
     * Return the value of isFirst.
     *
     * @return the value of isFirst
     */
    public boolean isFirst() {
        return config.getBoolOption("user", "first", true);
    }

    /**
     * Change the contactMethod setting for this account
     *
     * @param value new value for contactMethod
     */
    public void setContactMethod(final String value) {
        config.setOption("user", "contactMethod", value);
    }

    /**
     * Get the contactMethod setting for this account
     *
     * @return value for contactMethod
     */
    public String getContactMethod() {
        return config.getOption("user", "contactMethod", "SNOTICE");
    }

    /**
     * Returns this accounts config.
     *
     * @return Account's config
     */
    public Config getConfig() {
        return config;
    }
}
