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
import de.arraying.gravity.Gravity;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.ISnowflake;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class ChannelOrderSnapshot implements CommandAction {

    /**
     * Creates snapshot of channel order.
     * @param context The command context.
     */
    @Override
    public void accept(CommandContext context) {
        if (context.getArguments().length > 0) {
            Category category = UInput.getCategory(context.getGuild(), context.getArguments()[0]);

            if (category == null) {
                context.replyI18n("error.category_invalid");
                return;
            }

            snapshot(context, Collections.singletonList(category));
        } else {
            snapshot(context, context.getGuild().getCategories());
        }
    }

    /**
     * Private function to handle snapshot.
     * @param context The command context.
     * @param categoryList List of category objects.
     */
    private void snapshot(CommandContext context, List<Category> categoryList) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Order Of Channels");
        Gravity gravity = DataContainer.INSTANCE.getGravity();

        ChannelData textChannelData = gravity.load(
                new ChannelData(context.getGuild().getId(), "text")
        );

        ChannelData voiceChannelData = gravity.load(
                new ChannelData(context.getGuild().getId(), "voice")
        );

        if (categoryList.size() > 1) {
            textChannelData.getKeys().forEach(textChannelData::unset);
            voiceChannelData.getKeys().forEach(voiceChannelData::unset);
        }

        categoryList.forEach(category -> {
            List<GuildChannel> textChannels = category.getTextChannels().size() < 1 ? null :
                    category.modifyTextChannelPositions().getCurrentOrder();

            List<GuildChannel> voiceChannels = category.getVoiceChannels().size() < 1 ? null :
                    category.modifyVoiceChannelPositions().getCurrentOrder();

            if (textChannels != null) {
                textChannelData.set(category.getId(), textChannels.stream()
                        .map(ISnowflake::getId)
                        .collect(Collectors.joining(",")));

                gravity.save(textChannelData);

                embedBuilder.addField(String.format("Text Channels (%s)",
                        category.getName()), textChannels.stream()
                        .map(GuildChannel::getName)
                        .collect(Collectors.joining(", ")), false);
            }

            if (voiceChannels != null) {
                voiceChannelData.set(category.getId(), voiceChannels.stream()
                        .map(ISnowflake::getId)
                        .collect(Collectors.joining(",")));

                gravity.save(voiceChannelData);

                embedBuilder.addField(String.format("Voice Channels (%s)",
                        category.getName()), voiceChannels.stream()
                        .map(GuildChannel::getName)
                        .collect(Collectors.joining(", ")), false);
            }
        });

        context.replyEmbed(embedBuilder.build());
    }
}