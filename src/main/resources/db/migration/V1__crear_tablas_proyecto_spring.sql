
-- public.users definition

-- Drop table

-- DROP TABLE public.users;

CREATE TABLE public.users (
	id uuid NOT NULL,
	full_name varchar(256) NOT NULL,
	username varchar(64) NOT NULL,
	"password" varchar(128) NOT NULL,
	verified bool NOT NULL,
	image_id uuid NULL,
	erased bool NOT NULL,
	created timestamp NOT NULL,
	updated timestamp NOT NULL,
	CONSTRAINT users_pkey PRIMARY KEY (id),
	CONSTRAINT users_username_key UNIQUE (username)
);


-- public.profiles definition

-- Drop table

-- DROP TABLE public.profiles;

CREATE TABLE public.profiles (
	id uuid NOT NULL,
	"name" varchar(64) NOT NULL,
	erased bool NOT NULL,
	created timestamp NOT NULL,
	updated timestamp NOT NULL,
	CONSTRAINT profiles_name_key UNIQUE ("name"),
	CONSTRAINT profiles_pkey PRIMARY KEY (id)
);


-- public.user_profile definition

-- Drop table

-- DROP TABLE public.user_profile;

CREATE TABLE public.user_profile (
	profile_id uuid NOT NULL,
	user_id uuid NOT NULL,
	CONSTRAINT user_profile_pk_restrict UNIQUE (user_id, profile_id),
	CONSTRAINT user_profile_user_id_profile_id_key UNIQUE (user_id, profile_id),
	CONSTRAINT fk_user_profile_profiles FOREIGN KEY (profile_id) REFERENCES public.profiles(id),
	CONSTRAINT fk_user_profile_users FOREIGN KEY (user_id) REFERENCES public.users(id)
);


-- public.categories definition

-- Drop table

-- DROP TABLE public.categories;

CREATE TABLE public.categories (
	id uuid NOT NULL,
	"name" varchar(64) NOT NULL,
	"description" varchar(128) NOT NULL,
	create_at timestamp NOT NULL,
	update_at timestamp NOT NULL,
	erased bool NOT NULL,
	CONSTRAINT categories_pkey PRIMARY KEY (id)
);


-- public.assistance definition

-- Drop table

-- DROP TABLE public.assistance;

CREATE TABLE public.assistance (
	id uuid NOT NULL,
	hora_entrada time NULL,
	hora_salida time NULL,
	justified_absence bool NULL,
	on_time bool NULL,
	retard bool NULL,
	unjustified_absence bool NULL,
	work_date date NOT NULL,
	user_id uuid NOT NULL,
	create_at timestamp NOT NULL,
	update_at timestamp NOT NULL,
	CONSTRAINT assistance_pkey PRIMARY KEY (id),
	CONSTRAINT fk_assistance_users FOREIGN KEY (user_id) REFERENCES public.users(id)
);


-- public.expenses definition

-- Drop table

-- DROP TABLE public.expenses;

CREATE TABLE public.expenses (
	id uuid NOT NULL,
	"description" varchar(256) NOT NULL,
	amount numeric(21, 8) NOT NULL,
	bank_or_entity varchar(64) NOT NULL,
	payment_date date NOT NULL,
	payment_status bool NOT NULL,
	category_id uuid NOT NULL,
	user_id uuid NOT NULL,
	create_at timestamp NOT NULL,
	update_at timestamp NOT NULL,
	erased bool NOT NULL,
	CONSTRAINT expenses_pkey PRIMARY KEY (id),
	CONSTRAINT fk_expenses_users FOREIGN KEY (user_id) REFERENCES public.users(id),
	CONSTRAINT fk_expenses_category FOREIGN KEY (category_id) REFERENCES public.categories(id)
);


-- public.income definition

-- Drop table

-- DROP TABLE public.income;

CREATE TABLE public.income (
	id uuid NOT NULL,
	"description" varchar(256) NOT NULL,
	amount numeric(21, 8) NOT NULL,
	"source" varchar(128) NOT NULL,
	payment_date date NOT NULL,
	user_id uuid NOT NULL,
	create_at timestamp NOT NULL,
	update_at timestamp NOT NULL,
	erased bool NOT NULL,
	CONSTRAINT income_pkey PRIMARY KEY (id),
	CONSTRAINT fk_income_users FOREIGN KEY (user_id) REFERENCES public.users(id)
);


-- public.modules definition

-- Drop table

-- DROP TABLE public.modules;

CREATE TABLE public.modules (
	id uuid NOT NULL,
	"name" varchar(255) NOT NULL,
	"route" varchar(255) NOT NULL,
	icon_name varchar(255) NULL,
	"level" varchar(255) NULL,
	"description" varchar(255) NULL,
	is_parent bool NOT NULL,
	parent_id uuid NULL,
	erased bool NOT NULL,
	CONSTRAINT modules_name_key UNIQUE ("name"),
	CONSTRAINT modules_pkey PRIMARY KEY (id),
	CONSTRAINT fk_module_parent_modules FOREIGN KEY (parent_id) REFERENCES public.modules(id)
);


-- public.notes definition

-- Drop table

-- DROP TABLE public.notes;

CREATE TABLE public.notes (
	id uuid NOT NULL,
	title varchar(128) NULL,
	"content" text NULL,
	user_id uuid NOT NULL,
	erased bool NOT NULL,
	create_at timestamp NOT NULL,
	update_at timestamp NOT NULL,
	CONSTRAINT notes_pkey PRIMARY KEY (id),
	CONSTRAINT fk_notes_users FOREIGN KEY (user_id) REFERENCES public.users(id)
);


-- public.profile_modules definition

-- Drop table

-- DROP TABLE public.profile_modules;

CREATE TABLE public.profile_modules (
	profile_id uuid NOT NULL,
	module_id uuid NOT NULL,
	CONSTRAINT profile_modules_pkey PRIMARY KEY (module_id, profile_id),
	CONSTRAINT fk_profile_module_profiles FOREIGN KEY (profile_id) REFERENCES public.profiles(id),
	CONSTRAINT fk_profile_module_modules FOREIGN KEY (module_id) REFERENCES public.modules(id)
);


-- public.vault definition

-- Drop table

-- DROP TABLE public.vault;

CREATE TABLE public.vault (
    id uuid NOT NULL,
    site_name varchar(128) NOT NULL,
	site_url varchar(256) NULL,
	username varchar(128) NULL,
    password_encrypted text NULL,
	custom_key varchar(256) NULL,
	notes text NULL,
    user_id uuid NOT NULL,
    erased bool NOT NULL,
	create_at timestamp NOT NULL,
	update_at timestamp NOT NULL,
	CONSTRAINT vault_pkey PRIMARY KEY (id),
	CONSTRAINT fk_vault_users FOREIGN KEY (user_id) REFERENCES public.users(id)
);