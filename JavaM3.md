# Java Programming | Module III Notes | Vaultscapes

---

# PART A: Multithreaded Programming

---

## Concept: Multithreading — Basic Concepts & Need

### Definition
Multithreading is the ability of a CPU to execute multiple threads concurrently within a single process. From the user's perspective, running multiple programs simultaneously is multitasking; from the OS perspective, this is multithreading (also called concurrent programming).

### Terms Related to Concept
1. **Process**: A running instance of a program in memory with its own memory space and resources.
2. **Thread**: A single sequential flow of control within a process. Also called a lightweight process.
3. **Multitasking**: Running more than one program (process) at a time by periodically switching the CPU.
4. **Multithreading**: Multiple threads running concurrently within a single process sharing the same memory.
5. **Concurrent Programming**: Writing programs that execute multiple tasks in overlapping time periods.
6. **run() method**: The method that contains the code executed by the thread.
7. **start() method**: Initiates thread execution and internally calls `run()`.

### Concept Outline
A conventional (single-threaded) process cannot perform multiple operations simultaneously. In multithreading, CPU time given to a process is divided among all its threads. Each thread provides a separate execution path. Multithreading is a feature of Java not natively supported by C/C++.

### Need / Advantages of Multithreading

| Advantage | Description |
|---|---|
| Parallel Execution | Large tasks can be split into sub-tasks running in parallel |
| Better CPU Utilization | CPU is never idle; another thread runs while one is waiting (I/O, sleep) |
| Responsiveness | GUI applications stay responsive while background work is done |
| Resource Sharing | Threads share process memory — no costly IPC needed |
| Lightweight | Creating a thread requires far fewer resources than creating a new process |
| Rich Applications | Graphics, animation, sound applications benefit greatly |

---

## Concept: Life Cycle of a Thread

### Definition
A thread passes through multiple states during its lifetime. The thread life cycle describes all possible states a thread can be in, from creation to termination.

### Terms Related to Concept
1. **New (Born) State**: Thread object is created but `start()` has not been called yet.
2. **Runnable State**: Thread is ready to run; `start()` has been called. It is in the thread scheduler's run queue.
3. **Running State**: The thread scheduler has picked this thread and it is currently executing.
4. **Blocked/Waiting State**: Thread is temporarily inactive — waiting for I/O, a lock, `sleep()`, `wait()`, or `join()`.
5. **Dead (Terminated) State**: Thread has completed execution of `run()`, or was stopped. A dead thread cannot be restarted.

### Thread Life Cycle Diagram

```text
New  ──start()──►  Runnable  ◄──────────────────────────────┐
                      │                                       │
                 scheduled by OS                        notify()/notifyAll()
                      │                                 sleep expires / join completes
                      ▼                                       │
                  Running  ──wait()/sleep()/join()──►  Blocked/Waiting
                      │
                  run() ends / stop()
                      │
                      ▼
                    Dead
```

### State Transition Table

| Method/Event | From State | To State |
|---|---|---|
| `start()` | New | Runnable |
| Scheduler picks thread | Runnable | Running |
| `sleep(ms)` | Running | Blocked (timed) |
| `wait()` | Running | Blocked (waiting) |
| `notify()` / `notifyAll()` | Blocked | Runnable |
| `join()` on another thread | Running | Blocked |
| `run()` completes | Running | Dead |
| `yield()` | Running | Runnable |

### Detailed Notes
- `isAlive()` returns `true` if thread is in Runnable, Running, or Blocked state.
- Once a thread is in Dead state, it cannot be restarted. Calling `start()` on a dead thread throws `IllegalThreadStateException`.
- A thread in Blocked state does not consume CPU time.

---

## Concept: How to Create a Thread

### Definition
In Java, there are two ways to create a thread:
1. By extending the `Thread` class.
2. By implementing the `Runnable` interface.

