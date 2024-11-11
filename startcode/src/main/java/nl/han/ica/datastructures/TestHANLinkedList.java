package nl.han.ica.datastructures;

public class TestHANLinkedList {
    public static void main(String[] args) {
        HANLinkedList<Integer> list = new HANLinkedList<>();

        // Test adding elements
        list.addFirst(1);
        list.addFirst(2);
        list.addFirst(3);

        // Print the first element
        System.out.println("First element: " + list.getFirst()); // Should print 3

        // Test removing elements
        list.removeFirst();
        System.out.println("First element after removal: " + list.getFirst()); // Should print 2

        // Clear the list
        list.removeFirst();
        list.removeFirst();

        // Check if the list is empty
        try {
            System.out.println("First element after clearing: " + list.getFirst());
        } catch (RuntimeException e) {
            System.out.println(e.getMessage()); // Should print "FOUTJE"
        }
    }
}