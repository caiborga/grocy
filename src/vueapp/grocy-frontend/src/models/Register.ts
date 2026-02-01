export type RegisterRequest = {
	displayName: string;
	email: string;
	password: string;
};

export type RegisterResponse = {
	id: string;
	displayName: string;
	email: string;
	createdAt: string;
	updatedAt: string;
    activeHouseholdId: string;
};