### Terms Related to Concept
1. **Thread class**: Defined in `java.lang` package. Contains methods like `start()`, `run()`, `sleep()`, `getName()`, etc.
2. **Runnable interface**: Has a single abstract method `run()`. Preferred when the class already extends another class.
3. **run() method**: Contains the code to be executed concurrently. Must be overridden.
4. **start() method**: Causes JVM to call `run()` in a new thread of execution.
5. **this keyword**: Refers to the current object; used to pass the Runnable instance to Thread constructor.

### Method 1: Extending Thread Class

```java
class MyThread extends Thread {
    public void run() {
        for (int i = 0; i < 5; i++)
            System.out.println(getName() + " i=" + i);
    }
    MyThread() {
        start(); // calls run() automatically
    }
}

class Demo {
    public static void main(String[] args) {
        MyThread t1 = new MyThread();
        MyThread t2 = new MyThread();
    }
}
```

**Key Points:**
- Override `run()` to define thread behavior.
- Call `start()` to begin execution (do NOT directly call `run()` — that runs it in the current thread, not a new one).
- Use `getName()` to get the thread name (default: `Thread-0`, `Thread-1`, etc.).
- Use `setName(String)` to assign custom names.
- Each run may give different output because thread scheduling is non-deterministic.

### Method 2: Implementing Runnable Interface

```java
class MyTask implements Runnable {
    public void run() {
        for (int i = 0; i < 5; i++)
            System.out.println(Thread.currentThread().getName() + " i=" + i);
    }
}

class Demo {
    public static void main(String[] args) {
        Thread t1 = new Thread(new MyTask(), "Thread-A");
        Thread t2 = new Thread(new MyTask(), "Thread-B");
        t1.start();
        t2.start();
    }
}
```

**Key Points:**
- Preferred when your class already extends another class (Java supports single inheritance).
- Pass the Runnable object to the `Thread` constructor.
- Use `Thread.currentThread().getName()` to get the thread name inside `run()`.

### Comparison: Thread vs Runnable

| Aspect | Extending Thread | Implementing Runnable |
|---|---|---|
| Inheritance | Consumes inheritance slot | Keeps inheritance slot free |
| Flexibility | Less flexible | More flexible (can extend other class) |
| Thread methods | Directly accessible (`getName()`, etc.) | Must use `Thread.currentThread()` |
| Preferred when | Quick/simple thread creation | Professional/reusable code |

### Important Thread Class Methods

| Method | Description |
|---|---|
| `start()` | Starts thread execution, calls `run()` |
| `run()` | Thread's task code — override this |
| `sleep(long ms)` | Pauses thread for given milliseconds; throws `InterruptedException` |
| `getName()` | Returns name of the thread |
| `setName(String s)` | Sets name of the thread |
| `currentThread()` | Returns reference to currently executing thread |
| `isAlive()` | Returns `true` if thread has started and not yet terminated |
| `join()` | Waits for this thread to finish before proceeding |
| `yield()` | Hints the scheduler to give other threads a chance to run (Round-Robin) |
| `setPriority(int p)` | Sets thread priority |
| `getPriority()` | Returns thread priority |
| `setDaemon(boolean)` | Marks thread as daemon (must call before `start()`) |
| `isDaemon()` | Returns `true` if thread is a daemon |

### Sample Question
**Q**: Create two threads using `Runnable` that print their name and a counter from 1 to 3 with a 500ms delay.

```java
class Counter implements Runnable {
    String name;
    Counter(String n) { name = n; }
    public void run() {
        for (int i = 1; i <= 3; i++) {
            System.out.println(name + " : " + i);
            try { Thread.sleep(500); }
            catch (InterruptedException e) { System.out.println("Interrupted"); }
        }
    }
}
class Main {
    public static void main(String[] args) {
        new Thread(new Counter("Thread-A")).start();
        new Thread(new Counter("Thread-B")).start();
    }
}
```

---

## Concept: Thread Priorities

### Definition
Thread priority is a numerical value that suggests to the thread scheduler which thread should run first. A higher-priority thread runs before a lower-priority one.

