package cpen221.mp3.fsftbuffer;

/**
 * An immutable data type that represents a general bufferable object.
 */
public class Buffer implements Bufferable {
    private String id;
    private String text;

    /**
     * Abstraction function:
     *      AF(id) = identifier for the general bufferable object
     *      AF(text) = text associated with the general bufferable object
     */

    /**
     * Rep invariant:
     *      id and text != null
     */

    /**
     * Creates a Buffer object given an identifier.
     *
     * @param stringID identifier for the Buffer object
     * @param text associated text for the Buffer object
     */
    public Buffer(String stringID, String text){
        id = stringID;
        this.text = text;
    }

    /**
     * Returns the identifier associated with the Buffer object.
     *
     * @return a String representing the Buffer identifier
     */
    public String id() {
        return id;
    }

    /**
     * Returns the text associated with the Buffer object.
     *
     * @return a String representing the Buffer text
     */
    public String text() {
        return text;
    }

    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (!(o instanceof Buffer)) return false;

        Buffer buffer = (Buffer) o;
        return this.id.equals(buffer.id) && this.text.equals(buffer.text);
    }

    @Override
    public int hashCode() {
        return id.hashCode() + text.hashCode();
    }
}