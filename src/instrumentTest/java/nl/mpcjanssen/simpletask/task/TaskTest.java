package nl.mpcjanssen.simpletask.task;

import junit.framework.TestCase;

import java.util.Date;

import nl.mpcjanssen.simpletask.task.Task;

/**
 * Created with IntelliJ IDEA.
 * User: Mark Janssen
 * Date: 21-7-13
 * Time: 12:28
 */

public class TaskTest extends TestCase {
    public void testValidTag() throws Exception {
       assertEquals(false, Task.validTag(" "));
    }

    public void testEquals() {
        Task a = new Task(1, "Test");
        Task b = new Task(1, "Test");
        Task c = new Task(1, "Test ");
        Task d = new Task(2, "Test");
        assertNotSame(a,b);
        assertEquals(a,b);
        assertFalse(b.equals(c));
        assertFalse(b.equals(d));
    }

    public void testCompletion() {
        String rawText = "Test";
        Task t = new Task(0, rawText);
        Date completionDate = new Date();
        t.markComplete(completionDate);
        assertTrue(t.isCompleted());
        t.markIncomplete();
        assertFalse(t.isCompleted());
        assertEquals(rawText, t.inFileFormat());
    }
    public void testCompletionWithPrependDate() {
        String rawText = "Test";
        Task t = new Task(0, rawText, new Date());
        rawText = t.inFileFormat();
        Date completionDate = new Date();
        t.markComplete(completionDate);
        assertTrue(t.isCompleted());
        t.markIncomplete();
        assertFalse(t.isCompleted());
        assertEquals(rawText, t.inFileFormat());
    }

    public void testCompletionWithPriority() {
        String rawText = "(A) Test";
        Task t = new Task(0, rawText);
        t.update(rawText);
        assertEquals(t.getPriority(), Priority.A);
        Date completionDate = new Date();
        t.markComplete(completionDate);
        assertTrue(t.isCompleted());
        t.markIncomplete();
        assertFalse(t.isCompleted());
        assertEquals(Priority.NONE , t.getPriority());
        assertEquals("Test", t.inFileFormat());
    }

    public void testPriority() {
        Task t = new Task(0, "(C) Test");
        assertEquals(t.getPriority(), Priority.C);
        t.setPriority(Priority.A);
        assertEquals(t.getPriority(), Priority.A);
        t.setPriority(Priority.NONE);
        assertEquals(t.getPriority(), Priority.NONE);
        t = new Task(0, "Test");
        assertEquals(t.getPriority(), Priority.NONE);
        t.setPriority(Priority.A);
        assertEquals(t.getPriority(), Priority.A);
        assertEquals("(A) Test", t.inFileFormat());
        t.setPriority(Priority.NONE);
        assertEquals(t.getPriority(), Priority.NONE);
        assertEquals("Test", t.inFileFormat());
    }
}