### Terms Related to Concept
1. **NORM_PRIORITY**: Default priority of every thread. Value = `5`.
2. **MIN_PRIORITY**: Minimum possible thread priority. Value = `1`.
3. **MAX_PRIORITY**: Maximum possible thread priority. Value = `10`.
4. **setPriority(int p)**: Sets the priority of a thread (value must be between 1–10).
5. **getPriority()**: Returns the current priority of a thread.
6. **Thread Scheduler**: Part of JVM/OS that decides which ready thread to execute next.

### Concept Outline
All threads are created with `NORM_PRIORITY` (5) by default. Java's thread scheduler uses priority to determine which thread from the runnable pool gets CPU time. A higher-priority thread preempts lower-priority ones. However, priority behavior is platform-dependent — JVM does not guarantee strict priority ordering.

### Priority Constants

| Constant | Value | Meaning |
|---|---|---|
| `Thread.MIN_PRIORITY` | 1 | Lowest priority |
| `Thread.NORM_PRIORITY` | 5 | Default (normal) priority |
| `Thread.MAX_PRIORITY` | 10 | Highest priority |

### Code Example

```java
class Task implements Runnable {
    Thread thr;
    Task(String name, int priority) {
        thr = new Thread(this, name);
        thr.setPriority(priority);
        thr.start();
    }
    public void run() {
        System.out.println(thr.getName() + " priority=" + thr.getPriority());
    }
}

class PriorityDemo {
    public static void main(String[] args) {
        new Task("Low Thread",  Thread.MIN_PRIORITY);
        new Task("High Thread", Thread.MAX_PRIORITY);
        new Task("Norm Thread", Thread.NORM_PRIORITY);
    }
}
// Output order (typically): High Thread → Norm Thread → Low Thread
```

### Algorithm: Setting Thread Priority
1. Create thread object.
2. Call `setPriority(Thread.MAX_PRIORITY)` before calling `start()`.
3. Call `start()` to begin execution.
4. Higher-priority thread gets CPU time first.

---

## Concept: Thread Synchronization

### Definition
Thread synchronization is the mechanism to ensure that only one thread at a time accesses a shared resource. Java implements synchronization using a built-in monitor (also called a semaphore lock) associated with every object.

### Terms Related to Concept
1. **Monitor / Lock**: A mechanism that allows only one thread to execute a synchronized block/method at a time.
2. **synchronized method**: A method declared with the `synchronized` keyword; thread must acquire the object's lock to enter it.
3. **synchronized statement / block**: A block of code inside `synchronized(object) { }` that acquires the lock on the given object.
4. **Race Condition**: Problem that arises when multiple threads access/modify shared data concurrently without synchronization.
5. **Mutual Exclusion**: Only one thread executes in the critical section at any given time.
6. **Critical Section**: The portion of code that accesses shared resources.

### Concept Outline
Without synchronization, multiple threads accessing the same resource can interleave their operations unpredictably, producing wrong results. Java uses `synchronized` to enforce mutually exclusive access.

### Problem Without Synchronization

```java
class Display {
    void show(String s) {
        System.out.print("(" + s);
        try { Thread.sleep(1000); } catch (InterruptedException e) {}
        System.out.println(")");
    }
}
// Two threads calling show() on the same Display object produce:
// (Sweet(Sweety))   ← interleaved — WRONG
```

### Method 1: Synchronized Method

```java
class Display {
    synchronized void show(String s) {   // only one thread at a time
        System.out.print("(" + s);
        try { Thread.sleep(1000); } catch (InterruptedException e) {}
        System.out.println(")");
    }
}
// Output: (Sweet)  then  (Sweety)  ← correct
```

### Method 2: Synchronized Statement (Block)

```java
public void run() {
    synchronized(displayObj) {       // lock on the shared object
        displayObj.show(name);
    }
}
```

**Use synchronized block when**: you cannot modify the class (third-party code) or you want finer-grained control over locking.

### Synchronization Rules Summary

| Rule | Detail |
|---|---|
| One lock per object | Each object has exactly one monitor |
| Static synchronized | Locks the Class object, not an instance |
| Nested synchronized | The same thread can re-enter its own locks (reentrant) |
| No synchronization = race condition | Shared mutable data must be synchronized |

