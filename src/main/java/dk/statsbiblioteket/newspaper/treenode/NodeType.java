package dk.statsbiblioteket.newspaper.treenode;

/**
 * Defines the types of nodes found in a batch.
 */
public enum NodeType {
    BATCH,
    ROUNDTRIP,
    WORKSHIFT_ISO_TARGET,
    FILM,
    ISO_TARGET_ON_FILM,
    UNMATCHED,
    UDGAVE,
    PAGE,
    PAGE_IMAGE;
}
