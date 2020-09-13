/* Copyright 2017-2020 Arraying
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

import com.ibdiscord.command.CommandContext;
import com.ibdiscord.command.abstractions.PaginatedCommand;
import com.ibdiscord.data.db.DataContainer;
import com.ibdiscord.data.db.entries.monitor.MonitorMessageData;
import com.ibdiscord.data.db.entries.monitor.MonitorUserData;
import com.ibdiscord.pagination.Page;
import com.ibdiscord.pagination.Pagination;
import com.ibdiscord.utils.UFormatter;
import com.ibdiscord.utils.UInput;
import de.arraying.gravity.Gravity;
import de.arraying.gravity.data.property.Property;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;

import java.util.List;
import java.util.stream.Collectors;

public final class MonitorList extends PaginatedCommand<String> {

    /**
     * Gets all the monitor thingies.
     * @param context The command context.
     * @return The pagination.
     */
    @Override
    protected Pagination<String> getPagination(CommandContext context) {
        Gravity gravity = DataContainer.INSTANCE.getGravity();
        List<String> a = gravity.load(new MonitorUserData(context.getGuild().getId())).values()
                .stream()
                .map(Property::asString)
                .sorted()
                .map(id -> {
                    String formatted = String.format("User: %s", id);

                    Member member = UInput.getMember(context.getGuild(), id);
                    if (member != null) {
                        formatted = String.format("%s (%s)", formatted, UFormatter.formatMember(member.getUser()));
                    }

                    return formatted;
                })
                .collect(Collectors.toList());

        List<String> b = gravity.load(new MonitorMessageData(context.getGuild().getId())).values()
                .stream()
                .map(Property::asString)
                .sorted()
                .map(it -> String.format("Regex: %s", it))
                .collect(Collectors.toList());
        a.addAll(b);

        return new Pagination<>(a, 10);
    }

    /**
     * Adds the monitored entity.
     * @param context The context.
     * @param embedBuilder The embed builder.
     * @param page The page.
     */
    @Override
    protected void handle(CommandContext context, EmbedBuilder embedBuilder, Page<String> page) {
        embedBuilder.addField(__(context, "info.entry") + page.getNumber(), page.getValue(), false);
    }

    /**
     * Adds a description.
     * @param context The context.
     * @param embedBuilder The embed builder.
     */
    @Override
    protected void tweak(CommandContext context, EmbedBuilder embedBuilder) {
        embedBuilder.setDescription(__(context, "info.entry_list"));
    }

}
