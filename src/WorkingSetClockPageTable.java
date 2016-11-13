
import java.util.Arrays;

/**
 *
 * @author Joe Meszar
 * CS1550 Fall 2016
 * VM Simulator: Page Replacement Algorithms
 * 
 */
public class WorkingSetClockPageTable {

    WorkingSetClockPageFrame[] frames;
    private final int refresh;  // holds the refresh rate
    private int interrupt;      // holds current clock interrupt time
    private long virtualTime;   // holds the virtual time counter
    private long tau;
    
    private int oldestFrame;
    private int activeFrames;
    private int pageFaults;
    private int diskWrites;
    
    public WorkingSetClockPageTable(int numFrames, int refresh, long tau) {
        frames = new WorkingSetClockPageFrame[numFrames];
        for (int i=0; i < numFrames; i++) {
            frames[i] = new WorkingSetClockPageFrame(0);
        }
        
        this.refresh = refresh;     // refresh rate (clock cycles)
        this.interrupt = 0;         // zero-based countdown
        this.virtualTime = 0;       // counter for virtual time
        this.tau = tau;
        
        this.activeFrames = 0;
        this.pageFaults = 0;
        this.diskWrites = 0;
        this.oldestFrame = 0;
    }
    
    public int getNumPageFaults() {
        return this.pageFaults;
    }
    public int getNumWritesToDisk() {
        return this.diskWrites;
    }
    
    /**
     * Counts down from the refresh rate to zero. At zero, cleanup is performed
     *      to remove all pages that have been 
     */
    private void countdown() {
        interrupt = (interrupt+1) % refresh;
        
        if (interrupt == 0) {
            // set all active frames' Referenced bit back to 0
            for (int i=0; i < activeFrames; i++) {
                frames[i].isReferenced(false);
            }
        }
    }
   
    
    /**
     * Using the aging counter bits, look for the Least Recently Used one, 
     *      evict it, and insert the new frame.
     */
    private int replace(String address) {
        // increase the virtual clock time
        virtualTime++;
        
        // first search if this frame exists already
        int frameLocation = search(address);
        
        if ((frameLocation == -1) && (activeFrames < frames.length)) {
            // still have open frames to use
            frameLocation = activeFrames;   // frame to use is this empty one
            activeFrames++;                 // active frames goes up
            
            // update total number of page faults
            pageFaults++;
            
            updatePageFrame(frameLocation, address);
            
        } else if (frameLocation == -1) {
            pageFaults++;
            
            // no open frames left, must evict/replace
            boolean keepLooking = true;
            int curFrame = oldestFrame;     // start from the oldest
            int j=0;                        // loop through entire frame size
            
            while ((j < frames.length) && keepLooking) {
                // check reference bit
                if ((frames[curFrame].isReferenced() == false)
                        && (frames[curFrame].isDirty() == false)) {
                    
                    // unreferenced and clean; evict it
                    updatePageFrame(curFrame, address);
                    keepLooking = false;
                    
                    frameLocation = curFrame;
                    
                } else if ((frames[curFrame].isReferenced() == false) 
                        && ((virtualTime - frames[curFrame].getLastUsed()) < tau)
                        && (frames[curFrame].isDirty())) {
                    
                    // unreferenced; older than tau; dirty
                    // write to disk and mark as clean
                    diskWrites++;
                    frames[curFrame].isDirty(false);
                    
                    // ensure that this frame location will be looked at again
                    //      since it now meets the unreferenced, non-dirty requirements
                    //      needed to evict and replace.
                    j = 0;
                    
                } else {
                    j++; // increase count
                }
                
                curFrame = (curFrame+1) % frames.length;  // "wrap" around
            }
            
            // special case where all frames are currently being
            //      referenced, so evict the OLDEST
            if (keepLooking) {
                if (frames[oldestFrame].isDirty()) {
                    diskWrites++;
                }
                
                // evict and insert new page frame
                updatePageFrame(oldestFrame, address);
                
                // update the frame location used
                frameLocation = oldestFrame;
            }
            
            // update the oldest frame pointer
            for (int i=0; i < frames.length; i++) {
                if (frames[i].getLastUsed() < frames[oldestFrame].getLastUsed()) {
                    oldestFrame = i;
                }
            }
        } else if (frameLocation >= 0) {
            // frame already exists. update referenced bit and age
            updatePageFrame(frameLocation, address);
        }

        return frameLocation; // return the newly used frame's location
    }
    
    
    /**
     * Mimics the updating of a page frame.
     * 
     * @param frame The frame number to update.
     * @param address The new address to store in this frame.
     */
    private void updatePageFrame(int frame, String address) {
        frames[frame].setAddress(address);
        frames[frame].isReferenced(true);
        frames[frame].setLastUsed(virtualTime);
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
        replace(address);   // insert / update / replace frame
        countdown();                            // one refresh frame has occurred
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
        countdown();                            // one refresh frame has occurred
    }
    
    @Override
    public String toString() {
        return String.format("FRAMES: %s\nACTIVE: %d\nPAGE FAULTS: %d\nDISK WRITES: %d", 
                Arrays.toString(frames), this.activeFrames, this.pageFaults, this.diskWrites);
    }
}