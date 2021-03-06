/*
 * Copyright (c) 2006-2017 DFBnc Developers
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
 */
package com.dfbnc.servers.irc.commands;

import com.dfbnc.commands.CommandManager;
import com.dfbnc.commands.AbstractListEditCommand;

/**
 * This file represents the Perform-related commands
 */
public class PerformCommand extends AbstractListEditCommand {
    /**
     * Get the name of the property to store the list in.
     *
     * @param command The command passed as param[0]
     * @return The name of the property to store the list in.
     */
    @Override
    public String getPropertyName(final String command) {
        if (command.equalsIgnoreCase("cperform")) {
            return "perform.connect";
        } else if (command.equalsIgnoreCase("dperform")) {
            return "perform.lastdetach";
        } else if (command.equalsIgnoreCase("aperform")) {
            return "perform.firstattach";
        }
        return "";
    }

    /**
     * Get the name of the domain to store the list in.
     *
     * @param command The command passed as param[0]
     * @return The name of the domain to store the list in.
     */
    @Override
    public String getDomainName(final String command) { return "irc"; }

    /**
     * Get the name of the list.
     * This is used in various outputs from the command.
     *
     * @param command The command passed as param[0]
     * @return The name of the list
     */
    @Override
    public String getListName(final String command) {
        if (command.equalsIgnoreCase("cperform")) {
            return "onConnect Perform";
        } else if (command.equalsIgnoreCase("dperform")) {
            return "onLastDetach Perform";
        } else if (command.equalsIgnoreCase("aperform")) {
            return "onFirstAttach Perform";
        }
        return "";
    }

    /**
     * Get the output to give for an "add", "edit" or "ins" request without sufficient parameters
     *
     * @param command Command to get usage info for (add, edit, ins)
     * @return The output to give
     */
    @Override
    public String[] getUsageOutput(final String command) {
        if (command.equalsIgnoreCase("add")) {
            return new String[]{"You must specify something to add to the perform"};
        } else if (command.equalsIgnoreCase("edit")) {
            return new String[]{"You must specify a position number to edit, and something to use in the perform"};
        } else if (command.equalsIgnoreCase("ins")) {
            return new String[]{"You must specify a position to insert this item, and something to use in the perform"};
        } else {
            return new String[]{""};
        }
    }

    /**
     * Get the output to give for the syntax to add/edit commands to show valid input
     *
     * @return The output to give for the syntax to add/edit commands to show valid input
     */
    @Override
    public String getAddUsageSyntax() {
        return "<perform item>";
    }

    /**
     * Get the output to give for /dfbnc <command> on its own.
     * Returning null gives default output.
     *
     * @param command Command to get output for
     * @return The output to give
     */
    @Override
    public String[] getHelpOutput(final String command) {
        if (command.equalsIgnoreCase("perform")) {
            return new String[]{"DFBnc provides 3 types of perform.",
                                "    * On Connect (cperform) - When the BNC Connects to the IRC Server, with or without the user connected",
                                "    * On First Attach (aperform) - When the first user connects to the bnc whilst connected to a server",
                                "    * On Last Detach (dperform) - When the last user disconnects from the bnc whilst connected to a server (Also after cperform if no user is connected)"
                               };
        }
        return null;
    }

    /**
     * Can this list be added to?
     * (This also disables edit and insert)
     *
     * @param command Command to get output for
     * @return If this list can be added to.
     */
    @Override
    public boolean canAdd(final String command) { return true; }

    /**
     * What does this Command handle.
     *
     * @return String[] with the names of the tokens we handle.
     */
    @Override
    public String[] handles() {
        return new String[]{"perform", "cperform", "dperform", "aperform"};
    }

    /**
     * Create a new instance of the Command Object
     *
     * @param manager CommandManager that is in charge of this Command
     */
    public PerformCommand (final CommandManager manager) { super(manager); }

    /**
     * Get a description of what this command does
     *
     * @param command The command to describe (incase one Command does multiple
     *                things under different names)
     * @return A description of what this command does
     */
    @Override
    public String getDescription(final String command) {
        if (command.equalsIgnoreCase("cperform")) {
            return "This command lets you manipulate the onConnect Perform";
        } else if (command.equalsIgnoreCase("dperform")) {
            return "This command lets you manipulate the onLastDetach Perform";
        } else if (command.equalsIgnoreCase("aperform")) {
            return "This command lets you manipulate the onFirstAttach Perform";
        } else {
            return "This command gives you information on the types of Perform DFBnc provides";
        }
    }
}
