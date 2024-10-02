package nl.han.ica.datastructures;

public class HANStack<T> implements IHANStack<T>{

    private IHANLinkedList<T> stack;

    public HANStack() {
        stack = new HANLinkedList<>();
    }

    @Override
    public void push(T value) {
        stack.addFirst(value);
    }

    @Override
    public T pop() {
        if(stack.getSize() == 0) {
            throw new IndexOutOfBoundsException();
        }
        T value = stack.get(0);
        stack.removeFirst();
        return value;
    }

    @Override
    public T peek() {
        if(stack.getSize() == 0) {
            throw new IndexOutOfBoundsException();
        }
        return stack.get(0);
    }
}
