package de.skit.grocy.admin;

public record AdminStatsResponse(
        long usersTotal,
        long usersCreatedLast7Days,
        long usersCreatedLast30Days,
        long usersActiveLast7Days,
        long usersActiveLast30Days,
        long householdsTotal,
        long invitesActive,
        long accountDeletionsTotal,
        String activeUserDefinition) {
}
