package dk.statsbiblioteket.newspaper.eventhandlers.sequencechecker;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.DefaultTreeEventHandler;
import dk.statsbiblioteket.newspaper.structureChecker.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Used for building a set of number-name pairs, which can later be verified for completeness. This means:
 *     The numbers are in sequence, without holes and starts with 1. The constraint on the start with 1 can
 *     be overruled by overriding the #shouldStartWithOne() method.
 *     <p>
 *     Note that the number-name pairs doesn't need to be added in order, as they will be sorted on verify.
 *     </p>
 */
public class SequenceNumberingModel extends DefaultTreeEventHandler {
    private final ResultCollector resultCollector;
    private final List<SequenceMember> members = new ArrayList<>();
    private final String descriptionPrefix;

    public SequenceNumberingModel(ResultCollector resultCollector, String descriptionPrefix) {
        this.resultCollector = resultCollector;
        this.descriptionPrefix = descriptionPrefix;
    }

    public void addNumber(int number, String name) {
        members.add(new SequenceMember(number, name));
    }

    /**
     * Verifies the numbers in the added number-name pairs start with 1 and are in sequence without holes or duplicates.
     *
     * The number-name pairs are sorted according to numbers before the check.
     */
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
                reference, Constants.TYPE, getClass().getSimpleName(),
                descriptionPrefix + description);
    }

    /**
     * Defines that the sequence must start with one. May be overridden by subclasses.
     */
    protected boolean shouldStartWithOne() {
        return true;
    }
}