### Sample Question
**Q**: Without and with synchronization — demonstrate with Parentheses example.

- Without `synchronized`: Two threads interleave inside `display()` → garbled output `(Sweet(Sweety))`.
- With `synchronized void display(...)`: Second thread waits → output `(Sweet)(Sweety)`.

---

## Concept: Inter-Thread Communication

### Definition
Inter-thread communication (also called interprocess communication) is the mechanism by which threads coordinate their work by signaling each other using `wait()`, `notify()`, and `notifyAll()` methods.

### Terms Related to Concept
1. **wait()**: Makes the current thread release the monitor and go into a waiting state until another thread calls `notify()` or `notifyAll()` on the same object.
2. **notify()**: Wakes up one thread that is waiting on this object's monitor.
3. **notifyAll()**: Wakes up all threads waiting on this object's monitor. Only the highest-priority one gets the monitor.
4. **Producer-Consumer Problem**: Classic inter-thread communication problem where one thread produces data and another consumes it — they must coordinate via a shared queue.
5. **busy flag**: A boolean used in the shared resource to indicate current occupancy (e.g., `boolean busy = false`).

### Method Signatures (from java.lang.Object)

```java
final void wait() throws InterruptedException
final void wait(long timeout) throws InterruptedException
final void notify()
final void notifyAll()
```

> These methods must always be called from within a `synchronized` method or block, otherwise `IllegalMonitorStateException` is thrown.

### Concept Outline
`wait()` and `notify()` allow threads to cooperate without busy-waiting. A producer puts an item and notifies the consumer. The consumer retrieves it and notifies the producer that it is ready for more.

### Producer-Consumer Example

```java
class Queue {
    int item;
    boolean busy = false;

    synchronized int get() {
        if (!busy)
            try { wait(); } catch (InterruptedException e) {}
        System.out.println("Get: " + item);
        notify();
        return item;
    }

    synchronized void put(int item) {
        if (busy)
            try { wait(); } catch (InterruptedException e) {}
        this.item = item;
        busy = true;
        System.out.println("Put: " + item);
        notify();
    }
}

class Producer extends Thread {
    Queue q;
    Producer(Queue q) { this.q = q; }
    public void run() {
        for (int i = 0; i < 5; i++) q.put(i);
    }
}

class Consumer extends Thread {
    Queue q;
    Consumer(Queue q) { this.q = q; }
    public void run() {
        for (int i = 0; i < 5; i++) q.get();
    }
}

class Main {
    public static void main(String[] args) {
        Queue q = new Queue();
        new Producer(q).start();
        new Consumer(q).start();
    }
}
```

**Output:**
```text
Put: 0   Get: 0
Put: 1   Get: 1
Put: 2   Get: 2  ... (alternating, no interleaving)
```

### wait() vs notify() vs notifyAll()

| Method | Effect |
|---|---|
| `wait()` | Thread releases lock, enters waiting state |
| `notify()` | Wakes one waiting thread; lock not released until synchronized method exits |
| `notifyAll()` | Wakes all waiting threads; highest priority one gets the lock |

---

## Concept: Daemon Threads

### Definition
A daemon thread is a background service thread that runs as long as non-daemon threads are running. When all non-daemon threads finish, the JVM exits — daemon threads are automatically killed.

### Terms Related to Concept
1. **setDaemon(true)**: Marks thread as daemon — must be called before `start()`.
2. **isDaemon()**: Returns `true` if the thread is a daemon thread.
3. **Non-daemon thread**: Regular thread; JVM stays alive until all non-daemon threads complete.

```java
class BackgroundTask extends Thread {
    BackgroundTask() {
        setDaemon(true); // mark as daemon BEFORE start()
        start();
    }
    public void run() {
        while (true) {
            System.out.println("Daemon running...");
            try { sleep(100); } catch (InterruptedException e) {}
        }
    }
}
```

**Note**: Any thread created by a daemon thread is also automatically a daemon thread.

---

# PART B: Exception Handling

---

## Concept: The Idea Behind Exceptions

