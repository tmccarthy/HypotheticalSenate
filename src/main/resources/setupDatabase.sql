CREATE TABLE Election(
  electionID INTEGER UNIQUE,
  date TEXT(12),
  name TEXT
);

CREATE TABLE Party(
  electionID INTEGER,
  partyID TEXT(5),
  partyName TEXT,
  PRIMARY KEY (electionID, partyID),
  FOREIGN KEY (electionID) REFERENCES Election (electionID)
);

CREATE TABLE State(
  stateCode TEXT(3) UNIQUE,
  stateName TEXT,
  PRIMARY KEY(stateCode)
);

CREATE TABLE Candidate(
  electionID INTEGER,
  candidateID INTEGER,
  partyID TEXT(5),
  givenName TEXT,
  surname TEXT,
  PRIMARY KEY (electionID, candidateID),
  FOREIGN KEY (partyID) REFERENCES Party (partyID),
  FOREIGN KEY (electionID) REFERENCES Election (electionID)
);

CREATE TABLE GroupTicketInfo(
  electionID INTEGER,
  stateCode TEXT(3),
  groupID TEXT(2),
  ownerParty TEXT(5),
  PRIMARY KEY (electionID, stateCode, groupID),
  FOREIGN KEY (stateCode) REFERENCES State (stateCode),
  FOREIGN KEY (ownerParty) REFERENCES Party (partyID),
  FOREIGN KEY (electionID) REFERENCES Election (electionID)
);

CREATE TABLE GroupTicketPreference(
  electionID INTEGER,
  stateCode TEXT(3),
  ownerGroup TEXT(2),
  ticket INTEGER,
  preference INTEGER,
  preferencedCandidate INTEGER,
  PRIMARY KEY (stateCode, ownerGroup, ticket, preference),
  FOREIGN KEY (stateCode) REFERENCES State (stateCode),
  FOREIGN KEY (ownerGroup) REFERENCES GroupTicketInfo (groupID),
  FOREIGN KEY (preferencedCandidate) REFERENCES Candidate (candidateID),
  FOREIGN KEY (electionID) REFERENCES Election (electionID)
);

CREATE TABLE BelowTheLineBallot(
  electionID INTEGER,
  stateCode TEXT(3),
  ballotID INTEGER,
  batch INTEGER,
  paper INTEGER,
  candidateID INTEGER,
  preference INTEGER,
  PRIMARY KEY (stateCode, ballotID, candidateID),
  FOREIGN KEY (stateCode) REFERENCES State (stateCode),
  FOREIGN KEY (candidateID) REFERENCES Candidate (candidateID),
  FOREIGN KEY (electionID) REFERENCES Election (electionID)
);

CREATE TABLE AboveTheLineVotes(
  electionID INTEGER,
  stateCode TEXT(3),
  groupID TEXT(5),
  votes INTEGER,
  PRIMARY KEY (stateCode, groupID),
  FOREIGN KEY (stateCode) REFERENCES State (stateCode),
  FOREIGN KEY (groupID) REFERENCES GroupTicketInfo (groupID),
  FOREIGN KEY (electionID) REFERENCES Election (electionID)
)