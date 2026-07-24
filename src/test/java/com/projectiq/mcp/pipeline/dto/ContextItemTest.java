package com.projectiq.mcp.pipeline.dto;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Unit tests for {@link ContextItem}.
 */
class ContextItemTest {

    @Test
    void testConstructorWithContent() {
        ContextItem item = new ContextItem(ContextSourceType.CLASS_ANALYSIS, ContextPriority.HIGH, "TestClass");
        assertEquals(ContextSourceType.CLASS_ANALYSIS, item.getSourceType());
        assertEquals(ContextPriority.HIGH, item.getPriority());
        assertEquals("TestClass", item.getContent());
        assertEquals("TestClass", item.getDeduplicationKey());
    }

    @Test
    void testConstructorWithDedupKey() {
        ContextItem item = new ContextItem(ContextSourceType.REST_APIS, ContextPriority.MEDIUM, "GET /api/test", "api-key");
        assertEquals("api-key", item.getDeduplicationKey());
    }

    @Test
    void testEqualityByDedupKey() {
        ContextItem item1 = new ContextItem(ContextSourceType.CLASS_ANALYSIS, ContextPriority.HIGH, "ClassA", "key1");
        ContextItem item2 = new ContextItem(ContextSourceType.METHOD_ANALYSIS, ContextPriority.MEDIUM, "ClassA", "key1");
        assertEquals(item1, item2);
        assertEquals(item1.hashCode(), item2.hashCode());
    }

    @Test
    void testInequalityByDedupKey() {
        ContextItem item1 = new ContextItem(ContextSourceType.CLASS_ANALYSIS, ContextPriority.HIGH, "ClassA", "key1");
        ContextItem item2 = new ContextItem(ContextSourceType.CLASS_ANALYSIS, ContextPriority.HIGH, "ClassB", "key2");
        assertNotEquals(item1, item2);
    }

    @Test
    void testCompareToPriorityOrder() {
        ContextItem high = new ContextItem(ContextSourceType.CLASS_ANALYSIS, ContextPriority.HIGH, "A");
        ContextItem low = new ContextItem(ContextSourceType.CLASS_ANALYSIS, ContextPriority.LOW, "B");
        assertTrue(high.compareTo(low) < 0);
        assertTrue(low.compareTo(high) > 0);
    }

    @Test
    void testCompareToSamePriorityDifferentSource() {
        ContextItem classItem = new ContextItem(ContextSourceType.CLASS_ANALYSIS, ContextPriority.HIGH, "A");
        ContextItem methodItem = new ContextItem(ContextSourceType.METHOD_ANALYSIS, ContextPriority.HIGH, "B");
        assertTrue(classItem.compareTo(methodItem) < 0);
    }

    @Test
    void testSorting() {
        List<ContextItem> items = new ArrayList<>();
        items.add(new ContextItem(ContextSourceType.CLASS_ANALYSIS, ContextPriority.LOW, "Low1"));
        items.add(new ContextItem(ContextSourceType.CLASS_ANALYSIS, ContextPriority.HIGH, "High1"));
        items.add(new ContextItem(ContextSourceType.CLASS_ANALYSIS, ContextPriority.MEDIUM, "Med1"));

        Collections.sort(items);

        assertEquals(ContextPriority.HIGH, items.get(0).getPriority());
        assertEquals(ContextPriority.MEDIUM, items.get(1).getPriority());
        assertEquals(ContextPriority.LOW, items.get(2).getPriority());
    }

    @Test
    void testSetContentUpdatesDedupKey() {
        ContextItem item = new ContextItem();
        item.setContent("New Content");
        assertEquals("New Content", item.getDeduplicationKey());
    }
}