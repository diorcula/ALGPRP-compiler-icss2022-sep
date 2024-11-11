package nl.han.ica.datastructures;

import java.util.HashMap;

public class HashmapTableLinked<K, V> {
    private final IHANLinkedList<HashMap<K, V>> scopes;

    public HashmapTableLinked() {
        this.scopes = new HANLinkedList<>();
        this.pushScope(); // Ensure there is always at least one scope
    }

    public void pushScope() {
        this.scopes.addFirst(new HashMap<>());
        System.out.println("Scope pushed. Total scopes: " + scopes.getSize());
    }

    public void popScope() {
        if (scopes.getSize() > 0) {
            this.scopes.removeFirst();
            System.out.println("Scope popped. Total scopes: " + scopes.getSize());
        } else {
            throw new IllegalStateException("No scopes available to pop.");
        }
    }

    public void putVariable(K key, V value) {
        for (int i = scopes.getSize() - 1; i >= 0; i--) {
            if (scopes.get(i).containsKey(key)) {
                scopes.get(i).put(key, value);
                System.out.println("Variable " + key + " updated in scope " + i);
                return;
            }
        }
        if (scopes.getSize() > 0) {
            scopes.getFirst().put(key, value); //the current scope is the first one
            System.out.println("Variable " + key + " added to the first scope.");
        } else {
            throw new IllegalStateException("No scopes available to put the variable.");
        }
    }

    public V getVariable(K key) {
        if (scopes.getFirst().containsKey(key)) {
            return scopes.getFirst().get(key);
        } else {
            for (int i = scopes.getSize() - 1; i >= 0; i--) {
                System.out.println("looking up for: " + key + " in scope " + i);
                V result = scopes.get(i).get(key);
                if (result != null) {
                    System.out.println("Variable " + key + " found in scope " + i);
                    return result;
                }
            }
        }
        System.out.println("Variable " + key + " not found in any scope.");
        return null;
    }

    public int getTotalAmountOfScopes() {
        return scopes.getSize();
    }
}