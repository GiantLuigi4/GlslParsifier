package fuzzers.custom;

import java.util.*;

public class Scope {
    Map<String, List<String>> myVars = new HashMap<>();
    Scope parent;

    public Scope(Scope parent) {
        this.parent = parent;
    }

    public Scope() {
    }

    public void addVar(String type, String name) {
        List<String> list = myVars.get(type);
        //noinspection Java8MapApi
        if (list == null) {
            list = new ArrayList<>();
            myVars.put(type, list);
        }
        list.add(name);
    }

    public String getRandomValueForKey(Random rng, String key) {
        return getRandomValueForKey(rng, key, false);
    }

    public String getRandomValueForKey(Random rng, String key, boolean excludeGlobal) {
        // Step 1: Collect all relevant lists and calculate total elements
        List<List<String>> relevantLists = new ArrayList<>();
        int totalElements = 0;

        // Traverse the scope chain
        Scope current = this;
        while (current != null) {
            if (excludeGlobal && current.parent == null)
                break;

            List<String> list = current.myVars.get(key);
            if (list != null && !list.isEmpty()) {
                relevantLists.add(list);
                totalElements += list.size();
            }
            current = current.parent;
        }

        // Return null if no elements found
        if (totalElements == 0) return null;

        // Step 2: Generate random index
        int randomIndex = rng.nextInt(totalElements);

        // Step 3: Find the correct list using binary search
        // Build cumulative size array for binary search
        int[] cumulativeSizes = new int[relevantLists.size()];
        int cumulativeSum = 0;
        for (int i = 0; i < relevantLists.size(); i++) {
            cumulativeSizes[i] = cumulativeSum;
            cumulativeSum += relevantLists.get(i).size();
        }

        // Binary search to find the correct list
        int left = 0;
        int right = cumulativeSizes.length - 1;
        int selectedListIndex = 0;

        while (left <= right) {
            int mid = left + (right - left) / 2;

            if (cumulativeSizes[mid] <= randomIndex) {
                selectedListIndex = mid;
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }

        // Step 4: Return the element from the selected list
        List<String> selectedList = relevantLists.get(selectedListIndex);
        return selectedList.get(randomIndex - cumulativeSizes[selectedListIndex]);
    }

    public boolean hasValueForKey(String type) {
        Scope curr = this;
        while (curr != null) {
            if (curr.myVars.containsKey(type))
                return true;
            curr = curr.parent;
        }
        return false;
    }
}
