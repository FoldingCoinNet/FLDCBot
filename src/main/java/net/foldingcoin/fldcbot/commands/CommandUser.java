package net.foldingcoin.fldcbot.commands;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.math.NumberUtils;

import net.darkhax.botbase.BotBase;
import net.darkhax.botbase.commands.Command;
import net.darkhax.botbase.embed.MessageUser;
import net.darkhax.botbase.utils.UserUtils;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

/**
 * This command allows users to look up info about other users on Discord.
 */
public class CommandUser implements Command {

    @Override
    public void processCommand (BotBase bot, IChannel channel, IMessage message, String[] params) {

        // This command requires a guild to work, and at least one parameter.
        if (params.length >= 1 && !channel.isPrivate()) {

            // List of all the users that were found for the input.
            final Set<IUser> users = this.getUsers(bot, message, params);

            // Checks if no users were found, and gives a warning if it was.
            if (users.isEmpty()) {

                bot.sendMessage(channel, "No users were found for your request.");
                return;
            }

            // Iterate through all users and print their info to chat.
            for (final IUser user : users) {

                final MessageUser embed = new MessageUser(user, channel.getGuild(), true);
                embed.withColor(user.getColorForGuild(channel.getGuild()));
                bot.sendMessage(channel, embed.build());
            }
        }
        else if (channel.isPrivate()) {
            bot.sendMessage(channel, "Sorry, this command can only be used in a server!");
        }
        else {
            bot.sendMessage(channel, "This bot requires an argument!");
        }
    }

    private Set<IUser> getUsers (BotBase bot, IMessage message, String... users) {

        // Set of users allows for multiple users to be returned.
        final Set<IUser> found = new HashSet<>();

        // If the message doesn't mention @Everyone or @Here, grab all users.
        if (!message.mentionsEveryone() && !message.mentionsHere()) {

            found.addAll(message.getMentions());
        }

        // Attempts to parse input for valid user messages.
        for (final String userParam : users) {

            // Allows users to use 'me' to refer to their own info.
            if ("me".equalsIgnoreCase(userParam)) {

                found.add(message.getAuthor());
            }

            // Allows users to use 'bot' to refer to the bot user.
            else if ("bot".equals(userParam)) {

                found.add(bot.instance.getOurUser());
            }

            else if (message.getGuild() != null) {

                final IGuild guild = message.getGuild();

                // Add all users with a similar username.
                found.addAll(UserUtils.getUsersByName(guild, userParam));

                // If the name is a full number, attempt user id lookup as well.
                if (!userParam.contains(".") && NumberUtils.isParsable(userParam)) {

                    final IUser user = guild.getUserByID(Long.parseLong(userParam));

                    if (user != null) {

                        found.add(user);
                    }
                }
            }
        }

        return found;
    }

    @Override
    public String getDescription () {

        return "Displays info about the user.";
    }
}