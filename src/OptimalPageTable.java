
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Joe Meszar
 * CS1550 Fall 2016
 * VM Simulator: Page Replacement Algorithms
 * 
 */

public class OptimalPageTable {
    /**
     * Key - String value to hold memory address
     * Value - A LinkedList object holding the line-number(s) of the memory address
     */
    HashMap<String, LinkedList<Integer>> map;
    OptimalPageFrame[] frames;

    private int activeFrames;
    private int pageFaults;
    private int diskWrites;
    private int lineNumber;
    
    public OptimalPageTable(int numFrames, String traceFile) {
        
        frames = new OptimalPageFrame[numFrames];
        for (int i=0; i < numFrames; i++) {
            frames[i] = new OptimalPageFrame();
        }
        
        map = new HashMap<String, LinkedList<Integer>>();
        
        this.activeFrames = 0;
        this.pageFaults = 0;
        this.diskWrites = 0;
        this.lineNumber = 0;
        
        // build the hashmap using the tracefile
        File file = new File(traceFile);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(OptimalPageTable.class.getName()).log(Level.SEVERE, null, ex);
        }

        String line, address;
        int lineNum = 0;
        try {
            while ((line = reader.readLine()) != null) {
                // get the memory address
                address = line.split(" ")[0];
                
                // get the LinkedList within this map's address location
                LinkedList<Integer> lineNumbers = map.get(address);
                
                // make sure the LinkedList exists; create if not
                if (lineNumbers == null) {
                    lineNumbers = new LinkedList<Integer>();
                    map.put(address, lineNumbers);
                }
                
                // put the line number for this address
                lineNumbers.add(lineNum);
                
                // increase line number
                lineNum++;
            }
        } catch (IOException ex) {
            Logger.getLogger(OptimalPageTable.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        lineNum = 0;
    }
    
    public int getNumPageFaults() {
        return this.pageFaults;
    }
    public int getNumWritesToDisk() {
        return this.diskWrites;
    }
    
    
    /**
     * Using the aging counter bits, look for the Least Recently Used one, 
     *      evict it, and insert the new frame.
     */
    private int replace(String address) {
        // first search if this frame exists already
        int frameLocation = search(address);

        if ((frameLocation == -1) && (activeFrames < frames.length)) {
            // still have open frames to use; add this new frame
            
            frameLocation = activeFrames;   // frame to use is this empty one
            activeFrames++;                 // active frames goes up
            
            // update total number of page faults
            pageFaults++;
            
            // add frame
            frames[frameLocation].setAddress(address);
            
        } else if (frameLocation == -1) {
            // frame not currently loaded into page; must evict
            frameLocation = 0; // default if nothing found
            
            // find the frame in table that will be referenced
            //      LATER THAN ALL OTHERS currently in the table
            int frameAddress;
            int oldestFromNow = -1;
            LinkedList<Integer> lineNums;
            boolean neverUsedAgain;
            for (int frameNum=0; frameNum < frames.length; frameNum++) {
                // get the linked-list for this address
                lineNums = map.get(frames[frameNum].getAddress());
                
                // step through this linked-list and find the next
                //      line number that occurs AFTER the current
                //      line number. Keep the one farthest away
                neverUsedAgain = true;
                for (int num : lineNums) {
                    if (num > this.lineNumber) {
                        // now compare this against the other addresses
                        //      and store reference to the one FARTHEST away
                        if (num > oldestFromNow) { 
                            oldestFromNow = num;
                            frameLocation = frameNum; // store the frame's location in the table
                        }

                        // this frame is used again after this line number
                        neverUsedAgain = false;
                        break;
                    }
                }
                
                if (neverUsedAgain) {
                    // remove this frame as it's never used again
                    frameLocation = frameNum;
                    break;
                }
            }
            
            // evict the page farthest away, see if dirty first
            if (frames[frameLocation].isDirty()) {
                diskWrites++;
            }
            
            // add the new frame to the hashmap
            frames[frameLocation].setAddress(address);
            
        } else {
            // frame currently exists, do nothing   
        }

        // increase line number
        lineNumber++;
        
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
    }
    
    @Override
    public String toString() {
        return String.format("FRAMES: %s\nACTIVE: %d\nPAGE FAULTS: %d\nDISK WRITES: %d", 
                Arrays.toString(frames), this.activeFrames, this.pageFaults, this.diskWrites);
    }
}