export type RegisterRequest = {
	displayName: string;
	email: string;
	password: string;
};

export type RegisterResponse = {
	email: string;
	message: string;
};
