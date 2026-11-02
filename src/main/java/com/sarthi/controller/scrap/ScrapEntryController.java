package com.sarthi.controller.scrap;

import com.sarthi.dto.scrap.*;
import com.sarthi.service.AzureBlobStorageService;
import com.sarthi.service.scrap.ScrapEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/scrap-entry")
@CrossOrigin
public class ScrapEntryController {

    @Autowired
    private ScrapEntryService scrapEntryService;

    @Autowired
    private AzureBlobStorageService azureBlobStorageService;

    /**
     * Proxy endpoint to serve scrap images from private Azure Blob Storage.
     * Accepts the full blob URL and extracts the blob name to fetch via authenticated connection.
     */
    @GetMapping("/image-proxy")
    public ResponseEntity<byte[]> proxyScrapImage(@RequestParam String url) {
        try {
            // Extract blob name from the full Azure URL
            // e.g. https://ritesblobstorage.blob.core.windows.net/scrap-images/erc%2FWhatsApp...
            URI uri = new URI(url);
            String path = uri.getPath(); // e.g. /scrap-images/erc%2FWhatsApp...
            String containerPrefix = "/scrap-images/";
            String blobName;
            if (path.startsWith(containerPrefix)) {
                blobName = java.net.URLDecoder.decode(path.substring(containerPrefix.length()), "UTF-8");
            } else {
                // Fallback: use the raw URL decoded path after the last container segment
                blobName = java.net.URLDecoder.decode(path.replaceFirst("^/[^/]+/", ""), "UTF-8");
            }
            byte[] imageBytes = azureBlobStorageService.downloadFileFromContainer(blobName, "scrap-images");
            String contentType = blobName.toLowerCase().endsWith(".png") ? "image/png" : "image/jpeg";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CACHE_CONTROL, "max-age=3600, private")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(imageBytes);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/board-summary")
    public ResponseEntity<List<BoardSummaryRowDto>> getBoardSummary(
            @RequestParam(required = false, defaultValue = "ERC") String productType) {
        return ResponseEntity.ok(scrapEntryService.getBoardSummary(productType));
    }

    @GetMapping("/rejection-breakup")
    public ResponseEntity<RejectionBreakupDto> getRejectionBreakup(
            @RequestParam String poiCode,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam String stage,
            @RequestParam(required = false) String shift,
            @RequestParam(required = false) String lineName) {
        return ResponseEntity.ok(scrapEntryService.getRejectionBreakup(poiCode, date, stage, shift, lineName));
    }


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
