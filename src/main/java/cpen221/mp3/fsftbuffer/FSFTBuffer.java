package cpen221.mp3.fsftbuffer;

import cpen221.mp3.exceptions.InvalidObjectException;

import java.util.HashMap;

/**
 * Insert class abstraction function
 */
public class FSFTBuffer<T extends Bufferable> {

    public static final int DSIZE = 32;
    public static final int DTIMEOUT = 3600;
    private int maxTime;
    private int maxCapacity;
    private HashMap<String, T> buffer;
    private HashMap<String, Integer> accessTimes;

    /**
     * Create a buffer with a fixed capacity and a timeout value.
     * Objects in the buffer that have not been refreshed within the
     * timeout period are removed from the cache.
     *
     * @param capacity the number of objects the buffer can hold
     * @param timeout  the duration, in seconds, an object should
     *                 be in the buffer before it times out
     */
    public FSFTBuffer(int capacity, int timeout) {
        buffer = new HashMap<>();
        accessTimes = new HashMap<>();
        maxTime = timeout;
        maxCapacity = capacity;
    }

    /**
     * Create a buffer with default capacity and timeout values.
     */
    public FSFTBuffer() {
        this(DSIZE, DTIMEOUT);
    }

    /**
     * Add a value to the buffer.
     * If the buffer is full then remove the least recently accessed
     * object to make room for the new object.
     *
     * @param t the value to be added to the buffer
     * @return true if {@code t} has been successfully added, false otherwise
     */
    public boolean put(T t) {
        int currentTime = (int) System.currentTimeMillis() / 1000;
        updateCache(currentTime);

        // if buffer is full, remove object with oldest time
        if (accessTimes.size() >= maxCapacity) {
            String id = accessTimes.entrySet().stream().min((e1, e2) -> e1.getValue() > e2.getValue() ? 1: -1).get().getKey();
            buffer.remove(id);
            accessTimes.remove(id);
        }

        // if value isn't in buffer, add it in with current time and return true
        if (!buffer.containsKey((t.id()))) {
            buffer.put(t.id(), t);
            accessTimes.put(t.id(), currentTime);
            return true;
        }

        // if value is in buffer and hasn't timed out, change nothing and return false
        return false;
    }

    /**
     * Retrieves a given object from the buffer based on its identifier.
     *
     * @param id the identifier of the object to be retrieved
     * @return the object that matches the identifier from the buffer
     * @throws InvalidObjectException if there is no such identifier in the buffer
     */
    public T get(String id) throws InvalidObjectException {
        int currentTime = (int) System.currentTimeMillis() / 1000;
        updateCache(currentTime);

        if (buffer.containsKey(id)) {
            // renew access time of object t
            accessTimes.put(id, currentTime);
           return buffer.get(id);
        }

        throw new InvalidObjectException();
    }

    /**
     * Update the last refresh time for the object with the provided id.
     * This method is used to mark an object as "not stale" so that its
     * timeout is delayed.
     *
     * @param id the identifier of the object to "touch"
     * @return true if successful and false if no object with the provided id
     * exists in the buffer
     */
    public boolean touch(String id) {
        int currentTime = (int) System.currentTimeMillis() / 1000;
        updateCache(currentTime);

        if (buffer.containsKey(id)) {
            // renew access time of object t
            accessTimes.put(id, currentTime);
            return true;
        }
        return false;
    }

    /**
     * Update an object in the buffer.
     * This method updates an object and acts like a "touch" to
     * renew the object in the cache.
     *
     * @param t the object to update
     * @return true if successful and if the object doesn't exist in the buffer
     */
    public boolean update(T t) {
        int currentTime = (int) System.currentTimeMillis() / 1000;
        updateCache(currentTime);

        if (buffer.containsKey(t.id())) {
            // renew access time of object t
            accessTimes.put(t.id(), currentTime);
            return true;
        }
        return false;
    }

    /**
     * Updates the buffer so that all timed-out objects are removed.
     * @param currentTime the time at which the buffer is accessed
     */
    private void updateCache(int currentTime) {
        // remove all timed-out objects
        accessTimes.entrySet().removeIf(e -> e.getValue() + maxTime < currentTime);
        buffer.entrySet().removeIf(e -> !accessTimes.containsKey(e.getKey()));
    }
}
