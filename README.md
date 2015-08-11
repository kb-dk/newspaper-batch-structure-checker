newspaper-batch-structure-checker
=================================
Provides functionality to check the directory and file structure of a batch

### Architecture
The Batch structure checker uses an injected TreeIterator to traverse a batch tree.
The TreeParser will either be backed by a batch stored directly on the filesystem or
as a DOMS object structure.

The Batch structure checker must for each pass be configured with a set of checkers
and a ResultCollector, so the checkers can enrich the ResultCollector which information
on any failures to comply which the structure and naming rules implemented in the checkers.

### The Modules

 * newspaper-batch-structure-checker-component is the autonomous component.
 depends on
     * newspaper-batch-structure-checker-checkers is the master module for all the checks. Depend on this to get all the various metadata checks
   depends on
         * newspaper-batch-structure-checker-mfpakcheckers is the checkers that check metadata against the MFPak database system
         * newspaper-batch-structure-checker-sequencecheckers is the checkers that check that the various parts of a batch are in sequence
    both these depend on
             * newspaper-batch-structure-checker-common is the common checks, that are not all that newspaper specific
  
