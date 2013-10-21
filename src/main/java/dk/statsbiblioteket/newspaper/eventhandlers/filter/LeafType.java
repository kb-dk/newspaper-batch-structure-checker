package dk.statsbiblioteket.newspaper.eventhandlers.filter;

public enum LeafType {
    ALTO_XML(".alto.xml"),
    MIX_XML(".mix.xml"),
    JP2(".jp2"),
    MODS_XML(".mods.xml"),
    BRIK("-brik.jp2");

    private final String value;
    LeafType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static LeafType fromValue(String v) {
        for (LeafType c: LeafType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
