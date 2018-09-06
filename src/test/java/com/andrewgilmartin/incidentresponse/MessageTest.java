package com.andrewgilmartin.incidentresponse;

import java.awt.Color;
import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;

public class MessageTest {

    private static final Status NOT_DONE_STATUS = new Status("notdone", Color.GREEN, 10, false);
    private static final Status DONE_STATUS = new Status("done", Color.GRAY, 20, true);

    private static final Workspace WORKSPACE = new Workspace(
            "id-1234",
            "Test Workspace",
            Arrays.asList(
                    new Status("notdone", Color.GREEN, 10, false),
                    new Status("done", Color.GRAY, 20, true)
            )
    );

    @Test
    public void testText() {
        Message instance;

        instance = new Message(WORKSPACE, "This is not an error");
        assertFalse(instance.hasError());
        assertTrue(instance.hasText());
        assertEquals("This is not an error", instance.getText());
        
        instance = new Message(WORKSPACE, "This   is    not an   error    ");
        assertFalse(instance.hasError());
        assertTrue(instance.hasText());
        assertEquals("This is not an error", instance.getText());
        
        instance = new Message(WORKSPACE, "    ");
        assertFalse(instance.hasError());
        assertFalse(instance.hasText());
        
        instance = new Message(WORKSPACE, " "+DONE_STATUS+"  ");
        assertFalse(instance.hasError());
        assertFalse(instance.hasText());
        
        instance = new Message(WORKSPACE, "  <id|name>  ");
        assertFalse(instance.hasError());
        assertFalse(instance.hasText());
        
        instance = new Message(WORKSPACE, "   12   ");
        assertFalse(instance.hasError());
        assertFalse(instance.hasText());
    }
    
    @Test
    public void testUser() {
        Message instance;

        instance = new Message(WORKSPACE, "This is not an error");
        assertFalse(instance.hasError());

        instance = new Message(WORKSPACE, "This is an < error");
        assertTrue(instance.hasError());

        instance = new Message(WORKSPACE, "This is an <id error");
        assertTrue(instance.hasError());

        instance = new Message(WORKSPACE, "This is an <id| error");
        assertTrue(instance.hasError());

        instance = new Message(WORKSPACE, "This is an <id|name error");
        assertTrue(instance.hasError());

        instance = new Message(WORKSPACE, "This is not an <id1|name1> error");
        assertTrue(instance.hasUsers());
        assertTrue(instance.getUsers().size() == 1);
        assertEquals("id1", instance.getUsers().get(0).getId());
        assertEquals("name1", instance.getUsers().get(0).getName());
        
        instance = new Message(WORKSPACE, "This is not an <id1|name1> error <id2|name2>");
        assertTrue(instance.hasUsers());
        assertTrue(instance.getUsers().size() == 2);
        assertEquals("id1", instance.getUsers().get(0).getId());
        assertEquals("name1", instance.getUsers().get(0).getName());
        assertEquals("id2", instance.getUsers().get(1).getId());
        assertEquals("name2", instance.getUsers().get(1).getName());        
    }

    @Test
    public void testStatus() {
        Message instance;

        instance = new Message(WORKSPACE, "This is not an error");
        assertFalse(instance.hasError());

        instance = new Message(WORKSPACE, "This is not an ! error");
        assertFalse(instance.hasError());

        instance = new Message(WORKSPACE, "This is not an error!");
        assertFalse(instance.hasError());

        instance = new Message(WORKSPACE, "This is an !unknown error");
        assertTrue(instance.hasError());

        instance = new Message(WORKSPACE, "This is not an "+DONE_STATUS.toString()+" error");
        assertFalse(instance.hasError());
        assertTrue(instance.getStatuses().size() == 1);
        assertEquals(DONE_STATUS, instance.getStatuses().get(0));
        assertEquals(DONE_STATUS, instance.firstStatus());

        instance = new Message(WORKSPACE, "This "+DONE_STATUS.toString()+" is not an "+NOT_DONE_STATUS.toString()+" error");
        assertFalse(instance.hasError());
        assertTrue(instance.getStatuses().size() == 2);
        assertEquals(DONE_STATUS, instance.getStatuses().get(0));
        assertEquals(DONE_STATUS, instance.firstStatus());
        assertEquals(NOT_DONE_STATUS, instance.getStatuses().get(1));
    }

    @Test
    public void testId() {
        Message instance;

        instance = new Message(WORKSPACE, "This is not an error");
        assertFalse(instance.hasId());

        instance = new Message(WORKSPACE, "12 This is not an error");
        assertTrue(instance.hasId());
        assertEquals("12", instance.getId());

        instance = new Message(WORKSPACE, "This 12 is not an error");
        assertFalse(instance.hasId());
    }
}
