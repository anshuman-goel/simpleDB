package simpledb.server;

import simpledb.buffer.BufferAbortException;
import simpledb.buffer.BufferMgr;
import simpledb.file.Block;
import simpledb.remote.*;
import java.rmi.registry.*;

public class Startup {
   public static void main(String args[]) throws Exception {
      // configure and initialize the database
      //SimpleDB.init(args[0]);
      SimpleDB.init("simpledb");
      
      // create a registry specific for the server on the default port
      Registry reg = LocateRegistry.createRegistry(1099);
      
      // and post the server entry in it
      RemoteDriver d = new RemoteDriverImpl();
      reg.rebind("simpledb", d);
      
      System.out.println("database server ready");
      
      Block blk1 = new Block("filename", 1);
      BufferMgr basicBufferMgr = new SimpleDB().bufferMgr() ;
      //basicBufferMgr.pin(blk1);
      try {
    	  basicBufferMgr.pin(blk1);
    	  Block[] blks = new Block[11];
    	  System.out.println("Initially: "+basicBufferMgr.available());
    	  for(int i=1; i<=10;i++){
    		  blks[i] = new Block("filename"+i, i);
    	  }
    	  
    	  for(int i=1; i<=7;i++){
    		  basicBufferMgr.pin(blks[i]);
    		  System.out.println("Available buffers: "+basicBufferMgr.available());
    	  }
    	  System.out.println("End:" + basicBufferMgr.available());
    	  
    	  basicBufferMgr.unpin(basicBufferMgr.getMapping(blks[3]));
    	  basicBufferMgr.unpin(basicBufferMgr.getMapping(blks[2]));
    	  System.out.println("after unpin 2blks:" + basicBufferMgr.available());
    	  basicBufferMgr.pin(blks[1]);
    	  basicBufferMgr.pin(blks[9]);
    	  basicBufferMgr.pin(blks[10]);
    	  basicBufferMgr.pin(blks[8]);
    	  
      }
    	  catch (BufferAbortException e) {
    		  System.out.println("got BufferAbortException exception ");
    		  System.out.println(e.getMessage());
    	  }
      
      
//      pin(1), pin(2), pin(3), pin(4), pin(5), pin(6), pin(7), pin(8), unpin(3), unpin(2).
      
   }
}
