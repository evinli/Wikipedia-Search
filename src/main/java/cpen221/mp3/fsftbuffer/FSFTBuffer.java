package cpen221.mp3.fsftbuffer;

import cpen221.mp3.exceptions.InvalidObjectException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A mutable data type that represents a finite-space, finite-time buffer.
 */
public class FSFTBuffer<T extends Bufferable> {

    public static final int DSIZE = 32;
    public static final int DTIMEOUT = 3600;
    private static final int CONVERT_MS_TO_S = 1000;
    private int maxTime;
    private int maxCapacity;
    private Stack<T> buffer;
    private HashMap<String, T> bufferItems;
    private HashMap<String, Integer> accessTimes;

    /**
     * Abstraction function:
     *      AF(buffer) = all objects in the finite-space, finite-time buffer
     *      AF(bufferItems.keySet()) = IDs of all objects in the finite-space,
     *                                 finite-time buffer
     *      AF(accessTimes.get(ID)) = latest access time for a given object ID
     *      AF(maxTime) = the duration (in seconds) before an object times out
     *                    in the buffer
     *      AF(maxCapacity) = the maximum number of objects the buffer can hold
     */

    /**
     * Rep invariant:
     *      for every object in buffer, there should be a key corresponding
     *          to object.id() that exists both within bufferItems.keySet()
     *          and accessTimes.keySet()
     *      buffer contains no duplicate objects
     */

    /**
     * Check that the rep invariant is true.
     */
    private void checkRep() {
        for (T object : buffer) {
            assert (bufferItems.containsKey(object.id()));
            assert (accessTimes.containsKey(object.id()));
        }
        assert (buffer.stream().distinct().collect(Collectors.
                toCollection(ArrayList::new)).size() == buffer.size());
    }

    /**
     * Create a buffer with a fixed capacity and a timeout value.
     * Objects in the buffer that have not been refreshed within the
     * timeout period are removed from the cache. In other words,
     * objects are removed if the current time is >= than its
     * designated timeout time.
     *
     * @param capacity the number of objects the buffer can hold
     * @param timeout  the duration, in seconds, an object should
     *                 be in the buffer before it times out
     */
    public FSFTBuffer(int capacity, int timeout) {
        buffer = new Stack<>();
        bufferItems = new HashMap<>();
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
     * Add a object to the buffer.
     * If the buffer is full then remove the least recently used
     * object to make room for the new object. Note that repeated
     * "puts" of an object already in the buffer do not
     * refresh its access time, usage time nor does it update the
     * actual object itself in the buffer.
     *
     * @param t the object to be added to the buffer
     * @return true if {@code t} has been successfully added, false otherwise
     */
    public boolean put(T t) {
        int currentTime = (int) System.currentTimeMillis() / CONVERT_MS_TO_S;
        updateBuffer(currentTime);

        if (t == null) {
            checkRep();
            return false;
        }
        synchronized (this) {
            if (buffer.size() == maxCapacity) {
                bufferItems.remove(buffer.get(0).id());
                accessTimes.remove(buffer.get(0).id());
                buffer.remove(0);
            }
            if (!buffer.contains(t)) {
                bufferItems.put(t.id(), t);
                accessTimes.put(t.id(), currentTime);
                buffer.push(t);
            }
            checkRep();
        }
        return true;
    }

    /**
     * Retrieves a given object from the buffer based on its identifier. Note
     * that repeated "gets" of an object affects its usage time, but not its
     * access time.
     *
     * @param id the identifier of the object to be retrieved
     * @return the object that matches the identifier from the buffer
     * @throws InvalidObjectException if there is no such identifier in the
     * buffer
     */
    public T get(String id) throws InvalidObjectException {
        int currentTime = (int) System.currentTimeMillis() / CONVERT_MS_TO_S;
        updateBuffer(currentTime);

        synchronized (this) {
            if (bufferItems.containsKey(id)) {
                buffer.remove(buffer.indexOf(bufferItems.get(id)));
                buffer.push(bufferItems.get(id));
                checkRep();
                return buffer.peek();
            }
        }
        throw new InvalidObjectException();
    }

    /**
     * Update the last access time for the object with the provided id so as to
     * extend its absolute timeout time. Note that repeated "touches" of an
     * object affect its access time, but not its usage time.
     *
     * @param id the identifier of the object to "touch"
     * @return true if successful and false if no object with the provided id
     * exists in the buffer
     */
    public boolean touch(String id) {
        int currentTime = (int) System.currentTimeMillis() / CONVERT_MS_TO_S;
        updateBuffer(currentTime);

        synchronized (this) {
            if (bufferItems.containsKey(id)) {
                accessTimes.put(id, currentTime);
                return true;
            }
        }
        return false;
    }

    /**
     * Update an object in the buffer.
     * This method updates an object and also acts like a "touch" to
     * renew the object in the cache. Note that repeated "updates" of
     * an object affect its access time, but not its usage time.
     *
     * @param t the object to update
     * @return true if successful and if the object doesn't exist in the buffer
     */
    public boolean update(T t) {
        int currentTime = (int) System.currentTimeMillis() / CONVERT_MS_TO_S;
        updateBuffer(currentTime);

        synchronized (this) {
            if (bufferItems.containsKey(t.id())) {
                buffer.set(buffer.indexOf(bufferItems.get(t.id())), t);
                bufferItems.put(t.id(), t);
                accessTimes.put(t.id(), currentTime);
                checkRep();
                return true;
            }
        }
        return false;
    }

    /**
     * Updates the buffer so that all timed-out objects are removed.
     *
     * @param currentTime the time at which the buffer is accessed
     */
    private void updateBuffer(int currentTime) {
        synchronized (this) {
            accessTimes.entrySet().removeIf(o -> o.getValue() + maxTime
                    <= currentTime);
            bufferItems.entrySet().removeIf(o -> !accessTimes.containsKey
                    (o.getKey()));
            buffer.removeIf(o -> !bufferItems.containsValue(o));
            checkRep();
        }
    }
}
