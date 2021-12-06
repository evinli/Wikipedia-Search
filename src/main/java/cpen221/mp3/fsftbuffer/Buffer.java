package cpen221.mp3.fsftbuffer;

/**
 * An immutable data type that represents a general bufferable object.
 */
public class Buffer implements Bufferable {
    private String id;

    /**
     * Abstraction function:
     *      AF(id) = identifier for the general bufferable object
     */

    /**
     * Rep invariant:
     *      id != null
     */

    /**
     * Creates a Buffer object given an identifier.
     *
     * @param stringID identifier for the Buffer object
     */
    public Buffer(String stringID){
        this.id = stringID;
    }

    /**
     * Returns the identifier associated with the Buffer object.
     *
     * @return a String representing the Buffer identifier
     */
    public String id() {
        return this.id;
    }

    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (!(o instanceof Buffer)) return false;

        Buffer buffer = (Buffer) o;
        return this.id.equals(buffer.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}