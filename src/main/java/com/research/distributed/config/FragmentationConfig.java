package com.research.distributed.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FragmentationConfig {
    private static FragmentationConfig instance;

    private final Map<String, String> departmentToFragment;
    private final Map<String, String> fragmentToDepartment;
    private final List<String> allFragments;

    private FragmentationConfig() {
        departmentToFragment = new HashMap<>();
        fragmentToDepartment = new HashMap<>();

        // P1 department maps to p1 fragment
        departmentToFragment.put("P1", "p1");
        fragmentToDepartment.put("p1", "P1");

        // P2 department maps to p2 fragment
        departmentToFragment.put("P2", "p2");
        fragmentToDepartment.put("p2", "P2");

        allFragments = Arrays.asList("p1", "p2");
    }

    public static synchronized FragmentationConfig getInstance() {
        if (instance == null) {
            instance = new FragmentationConfig();
        }
        return instance;
    }

    public String getFragmentForDepartment(String department) {
        return departmentToFragment.get(department);
    }

    public String getDepartmentForFragment(String fragment) {
        return fragmentToDepartment.get(fragment);
    }

    public List<String> getAllFragments() {
        return allFragments;
    }

    public String getFragmentForGroup(String groupId) {
        // Determine fragment based on group ID pattern
        // Groups NC01, NC02 are in P1; NC03, NC04 are in P2
        if (groupId == null || groupId.isEmpty()) {
            return null;
        }

        // Extract numeric part
        String numPart = groupId.replaceAll("[^0-9]", "");
        if (numPart.isEmpty()) {
            return null;
        }

        int num = Integer.parseInt(numPart);
        // Groups 1-2 are in P1, 3-4 are in P2
        return num <= 2 ? "p1" : "p2";
    }

    public String getTableName(String baseTable, String fragment) {
        return baseTable + "_" + fragment;
    }

    public boolean isValidFragment(String fragment) {
        return allFragments.contains(fragment);
    }

    public boolean isValidDepartment(String department) {
        return departmentToFragment.containsKey(department);
    }
}
