package com.sarthi.controller.scrap;

import com.sarthi.dto.scrap.*;
import com.sarthi.service.scrap.ScrapEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/scrap-entry")
@CrossOrigin
public class ScrapEntryController {

    @Autowired
    private ScrapEntryService scrapEntryService;

    @GetMapping("/plants")
    public ResponseEntity<List<PlantInfoDto>> getMappedPlants(
            @RequestParam Integer ieUserId,
            @RequestParam String ieType) {
        return ResponseEntity.ok(scrapEntryService.getMappedPlants(ieUserId, ieType));
    }

    @GetMapping("/ledger")
    public ResponseEntity<ScrapLedgerResponseDto> getLedger(
            @RequestParam String poiCode,
            @RequestParam(required = false) String unitLineShed) {
        return ResponseEntity.ok(scrapEntryService.getLedger(poiCode, unitLineShed));
    }

    @GetMapping("/pending-qty")
    public ResponseEntity<Integer> getPendingQty(
            @RequestParam String poiCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(scrapEntryService.getPendingQty(
                poiCode, date != null ? date : LocalDate.now()));
    }

    @PostMapping("/add")
    public ResponseEntity<ScrapEntryResponseDto> addEntry(@RequestBody ScrapEntryRequestDto req) {
        return ResponseEntity.ok(scrapEntryService.addEntry(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ScrapEntryResponseDto> updateEntry(
            @PathVariable Integer id,
            @RequestBody ScrapEntryUpdateDto req) {
        return ResponseEntity.ok(scrapEntryService.updateEntry(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEntry(
            @PathVariable Integer id,
            @RequestParam String reason,
            @RequestParam String actionBy) {
        scrapEntryService.deleteEntry(id, reason, actionBy);
        return ResponseEntity.noContent().build();
    }
}
