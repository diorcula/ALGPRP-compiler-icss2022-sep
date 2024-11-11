package nl.han.ica.datastructures;

public class HANLinkedList<T> implements IHANLinkedList<T> {
    private ListNode<T> head = null;

    public HANLinkedList() {
    }

    @Override
    public void addFirst(T value) {
        if (head == null) {
            head = new ListNode<T>(value, null);
        } else {
            head = new ListNode<T>(value, head);
        }
    }

    @Override
    public void removeFirst() {
        head = head.next;
    }

    @Override
    public T getFirst() {
        if (head == null) {
            throw new RuntimeException("HEAD IS NULL, HANLinkedList is empty");
        }
        return head.element;
    }

    @Override
    public int getSize() {
        int size = 0;
        ListNode<T> current = head;
        if (current == null) {
            return 0;
        }
        while (current.next != null) {
            size++;
            current = current.next;
        }
        return size;
    }

    public void printList(HANLinkedList<T> list) {
        ListNode<T> currentListNode = list.head.next;

        while (currentListNode != null) {
            currentListNode = currentListNode.next;
        }
    }

    @Override
    public void clear() {

    }

    @Override
    public void insert(int index, T value) {

    }

    @Override
    public void delete(int pos) {

    }

    @Override
    public T get(int pos) {
        ListNode<T> tmp = head;

        for (int i = 0; i <= pos; i++) {
            tmp = tmp.next;
        }

        return tmp.element;
    }

    private static class ListNode<T> {
        T element;
        ListNode<T> next;

        ListNode(T element, ListNode<T> next) {
            this.element = element;
            this.next = next;
        }
    }
}