### Definition
An exception is a runtime error — an unexpected condition that disrupts the normal flow of program execution. Java provides a structured exception handling mechanism to trap these errors and handle them gracefully, preventing abnormal program termination.

### Terms Related to Concept
1. **Compile-time error**: Syntactic error caught by the compiler (e.g., missing semicolons). Not an exception.
2. **Runtime error / Exception**: Error that occurs during program execution (e.g., division by zero, null pointer).
3. **Logical error**: Bug in program logic that produces wrong output — not caught by compiler or runtime.
4. **Robust program**: A program that can handle unexpected inputs and errors without crashing.
5. **Exception object**: An instance of an exception class that is thrown when an error occurs.
6. **Stack trace**: Output showing the sequence of method calls that led to the exception — useful for debugging.

### Concept Outline
Without exception handling, programs guard against errors using `if-else` checks — this becomes messy and unmanageable. Java's exception handling separates error-detection code from normal logic using `try`, `catch`, `finally`, `throw`, and `throws`.

### Common Examples of Exceptions
- Division by zero → `ArithmeticException`
- Array index out of bounds → `ArrayIndexOutOfBoundsException`
- Accessing a null object → `NullPointerException`
- Invalid type cast → `ClassCastException`
- File not found → `FileNotFoundException`
- Invalid string-to-number conversion → `NumberFormatException`

---

## Concept: Exception Hierarchy — Exceptions & Errors

### Definition
In Java, all exception-related classes are organized in a hierarchy rooted at `java.lang.Throwable`. It has two direct subclasses: `Exception` and `Error`.

### Exception Class Hierarchy

```text
java.lang.Object
    └── java.lang.Throwable
            ├── java.lang.Error                  (Unchecked — serious system errors)
            │       ├── OutOfMemoryError
            │       ├── StackOverflowError
            │       └── VirtualMachineError
            └── java.lang.Exception              (Application-level exceptions)
                    ├── RuntimeException         (Unchecked)
                    │       ├── ArithmeticException
                    │       ├── NullPointerException
                    │       ├── ArrayIndexOutOfBoundsException
                    │       ├── ClassCastException
                    │       ├── NumberFormatException
                    │       └── IllegalArgumentException
                    ├── IOException              (Checked)
                    ├── ClassNotFoundException   (Checked)
                    ├── InterruptedException     (Checked)
                    └── ... (other checked exceptions)
```

### Terms Related to Concept
1. **Throwable**: Root class of all errors and exceptions. All throwable objects must be instances of this class.
2. **Error**: Represents serious problems that a reasonable application should not try to catch (e.g., `OutOfMemoryError`). Thrown by the JVM.
3. **Exception**: Represents conditions that a program can reasonably catch and recover from.
4. **RuntimeException**: Subclass of Exception. Unchecked — not required to be declared or caught.

---

## Concept: Types of Exceptions — Checked vs Unchecked

### Definition
Java distinguishes between checked and unchecked exceptions based on whether the compiler enforces handling requirements.

### Checked vs Unchecked

| Feature | Checked Exception | Unchecked Exception |
|---|---|---|
| Compiler enforcement | Yes — must catch or declare with `throws` | No — optional to catch |
| Base class | `Exception` (excluding `RuntimeException`) | `RuntimeException` or `Error` |
| Occurs at | Compile time check, runtime occurrence | Runtime only |
| Examples | `IOException`, `ClassNotFoundException`, `InterruptedException` | `ArithmeticException`, `NullPointerException`, `ArrayIndexOutOfBoundsException` |

### Common Unchecked (Runtime) Exceptions

| Exception Class | Cause |
|---|---|
| `ArithmeticException` | Division by zero |
| `ArrayIndexOutOfBoundsException` | Array index out of range |
| `NullPointerException` | Using a null reference |
| `ClassCastException` | Invalid type cast |
| `NumberFormatException` | Invalid string-to-number conversion |
| `StringIndexOutOfBoundsException` | String index out of range |
| `NegativeArraySizeException` | Array created with negative size |
| `IllegalArgumentException` | Illegal method argument |
| `IllegalThreadStateException` | Thread operation incompatible with current state |
| `UnsupportedOperationException` | Operation not supported |

