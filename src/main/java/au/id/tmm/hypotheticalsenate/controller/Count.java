package au.id.tmm.hypotheticalsenate.controller;

import au.id.tmm.hypotheticalsenate.GUIMain;
import au.id.tmm.hypotheticalsenate.database.HypotheticalSenateDatabase;
import au.id.tmm.hypotheticalsenate.model.AustralianState;
import au.id.tmm.hypotheticalsenate.model.Election;
import au.id.tmm.hypotheticalsenate.model.Result;

import java.util.function.BiFunction;

/**
 * @author timothy
 */
public class Count {

    private final AustralianState state;
    private final Election election;
    private final HypotheticalSenateDatabase database;
    private final BiFunction<Election, AustralianState, BallotCollector> ballotCollectorConstructor;

    public Count(HypotheticalSenateDatabase database, Election election, AustralianState state, BiFunction<Election, AustralianState, BallotCollector> ballotCollectorConstructor) {
        this.state = state;
        this.election = election;
        this.database = database;

        this.ballotCollectorConstructor = ballotCollectorConstructor;
    }

    public Count(HypotheticalSenateDatabase database, Election election, AustralianState state) {
        this(database, election, state, BallotCollector::new);
    }

    public void perform() {
        GUIMain.out.println("Performing count for " + state.render());

        BallotCollector ballotCollector = this.ballotCollectorConstructor.apply(this.election, this.state)
                .loadBallots(this.database)
                .loadCandidates(this.database);

        BallotCounter ballotCounter = new BallotCounter(
                state.getNormalVacancies(),
                ballotCollector.getCandidates(),
                ballotCollector.getBallots());

        Result result = ballotCounter.run();

        result.printTo(GUIMain.out);
    }
}
