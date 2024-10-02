package nl.han.ica.datastructures;

import java.util.Iterator;

public class HANLinkedList<T> implements IHANLinkedList<T> {

    private ListNode<T> head;
    private int size;

    public HANLinkedList() {
        head = new ListNode<>();
        size = 0;
    }

    @Override
    public void addFirst(T value) {
        ListNode<T> newNode = new ListNode<>();
        newNode.Element = value;
        newNode.next = head.next;
        head.next = newNode;
        size++;
    }

    @Override
    public void clear() {
        head.next = null;
        size = 0;
    }

    @Override
    public void insert(int index, T value) {
        ListNode<T> current = head;
        if(index < 0 || index > size) {
            throw new IndexOutOfBoundsException();
        }
        for (int i = 0; i < index; i++) {
            current = current.next;
        }
        ListNode<T> newNode = new ListNode<>();
        newNode.Element = value;
        newNode.next = current.next;
        current.next = newNode;
        size++;
    }

    @Override
    public void delete(int pos) {
        ListNode<T> current = head;
        if (pos < 0 || pos >= size) {
            throw new IndexOutOfBoundsException();
        }
        for (int i = 0; i < pos; i++) {
            current = current.next;
        }
        current.next = current.next.next;
        size--;
    }

    @Override
    public T get(int pos) {
        if (pos < 0 || pos >= size) {
            throw new IndexOutOfBoundsException();
        }
        ListNode<T> current = head;
        for (int i = 0; i <= pos; i++) {
            current = current.next;
        }
        return current.Element;
    }

    @Override
    public void removeFirst() {
        head.next = head.next.next;
        size--;
    }

    @Override
    public T getFirst() {
        return head.next.Element;
    }

    @Override
    public int getSize() {
        return size;
    }
}