### Common Checked Exceptions

| Exception Class | Cause |
|---|---|
| `IOException` | Input/output operation failed |
| `ClassNotFoundException` | Class not found |
| `InterruptedException` | Thread interrupted by another thread |
| `IllegalAccessException` | Access to class denied |
| `InstantiationException` | Cannot instantiate abstract class or interface |
| `NoSuchMethodException` | Method not found |
| `CloneNotSupportedException` | Object does not implement Cloneable |

---

## Concept: Control Flow in Exception Handling

### Definition
When an exception is thrown in a `try` block, the JVM immediately stops execution at that point and searches for a matching `catch` block. Control never returns to the point of the exception.

### Control Flow Rules
1. Code after the throw statement inside `try` is skipped.
2. JVM searches catch blocks top to bottom for a matching type.
3. Only the first matching catch block executes.
4. After `catch` executes, control moves to the code after the entire try-catch block.
5. `finally` always executes regardless of whether an exception was thrown or caught.

### Control Flow Diagram

```text
try block starts
    ──► normal statement
    ──► normal statement
    ──► exception THROWN  ─────────────────────┐
    (remaining try code SKIPPED)               │
                                               ▼
                                    catch(TypeA e)  ← match? YES → execute → finally → resume
                                    catch(TypeB e)  ← checked if TypeA didn't match
                                    catch(Exception e) ← catches all
                                               │
                                           no match
                                               │
                                           finally executes
                                               │
                                        JVM default handler
                                        (program terminates)
```

### Without try-catch — Default Handler

```java
class Main {
    public static void main(String[] args) {
        String s = "hello";
        int x = s.charAt(s.length() + 1); // StringIndexOutOfBoundsException
        System.out.println("This won't print");
    }
}
// Output: java.lang.StringIndexOutOfBoundsException (stack trace printed, program terminates)
```

---

## Concept: Use of try and catch

### Definition
The `try` block encloses code that might throw an exception. The `catch` block handles the exception if it is thrown. Together, they prevent abnormal termination.

### Basic Syntax

```java
try {
    // Code that might throw an exception
} catch (ExceptionType1 e1) {
    // Handle ExceptionType1
} catch (ExceptionType2 e2) {
    // Handle ExceptionType2
}
```

### Useful Exception Methods (from Throwable)

| Method | Description |
|---|---|
| `getMessage()` | Returns a string describing the exception |
| `printStackTrace()` | Prints stack trace to standard error |
| `toString()` | Returns class name + message as string |

### Single catch Example

```java
class Demo {
    public static void main(String[] args) {
        try {
            int result = Integer.parseInt("ABC123");
        } catch (NumberFormatException e) {
            System.out.println("Cannot convert: " + e.getMessage());
        }
        System.out.println("Program continues normally");
    }
}
```

### Multiple catch Blocks

```java
import java.io.*;
class Demo {
    public static void main(String[] args) {
        try {
            DataInputStream in = new DataInputStream(System.in);
            int n1 = Integer.parseInt(in.readLine());
            int n2 = Integer.parseInt(in.readLine());
            System.out.println("Result: " + (n1 / n2));
        } catch (ArithmeticException e) {
            System.out.println("Division by zero: " + e);
        } catch (NumberFormatException e) {
            System.out.println("Not a valid number: " + e);
        } catch (IOException e) {
            System.out.println("IO Error: " + e);
        }
    }
}
```

**Important Rule — Ordering of catch blocks:**
- More specific exception types must come before more general ones.
- Placing a parent class catch block before a child class catch block causes a compile-time error (unreachable code).

```java
// WRONG — compile error: ArrayIndexOutOfBoundsException already caught
catch (IndexOutOfBoundsException e) { ... }      // parent
catch (ArrayIndexOutOfBoundsException e) { ... } // child — unreachable!

// CORRECT
catch (ArrayIndexOutOfBoundsException e) { ... } // child first
catch (IndexOutOfBoundsException e) { ... }      // parent second
```

