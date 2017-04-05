CREATE TABLE [dbo].[LeadContact](
	[Id] [int] IDENTITY(1,1) NOT NULL,
	[Name] [varchar](255) NULL,
	[Email] [varchar](255) NULL,
	[WebSite] [varchar](255) NULL,
	[RemoteAddress] [varchar](255) NULL,
	[Address] [varchar](max) NULL,
	[Phone] [varchar](255) NULL,
	[BusinessName] [varchar](255) NULL,
	[Source] [varchar](255) NULL,
	[timestamp] [datetime] NULL CONSTRAINT [DF_LeadContact_timestamp]  DEFAULT (getdate()),
 CONSTRAINT [PK_LeadContact] PRIMARY KEY CLUSTERED 
(
	[Id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]


CREATE TABLE [dbo].[LeadOrganization](
	[Id] [int] IDENTITY(1,1) NOT NULL,
	[Name] [varchar](max) NULL,
	[Address] [varchar](max) NULL,
	[City] [varchar](255) NULL,
	[StateProv] [varchar](255) NULL,
	[PostalCode] [varchar](255) NULL,
	[Country] [varchar](255) NULL,
 CONSTRAINT [PK_LeadOrganization] PRIMARY KEY CLUSTERED 
(
	[Id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]


CREATE TABLE [dbo].[LeadRemoteAddress](
	[ip] [varchar](255) NOT NULL,
	[LeadOrganization_uid] [varchar](255) NULL,
	[prefered] [int] NULL
) ON [PRIMARY]


CREATE TABLE [dbo].[leadSession](
	[Id] [int] IDENTITY(1,1) NOT NULL,
	[sessionId] [varchar](max) NULL,
	[remoteAddress] [varchar](max) NULL,
	[pageTitle] [varchar](max) NULL,
	[referer] [varchar](max) NULL,
	[location] [varchar](max) NULL,
	[text] [varchar](max) NULL,
	[userAgent] [varchar](max) NULL,
	[screenHeight] [varchar](max) NULL,
	[screenWidth] [varchar](max) NULL,
	[temp] [varchar](max) NULL,
	[timeIn] [datetime] NULL,
	[timeOut] [datetime] NULL,
 CONSTRAINT [PK_leadTemporary] PRIMARY KEY CLUSTERED 
(
	[Id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
