git restore --staged src/main/resources/application.properties        final Map<String, Long> finalAcceptedQtyMap = acceptedQtyMap;

        List<VendorInspectionCallStatusDto> results = inspectionCalls.stream()
                .map(ic -> mapToVendorInspectionCallStatusDtoOptimized(
                        ic,
                        transitionMap.get(ic.getIcNumber()),
                        poMap.get(ic.getPoNo()),
                        ic.getRmInspectionDetails(),
                        ic.getProcessInspectionDetails(),
                        ic.getFinalInspectionDetails(),
                        finalUserNamesMap,
                        finalRmHeatCountMap,
                        finalFinalLotNoMap,
                        finalAcceptedQtyMap))
                .collect(Collectors.toList());

        long endTime = System.currentTimeMillis();
        logger.info("Successfully fetched {} inspection calls for vendor: {} in {}ms", results.size(), vendorId, (endTime - startTime));

        return results;
    }

    /**
     * Optimized mapping from InspectionCall entity to VendorInspectionCallStatusDto
     */
    private VendorInspectionCallStatusDto mapToVendorInspectionCallStatusDtoOptimized(
            InspectionCall ic,
            WorkflowTransition latestTransition,
            PoHeader ph,
            RmInspectionDetails rmDetails,
            List<ProcessInspectionDetails> processList,
            FinalInspectionDetails finalDetails,
            Map<Integer, String> userNamesMap,
            Map<Long, Long> rmHeatCountMap,
            Map<Long, String> finalLotNoMap,
            Map<String, Long> acceptedQtyMap) {

        // Get item name and quantity based on type of call
        String itemName = getItemNameOptimized(ic, rmDetails, processList, finalDetails);
        Integer quantityOffered = getQuantityOfferedOptimized(ic, rmDetails, processList, finalDetails);

        // Fetch PoHeader details
        String rlyShortName = ph != null ? ph.getRlyShortName() : "N/A";
        String rlyCd = ph != null ? ph.getRlyCd() : "N/A";

        // IE Name from Map
        String ieName = "Not Assigned";
        if (latestTransition != null) {
            if (latestTransition.getAssignedToUser() != null) {
                ieName = userNamesMap.getOrDefault(latestTransition.getAssignedToUser(), "Not Assigned");
            } else if (latestTransition.getProcessIeUserId() != null) {
                ieName = userNamesMap.getOrDefault(latestTransition.getProcessIeUserId(), "Not Assigned");
            }
        }

        // Get Heats/Lots count
        Integer noOfHeatsRM = null;
        String lotNoProcess = null;
        String lotNoFinal = null;
        String uom = "N/A";

        if ("Raw Material".equalsIgnoreCase(ic.getTypeOfCall()) && rmDetails != null) {
            Long count = rmHeatCountMap.get(rmDetails.getId());
            noOfHeatsRM = count != null ? count.intValue() : 0;
            uom = rmDetails.getUnitOfMeasurement();
        } else if ("Process".equalsIgnoreCase(ic.getTypeOfCall()) && processList != null && !processList.isEmpty()) {
            lotNoProcess = processList.get(0).getLotNumber();
        } else if ("Final".equalsIgnoreCase(ic.getTypeOfCall()) && finalDetails != null) {
            lotNoFinal = finalLotNoMap.get(finalDetails.getId());
        }

        String scheduledDate = null;
        if (latestTransition != null && "SCHEDULED".equalsIgnoreCase(latestTransition.getStatus())) {
            scheduledDate = ic.getActualInspectionDate() != null ? ic.getActualInspectionDate().format(DATE_FORMATTER) : null;
        }

        return VendorInspectionCallStatusDto.builder()
                .workflowTransitionId(latestTransition != null ? latestTransition.getWorkflowTransitionId() : null)
                .icNumber(ic.getIcNumber())
                .poNo(ic.getPoNo())
                .poSerialNo(ic.getPoSerialNo())
                .typeOfCall(ic.getTypeOfCall())
                .desiredInspectionDate(ic.getDesiredInspectionDate() != null ? ic.getDesiredInspectionDate().format(DATE_FORMATTER) : null)
                .placeOfInspection(ic.getPlaceOfInspection())
                .itemName(itemName)
                .quantityOffered(quantityOffered)
                .workflowStatus(latestTransition != null ? latestTransition.getStatus() : ic.getStatus())
                .currentRoleName(latestTransition != null ? latestTransition.getCurrentRoleName() : null)
                .nextRoleName(latestTransition != null ? latestTransition.getNextRoleName() : null)
                .jobStatus(latestTransition != null ? latestTransition.getJobStatus() : null)
                .companyName(ic.getCompanyName())
                .unitName(ic.getUnitName())
                .createdAt(ic.getCreatedAt() != null ? ic.getCreatedAt().format(DATE_FORMATTER) : null)
                .updatedAt(ic.getUpdatedAt() != null ? ic.getUpdatedAt().format(DATE_FORMATTER) : null)
                .rlyShortName(rlyShortName)
                .rlyCd(rlyCd)
                .ercType(ic.getErcType())
                .noOfHeatsRM(noOfHeatsRM)
                .lotNoProcess(lotNoProcess)
                .lotNoFinal(lotNoFinal)
                .ieName(ieName)
                .uom(uom)
                .scheduledDate(scheduledDate)
                .acceptedQty(acceptedQtyMap.getOrDefault(ic.getIcNumber(), 0L))
                .build();
    }

    private String getItemNameOptimized(InspectionCall ic, RmInspectionDetails rmDetails, List<ProcessInspectionDetails> processList, FinalInspectionDetails finalDetails) {
        if ("Raw Material".equalsIgnoreCase(ic.getTypeOfCall()) && rmDetails != null) {
            return rmDetails.getItemDescription();
        } else if ("Process".equalsIgnoreCase(ic.getTypeOfCall()) && processList != null && !processList.isEmpty()) {
            return "Process Inspection - Lot: " + processList.get(0).getLotNumber();
        } else if ("Final".equalsIgnoreCase(ic.getTypeOfCall()) && finalDetails != null) {
            return "Final Inspection - " + finalDetails.getTotalLots() + " lots";
        }
        return "N/A";
    }

    private Integer getQuantityOfferedOptimized(InspectionCall ic, RmInspectionDetails rmDetails, List<ProcessInspectionDetails> processList, FinalInspectionDetails finalDetails) {
        if ("Raw Material".equalsIgnoreCase(ic.getTypeOfCall()) && rmDetails != null) {
            return rmDetails.getOfferedQtyErc();
        } else if ("Process".equalsIgnoreCase(ic.getTypeOfCall()) && processList != null && !processList.isEmpty()) {
            return processList.get(0).getOfferedQty();
        } else if ("Final".equalsIgnoreCase(ic.getTypeOfCall()) && finalDetails != null) {
            return finalDetails.getTotalOfferedQty();
        }
        return 0;
    }

    // Deprecated methods replaced by optimized versions

    @Override
    @Transactional(readOnly = true)
    public byte[] getTcDocsByCallNo(String callNo) {
        logger.info("Fetching TC docs for call number: {}", callNo);
        List<String> tcFilePaths = inventoryEntryRepository.findTcFilePathsByCallNo(callNo);
        if (tcFilePaths == null || tcFilePaths.isEmpty()) {
            throw new BusinessException(new ErrorDetails(AppConstant.NO_RECORD_FOUND, AppConstant.ERROR_TYPE_CODE_VALIDATION, AppConstant.ERROR_TYPE_VALIDATION, "No TC Documents found for this call number"));
        }

        List<byte[]> pdfBytesList = new ArrayList<>();
        for (String path : tcFilePaths) {
            try {
                if (path == null || path.isBlank()) continue;
                String blobName = path.substring(path.lastIndexOf('/') + 1);
                blobName = java.net.URLDecoder.decode(blobName, java.nio.charset.StandardCharsets.UTF_8);
                byte[] pdfBytes = azureBlobStorageService.downloadFile(blobName);
                if (pdfBytes != null && pdfBytes.length > 0) {
                    pdfBytesList.add(pdfBytes);
                }
            } catch (Exception e) {
                logger.error("Failed to download TC document from blob: {}", path, e);
            }
        }

        if (pdfBytesList.isEmpty()) {
            throw new BusinessException(new ErrorDetails(AppConstant.NO_RECORD_FOUND, AppConstant.ERROR_TYPE_CODE_VALIDATION, AppConstant.ERROR_TYPE_VALIDATION, "Failed to download any TC Documents"));
        }
        
        if (pdfBytesList.size() == 1) {
            return pdfBytesList.get(0);
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfCopy copy = new PdfCopy(document, baos);
            document.open();
            for (byte[] pdf : pdfBytesList) {
                PdfReader reader = new PdfReader(pdf);
                for (int i = 1; i <= reader.getNumberOfPages(); i++) {
                    copy.addPage(copy.getImportedPage(reader, i));
                }
                reader.close();
            }
            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            logger.error("Error merging PDF documents for call: {}", callNo, e);
            throw new BusinessException(new ErrorDetails(AppConstant.INTERNAL_SERVER_ERROR, AppConstant.ERROR_TYPE_CODE_INTERNAL, AppConstant.ERROR_TYPE_INTERNAL, "Error merging TC documents"));
        }
    }
}