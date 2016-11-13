
import java.util.Arrays;

/**
 *
 * @author Joe Meszar
 * CS1550 Fall 2016
 * VM Simulator: Page Replacement Algorithms
 * 
 */

public class ClockPageTable {
    ClockPageFrame[] frames;
    int oldestFrame;
    int activeFrames;
    int pageFaults;
    int diskWrites;
    
    public ClockPageTable(int numFrames) {
        frames = new ClockPageFrame[numFrames];
        for (int i=0; i < numFrames; i++) {
            frames[i] = new ClockPageFrame();
        }
        
        oldestFrame = 0;
        activeFrames = 0;
        pageFaults = 0;
        diskWrites = 0;
    }
    
    public int getNumPageFaults() {
        return this.pageFaults;
    }
    public int getNumWritesToDisk() {
        return this.diskWrites;
    }
    
    
    /**
     * Starting from the current frame pointer, look for the oldest page frame that is not
     *      currently being referenced, evict it, and insert the new frame:
     * 
     *      If current frame is referenced, mark as unreferenced and move to next
     *      If current frame is unreferenced, replace with new frame.
     */
    private void replace(String address, boolean isDirty) {
        while (frames[oldestFrame].isReferenced()) {
            frames[oldestFrame].isReferenced(false);
            oldestFrame = (oldestFrame+1) % frames.length;  // "wrap" around
        }
        
        // see if the current frame needs written to disk before replacing
        if (frames[oldestFrame].isDirty()) {
            diskWrites++;
        }
        
        // overwrite the oldest unreferenced frame with the new address
        frames[oldestFrame].setAddress(address);
        frames[oldestFrame].isReferenced(true);
        frames[oldestFrame].isDirty(isDirty);
        
        // update total number of page faults
        pageFaults++;
        
        // oldest is now next to the currently replaced page frame
        oldestFrame = (oldestFrame+1) % frames.length;  // "wrap" around
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
     * Looks into the ClockPageTable structure for the given frame. If it exists, then
     *      the frame is updated to be referenced; otherwise this frame will be placed
     *      within the structure and marked as referenced.
     * 
     * @param address The new/existing page frame address to update as referenced.
     */
    public void read(String address) {
        // first search if this frame exists
        int location = search(address);
        
        // couldn't find, so insert
        if (location == -1) {
            if (activeFrames < frames.length) {
                // there's room to insert
                frames[activeFrames].setAddress(address);
                frames[activeFrames].isDirty(false);
                frames[activeFrames].isReferenced(true);
                activeFrames++;
                pageFaults++;
            } else {
                // must evict an existing page and insert new
                replace(address, false);
            }
        } else {
            // frame already existed; update referenced flag
            frames[location].isReferenced(true);
        }
    }
    
    
    /**
     * Looks into the ClockPageTable structure for the given frame and if it exists, then
      the frame is updated to be referenced AND it's dirty bit is set to reflect
      that this page frame no longer corresponds with data that is written to disk
      and it must be written back to disk for the changes to be effective.
     * 
     *      If the page frame does not exist, then this frame will be placed within the
      ClockPageTable structure with its referenced bit and dirty bit set.
     *      
     * @param address The new/existing page frame name to update as dirty.
     */
    public void write(String address) {
        // first search if this frame exists
        int location = search(address);
        
        // couldn't find, so insert
        if (location == -1) {
            if (activeFrames < frames.length) {
                // there's room to insert
                frames[activeFrames].setAddress(address);
                frames[activeFrames].isDirty(true);
                frames[activeFrames].isReferenced(true);
                activeFrames++;
            } else {
                // must evict an existing page and insert new
                replace(address, true);
            }
        } else {
            // frame already existed; update referenced & dirty flag
            frames[location].isReferenced(true);
            frames[location].isDirty(true);         // dirty because this is a write
        }
    }
    
    @Override
    public String toString() {
        return String.format("FRAMES: %s\nOLDEST: %d\nACTIVE: %d\nPAGE FAULTS: %d\nDISK WRITES: %d", 
                Arrays.toString(frames), this.oldestFrame, this.activeFrames, this.pageFaults, this.diskWrites);
    }
}