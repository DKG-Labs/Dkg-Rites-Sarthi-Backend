package com.sarthi.Sleeper.service.scrap;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Pure helpers that decide sleeper identity and access; no Spring context needed. */
class SleeperRejectionBasketServiceHelpersTest {

    @Test
    void sameSleeperMatchesAcrossLabelFormats() {
        String key = SleeperRejectionBasketService.sleeperKey("601", "210-A");
        assertEquals(key, SleeperRejectionBasketService.sleeperKey("601", "210A"));
        assertEquals(key, SleeperRejectionBasketService.sleeperKey("601", "601/210A"));
        assertEquals(key, SleeperRejectionBasketService.sleeperKey(" 601 ", "210 a"));
    }

    @Test
    void differentBatchesOrSleepersDoNotMatch() {
        assertNotEquals(SleeperRejectionBasketService.sleeperKey("601", "210A"),
                SleeperRejectionBasketService.sleeperKey("602", "210A"));
        assertNotEquals(SleeperRejectionBasketService.sleeperKey("601", "210A"),
                SleeperRejectionBasketService.sleeperKey("601", "210B"));
    }

    @Test
    void demouldingIdUsesBenchWhenOnlySequenceRecorded() {
        assertEquals("210-A", SleeperRejectionBasketService.demouldingSleeperNo("210", "A", "A"));
        assertEquals("210-A", SleeperRejectionBasketService.demouldingSleeperNo("210", "A", null));
        assertEquals("210-C", SleeperRejectionBasketService.demouldingSleeperNo("210", null, "C"));
        // A full label is kept as recorded
        assertEquals("210A", SleeperRejectionBasketService.demouldingSleeperNo("210", "A", "210A"));
        assertEquals("1AS", SleeperRejectionBasketService.demouldingSleeperNo(null, null, "1AS"));
    }

    @Test
    void demouldingIdIsRejectedWhenAmbiguous() {
        // "A" alone would merge sleeper A of every bench in the batch
        assertNull(SleeperRejectionBasketService.demouldingSleeperNo(null, "A", "A"));
        assertNull(SleeperRejectionBasketService.demouldingSleeperNo(null, null, "B"));
        assertNull(SleeperRejectionBasketService.demouldingSleeperNo("210", null, null));
    }

    @Test
    void roleNamesNormalise() {
        assertEquals("RAILWAY BOARD", SleeperRejectionBasketService.normRole("ROLE_RAILWAY_BOARD"));
        assertEquals("RITES ADMIN", SleeperRejectionBasketService.normRole(" Rites  ADMin "));
        assertEquals("SLEEPER PROCESS IE", SleeperRejectionBasketService.normRole("Sleeper Process IE"));
        assertEquals("", SleeperRejectionBasketService.normRole(null));
    }

    @Test
    void engineerRoleFollowsStageAndShift() {
        assertEquals("IE-Shift A", SleeperRejectionBasketService.ieRoleForRejection("DEMOULDING", "A"));
        assertEquals("IE-Shift B", SleeperRejectionBasketService.ieRoleForRejection("DEMOULDING", "Shift B"));
        assertEquals("IE-General", SleeperRejectionBasketService.ieRoleForRejection("DEMOULDING", "General"));
        assertNull(SleeperRejectionBasketService.ieRoleForRejection("DEMOULDING", null));
        // Final inspection and destructive tests are logged by the General IE, whatever the call's shift
        assertEquals("IE-General", SleeperRejectionBasketService.ieRoleForRejection("FINAL", "Shift A"));
        assertEquals("IE-General", SleeperRejectionBasketService.ieRoleForRejection("TESTING", null));

        assertEquals("IE-General", SleeperRejectionBasketService.ieRoleForScrap("General", "PROCESS_IE"));
        assertEquals("IE-Shift C", SleeperRejectionBasketService.ieRoleForScrap("C", "PROCESS_IE"));
        assertEquals("Main IE", SleeperRejectionBasketService.ieRoleForScrap(null, "MAIN_IE"));

        assertEquals("Nitin (IE-Shift A)", SleeperRejectionBasketService.engineerLabel("Nitin", "IE-Shift A"));
        assertEquals("Nitin", SleeperRejectionBasketService.engineerLabel("Nitin", null));
        assertEquals("IE-General", SleeperRejectionBasketService.engineerLabel(null, "IE-General"));
    }

    @Test
    void shiftsGroupTheSameWhateverTheSpelling() {
        assertEquals("A", SleeperRejectionBasketService.shiftLabel("Shift A"));
        assertEquals("A", SleeperRejectionBasketService.shiftLabel(" a "));
        assertEquals("General", SleeperRejectionBasketService.shiftLabel("GENERAL"));
        assertEquals("Night", SleeperRejectionBasketService.shiftLabel("Night"));
        assertNull(SleeperRejectionBasketService.shiftLabel(" "));
    }

    @Test
    void testDatesParseFromTextFormats() {
        assertEquals(java.time.LocalDate.of(2026, 7, 12), SleeperRejectionBasketService.parseFlexDate("2026-07-12"));
        assertEquals(java.time.LocalDate.of(2026, 7, 12), SleeperRejectionBasketService.parseFlexDate("12-07-2026"));
        assertEquals(java.time.LocalDate.of(2026, 7, 12), SleeperRejectionBasketService.parseFlexDate("12/07/2026"));
        assertNull(SleeperRejectionBasketService.parseFlexDate("N/A"));
        assertNull(SleeperRejectionBasketService.parseFlexDate(null));
    }

    @Test
    void stageLabels() {
        assertEquals("MR Test", SleeperRejectionBasketService.stageLabel("TESTING"));
        assertEquals("Demoulding", SleeperRejectionBasketService.stageLabel("DEMOULDING"));
    }

    @Test
    void plantIdsLoseLeadingColon() {
        assertEquals("41647/01", SleeperRejectionBasketService.normPlant(":41647/01"));
        assertNull(SleeperRejectionBasketService.normPlant(" : "));
    }
}
