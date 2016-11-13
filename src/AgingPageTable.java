
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
        this.clock = 0;     // zero-based countdown
        
        this.activeFrames = 0;
        this.pageFaults = 0;
        this.diskWrites = 0;
    }
    
    public int getNumPageFaults() {
        return this.pageFaults;
    }
    public int getNumWritesToDisk() {
        return this.diskWrites;
    }
    
    /**
     * Counts down from the refresh rate to zero. At zero, the "current" bit in
     *      in the counter gets shifted to the right, filling up the counter from
     *      most significant bit to least significant bit.
     */
    private void countdown() {
        clock = (clock+1) % refresh;
        
        if (clock == 0) {
            // shift right
            for (int i=0; i < counters.length; i++) {
                counters[i] = counters[i] >> 1;
            }
        }
    }
   
    
    /**
     * Using the aging counter bits, look for the Least Recently Used one, 
     *      evict it, and insert the new frame.
     */
    private int replace(String address) {
        // first search if this frame exists already
        int frameLocation = search(address);
        
        if ((frameLocation == -1) && (activeFrames < frames.length)) {
            // still have open frames to use
            frameLocation = activeFrames;   // frame to use is this empty one
            activeFrames++;                 // active frames goes up
            
            // update total number of page faults
            pageFaults++;
            
            frames[frameLocation].setAddress(address);
            
        } else if (frameLocation == -1) {
            // must evict. find the frame with the least amount of use
            int value = counters[0];
            frameLocation = 0;
            for (int i=0; i < activeFrames; i++) {
                if (counters[i] < value) { 
                    frameLocation = i;              // hold location of oldest frame
                    value = counters[i];    // hold oldest frame's value
                }
            }
            
            // update total number of page faults
            pageFaults++;
            
            // see if the current frame needs written to disk before replacing
            if (frames[frameLocation].isDirty()) { diskWrites++; }
            
            // set the new address of this frame
            frames[frameLocation].setAddress(address);
            
            // default to not being dirty (will change in Write function if needed)
            frames[frameLocation].isDirty(false);
            
            // new frame means our aging counter needs reset
            counters[frameLocation] = 0;
            
        } else if (frameLocation >= 0) {
            // frame already exists. 
        }

        return frameLocation; // return the newly used frame's location
    }
    
    
    /**
     * Searches the RAM array for the given address and returns its location if found.
     * 
     * @param address The memory address to search for.
     * @return The location of the referenced memory address in RAM; otherwise -1.
     */
    private int search(String address) {
        int frameLocation = -1;
        for (int i=0; i<activeFrames; i++) {
            if (frames[i].equalsAddress(address)) {
                frameLocation = i;
            }
        }
        
        return frameLocation;
    }
    
    
    /**
     * Looks into the ClockRAM structure for the given frame. If it exists, then
     *      the frame is updated to be referenced; otherwise this frame will be placed
     *      within the structure and marked as referenced.
     * 
     * @param address The new/existing page frame address to update as referenced.
     */
    public void read(String address) {
        int frameLocation = replace(address);   // insert / update / replace frame
        updateCounter(frameLocation);           // mark this frame as referenced within the bitmap
        countdown();                            // one refresh frame has occurred
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
        int frameLocation = replace(address);   // insert / update / replace frame
        frames[frameLocation].isDirty(true);    // mark dirty because this is a write
        updateCounter(frameLocation);           // mark this frame as referenced within the bitmap
        countdown();                            // one refresh frame has occurred
    }
    
    @Override
    public String toString() {
        return String.format("FRAMES: %s\nACTIVE: %d\nPAGE FAULTS: %d\nDISK WRITES: %d", 
                Arrays.toString(frames), this.activeFrames, this.pageFaults, this.diskWrites);
    }
}