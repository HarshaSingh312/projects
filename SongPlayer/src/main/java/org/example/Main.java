package org.example;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    static void main() {
        //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
        // to see how IntelliJ IDEA suggests fixing it.
        SongPlayer sp = new SongPlayer();

        // ---------- Test 1: Add + Basic Play ----------
        sp.add_song("S1");
        sp.add_song("S2");
        sp.add_song("S3");

        sp.play_song("U1", "S1");
        sp.play_song("U2", "S1");
        sp.play_song("U1", "S2");

        System.out.printf("Top 2 songs: %s\n", sp.get_top_songs(2));
        // Expected: [S1, S2]

        // ---------- Test 2: Unique user constraint ----------
        sp.play_song("U1", "S1"); // duplicate user → should NOT increase count

        System.out.printf("Top 1 song after duplicate play: %s\n", sp.get_top_songs(1));
        // Expected: [S1]

        // ---------- Test 3: Ranking tie (lexicographic order) ----------
        sp.play_song("U3", "S2"); // S1=2, S2=2

        System.out.printf("Top 2 songs (tie case): %s\n", sp.get_top_songs(2));
        // Expected: [S1, S2] (TreeSet → lexicographically sorted)

        // ---------- Test 4: More songs, different ranks ----------
        sp.play_song("U4", "S3");
        sp.play_song("U5", "S3");
        sp.play_song("U6", "S3"); // S3 = 3 (highest)

        System.out.printf("Top 3 songs: %s\n", sp.get_top_songs(3));
        // Expected: [S3, S1, S2]

        // ---------- Test 5: Recently played ----------
        System.out.printf("Recently played U1 (k=2): %s\n", sp.recently_played("U1", 2));
        // Expected: [S2, S1]

        // ---------- Test 6: Recently played ordering update ----------
        sp.play_song("U1", "S2"); // should move S1 to most recent

        System.out.printf("Recently played U1 (k=2) after replay: %s\n", sp.recently_played("U1", 2));
        // Expected: [S1, S2], getting [s2, s1]

        // ---------- Test 7: k > available ----------
        System.out.printf("Top 10 songs: %s\n", sp.get_top_songs(10));
        // Expected: all songs without error

        System.out.printf("Recently played U1 (k=10): %s\n", sp.recently_played("U1", 10));
        // Expected: all songs for U1

        // ---------- Test 8: User with no history ----------
        System.out.printf("Recently played U99: %s\n", sp.recently_played("U99", 3));
        // Expected: []

        // ---------- Test 9: Invalid song ----------
        try {
            sp.play_song("U1", "INVALID");
        } catch (Exception e) {
            System.out.printf("Invalid song test: %s\n", e.getMessage());
        }

        // ---------- Test 10: No songs added ----------
        SongPlayer sp2 = new SongPlayer();
        System.out.printf("Top songs when none exist: %s\n", sp2.get_top_songs(3));
        // Expected: []
    }
}
