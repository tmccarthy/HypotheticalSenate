CREATE TABLE Party(
  partyID TEXT(5) UNIQUE,
  partyName TEXT,
  PRIMARY KEY(partyID)
);

CREATE TABLE State(
  stateCode TEXT(3) UNIQUE,
  stateName TEXT,
  PRIMARY KEY(stateCode)
);

CREATE TABLE Candidate(
  candidateID INTEGER UNIQUE,
  partyID TEXT(5),
  givenName TEXT,
  surname TEXT,
  PRIMARY KEY (candidateID),
  FOREIGN KEY (partyID) REFERENCES Party (partyID)
);

CREATE TABLE GroupTicketInfo(
  stateCode TEXT(3),
  groupID TEXT(2),
  ownerParty TEXT(5),
  PRIMARY KEY (stateCode, groupID),
  FOREIGN KEY (stateCode) REFERENCES State (stateCode),
  FOREIGN KEY (ownerParty) REFERENCES Party (partyID)
);

CREATE TABLE GroupTicketPreference(
  stateCode TEXT(3),
  ownerGroup TEXT(2),
  ticket INTEGER,
  preference INTEGER,
  preferencedGroup TEXT(2),
  PRIMARY KEY (stateCode, ownerGroup, ticket, preference),
  FOREIGN KEY (stateCode) REFERENCES State (stateCode),
  FOREIGN KEY (ownerGroup) REFERENCES GroupTicketInfo (groupID),
  FOREIGN KEY (preferencedGroup) REFERENCES GroupTicketInfo (groupID)
);

CREATE TABLE BelowTheLineBallot(
  stateCode TEXT(3),
  ballotBatch INTEGER,
  ballotPaper INTEGER,
  candidateID INTEGER,
  preference INTEGER,
  PRIMARY KEY (stateCode, ballotBatch, ballotPaper, candidateID),
  FOREIGN KEY (stateCode) REFERENCES State (stateCode),
  FOREIGN KEY (candidateID) REFERENCES Candidate (candidateID)
);

CREATE TABLE AboveTheLineVotes(
  stateCode TEXT(3),
  groupID TEXT(2),
  votes INTEGER,
  PRIMARY KEY (stateCode, groupID),
  FOREIGN KEY (stateCode) REFERENCES State (stateCode),
  FOREIGN KEY (groupID) REFERENCES GroupTicketInfo
)