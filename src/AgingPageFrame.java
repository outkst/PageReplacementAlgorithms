
import java.util.Arrays;

/**
 *
 * @author Joe Meszar
 * CS1550 Fall 2016
 * VM Simulator: Page Replacement Algorithms
 * 
 */

public class AgingPageFrame {
    private String address;
    private boolean dirty;
    
    public AgingPageFrame() {
        this.dirty = false;
    }
    
    public AgingPageFrame(String address, boolean isDirty) {
        this.address = address;
        this.dirty = isDirty;
    }
    
    
    /**
     * Determines if the current page frame is empty and does not refer
     *      to any specific memory address.
     * 
     * @return True if the frame is empty; otherwise false.
     */
    public boolean isEmpty() {
        return address==null;
    }
    
    
    /**
     * Returns the memory address that this page frame references.
     * 
     * @return The memory address associated with this page frame's data.
     */
    public String getAddress() {
        return this.address;
    }
    
    /**
     * Sets the memory address that this frame corresponds to.
     * 
     * @param address The memory address this page frame will refer to.
     */
    public void setAddress(String address) {
        this.address = address;
    }
        
    /**
     * Determines if the given memory address is the same as the memory address
     *      referenced by this page frame.
     * 
     * @param address A memory address associated with a frame.
     * @return True if the addresses are equal; otherwise false.
     */  
    public boolean equalsAddress(String address) {
        return this.address.equals(address);
    }
    
    
    /**
     * Determines if this page frame is flagged as being "dirty" AKA
     *      containing updated data that has not yet been written to disk.
     * 
     * @return True if this frame has updated data (dirty); otherwise false.
     */
    public boolean isDirty() { 
        return this.dirty;
    }
    
    /**
     * Sets the dirty flag to show that this page has data that needs to be written to disk.
     * 
     * @param dirty True if the frame is to be considered as dirty; otherwise false.
     */
    public void isDirty(boolean dirty) {
        this.dirty = dirty;
    }
    
    @Override
    public String toString() {
        if (this.isEmpty()) { return "null"; }
        
        return String.format("ADDRESS: %s DIRTY: %s", this.address, this.dirty);
    }
}