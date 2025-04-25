package com.mycompany.webhookapp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.*;

public class FollowerSolver {

    public static JsonNode solve(JsonNode input) {
        int n = input.get("n").asInt();
        int findId = input.get("findId").asInt();
        JsonNode users = input.get("users");

        Map<Integer, List<Integer>> graph = new HashMap<>();
        for (JsonNode user : users) {
            int id = user.get("id").asInt();
            List<Integer> follows = new ArrayList<>();
            for (JsonNode f : user.get("follows")) {
                follows.add(f.asInt());
            }
            graph.put(id, follows);
        }

        Set<Integer> visited = new HashSet<>();
        Queue<Integer> queue = new LinkedList<>();
        queue.add(findId);
        visited.add(findId);

        for (int level = 0; level < n; level++) {
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                int current = queue.poll();
                for (int neighbor : graph.getOrDefault(current, List.of())) {
                    if (!visited.contains(neighbor)) {
                        queue.add(neighbor);
                        visited.add(neighbor);
                    }
                }
            }
        }

        // queue now has IDs at nth level
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode result = mapper.createArrayNode();
        for (int id : queue) {
            result.add(id);
        }
        return result;
    }
}
