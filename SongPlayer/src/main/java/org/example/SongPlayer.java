package org.example;

import java.util.*;

public class SongPlayer {

    // songId to users
    HashMap<String, Set<String>> song = new HashMap<>();
    // count -> list of songs
    Map<Integer, TreeSet<String>> rankBySongList = new TreeMap<>(Collections.reverseOrder());
    // songId -> count
    Map<String, Integer> songCountRank = new HashMap<>();
    // userId -> songs
    HashMap<String, LinkedHashSet<String>> userRecencyMap = new HashMap<>();

    public void add_song(String songId) {
        song.put(songId, new HashSet<>());
    }

    public void play_song(String userId, String songId) {
        if (!song.containsKey(songId)) throw new IllegalArgumentException("SongId not valid");
        Set<String> currentList = song.get(songId);

        // Adding song to current user list.
        // If song already added in user set and  we still want to update recency
        LinkedHashSet<String> set = userRecencyMap.getOrDefault(userId, new LinkedHashSet<>());
        set.remove(songId);
        set.add(songId);
        userRecencyMap.put(userId, set);

        if (!currentList.add(userId)) return;
        song.put(songId, currentList);



        // Getting current rank
        int songRank = songCountRank.getOrDefault(songId, 0);
        int newSongRank = songRank + 1;
        // update the new rank for the song
        songCountRank.put(songId, newSongRank);

        // Removing from previous rank
        TreeSet<String> existingSongsForOldCount = rankBySongList.getOrDefault(songRank, new TreeSet<>());
        existingSongsForOldCount.remove(songId);
        rankBySongList.put(songRank, existingSongsForOldCount);

        if (existingSongsForOldCount.isEmpty()) {
            rankBySongList.remove(songRank);
        }

        // Adding to new rank
        TreeSet<String> existingSongsForCount = rankBySongList.getOrDefault(newSongRank, new TreeSet<>());
        existingSongsForCount.add(songId);
        rankBySongList.put(newSongRank, existingSongsForCount);
    }

    public List<String> get_top_songs(int k) {
        List<String> result = new ArrayList<>();
        // Iterating over the sorted map
        for (Map.Entry<Integer, TreeSet<String>> entry : rankBySongList.entrySet()) {
            if (result.size() >= k) break;
            result.addAll(entry.getValue());
        }
        return result.subList(0, Math.min(k, result.size()));
    }

    public List<String> recently_played(String userId, int k) {
        LinkedHashSet<String> set = userRecencyMap.getOrDefault(userId, new LinkedHashSet<>());

        List<String> list = new ArrayList<>(set);
        Collections.reverse(list);

        return list.subList(0, Math.min(k, list.size()));
    }

}
