import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Stack;

public class Parser {

	ArrayList<Block[]> functions; // each function contains a Block array
	ArrayList<String> functionName;
	int current_function_Index;
	HashMap<Integer, Block> current_map;
	final boolean debug = false;

	// need: stack
	// block objects
	// sets --implemented by integers
	public Parser(File sumF) {
		/*
		 * 1.initialize variables 2.read inputs
		 */
		this.functions = new ArrayList<Block[]>();
		this.functionName = new ArrayList<String>();
		this.current_function_Index = 0;
		this.current_map = null;
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(sumF), "UTF-8"));
			String line;
			ArrayList<Block> blocks = null;
			Block[] template = new Block[0];
			while ((line = reader.readLine()) != null) {
				if (line.isEmpty()) continue;
				if (line.contains("function")) {
					if (blocks != null) {
						functions.add(blocks.toArray(template)); // push the previous function
					}
					functionName.add(line.split("function")[1].trim());
					blocks = new ArrayList<Block>();
				} else { // block assignment
					String[] s = line.split(" -> ");
					Block block = new Block(Integer.parseInt(s[0].trim()));
					if (s.length == 2) {
						String[] s2 = s[1].split(" ");
						int[] s2i = new int[s2.length];
						for (int i = 0; i < s2.length; i++) {
							s2i[i] = Integer.parseInt(s2[i].trim());
						}
						block.addSucc(s2i);

					} else { // ending block
					}
					// System.out.println(block.blockN);
					blocks.add(block);
				}
			}
			// after iteration, we need to add the last one
			if (functions.size() < functionName.size())
				functions.add(blocks.toArray(template));
			System.out.printf("# functions = %d\n", functions.size());
			reader.close();

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

	}

	public void run() {
		/*
		 * for each function 1.update blocks:pred, dom 2.look for loops; for each loop
		 * found, define the loop 3.combine stacks that have the same header
		 */
		int max = functionName.size();
		while (current_function_Index < max) {
			this.updateMap();
			this.updatePreds();

			// initiate dominators
			Block[] current = functions.get(current_function_Index);
			Integer[] blockNums = this.current_map.keySet().toArray(new Integer[0]);
			for (Block b : current) {
				if (b.pred.size() == 0) {
					Integer[] a = { b.blockN };
					b.setDom(new Collection(a));
				} else {
					b.setDom(new Collection(blockNums));
				}
			}
			this.updateDoms();

			// debug
			if (debug) {
				System.out.printf("function %s block information:\n", functionName.get(current_function_Index));
				for (Block b : current) {
					System.out.println("block - " + b.blockN);
					System.out.println("succ - " + b.succ);
					System.out.println("pred - " + b.pred);
					System.out.println("dominator - " + b.dominator + "\n");
				}
			}
			this.findLoop();
			System.out.printf("--------\n FUNCTION END\n---------\n\n");
			current_function_Index += 1;
		}
		System.out.println("Parsing finished. For multiple functions, scroll up to find function loop information.");

	}

	private void updateMap() {
		Block[] current = functions.get(current_function_Index);
		this.current_map = new HashMap<Integer, Block>(current.length);
		for (Block each : current) {
			//System.out.println("each = " + each);
			current_map.put(each.blockN, each);
		}

	}

	private void updateDoms() {
		boolean updated = true;
		Block[] current = functions.get(current_function_Index);
		int i = 0;
		while (updated) {
			if (debug) System.out.println("update iteration..." + i);
			i += 1;
			updated = false;
			for (Block b : current) {
				if (b.update()) {
					//System.out.println("Block " + b.blockN + " detected change.");
					updated = true;
					break;
				}
			}
		}
	}

	/**
	 * used for updating a function's blocks' preds
	 */
	private void updatePreds() {
		Block[] current = functions.get(current_function_Index);
		for (Block each : current) {
			ArrayList<Integer> arr = new ArrayList<Integer>();
			for (Block gothrough : current) {
				if (gothrough.succ.contains(each.blockN))
					arr.add(gothrough.blockN);
			}
			each.addPred(arr.toArray(new Integer[0]));
		}
	}

	public void findLoop() {
		/*
		 * algorithm 1.look for back edge 2.identify header block 3.stack-loop
		 * implementation
		 */
		Block[] current = functions.get(current_function_Index);
		// ArrayList<Integer> backedge = new ArrayList<Integer>();
		// ArrayList<Integer> header = new ArrayList<Integer>();
		HashMap<Integer, ArrayList<Integer>> headback_map = new HashMap<Integer, ArrayList<Integer>>();
		for (Block each : current) {
			// boolean found=false;
			for (Integer succ : each.succ) {
				if (each.dominator.exist(succ)) {
					// found = true;
					if (headback_map.containsKey(succ)) {
						headback_map.get(succ).add(each.blockN);
					} else {
						ArrayList<Integer> backedge = new ArrayList<Integer>();
						backedge.add(each.blockN);
						headback_map.put(succ, backedge);
					}
					break;
				}
			}
		}
		// 3. find the loop implementation for each backedge
		// ArrayList<Parser.Collection> loops = new ArrayList<Parser.Collection>();
		System.out.printf("function %s contains: \n", functionName.get(current_function_Index));
		if (headback_map.isEmpty()) System.out.println("no loops.");
		for (Integer header : headback_map.keySet()) {
			ArrayList<Integer> backedge = headback_map.get(header);
			Collection f_loop = new Collection(header);
			// for each header block, run loops
			for (int loop_i = 0; loop_i < backedge.size(); loop_i++) {
				Collection loop = new Collection(header);
				Stack<Integer> stack = new Stack<Integer>();
				int n = backedge.get(loop_i);
				if (n != header) {
					loop.add(n);
					stack.push(n);
				}
				while (!stack.empty()) {
					Block cur_b = this.current_map.get(stack.pop());
					for (Integer succ : cur_b.pred) {
						if (!loop.exist(succ)) {
							loop.add(succ);
							stack.push(succ);
						}
					}
				}
				System.out.println("(loop) "+loop);
				f_loop = f_loop.union(loop);
			}
			// results.
			System.out.printf("combined loop: header = %d; %s.\n", header, f_loop);
		}
		/*
		 * for debug for (Collection c: loops) { System.out.println("loop = "+c); }
		 */
		// check header overlaps

	}

	// private

	public Collection updateDom(Block b) {
		/*
		 * updateDom is always equal to intersection of Dom(b's pred) + b
		 */
		ArrayList<Integer> preds = b.pred;
		if (debug) System.out.println("updateDom function - " + b.blockN);
		if (preds.size() == 0) {
			Integer[] a = { b.blockN };
			return new Collection(a);
		} else {
			Collection col = current_map.get(preds.get(0)).dominator.getCopy();
			int size = preds.size();
			for (int i = 1; i < size; i++) {
				// System.out.println("intersect - "+current_map.get(preds.get(i)).blockN);
				col = col.intersect(current_map.get(preds.get(i)).dominator);
			}
			// System.out.println("before add"+col.array);
			// System.out.println(col.hashCode());
			col.add(b.blockN);
			// System.out.println("after add"+col.array);
			return col;
		}
	}

	class Collection {
		/**
		 * A BitSet that represents the idea for a collection of blocks {1,2,3,4,5,6,7}
		 * {1,2,5,7} Used for creating dom sets and loop
		 */
		BitSet array;

		public Collection(Integer... Args) {
			// args = {1,2,3...}
			// convert to bitset
			array = new BitSet(30);
			for (int i : Args) {
				this.array.set(i);
			}

		}

		public Collection getCopy() {
			return new Collection(this.array);
		}

		private Collection(BitSet newArray) {
			this.array = (BitSet) newArray.clone();
		}

		public boolean exist(int blockN) {
			return array.get(blockN);
		}

		public void add(int blockN) {
			this.array.set(blockN);
		}

		public void remove(int blockN) {
			this.array.set(blockN, false);
		}

		public Collection intersect(Collection theOther) {
			BitSet newArray = (BitSet) this.array.clone();
			newArray.and(theOther.array);
			return new Collection(newArray);
		}

		public Collection union(Collection theOther) {
			BitSet newArray = (BitSet) this.array.clone();
			newArray.or(theOther.array);
			return new Collection(newArray);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this)
				return true;
			if (!(obj instanceof Collection))
				return false;
			else {
				Collection theOther = (Collection) obj;
				return this.array.equals(theOther.array);
			}
		}

		@Override
		public String toString() {
			return this.array.toString();
		}
	}

	class Block {
		ArrayList<Integer> pred;
		ArrayList<Integer> succ;
		Collection dominator;
		int blockN;

		public Block(int blockN) {
			this.blockN = blockN;
			pred = new ArrayList<Integer>();
			succ = new ArrayList<Integer>(2);
		}

		public void addPred(Integer... args) {
			if (args.length > 0) {
				for (int i : args) {
					pred.add(i);
				}
			}
			//System.out.println(this.blockN + "pred = " + this.pred);
		}

		public void addPred(int[] args) {
			if (args.length > 0) {
				for (int i : args) {
					pred.add(i);
				}
			}
			//System.out.println(this.blockN + "pred = " + this.pred);
		}

		public void addSucc(int[] args) {
			if (args.length > 0) {
				for (int i : args) {
					succ.add(i);
				}
			}
			//System.out.println(this.blockN + ".succ = " + this.succ);
		}

		public void setDom(Collection dom) {
			this.dominator = dom;
		}

		@Deprecated
		public void setDom(int totalBlocks) {
			this.dominator = new Collection(totalBlocks);
		}

		public boolean update() {
			// we do Dom check here
			Collection dom = Parser.this.updateDom(this);
			if (dom.equals(this.dominator)) {
				// System.out.println("this dom equals -"+this.dominator +"; updateDom = "+dom);
				return false;
			} else {
				// System.out.println("detect change. This.dom="+this.dominator+"; new =
				// "+dom.getCopy());
				this.dominator = dom;
				return true;
			}
		}

	}

}
