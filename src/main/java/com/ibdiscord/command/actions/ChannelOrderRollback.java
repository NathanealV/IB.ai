/* Copyright 2020 Nathaneal Varghese
 *
 * This file is part of IB.ai.
 *
 * IB.ai is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * IB.ai is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with IB.ai. If not, see http://www.gnu.org/licenses/.
 */

package com.ibdiscord.command.actions;

import com.ibdiscord.command.CommandAction;
import com.ibdiscord.command.CommandContext;
import com.ibdiscord.data.db.DataContainer;
import com.ibdiscord.data.db.entries.ChannelData;
import com.ibdiscord.utils.UInput;
import com.ibdiscord.utils.UPermission;
import com.ibdiscord.utils.UString;
import de.arraying.gravity.Gravity;
import net.dv8tion.jda.api.entities.*;

public final class ChannelOrderRollback implements CommandAction {

    /**
     * Rolls back channel positions.
     *
     * @param context The command context.
     */
    @Override
    public void accept(CommandContext context) {
        Gravity gravity = DataContainer.INSTANCE.getGravity();

        ChannelData textChannelData = gravity.load(new ChannelData(context.getGuild().getId(), "text"));
        ChannelData voiceChannelData = gravity.load(new ChannelData(context.getGuild().getId(), "voice"));

        if (context.getArguments().length > 0) {
            String identifier = UString.concat(context.getArguments(), " ", 0).toLowerCase();
            Category category = UInput.getCategory(context.getGuild(), identifier);

            if (category == null) {
                context.replyI18n("error.category_invalid");
                return;
            }

            String textChannels = textChannelData.get(category.getId()).defaulting("").toString();
            String voiceChannels = voiceChannelData.get(category.getId()).defaulting("").toString();

            reorder(context, category.getId(), textChannels);
            reorder(context, category.getId(), voiceChannels);
        } else {
            textChannelData.getKeys().forEach(categoryId -> {
                reorder(context, categoryId, textChannelData.get(categoryId).toString());
            });

            voiceChannelData.getKeys().forEach(categoryId -> {
                reorder(context, categoryId, voiceChannelData.get(categoryId).toString());
            });
        }

        context.replyI18n("success.done");
    }

    /**
     * Private function to handle reorder.
     * @param context The command context.
     * @param categoryId The category ID.
     * @param channelList List of channels as string.
     */
    private void reorder(CommandContext context, String categoryId, String channelList) {
        Member selfMember = context.getGuild().getSelfMember();
        String[] channels = channelList.split(",");

        for (int i = 0; i < channels.length; i++) {
            GuildChannel channel = UInput.getChannelGuild(context.getGuild(), channels[i], false);
            if (channel != null && UPermission.canMoveChannel(selfMember, channel)) {
                channel.getManager().setParent(context.getGuild().getCategoryById(categoryId))
                        .setPosition(i).queue();
            }
        }
    }
}