### Nested try-catch Blocks

```java
try {
    int a = Integer.parseInt(args);
    int b = Integer.parseInt(args);[1]
    try {
        System.out.println(a / b);
    } catch (ArithmeticException e) {
        System.out.println("Inner catch: divide by zero");
    }
} catch (NumberFormatException e) {
    System.out.println("Outer catch: not a number");
}
```

### Algorithm: Step-by-Step for Exception Handling
1. Identify code that may throw an exception.
2. Wrap it in a `try` block.
3. Add `catch` blocks for each expected exception type (specific → general).
4. Use `e.getMessage()` or `e.printStackTrace()` to display error info.
5. Optionally add `finally` for cleanup code.

---

## Concept: The finally Block

### Definition
The `finally` block contains code that always executes after the `try`-`catch` block, regardless of whether an exception was thrown, caught, or not. It is used for cleanup operations.

### Syntax

```java
try {
    // risky code
} catch (ExceptionType e) {
    // handle exception
} finally {
    // ALWAYS executes: cleanup code (close files, connections, etc.)
}
```

### Behavior of finally

| Scenario | Does finally execute? |
|---|---|
| No exception thrown | YES |
| Exception thrown and caught | YES |
| Exception thrown and NOT caught | YES (before JVM crash) |
| `return` inside try block | YES (before returning) |

### Code Example

```java
class FinallyDemo {
    static void test(boolean throwEx) {
        try {
            System.out.println("In try");
            if (throwEx) throw new NullPointerException();
            System.out.println("No exception");
        } catch (NullPointerException e) {
            System.out.println("Caught: " + e);
        } finally {
            System.out.println("Finally always runs");
        }
    }
    public static void main(String[] args) {
        test(false);  // No exception
        test(true);   // Exception thrown and caught
    }
}
```

**Output:**
```text
In try
No exception
Finally always runs
In try
Caught: java.lang.NullPointerException
Finally always runs
```

### returning from method — finally still runs

```java
static void fun() {
    try {
        return; // method will return
    } finally {
        System.out.println("Finally before return"); // still executes!
    }
}
```

---

## Concept: The throw Keyword

### Definition
The throw keyword is used to explicitly throw an exception object — either a built-in Java exception or a user-defined one. This is called an explicit or manual exception.

### Syntax

```java
throw new ExceptionClassName("optional message");
// or
ExceptionClassName obj = new ExceptionClassName("message");
throw obj;
```

### Rules
- `throw` requires a single Throwable object as its argument.
- After `throw`, control immediately leaves the current block and searches for a matching `catch`.
- `throw` can also be used inside a `catch` block to re-throw an exception.

### Example 1 — Basic throw

```java
class Demo {
    public static void main(String[] args) {
        try {
            throw new ArithmeticException("Manual throw demo");
        } catch (ArithmeticException e) {
            System.out.println("Caught: " + e.getMessage());
        }
    }
}
// Output: Caught: Manual throw demo
```

### Example 2 — throw in a method

```java
class Stack {
    int[] arr = new int;
    int top = 0;

    int pop() {
        if (top == 0)
            throw new RuntimeException("Stack is empty");
        return arr[--top];
    }
}
```

### Example 3 — Re-throwing an exception

```java
catch (Exception e) {
    System.out.println("Caught, re-throwing...");
    throw e; // passes to next higher context
}
```

---

## Concept: The throws Clause

### Definition
The throws clause is used in a method declaration to announce that the method may throw one or more checked exceptions. The caller must then either catch these exceptions or propagate them using their own throws clause.

### Syntax

```java
access_modifier return_type methodName(params) throws ExceptionClass1, ExceptionClass2 {
    // method body
}
```

### When to Use throws
- When a method does not handle a checked exception internally.
- The compiler forces you to either catch it or declare it with `throws`.
- Does NOT apply to `RuntimeException` or `Error` subclasses.

### Example

