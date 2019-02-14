package com.ibdiscord.command.commands;

import com.ibdiscord.command.Command;
import com.ibdiscord.command.CommandContext;
import com.ibdiscord.command.permissions.CommandPermission;
import com.ibdiscord.data.db.DContainer;
import com.ibdiscord.data.db.entries.GuildData;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.HashSet;
import java.util.Set;

/**
 * Copyright 2019 Ray Clark, Arraying
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public final class ModLogCommand extends Command {

    /**
     * The ID of a disabled modlog.
     */
    public static final long DISABLED_MOD_LOG = 0L;

    /**
     * Creates the command.
     */
    public ModLogCommand() {
        super("modlog",
                Set.of("setmodlog"),
                CommandPermission.discord(Permission.MANAGE_SERVER),
                new HashSet<>()
        );
    }

    /**
     * (Re) sets the mod log channel.
     * @param context The command context.
     */
    @Override
    protected void execute(CommandContext context) {
        TextChannel channel;
        if(context.getMessage().getMentionedChannels().isEmpty()) {
            channel = null;
        } else {
            channel = context.getMessage().getMentionedChannels().get(0);
        }
        GuildData guildData = DContainer.INSTANCE.getGravity().load(new GuildData(context.getGuild().getId()));
        guildData.set(GuildData.MODLOGS, channel == null ? DISABLED_MOD_LOG : channel.getId());
        DContainer.INSTANCE.getGravity().save(guildData);
        context.reply("The channel has been set to: " + (channel == null ? "nothing" : channel.getAsMention()) + ".");
    }

}