import { Role } from "./Role";

export interface Me {
	id: string;
	name: string;
	email: string;
	createdAt: Date;
	updatedAt: Date;
	activeHouseholdId: string;
	role: Role;
}
