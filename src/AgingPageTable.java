
import java.util.Arrays;

/**
 *
 * @author Joe Meszar
 * CS1550 Fall 2016
 * VM Simulator: Page Replacement Algorithms
 * 
 */

public class AgingPageTable {
    AgingPageFrame[] frames;
    private int[] counters;
    private int refresh;
    private int clock;
    
    private int activeFrames;
    private int pageFaults;
    private int diskWrites;
    
    public AgingPageTable(int numFrames, int refresh) {
        frames = new AgingPageFrame[numFrames];
        for (int i=0; i < numFrames; i++) {
            frames[i] = new AgingPageFrame();
        }
        
        
        this.counters = new int[numFrames];  // hold counter for each frame
        
        this.refresh = refresh;     // refresh rate (clock cycles)
        this.clock = refresh-1;     // zero-based countdown
        
        this.activeFrames = 0;
        this.pageFaults = 0;
        this.diskWrites = 0;
    }
    
    /**
     * Counts down from the refresh rate to zero. At zero, the "current" bit in
     *      in the counter gets shifted to the right, filling up the counter from
     *      most significant bit to least significant bit.
     */
    private void countdown() {
        clock = (clock+1) % refresh;
        
        if (clock==0) {
            // shift right
            for (int counter : counters) {
                System.out.println(String.format("(%d) %s", counter, Integer.toBinaryString(counter)));
                counter = counter >> 1;
                System.out.println(String.format("(%d) %s", counter, Integer.toBinaryString(counter)));
            }
        }
    }
   
    
    /**
     * Starting from the current frame pointer, look for the oldest page frame that is not
     *      currently being referenced, evict it, and insert the new frame:
     * 
     *      If current frame is referenced, mark as unreferenced and move to next
     *      If current frame is unreferenced, replace with new frame.
     */
    private void replace(String address, boolean isDirty) {
        // find the frame with the least amount of use
        int oldestFrame = counters[0];
        for (int x : counters) {
            if (x < oldestFrame) { oldestFrame = x; }
        }
        
        // see if the current frame needs written to disk before replacing
        if (frames[oldestFrame].isDirty()) {
            diskWrites++;
        }
        
        // overwrite the oldest unreferenced frame with the new address
        frames[oldestFrame].setAddress(address);
        frames[oldestFrame].isDirty(isDirty);
        
        // update total number of page faults
        pageFaults++;
    }
    
    
    /**
     * Searches the RAM array for the given address and returns its location if found.
     * 
     * @param address The memory address to search for.
     * @return The location of the referenced memory address in RAM; otherwise -1.
     */
    private int search(String address) {
        int location = -1;
        for (int i=0; i<activeFrames; i++) {
            if (frames[i].equalsAddress(address)) {
                location = i;
            }
        }
        
        return location;
    }
    
    
    /**
     * Looks into the ClockRAM structure for the given frame. If it exists, then
     *      the frame is updated to be referenced; otherwise this frame will be placed
     *      within the structure and marked as referenced.
     * 
     * @param address The new/existing page frame address to update as referenced.
     */
    public void read(String address) {
        // first search if this frame exists
        int frameLocation = search(address);
        
        // couldn't find, so insert
        if (frameLocation == -1) {
            if (activeFrames < frames.length) {
                // there's room to insert
                frames[activeFrames].setAddress(address);
                frames[activeFrames].isDirty(false);
                activeFrames++;
            } else {
                // must evict an existing page and insert new
                replace(address, false);
            }
        } else {
            // frame already existed; update counter
            updateCounter(frameLocation);
        }
    }
    
    
    /**
     * Sets the 8th bit as referenced.
     * 
     * @param frameCounter The frame counter number to update.
     */
    private void updateCounter(int frameCounter) {
        counters[frameCounter] = counters[frameCounter] | 256;
    }
    
    /**
     * Looks into the ClockRAM structure for the given frame and if it exists, then
     *      the frame is updated to be referenced AND it's dirty bit is set to reflect
     *      that this page frame no longer corresponds with data that is written to disk
     *      and it must be written back to disk for the changes to be effective.
     * 
     *      If the page frame does not exist, then this frame will be placed within the
     *      ClockRAM structure with its referenced bit and dirty bit set.
     *      
     * @param address The new/existing page frame name to update as dirty.
     */
    public void write(String address) {
        // first search if this frame exists
        int frameLocation = search(address);
        
        // couldn't find, so insert
        if (frameLocation == -1) {
            if (activeFrames < frames.length) {
                // there's room to insert
                frames[activeFrames].setAddress(address);
                frames[activeFrames].isDirty(true);
                activeFrames++;
            } else {
                // must evict an existing page and insert new
                replace(address, true);
            }
        } else {
            // frame already existed; update referenced & dirty flag
            frames[frameLocation].isDirty(true);         // dirty because this is a write
        }
    }
    
    @Override
    public String toString() {
        return String.format("FRAMES: %s\nOLDEST: %d\nACTIVE: %d\nPAGE FAULTS: %d\nDISK WRITES: %d", 
                Arrays.toString(frames), this.activeFrames, this.pageFaults, this.diskWrites);
    }
}