```java
import java.io.*;

class Demo {
    static void readInput() throws IOException {
        DataInputStream in = new DataInputStream(System.in);
        String line = in.readLine(); // readLine() throws IOException
        System.out.println("Read: " + line);
    }

    public static void main(String[] args) {
        try {
            readInput();
        } catch (IOException e) {
            System.out.println("IO Error: " + e.getMessage());
        }
    }
}
```

### throw vs throws

| Feature | throw | throws |
|---|---|---|
| Purpose | Actually throws an exception object | Declares that a method may throw exceptions |
| Location | Inside method body | In method signature |
| Syntax | `throw new Exception(...)` | `methodName() throws IOException` |
| Followed by | Exception object | Exception class name(s) |
| For checked/unchecked | Both | Mainly checked exceptions |

---

## Concept: Creating Your Own Exceptions

### Definition
Java allows you to create user-defined exception classes by extending `Exception` (for checked) or `RuntimeException` (for unchecked).

### Steps to Create Custom Exception
1. Create a class that extends `Exception` or `RuntimeException`.
2. Provide a constructor (optionally passing message to `super()`).
3. Optionally override `toString()` for custom display.
4. Use `throw new YourException(...)` to throw it.
5. Declare in `throws` clause if checked.

### Example 1 — Simple Custom Exception

```java
class InsufficientFundsException extends Exception {
    InsufficientFundsException(String msg) {
        super(msg); // passes message to Exception's getMessage()
    }
}

class BankAccount {
    int balance = 100;

    void withdraw(int amount) throws InsufficientFundsException {
        if (amount > balance)
            throw new InsufficientFundsException("Need " + (amount - balance) + " more");
        balance -= amount;
    }
}

class Main {
    public static void main(String[] args) {
        BankAccount acc = new BankAccount();
        try {
            acc.withdraw(200);
        } catch (InsufficientFundsException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
// Output: Error: Need 100 more
```

### Example 2 — Custom Exception with toString()

```java
class MyException extends Exception {
    private int code;
    MyException(int c) { code = c; }
    public String toString() {
        return "MyException: error code = " + code;
    }
}

class Demo {
    static void check(int x) throws MyException {
        if (x < 0) throw new MyException(x);
        System.out.println("Valid: " + x);
    }
    public static void main(String[] args) {
        try {
            check(5);
            check(-3);
        } catch (MyException e) {
            System.out.println("Caught: " + e);
        }
    }
}
// Output:
// Valid: 5
// Caught: MyException: error code = -3
```

---

## Quick Reference — Exception Keywords

| Keyword | Role | Where Used |
|---|---|---|
| `try` | Encloses risky code — "guarded region" | Before risky statements |
| `catch` | Handles a specific thrown exception | After `try` block |
| `finally` | Always-execute cleanup block | After `catch` block(s) |
| `throw` | Explicitly throws an exception object | Inside method body |
| `throws` | Declares possible checked exceptions from a method | Method signature |

---

## Summary — Module III at a Glance

### Multithreaded Programming

| Topic | Key Points |
|---|---|
| Thread creation | Extend `Thread` class OR implement `Runnable` interface |
| Thread lifecycle | New → Runnable → Running → Blocked → Dead |
| Priority | `MIN(1)`, `NORM(5)`, `MAX(10)` — use `setPriority()` / `getPriority()` |
| Synchronization | `synchronized` method or block; uses monitor/lock |
| Inter-thread comm. | `wait()`, `notify()`, `notifyAll()` — must be in synchronized context |
| Daemon threads | `setDaemon(true)` before `start()`; JVM exits when only daemons remain |

### Exception Handling

| Topic | Key Points |
|---|---|
| Exception hierarchy | `Throwable` → `Exception` / `Error` → `RuntimeException` |
| Checked vs unchecked | Checked must be caught/declared; unchecked are optional |
| try-catch | Encloses risky code; catch matches exception type |
| Multiple catch | Specific types before general; only one executes |
| finally | Always runs; used for resource cleanup |
| throw | Explicitly throws exception object |
| throws | Declares exception in method signature |
| Custom exceptions | Extend `Exception` class, use `super(msg)` in constructor |