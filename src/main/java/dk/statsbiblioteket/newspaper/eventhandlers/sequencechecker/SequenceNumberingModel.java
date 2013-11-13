package dk.statsbiblioteket.newspaper.eventhandlers.sequencechecker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.DefaultTreeEventHandler;

public class SequenceNumberingModel extends DefaultTreeEventHandler {
    private final ResultCollector resultCollector;
    private final List<SequenceMember> members = new ArrayList<>();

    public SequenceNumberingModel(ResultCollector resultCollector) {
        this.resultCollector = resultCollector;
    }

    public void addNumber(int number, String name) {
        members.add(new SequenceMember(number, name));
    }

    public void verifySequence() {
        Collections.sort(members);
        SequenceMember previousMember = null;
        for (SequenceMember currentMember : members) {
            if (previousMember == null) {
                if (shouldStartWithOne() && currentMember.number != 1) {
                    addFailure(currentMember.name, "Numbering didn't start with 1 as required");
                }
            } else if (currentMember.number == previousMember.number) {
                addFailure(currentMember.name, "Duplicate number in sequence, previous elements was " +
                        previousMember.name);
            } else if (currentMember.number > previousMember.number +1) {
                addFailure(currentMember.name, "Missing number in sequence, previous elements was " +
                        previousMember.name);
            }
            previousMember = currentMember;
        }
    }

    private class SequenceMember implements Comparable<SequenceMember> {
        private final int number;
        private final String name;

        public SequenceMember(int number, String name) {
            this.number = number;
            this.name = name;
        }

        @Override
        public int compareTo(SequenceMember sequenceMember) {
            return this.number - sequenceMember.number;
        }
    }

    private void addFailure(String reference, String description) {
        resultCollector.addFailure(
                reference, "SequenceProblem", getClass().getSimpleName(), description);
    }

    protected boolean shouldStartWithOne() {
        return true;
    }
}
