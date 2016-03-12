package org.seim.haven.models;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeSet;

import org.seim.haven.util.LongUtils;

/**
 * Stores counts for the top N tokens.
 * 
 * <p>
 * This implementation assumes that most counts will only get larger. A
 * substantial number of decrements on a truncated Top can cause fewer items to
 * be tracked than desired.
 * 
 * <p>
 * We also assume that all counts must be greater than zero or the item will be
 * removed from this data structure.
 * 
 * @author Kevin Seim
 */
public class Top {

  private int size;
  private int capacity;
  private boolean truncated;
  private float maxError = 0.0f;
  // tree set of top items ordered by count
  private TreeSet<Item> heap = new TreeSet<Item>();
  // map of top items by term
  private Map<String, Item> items = new HashMap<String, Item>(); 

  private transient long maxCount = 0;

  /**
   * Constructs a new Top.
   * @param capacity
   */
  public Top(int capacity) {
    this.capacity = capacity;
  }

  public Top(int capacity, Map<String, Long> table, boolean truncated) {
    this(capacity);
    this.truncated = truncated;
    for (Map.Entry<String, Long> entry : table.entrySet()) {
      offer(entry.getKey(), entry.getValue());
    }
  }

  public Top(byte[] b) {
    try {
      deserialize(new DataInputStream(new ByteArrayInputStream(b)));
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  public Top(DataInputStream in) throws IOException {
    deserialize(in);
  }

  private void deserialize(DataInputStream in) throws IOException {
    this.size = in.readInt();
    this.capacity = in.readInt();
    this.maxError = in.readFloat();
    this.truncated = in.readBoolean();
    int bytesPerCount = in.readByte();
    for (int i = 0; i < size; i++) {
      String term = in.readUTF();
      long count = LongUtils.readUnsignedLong(in, bytesPerCount);

      Item item = new Item(term, count);
      heap.add(item);
      items.put(term, item);

      if (count > this.maxCount) {
        this.maxCount = count;
      }
    }
  }

  public byte[] serialize() {
    try {
      ByteArrayOutputStream bout = new ByteArrayOutputStream();
      DataOutputStream dout = new DataOutputStream(bout);
      serialize(dout);
      return bout.toByteArray();
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }
  
  public void serialize(DataOutputStream out) throws IOException {
    if (heap.size() != size) {
      throw new IllegalStateException("Heap size " + heap.size() + " does not match size " + size);
    }
    out.writeInt(size);
    out.writeInt(capacity);
    out.writeFloat(maxError);
    out.writeBoolean(truncated);
    int bytesPerCount = LongUtils.getMinUnsignedBytes(maxCount);
    out.writeByte(bytesPerCount);
    for (Item item : heap) {
      out.writeUTF(item.getTerm());
      LongUtils.writeUnsignedLong(out, bytesPerCount, item.getCount());
    }
  }

  /**
   * Returns the capacity of this Top.
   * @return the capacity
   */
  public int getCapacity() {
    return capacity;
  }

  /**
   * Returns whether the items stored in this Top are incomplete.
   * @return true if truncated, false otherwise
   */
  public boolean isTruncated() {
    return truncated;
  }

  /**
   * Returns the maximum error observed by {@link #offer(String, long, float)}.
   * @return the maximum observed error
   */
  public float getMaxError() {
    return maxError;
  }

  /**
   * Returns the number of items tracked by this Top.
   * @return the current size
   */
  public int size() {
    return size;
  }

  /**
   * Returns the item with the lowest count.
   * @return the {@link Item} with the lowest count
   */
  public Item least() {
    return (size == 0) ? null : heap.first();
  }

  /**
   * Returns an item for the given term.
   * @param term the term
   * @return the {@link Item} or null if the term is not in the Top
   */
  public Item getItem(String term) {
    return items.get(term);
  }

  //@Override
  public Iterator<Item> iterator() {
    return heap.descendingIterator();
  }

  /**
   * 
   * @param term
   * @param count the total count
   * @return true if incremented, false otherwise
   */
  public boolean incr(String term, long count) {
    return incr(term, count, 0.0f);
  }

  /**
   * 
   * @param term
   * @param count the total count
   * @return true if incremented, false otherwise
   */
  public boolean incr(String term, long count, float error) {
    return offer(term, count, 1, error);
  }

  public boolean decr(String term, long count) {
    return decr(term, count, 0.0f);
  }

  public boolean decr(String term, long count, float error) {
    return offer(term, count, -1, error);
  }

  /**
   * Updates this top based on changes in frequency of a term.
   * @param term the term to update
   * @param count the exact frequency of the term
   * @return true if this Top was updated, false otherwise
   */
  public boolean offer(String term, long count) {
    return offer(term, count, 0.0f);
  }

  /**
   * Updates this top based on changes in frequency of a term.
   * @param term the term to update
   * @param count the estimated frequency of the term
   * @param error the maximum amount of error in the frequency estimation
   * @return true if this Top was updated, false otherwise
   */
  public boolean offer(String term, long count, float error) {
    return offer(term, count, 0, error);
  }

  public boolean offer(final String term, final long count, final int offset, final float error) {
    if (count < 0) {
      throw new IllegalArgumentException("Invalid count: " + count);
    }

    if (count > maxCount) {
      maxCount = count;
    }

    if (size == 0) {
      if (count == 0) {
        return false;
      } else {
        size++;
        add(term, count, error);
        return true;
      }
    }

    Item item = items.get(term);
    if (item != null) {
      if (offset != 0) {
        long n = item.getCount() + offset;
        if (n == 0 || (truncated && n < heap.first().getCount())) {
          heap.remove(item);
          items.remove(term);
          size--;
        } else {
          heap.remove(item);
          add(term, n, 0.0f);
        }
      } else {
        if (item.getCount() == count) {
          return false;
        }

        if (count > item.getCount()) {
          heap.remove(item);
          add(term, count, error);
        } else if (count == 0 || (truncated && count < heap.first().getCount())) {
          heap.remove(item);
          items.remove(term);
          size--;
        } else {
          heap.remove(item);
          add(term, count, error);
        }
      }
      return true;
    }

    if (count == 0) {
      return false;
    }

    if (!truncated && size < capacity) {
      size++;
      add(term, count, error);
      return true;
    }

    truncated = true;

    Item first = heap.first();
    if (count < first.getCount()) {
      return false;
    } else if (size < capacity) {
      size++;
      add(term, count, error);
      return true;
    } else if (count > first.getCount()) {
      heap.remove(first);
      items.remove(first.getTerm());
      add(term, count, error);
      return true;
    } else {
      return false;
    }
  }

  private Item add(String term, long count, float error) {
    Item item = new Item(term, count);
    heap.add(item);
    items.put(term, item);
    if (error > maxError) {
      maxError = error;
    }
    return item;
  }

  /**
   * Creates a new Top by truncating this one.
   * @param capacity the maximum capacity of the new Top
   * @return the truncated Top
   * @throws IllegalArgumentException if <code>capacity</code> is greater than
   *           the capacity of this Top
   */
  public Top truncate(int capacity) throws IllegalArgumentException {
    if (capacity > this.capacity) {
      throw new IllegalArgumentException("Capacity cannot exceed " + this.capacity);
    }
    Top t = new Top(capacity);
    t.size = Math.min(capacity, this.size);
    t.truncated = this.truncated || t.size < this.size;
    t.maxError = this.maxError;
    t.maxCount = this.maxCount;

    int i = 0;
    for (Item item : this.heap.descendingSet()) {
      t.heap.add(item);
      t.items.put(item.getTerm(), item);
      if (++i == capacity) {
        break;
      }
    }

    return t;
  }

  @Override
  public String toString() {
    return toMap().toString();
  }

  public Map<String, Long> toMap() {
    return toMap(size);
  }

  public Map<String, Long> toMap(int limit) {
    LinkedHashMap<String, Long> map = new LinkedHashMap<String, Long>();

    int i = 0;
    for (Item item : heap.descendingSet()) {
      map.put(item.getTerm(), item.getCount());
      if (++i == limit) {
        break;
      }
    }

    return map;
  }

  protected void setHeap(Map<String, Long> heap) {
    this.maxCount = 0L;
    this.heap.clear();
    this.items.clear();

    for (Map.Entry<String, Long> entry : heap.entrySet()) {
      Item item = new Item(entry.getKey(), entry.getValue());
      this.heap.add(item);
      this.items.put(item.getTerm(), item);
      if (item.getCount() > maxCount) {
        maxCount = item.getCount();
      }
    }

    this.size = heap.size();
  }

  /**
   * This class should be immutable.
   */
  public static final class Item implements Comparable<Item> {
    private String term;
    private long count;

    public Item(String term, long count) {
      this.term = term;
      this.count = count;
    }

    public String getTerm() {
      return term;
    }

    public long getCount() {
      return count;
    }

    @Override
    public int compareTo(Item o) {
      int n = new Long(count).compareTo(o.count);
      if (n == 0) {
        n = -term.compareTo(o.term);
      }
      return n;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == null) {
        return false;
      }
      if (obj == this) {
        return true;
      }
      if (obj instanceof Item) {
        Item i = (Item) obj;
        return i.count == this.count && i.term.equals(this.term);
      }
      return false;
    }

    @Override
    public String toString() {
      return term + ":" + count;
    }
  }
}
