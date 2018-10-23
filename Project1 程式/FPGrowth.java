import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

public class FPGrowth {
    private File file;
    private double min_support, min_confident;
    private int threshold, transaction_count = 0;
    private int patternCount = 1;   // used in inserting conditional pattern tree, default number(1) is for phase2 fp-tree construct.
    private LinkedHashSet<FPTreeNode> headerTable = new LinkedHashSet<>();
    private HashMap<String, Integer> itemsToFreq = new HashMap<>(); // a key-value pair to count the number of occurrences of items <item, count>
    private LinkedHashMap<String, Integer> freqPatterns = new LinkedHashMap<>();    // <pattern, count>
    private LinkedHashMap<String, Double> x_HandSidefreqPatterns = new LinkedHashMap<>();  // all freq patterns that have left hand and right hand side derived from freqPattenrs. <(lhs)>(rhs):count, conf>
    private ArrayList<ArrayList<String>> sortedTransaction = new ArrayList<ArrayList<String>>();    // all sorted transaction, for the purpose of cal confident, duplicate is allowed, for the purpose of caling conf.

    public FPGrowth(File file, double min_support, double conf) throws FileNotFoundException {
        this.file = file;
        this.min_support = min_support;
        this.min_confident = conf;
        LinkedList<String> sortedItemsByFreq = new LinkedList<>();  // part of itemsToFreq set(# more than threshold), and is ordered for sort purpose in the next step. <item>

        findFreqOneItem(itemsToFreq, sortedItemsByFreq);
        constructFPTree(sortedItemsByFreq);
        FPgrowth(null, headerTable);
        findCandidateRules();

        if(x_HandSidefreqPatterns.isEmpty()) System.out.println("No rules found!");
        for (String frequentPattern : sortByValue(x_HandSidefreqPatterns).keySet())
            System.out.printf("%s,\t conf = %.2f\n", frequentPattern, x_HandSidefreqPatterns.get(frequentPattern));
    }

    private static Map<String, Double> sortByValue(Map<String, Double> unsortMap) {
        List<Map.Entry<String, Double>> list = new LinkedList<>(unsortMap.entrySet());
        Collections.sort(list, (o1, o2) -> (o2.getValue()).compareTo(o1.getValue()));

        /// Loop the sorted list and put it into a new insertion order Map
        LinkedHashMap<String, Double> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<String, Double> entry : list)
            sortedMap.put(entry.getKey(), entry.getValue());

