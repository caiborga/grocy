package de.skit.grocy.common.exceptions;

import java.util.List;
import java.util.UUID;

public class OwnershipTransferRequiredException extends ConflictException {

    public OwnershipTransferRequiredException(List<UUID> householdIds) {
        super(
                "ownership_transfer_required",
                "Bitte übertrage zuerst das Eigentum an Haushalten mit weiteren Mitgliedern.",
                householdIds);
    }
}
