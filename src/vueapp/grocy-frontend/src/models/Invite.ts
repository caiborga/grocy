import { Role } from "./Role";

export enum InviteStatus {
	PENDING = "PENDING",
	ACCEPTED = "ACCEPTED",
	EXPIRED = "EXPIRED",
	REVOKED = "REVOKED"
}

export interface InvitePreview {
	householdId: string;
	householdName: string;
	role: Role;
	expiresAt: string;
	status: InviteStatus;
	remainingUses: number;
}
