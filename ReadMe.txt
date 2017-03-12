
Team members:
Anshul Chandra:			achand13
Anshuman Goel:			agoel5
Bhavesh Kasliwal:		bkasliw
Kaustubh Gondhalekar:	kgondha

Modified Files
./buffer/BasicBufferMgr.java
./buffer/BufferMgr.java
./log/LogIterator.java
./log/LogMgr.java
./server/SimpleDB.java
./server/Startup.java
./tx/recovery/CheckpointRecord.java
./tx/recovery/CommitRecord.java
./tx/recovery/LogRecord.java
./tx/recovery/LogRecordIterator.java
./tx/recovery/RecoveryMgr.java
./tx/recovery/RollbackRecord.java
./tx/recovery/SetIntRecord.java
./tx/recovery/SetStringRecord.java
./tx/recovery/StartRecord.java

How to test:
	The main method is in the Startup.java file. It contains the following three methods for each test scenario:
	1. testBufferPool() - for testing new impletementation of FIFO and HashMap of testBufferPool
	2. testRecovery1() - for testing LogRecordIterator
	3. testRecovery2() - for testing Recovery

	The methods can be uncommented one at a time to test each scenario separately.
	
More about FIFO Method Implementation and Testing:
To implement the FIFO replacement policy, we have maintained an arraylist of Buffer which can grow upto maximum of allowed number of buffers.
When a block has to be replaced, we search form the starting of the list to the first unpinned buffer.
As soon as the suitable candidate is found it is removed from the arraylist and is placed onto the last of the list.
This way ensures our FIFO algorithm, and to test it we have printed the whole arraylist of buffers whenever chooseUnpinnedBuffer method is called.
The arraylist prints the Buffer object no. Here, one can notice in the output screen is that the buffer which is suitable candidate 
(i.e., first unpinned buffer) is removed from it location and a desired one is added in last of arraylist.
Sample output is like given below.
[simpledb.buffer.Buffer@76ed5528, simpledb.buffer.Buffer@2c7b84de, simpledb.buffer.Buffer@3fee733d, simpledb.buffer.Buffer@5acf9800, simpledb.buffer.Buffer@4617c264, simpledb.buffer.Buffer@36baf30c, simpledb.buffer.Buffer@2626b418, simpledb.buffer.Buffer@5a07e868]
Here, the buffer simpledb.buffer.Buffer@3fee733d is first unpinned buffer, which is perfect candidate for replacement.
[simpledb.buffer.Buffer@76ed5528, simpledb.buffer.Buffer@2c7b84de, simpledb.buffer.Buffer@5acf9800, simpledb.buffer.Buffer@4617c264, simpledb.buffer.Buffer@36baf30c, simpledb.buffer.Buffer@2626b418, simpledb.buffer.Buffer@5a07e868, simpledb.buffer.Buffer@3fee733d]
In this, one can find that the buffer simpledb.buffer.Buffer@3fee733d is now positioned at the last of the list with new pin block.
