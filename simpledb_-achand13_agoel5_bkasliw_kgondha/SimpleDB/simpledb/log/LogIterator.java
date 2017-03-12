package simpledb.log;

import static simpledb.file.Page.INT_SIZE;
import java.util.Stack;
import simpledb.file.*;
import simpledb.server.SimpleDB;

import java.util.Iterator;

/**
 * A class that provides the ability to move through the records of the log file
 * in reverse order.
 * 
 * @author Edward Sciore
 */
public class LogIterator implements Iterator<BasicLogRecord> {
	private Block blk;
	private Page pg = new Page();
	private int currentrec;
	private Stack<Integer> forwardRecordList = new Stack<Integer>();
	private boolean isForward;

	public void makeForward() {
		this.isForward = true;
		this.blk = new Block(this.blk.fileName(), this.blk.number() - 1);
		int start_record = this.currentrec;
		moveToNextForwardBlock();
		int temp_currentrec;
		// move through the block to the revIter.currentrec
		while (!forwardRecordList.empty()) {
			temp_currentrec = forwardRecordList.pop();
			if (temp_currentrec == start_record) {
				forwardRecordList.push(temp_currentrec);
				break;
			}
		}
		this.currentrec = forwardRecordList.pop();
	}

	/**
	 * Creates an iterator for the records in the log file, positioned after the
	 * last log record. This constructor is called exclusively by
	 * {@link LogMgr#iterator()}.
	 */
	LogIterator(Block blk) {
		this.blk = blk;
		pg.read(blk);
		currentrec = pg.getInt(LogMgr.LAST_POS);
		this.isForward = false;
	}

	LogIterator(Block blk, boolean isForward) {
		this.blk = blk;
		pg.read(blk);
		currentrec = pg.getInt(LogMgr.LAST_POS);
		this.isForward = isForward;
		if (this.isForward) {
			this.blk = new Block(blk.fileName(), blk.number() - 1);
			moveToNextForwardBlock();
		}
	}

	/**
	 * Determines if the current log record is the earliest record in the log
	 * file.
	 * 
	 * @return true if there is an earlier record
	 */
	public boolean hasNext() {
		if (this.isForward) {
			if (!forwardRecordList.empty())
				return true;
			if (blk.number() < SimpleDB.fileMgr().size(blk.fileName()) - 1)
				return true;
			return false;
		}

		return currentrec > 0 || blk.number() > 0;
	}

	/**
	 * Moves to the next log record in reverse order. If the current log record
	 * is the earliest in its block, then the method moves to the next oldest
	 * block, and returns the log record from there.
	 * 
	 * @return the next earliest log record
	 */
	public BasicLogRecord next() {
		if (this.isForward)
			return nextForward();

		if (currentrec == 0)
			moveToNextBlock();
		currentrec = pg.getInt(currentrec);

		return new BasicLogRecord(pg, currentrec + INT_SIZE);
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Moves to the next log block in reverse order, and positions it after the
	 * last record in that block.
	 */
	private void moveToNextBlock() {
		blk = new Block(blk.fileName(), blk.number() - 1);
		pg.read(blk);
		currentrec = pg.getInt(LogMgr.LAST_POS);
	}

	/**
	 * Moves to the next log record in forward order. If the current log record
	 * is the latest in its block, then the method moves to the next block, and
	 * returns the log record from there.
	 * 
	 * @return the next log record
	 */
	public BasicLogRecord nextForward() {
		if (forwardRecordList.empty()) {
			moveToNextForwardBlock();
		}
		currentrec = forwardRecordList.pop();
		BasicLogRecord rec = new BasicLogRecord(pg, currentrec + INT_SIZE);
		return rec;
	}

	/**
	 * Moves to the next log block in forward order, and positions it at the
	 * first record in that block.
	 */
	private void moveToNextForwardBlock() {
		blk = new Block(blk.fileName(), blk.number() + 1);
		pg.read(blk);
		currentrec = pg.getInt(LogMgr.LAST_POS);
		currentrec = pg.getInt(currentrec); // to place the pointer to the start
											// of the record
		forwardRecordList.clear();
		while (currentrec > 0) {
			forwardRecordList.push(currentrec);
			currentrec = pg.getInt(currentrec);
		}
		forwardRecordList.push(0);
		currentrec = 0;
	}
}
