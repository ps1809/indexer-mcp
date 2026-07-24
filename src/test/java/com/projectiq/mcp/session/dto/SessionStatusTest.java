package com.projectiq.mcp.session.dto;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class SessionStatusTest {

    @Test
    void testSessionStatusValues() {
        assertThat(SessionStatus.values()).hasSize(4);
        assertThat(SessionStatus.valueOf("CREATED")).isEqualTo(SessionStatus.CREATED);
        assertThat(SessionStatus.valueOf("IN_PROGRESS")).isEqualTo(SessionStatus.IN_PROGRESS);
        assertThat(SessionStatus.valueOf("COMPLETED")).isEqualTo(SessionStatus.COMPLETED);
        assertThat(SessionStatus.valueOf("ARCHIVED")).isEqualTo(SessionStatus.ARCHIVED);
    }

    @Test
    void testSessionStatusDisplayNames() {
        assertThat(SessionStatus.CREATED.getDisplayName()).isEqualTo("Created");
        assertThat(SessionStatus.IN_PROGRESS.getDisplayName()).isEqualTo("In Progress");
        assertThat(SessionStatus.COMPLETED.getDisplayName()).isEqualTo("Completed");
        assertThat(SessionStatus.ARCHIVED.getDisplayName()).isEqualTo("Archived");
    }
}