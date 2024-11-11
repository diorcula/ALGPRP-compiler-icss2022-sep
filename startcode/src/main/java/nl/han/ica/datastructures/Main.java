package nl.han.ica.datastructures;

public class Main {
    public static void main(String[] args) {

        HANStack<Object> HANStack = new HANStack<>();

        HANStack.push("test-1");
//        HANStack.printStack();
        HANStack.push("test-2");
//        HANStack.printStack();
        HANStack.push("test-3");
//
        HANStack.peek();
//        HANStack.pop();
        HANStack.printStack();

    }
}
