package simpledb.buffer;

import java.util.ArrayList;
import java.util.HashMap;

import simpledb.file.Block;
import simpledb.file.FileMgr;

/**
 * Manages the pinning and unpinning of buffers to blocks.
 * @author Edward Sciore
 *
 */
class BasicBufferMgr {
   //private Buffer[] bufferpool;
   private int numAvailable;
   private HashMap<Block, Buffer> bufferPoolMap; //new stuff
   private ArrayList<Buffer> fifo_queue; //new stuff
   private int buffer_length; //new stuff
   /**
    * Creates a buffer manager having the specified number 
    * of buffer slots.
    * This constructor depends on both the {@link FileMgr} and
    * {@link simpledb.log.LogMgr LogMgr} objects 
    * that it gets from the class
    * {@link simpledb.server.SimpleDB}.
    * Those objects are created during system initialization.
    * Thus this constructor cannot be called until 
    * {@link simpledb.server.SimpleDB#initFileAndLogMgr(String)} or
    * is called first.
    * @param numbuffs the number of buffer slots to allocate
    */
   BasicBufferMgr(int numbuffs) {

      //bufferpool = new Buffer[numbuffs];
      numAvailable = numbuffs;
      bufferPoolMap = new HashMap<Block, Buffer>(); //new stuff
      fifo_queue = new ArrayList<Buffer>(); //new stuff
      buffer_length = numbuffs; //new stuff
      
      //for (int i=0; i<numbuffs; i++)
        // bufferpool[i] = new Buffer();
   }
   
   /**
    * Flushes the dirty buffers modified by the specified transaction.
    * @param txnum the transaction's id number
    */
   synchronized void flushAll(int txnum) {
	  //new stuff
	  for(Buffer buff : bufferPoolMap.values()){
		  if (buff.isModifiedBy(txnum)){
			  buff.flush();		  
		  }
	  }
	   /*
      for (Buffer buff : bufferpool)
         if (buff.isModifiedBy(txnum))
         buff.flush();
         */
   }
   
   /**
    * Pins a buffer to the specified block. 
    * If there is already a buffer assigned to that block
    * then that buffer is used;  
    * otherwise, an unpinned buffer from the pool is chosen.
    * Returns a null value if there are no available buffers.
    * @param blk a reference to a disk block
    * @return the pinned buffer
    */
   synchronized Buffer pin(Block blk) {
      Buffer buff = findExistingBuffer(blk);
      
      if (buff == null) {
         buff = chooseUnpinnedBuffer();
         if (buff == null){
            return null;
         }
         buff.assignToBlock(blk);
         //add mapping too
         bufferPoolMap.put(buff.block(), buff); //new stuff
         //System.out.println("pinned a new block. BufferPoolMap: " + bufferPoolMap);
         //also add buffer to queue
         fifo_queue.add(buff); //new stuff
      }
      if (!buff.isPinned()){
         numAvailable--;
      }
      buff.pin();
      return buff;
   }
   
   /**
    * Allocates a new block in the specified file, and
    * pins a buffer to it. 
    * Returns null (without allocating the block) if 
    * there are no available buffers.
    * @param filename the name of the file
    * @param fmtr a pageformatter object, used to format the new block
    * @return the pinned buffer
    */
   synchronized Buffer pinNew(String filename, PageFormatter fmtr) {
      Buffer buff = chooseUnpinnedBuffer();
      if (buff == null){
         return null;
      }
      buff.assignToNew(filename, fmtr);
      //add mapping too
      bufferPoolMap.put(buff.block(), buff); //new buff
      System.out.println("added a new block to the map: " + bufferPoolMap);
      //add buffer to fifo queue
      fifo_queue.add(buff); //new buff
      
      numAvailable--;
      buff.pin();
      return buff;
   }
   
   /**
    * Unpins the specified buffer.
    * @param buff the buffer to be unpinned
    */
   synchronized void unpin(Buffer buff) {
      buff.unpin();
      if (!buff.isPinned())
         numAvailable++;
   }
   
   /**
    * Returns the number of available (i.e. unpinned) buffers.
    * @return the number of available buffers
    */
   int available() {
      return numAvailable;
   }
   
   private Buffer findExistingBuffer(Block blk) {
	  //search via block
	   //new stuff
	  if(bufferPoolMap.containsKey(blk)){
		  System.out.println("Found the block in the hashmap. Block: " + blk);
		  return bufferPoolMap.get(blk);
	  }
	  /* 
      for (Buffer buff : bufferpool) {
         Block b = buff.block();
         if (b != null && b.equals(blk))
            return buff;
      }
      */
      return null;
   }
   
   private Buffer chooseUnpinnedBuffer() {
	   //fifo wala code hai
	  
	  if(fifo_queue.size() < buffer_length){
		  return new Buffer();
	  }
	  Buffer unpinned_buff = null;
	  System.out.println("Fifo Queue:"+fifo_queue);
	  for(Buffer buff : fifo_queue){
		  if( !buff.isPinned()){
	//		  System.out.println("Unpinned Buffer no : "+fifo_queue.indexOf(buff));
        	 unpinned_buff = buff;
        	 break;  
		  }
	  }
	  if(unpinned_buff != null){
		  //delete value from map too
		  bufferPoolMap.remove(unpinned_buff.block()); //new stuff 
		  //remove from fifo queue
		  fifo_queue.remove(unpinned_buff);
		  return unpinned_buff;
	  }
	  /*
      for (Buffer buff : bufferpool){
         if (!buff.isPinned()){
        	 //delete value from map too
        	 bufferPoolMap.remove(buff.block()); //new stuff
        	 return buff;
         }
      }
      */
      return null;
   }
 
	/**
	 * Determines whether the map has a mapping from the block to some buffer.
	 * 
	 * @paramblk the block to use as a key
	 * @return true if there is a mapping; false otherwise
	 */
	boolean containsMapping(Block blk) {
		return bufferPoolMap.containsKey(blk);
	}

	/**
	 * Returns the buffer that the map maps the specified block to.
	 * 
	 * @paramblk the block to use as a key
	 * @return the buffer mapped to if there is a mapping; null otherwise
	 */
	Buffer getMapping(Block blk) {
		return bufferPoolMap.get(blk);
	}
   
}
