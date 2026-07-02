package com.challenge.backend.domain.model.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StatusTest {

    @Test
    void draftShouldBeEditable() {
        assertTrue(Status.DRAFT.canEdit());
    }

    @Test
    void submittedShouldNotBeEditable() {
        assertFalse(Status.SUBMITTED.canEdit());
    }

    @Test
    void inReviewShouldNotBeEditable() {
        assertFalse(Status.IN_REVIEW.canEdit());
    }

    @Test
    void submittedShouldBeAnalyzable() {
        assertTrue(Status.SUBMITTED.canAnalyze());
    }

    @Test
    void inReviewShouldBeAnalyzable() {
        assertTrue(Status.IN_REVIEW.canAnalyze());
    }

    @Test
    void draftShouldNotBeAnalyzable() {
        assertFalse(Status.DRAFT.canAnalyze());
    }

    @Test
    void approvedShouldBeTerminal() {
        assertTrue(Status.APPROVED.isTerminal());
    }

    @Test
    void rejectedShouldBeTerminal() {
        assertTrue(Status.REJECTED.isTerminal());
    }

    @Test
    void draftShouldNotBeTerminal() {
        assertFalse(Status.DRAFT.isTerminal());
    }
}
