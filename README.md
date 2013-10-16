newspaper-batch-structure-checker
=================================
Provides functionality to check the directory and file structure of a batch

### Architecture
The Batch structure checker used a injected TreeIterator to transvers a batch tree. The TreeParser will either be backed by
a batch storeddirectly on the filesystem or as a DOMS model.

The Batch structure checker must for each pass be configured with a set of checkers and a ResultCollector,
so the checkers can enrich the ResultCollector which information on any failures to comply which the structure and
naming rules implemented in the checkers.