        return sortedMap;
    }

    /** Phase1 - Find frequent 1-item(itemsToFreq), sorted items in frequency desc order by scanning DB */
    private void findFreqOneItem(HashMap<String, Integer> itemsToFreq, LinkedList<String> sortedItemsByFreq) throws FileNotFoundException {
        // step 1.1: counting each item.
        Scanner scanner = new Scanner(new FileInputStream(file));
        int currentTID = -1;    // for counting # of transaction.
        while (scanner.hasNextLine()) {
            String oneLine = scanner.nextLine();
            String split[] = oneLine.split(" ");    // use space to split, and [0] is the transaction id, [1] is the item id.
            itemsToFreq.put(split[1], (itemsToFreq.get(split[1]) == null ? 1 : itemsToFreq.get(split[1]) + 1));

            if (Integer.parseInt(split[0]) != currentTID) {
                transaction_count += 1;
                currentTID = Integer.parseInt(split[0]);
            }
        }
        threshold = (int) Math.ceil(transaction_count * min_support);    // cal threshold: # of transaction * min_support.

        // step 1.2: order by freq(insertion sort), and also remove the item that occurred less than the threshold.
        sortedItemsByFreq.add("null");  // add temp value to sorted list first(for the purpose of comapare)
        itemsToFreq.put("null", 0);
        for (String item : itemsToFreq.keySet()) {
            int count = itemsToFreq.get(item);
            if (count < threshold) // if the number of an item less than threshold, then just added it to the remove list.
                continue;

            int i = 0;
            for (String itemInList : sortedItemsByFreq) {
                if (itemsToFreq.get(itemInList) < count) {
                    sortedItemsByFreq.add(i, item);
                    break;
                }
                i++;
            }
        }
        sortedItemsByFreq.remove("null");   // remove the temp value from all the set.
        itemsToFreq.remove("null");
    }

    /** Phase2 - Scan DB and construct the FP-Tree. */
    private void constructFPTree(LinkedList<String> sortedItemsByFreq) throws FileNotFoundException {
        Scanner scanner = new Scanner(new FileInputStream(file));
        String oneLine[], previousLine[] = null;
        String currentTID = "-1";   // default -1
        LinkedHashSet<String> aSortedTransaction = new LinkedHashSet<>();    // ordered, and non-duplicate(# of same item isn't important)
        boolean itemsExist[] = new boolean[sortedItemsByFreq.size()];

        // step 2.1: init headerTable, and add all freq item to it.
        for (String item : sortedItemsByFreq)
            headerTable.add(new FPTreeNode(item, itemsToFreq.get(item)));

        // step 2.2: construct fptree root.
        FPTreeNode treeRoot = new FPTreeNode("null");

        // step 2.3: Scanning the DB, (2.3.1)reserved the freq items and sort them and remove non-freq item in each transaction,
        // (2.3.2)then construct the fptree based on the sorted freq items.
        while (scanner.hasNextLine()) {
            if (previousLine != null) {
                oneLine = previousLine;
                previousLine = null;
            } else oneLine = scanner.nextLine().split(" ");   // if previous line is null, just scan next line.

            if (oneLine[0].equals(currentTID)) {  // same transaction data, keep inserting if it's freq item.
                String item = oneLine[1];
                if (!sortedItemsByFreq.contains(item))
                    continue;  // this item should be delete, so wouldn't add to sortedTransaction set.
                itemsExist[sortedItemsByFreq.indexOf(item)] = true;
            } else {   // means this line is next Tansaction.
                // In this section, we sort the reserved item based on sortedItemsByFreq
                previousLine = oneLine;

                // create FPTreeNode for each sorted, reserved item, and construct FPTree.
                sortingTransaction(aSortedTransaction, itemsExist, sortedItemsByFreq);
                if(!aSortedTransaction.isEmpty()) sortedTransaction.add(new ArrayList<>(aSortedTransaction));
                insertToTree(treeRoot, headerTable, aSortedTransaction);
                aSortedTransaction.clear(); // clear itself
            }
            currentTID = oneLine[0];
        }
        // deal with last transaction.
        sortingTransaction(aSortedTransaction, itemsExist, sortedItemsByFreq);
        if(!aSortedTransaction.isEmpty()) sortedTransaction.add(new ArrayList<>(aSortedTransaction));
        insertToTree(treeRoot, headerTable, aSortedTransaction);
    }

    private void sortingTransaction(LinkedHashSet<String> sortedTransaction, boolean itemsExist[], LinkedList<String> sortedItemsByFreq) {
        for (int i = 0; i < itemsExist.length; i++) {
            if (itemsExist[i]) sortedTransaction.add(sortedItemsByFreq.get(i));
            itemsExist[i] = false;
        }
    }

    // recursive method to insert item to the fp tree
    private void insertToTree(FPTreeNode currentNode, LinkedHashSet<FPTreeNode> headerTable, LinkedHashSet<String> sortedTransaction) {
        if (sortedTransaction.isEmpty()) return; // terminal condition.

        String item = sortedTransaction.iterator().next();  // get first one in the set.
        FPTreeNode newNode = null;
        boolean alreadyAdded = false;   // Is this pattern(from root to this item) already exist?

        for (FPTreeNode child : currentNode.getChildren()) {
            if (child.getItem().equals(item)) {   // means that this item is already in the fp tree, just count++
                alreadyAdded = true;
                newNode = child;    // point to this node
                child.countAdd(1);
                break;  // loop
            }
        }
        if (!alreadyAdded) {
            newNode = new FPTreeNode(item, patternCount);
            newNode.setParent(currentNode);  // point to each other
            currentNode.setChildren(newNode);

            for (FPTreeNode headerPointer : headerTable) {
                if (headerPointer.getItem().equals(item)) {
                    while (headerPointer.getNext() != null) // not null means it already pointed to an Node, so keep tracing the linkedList to find the tailer
                        headerPointer = headerPointer.getNext();
                    headerPointer.setNext(newNode); // tailer in the linkedList point to the new Node.
                    break;
                }
            }
        }
        sortedTransaction.remove(sortedTransaction.iterator().next());  // we get item in the set first, and del it after finished insert.
        insertToTree(newNode, headerTable, sortedTransaction);
    }

    /** Phase3 - find Cond. Pattern base first, then build Cond. FP-tree, last find Frequent patterns. */
    private void FPgrowth(String base, LinkedHashSet<FPTreeNode> headerTable){  // parameter for the purpose of recursive.
        for(FPTreeNode node: headerTable){
            String currentPattern = (base != null ? base + " " : "") + node.getItem();
            int supportOfCurrentPattern = 0;
            HashMap<String, Integer> condPatternBase = new HashMap<>();
            while (node.getNext() != null) {
                node = node.getNext();
                supportOfCurrentPattern += node.getCount();
                String condPattern = null;
                FPTreeNode conditionalItem = node.getParent();

                while (conditionalItem.getParent() != null) {   // bottom-up to find the pattern, if conditionalItem is root, won't entry this scope.
                    condPattern = conditionalItem.getItem() + " " + (condPattern != null ? condPattern : "");
                    conditionalItem = conditionalItem.getParent();
                }
                if (condPattern != null)
                    condPatternBase.put(condPattern, node.getCount());
            }
            if(base != null)    // base equals to null means there is only one item in currentPattern, don't put this in freqPatterns.
                freqPatterns.put(currentPattern, supportOfCurrentPattern);

            HashMap<String, Integer> condItemsMaptoFreq = new HashMap<>();
            for (String conditionalPattern : condPatternBase.keySet()) {
                String split[] = conditionalPattern.split(" ");
                for(String substr : split) {
                    if (condItemsMaptoFreq.containsKey(substr))
                        condItemsMaptoFreq.put(substr, condItemsMaptoFreq.get(substr) + condPatternBase.get(conditionalPattern));
                    else
                        condItemsMaptoFreq.put(substr, condPatternBase.get(conditionalPattern));
                }
            }

            LinkedHashSet<FPTreeNode> condHeaderTable = new LinkedHashSet<>();
            for (String itemsforTable : condItemsMaptoFreq.keySet()) {
                int count = condItemsMaptoFreq.get(itemsforTable);
                if (count < threshold) continue;
                condHeaderTable.add(new FPTreeNode(itemsforTable, count));
            }
            FPTreeNode condFPtree = condFPtree_constructor(condPatternBase, condItemsMaptoFreq, condHeaderTable);

            if (!condFPtree.getChildren().isEmpty())
                FPgrowth(currentPattern, condHeaderTable);
        }
    }

    private FPTreeNode condFPtree_constructor(HashMap<String, Integer> condPatternBase, HashMap<String, Integer> condItemsMaptoFreq, LinkedHashSet<FPTreeNode> condHeaderTable) {
        FPTreeNode condFPtree = new FPTreeNode("null");
        for (String pattern : condPatternBase.keySet()) {
            LinkedHashSet<String> pattern_vector = new LinkedHashSet<>();
            String split[] = pattern.split(" ");
            for(String substr : split)
                if (condItemsMaptoFreq.get(substr) >= threshold)
                    pattern_vector.add(substr);
            patternCount = condPatternBase.get(pattern);
            insertToTree(condFPtree, condHeaderTable, pattern_vector);
        }
        return condFPtree;
    }

    /** Phase4 - find the candidate rules(have lhs and rhs) of freqPatterns, and cal confident to filter some patterns. */
    private void findCandidateRules(){
        for (String frequentPattern : freqPatterns.keySet()){
            ArrayList<String> split = new ArrayList<>(Arrays.asList(frequentPattern.split(" ")));
            findCandidateRules(new ArrayList<>(), split, freqPatterns.get(frequentPattern), true);
        }
    }

    private void findCandidateRules(ArrayList<String> leftSide, ArrayList<String> rightSide, int count, boolean recursive_root){
        if(rightSide.size() <= 1) return;   // rightSide most have at least one.
        for(int i=0; i<rightSide.size(); i++){
            if(recursive_root && i != 0) return;    // e.g., "A B C D", only needs those combination that start with "A"
            ArrayList<String> tempLeftSide = new ArrayList<>(leftSide);
            tempLeftSide.add(rightSide.get(i));
            ArrayList<String> tempRightSide = new ArrayList<>(rightSide);
            tempRightSide.remove(i);

            // cal Confident, if bigger than min_confident, put it into x_HandSidefreqPatterns
            calConfident(tempLeftSide, tempRightSide, count);
            calConfident(tempRightSide, tempLeftSide, count);

            findCandidateRules(tempLeftSide, tempRightSide, count, false);
        }
    }

    private void calConfident(ArrayList<String> leftSide, ArrayList<String> rightSide, int count){
        double Lcount = 0, Rcount = 0;

        for(ArrayList<String> list : sortedTransaction){
            if(list.containsAll(leftSide)) {
                Lcount++;
                if(list.containsAll(rightSide))
                    Rcount++;
            }
        }

        double conf = Rcount/Lcount;
        if(conf >= min_confident){
            String str = leftSide + " -> " + rightSide;
            x_HandSidefreqPatterns.put(str, conf);
        }
    }

    public static void main(String args[]) throws FileNotFoundException {
        double min_support = 0.02;
        double conf = 0.9;
        new FPGrowth(new File("src/1_10_0.05.txt"), min_support, conf);
    }
}

class FPTreeNode {
    private String item;
    private int count = 1;  // default one(itself)
    private FPTreeNode parent;
    private FPTreeNode next;    // for header table
    private ArrayList<FPTreeNode> children;

    public FPTreeNode(String item){
        this.item = item;
        children = new ArrayList<>();
    }

    public FPTreeNode(String item, int count){  // for header table
        this(item);
        this.count = count;
    }

    public void countAdd(int n) {
        this.count += n;
    }

    public void setNext(FPTreeNode next) {
        this.next = next;
    }

    public void setParent(FPTreeNode parent) {
        this.parent = parent;
    }

    public void setChildren(FPTreeNode children) {
        this.children.add(children);
    }

    public FPTreeNode getNext() {
        return next;
    }

    public FPTreeNode getParent() {
        return parent;
    }

    public int getCount() {
        return count;
    }

    public String getItem() {
        return item;
    }

    public ArrayList<FPTreeNode> getChildren() {
        return children;
    }
}