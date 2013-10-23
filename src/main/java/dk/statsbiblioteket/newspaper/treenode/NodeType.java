package dk.statsbiblioteket.newspaper.treenode;

/**
 * Defines the types of nodes found in a batch.
 */
public enum NodeType {
    BATCH,
    WORKSHIFT_ISO_TARGET,
    WORKSHIFT_TARGET,
    FILM,
    FILM_ISO_TARGET,
    FILM_TARGET,
    TARGET_IMAGE,
    UNMATCHED,
    EDITION,
    PAGE,
    PAGE_IMAGE;
}
